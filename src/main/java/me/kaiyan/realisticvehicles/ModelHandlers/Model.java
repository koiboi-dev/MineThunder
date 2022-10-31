package me.kaiyan.realisticvehicles.ModelHandlers;

import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.RealisticVehicles;
import me.kaiyan.realisticvehicles.VersionHandler.VersionHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.*;

import static me.kaiyan.realisticvehicles.RealisticVehicles.SCRAPKEY;

public class Model {
    private final HashMap<int[], ArmorStand> activeEnts = new HashMap<>();
    private final HashMap<Vector, ArmorStand> seats = new HashMap<>();
    public static final float SEAT_LOWER_OFFSET = 1f;
    public static final int GRID_OFFSET = 7;
    private final boolean shiftGrid;
    private final float middleOffset;
    private final boolean dontRoll;

    public Model(@Nullable ArmorStand seat, Vector seatPos, float middleOffset, boolean yawOnly){
        if (seat != null) {
            seats.put(seatPos, seat);
        }
        shiftGrid = false;
        this.middleOffset = middleOffset;
        this.dontRoll = yawOnly;
    }
    public Model(ArmorStand seat, Vector seatPos, float middleOffset, boolean shiftGrid, boolean yawOnly){
        seats.put(seatPos, seat);
        this.shiftGrid = shiftGrid;
        this.middleOffset = middleOffset;
        this.dontRoll = yawOnly;
    }

    public void addCorner(int[] coords, ArmorStand stand){
        activeEnts.put(coords, stand);
    }
    public void addSeat(Vector vec, ArmorStand stand){seats.put(vec, stand);}

    public void updatePositions(Location loc, float pitch, float yaw, float roll){
        loc = loc.clone();
        Vector[] gotoPoints = new Vector[activeEnts.size()];
        int loops = 0;
        for (int[] coords : activeEnts.keySet()){
            Vector offset;
            if (!shiftGrid){
                offset = new Vector(coords[0]*GRID_OFFSET, 0, coords[1]*GRID_OFFSET+middleOffset);
            } else if ((coords[1]+1) % 2 == 0){
                offset = new Vector(coords[0]*GRID_OFFSET+(GRID_OFFSET/2f), 0, coords[1]*GRID_OFFSET+middleOffset);
            } else {
                offset = new Vector(coords[0]*GRID_OFFSET, 0, coords[1]*GRID_OFFSET+middleOffset);
            }
            //fireFrom.rotateAroundX(Math.toRadians(getRoll())).rotateAroundZ(Math.toRadians(getPitch())).rotateAroundY(Math.toRadians(getYaw()));
            offset.rotateAroundZ(Math.toRadians(roll)).rotateAroundX(Math.toRadians(pitch)).rotateAroundY(-Math.toRadians(yaw));
            Vector endLoc = loc.toVector();
            endLoc.add(offset);
            gotoPoints[loops] = endLoc;
            loops++;
            /*VersionHandler.teleport(stand, endLoc, yaw, 0);
            if (!yawOnly) {
                stand.setHeadPose(new EulerAngle(Math.toRadians(pitch), 0, -Math.toRadians(roll)));
            } else {
                stand.setRotation(yaw, 0);
            }
            */
        }
        if (!dontRoll) {
            for (int i = 0; i < gotoPoints.length; i++) {
                VersionHandler.teleport((ArmorStand) activeEnts.values().toArray()[i], gotoPoints[i], yaw, 0);
                ((ArmorStand) activeEnts.values().toArray()[i]).setHeadPose(new EulerAngle(Math.toRadians(pitch), 0, -Math.toRadians(roll)));
            }
        } else {
            for (int i = 0; i < gotoPoints.length; i++) {
                VersionHandler.teleport((ArmorStand) activeEnts.values().toArray()[i], gotoPoints[i], yaw, pitch);
            }
        }
        if (seats.size() != 0) {
            for (Map.Entry<Vector, ArmorStand> map : seats.entrySet()) {
                Vector offset = map.getKey().clone();
                ArmorStand stand = map.getValue();
                offset.add(new Vector(0, 0, middleOffset));
                offset.rotateAroundZ(Math.toRadians(roll)).rotateAroundX(Math.toRadians(pitch)).rotateAroundY(-Math.toRadians(yaw));
                Vector endLoc = loc.clone().toVector();
                endLoc.add(offset);
                endLoc.subtract(new Vector(0, SEAT_LOWER_OFFSET, 0));

                VersionHandler.teleport(map.getValue(), endLoc, yaw, pitch);
                if (!dontRoll) {
                    stand.setHeadPose(new EulerAngle(Math.toRadians(pitch), 0, -Math.toRadians(roll)));
                }
            }
        }
    }

    public boolean containsStand(ArmorStand stand){
        if (seats.size() != 0){
            for (ArmorStand astand : seats.values()){
                if (Objects.equals(stand.getUniqueId().toString(), astand.getUniqueId().toString())){
                    return true;
                }
            }
        }
        return false;
    }

    public void setGearState(boolean extended){
        for (ArmorStand stand : activeEnts.values()){
            String ids = stand.getPersistentDataContainer().get(RealisticVehicles.LANDINGKEY, PersistentDataType.STRING);
            ItemStack item = new ItemStack(Material.WOODEN_HOE);
            ItemMeta meta = item.getItemMeta();
            if (extended){
                meta.setCustomModelData(Integer.valueOf(ids.split(":")[1]));
            } else {
                meta.setCustomModelData(Integer.valueOf(ids.split(":")[0]));
            }
            item.setItemMeta(meta);
            stand.getEquipment().setHelmet(item);
        }
    }

    public void clearAll() {
        if (seats.size() != 0){
            for (ArmorStand stand : seats.values()) {
                stand.remove();
            }
        }
        for (ArmorStand stand : activeEnts.values()){
            stand.remove();
        }
    }

    public void scrapStands(){
        if (seats.size() != 0) {
            for (ArmorStand stand : seats.values()) {
                stand.remove();
            }
        }
        Random rand = new Random();
        for (ArmorStand stand : activeEnts.values()){
            stand.getPersistentDataContainer().set(SCRAPKEY, PersistentDataType.INTEGER, (int) Math.round(rand.nextDouble()*RealisticVehicles.getInstance().getConfig().getDouble("scrap-reward")));
        }
    }

    public HashMap<int[], ArmorStand> getActiveEnts() {
        return activeEnts;
    }

    public void sleepStands(VehicleType type, String name, float yaw, String data){
        String sleepID = UUID.randomUUID().toString();
        for(Map.Entry<int[], ArmorStand> stand : activeEnts.entrySet()){
            stand.getValue().getPersistentDataContainer().set(RealisticVehicles.SLEEPKEY, PersistentDataType.STRING, sleepID+";"+type+";"+name+";"+data+";"+stand.getKey()[0]+";"+stand.getKey()[1]+";0;"+yaw);
        }
        if (seats.size() != 0){
            Map.Entry<Vector, ArmorStand> stand = seats.entrySet().stream().findFirst().get();
            stand.getValue().getPersistentDataContainer().set(RealisticVehicles.SLEEPKEY, PersistentDataType.STRING, sleepID+";"+type+";"+name+";"+data+";"+stand.getKey().getX()+";"+stand.getKey().getZ()+";1;"+yaw);
            if (seats.size() > 1){
                boolean first = true;
                for (ArmorStand stand1 : seats.values()){
                    if (first){
                        first = false;
                        continue;
                    }
                    stand1.remove();
                }
            }
        }
    }
}
