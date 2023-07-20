package me.kaiyan.realisticvehicles.Models;

import me.kaiyan.realisticvehicles.RealisticVehicles;
import me.kaiyan.realisticvehicles.VehicleManagers.VehicleSaver;
import me.kaiyan.realisticvehicles.VersionHandler.VersionHandler;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Model {
    private HashMap<Vector, Interaction> seats = new HashMap<>();
    private final ItemDisplay disp;
    private final boolean doesRoll;
    private boolean gearDown = true;
    private final Vector scale;
    public static float SEAT_LOWER_OFFSET = 1f;

    public Model(@Nullable Interaction seat, @Nullable Vector seatPos, ItemDisplay disp, boolean doesRoll, Vector scale){
        if (seat != null) {
            seats.put(seatPos, seat);
        }
        this.doesRoll = doesRoll;
        this.disp = disp;
        //this.disp.setInterpolationDelay(-1);
        //this.disp.setInterpolationDuration(1);
        this.scale = scale;

        //TODO Un-floor the plane
        //TODO Why wont interact chair
        //TODO uh oh does item display block inputs?
        // TODO FIX JITTERY MOVEMENT
    }

    public void addSeat(Vector seatPos, Interaction seat){
        seats.put(seatPos, seat);
    }

    public void updatePositions(Location loc, float yaw, float pitch, float roll){
        loc = loc.clone().add(new Vector(0, 2.2, 0));
        VersionHandler.teleport(disp, loc.toVector(), 0, 0);
        disp.setTransformation(new Transformation(new Vector3f(), getQuaternionFromAngles(yaw, pitch, roll), scale.toVector3f(), new Quaternionf()));

        if (seats.size() != 0) {
            for (Map.Entry<Vector, Interaction> map : seats.entrySet()) {
                Vector offset = map.getKey().clone();
                offset.subtract(new Vector(0, SEAT_LOWER_OFFSET, 0));
                if (map.getValue().getPassengers().size() != 0){
                    offset.subtract(new Vector(0,1.7f,0));
                }
                offset.rotateAroundZ(Math.toRadians(roll)).rotateAroundX(Math.toRadians(pitch)).rotateAroundY(-Math.toRadians(yaw));
                Vector endLoc = loc.clone().toVector();
                endLoc.add(offset);
                //endLoc.subtract(new Vector(0, SEAT_LOWER_OFFSET, 0));

                VersionHandler.teleport(map.getValue(), endLoc, yaw, pitch);
            }
        }
    }

    public static Quaternionf getQuaternionFromAngles(float yaw, float pitch, float roll){
        // Thanks to Amir!
        // https://math.stackexchange.com/questions/2975109/how-to-convert-euler-angles-to-quaternions-and-get-the-same-euler-angles-back-fr
        double qx = Math.sin(Math.toRadians(roll/2)) * Math.cos(Math.toRadians(pitch/2)) * Math.cos(Math.toRadians(yaw/2)) - Math.cos(Math.toRadians(roll/2)) * Math.sin(Math.toRadians(pitch/2)) * Math.sin(Math.toRadians(yaw/2));
        double qy = Math.cos(Math.toRadians(roll/2)) * Math.sin(Math.toRadians(pitch/2)) * Math.cos(Math.toRadians(yaw/2)) + Math.sin(Math.toRadians(roll/2)) * Math.cos(Math.toRadians(pitch/2)) * Math.sin(Math.toRadians(yaw/2));
        double qz = Math.cos(Math.toRadians(roll/2)) * Math.cos(Math.toRadians(pitch/2)) * Math.sin(Math.toRadians(yaw/2)) - Math.sin(Math.toRadians(roll/2)) * Math.sin(Math.toRadians(pitch/2)) * Math.cos(Math.toRadians(yaw/2));
        double qw = Math.cos(Math.toRadians(roll/2)) * Math.cos(Math.toRadians(pitch/2)) * Math.cos(Math.toRadians(yaw/2)) + Math.sin(Math.toRadians(roll/2)) * Math.sin(Math.toRadians(pitch/2)) * Math.sin(Math.toRadians(yaw/2));
        return new Quaternionf(qz, qy, qx, qw);
    }

    public void clearAll(){

    }

    public void scrapStands(){

    }

    public void sleep(VehicleSaver saver){
        try {
            disp.getPersistentDataContainer().set(RealisticVehicles.SLEEPKEY, PersistentDataType.BYTE_ARRAY, saver.toBytes());
            for (Interaction stand : seats.values()){
                stand.getPersistentDataContainer().set(RealisticVehicles.SLEEPKEY, PersistentDataType.STRING, disp.getUniqueId().toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean containsSeat(Entity ent){
        return seats.containsValue(ent);
    }

    public void setGearState(boolean state){
        gearDown = state;
    }

    public boolean isDoesRoll() {
        return doesRoll;
    }
}
