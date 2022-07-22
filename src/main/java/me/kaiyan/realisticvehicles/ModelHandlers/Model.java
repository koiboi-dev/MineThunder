package me.kaiyan.realisticvehicles.ModelHandlers;

import it.unimi.dsi.fastutil.Hash;
import me.kaiyan.realisticvehicles.DamageModel.Hitboxes.ArmourPlate;
import me.kaiyan.realisticvehicles.DataTypes.RadarTarget;
import me.kaiyan.realisticvehicles.RealisticVehicles;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftArmorStand;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.checkerframework.checker.units.qual.A;

import javax.naming.Name;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import static me.kaiyan.realisticvehicles.RealisticVehicles.SCRAPKEY;

public class Model {
    private final HashMap<int[], ArmorStand> activeEnts = new HashMap<>();
    private final HashMap<Vector, ArmorStand> seats = new HashMap<>();
    public static final float SEAT_LOWER_OFFSET = 1f;
    public static final int GRID_OFFSET = 7;
    private final boolean shiftGrid;
    private final float middleOffset;

    public Model(ArmorStand seat, Vector seatPos, float middleOffset){
        if (seat != null) {
            seats.put(seatPos, seat);
        }
        shiftGrid = false;
        this.middleOffset = middleOffset;
    }
    public Model(ArmorStand seat, Vector seatPos, float middleOffset, boolean shiftGrid){
        seats.put(seatPos, seat);
        this.shiftGrid = shiftGrid;
        this.middleOffset = middleOffset;
    }

    public void addCorner(int[] coords, ArmorStand stand){
        activeEnts.put(coords, stand);
    }

    public void updatePositions(Location loc, float pitch, float yaw, float roll){
        loc = loc.clone();
        for (Map.Entry<int[], ArmorStand> map : activeEnts.entrySet()){
            int[] coords = map.getKey();
            ArmorStand stand = map.getValue();
            Vector offset;
            if (!shiftGrid){
                offset = new Vector(coords[0]*GRID_OFFSET, 0, coords[1]*GRID_OFFSET+middleOffset);
            } else {
                offset = new Vector(coords[0]*GRID_OFFSET+(GRID_OFFSET/2f), 0, coords[1]*GRID_OFFSET+middleOffset);
            }
            //fireFrom.rotateAroundX(Math.toRadians(getRoll())).rotateAroundZ(Math.toRadians(getPitch())).rotateAroundY(Math.toRadians(getYaw()));
            offset.rotateAroundZ(Math.toRadians(roll)).rotateAroundX(Math.toRadians(pitch)).rotateAroundY(-Math.toRadians(yaw));
            Location endLoc = loc.clone();
            endLoc.add(offset);

            ((CraftArmorStand) stand).getHandle().b(endLoc.getX(), endLoc.getY(), endLoc.getZ(), yaw, 0);
            stand.setHeadPose(new EulerAngle(Math.toRadians(pitch), 0, -Math.toRadians(roll)));
        }
        if (seats.size() != 0) {
            for (Map.Entry<Vector, ArmorStand> map : seats.entrySet()) {
                Vector offset = map.getKey().clone();
                ArmorStand stand = map.getValue();
                offset.add(new Vector(0, 0, middleOffset));
                offset.rotateAroundZ(Math.toRadians(roll)).rotateAroundX(Math.toRadians(pitch)).rotateAroundY(-Math.toRadians(yaw));
                Location endLoc = loc.clone();
                endLoc.add(offset);

                ((CraftArmorStand) stand).getHandle().b(endLoc.getX(), endLoc.getY() - SEAT_LOWER_OFFSET, endLoc.getZ(), yaw, 0);
                stand.setHeadPose(new EulerAngle(Math.toRadians(pitch), 0, -Math.toRadians(roll)));
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
}
