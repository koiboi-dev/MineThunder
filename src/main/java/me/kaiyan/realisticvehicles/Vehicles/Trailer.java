package me.kaiyan.realisticvehicles.Vehicles;

import me.kaiyan.realisticvehicles.Counters.Updates;
import me.kaiyan.realisticvehicles.DataTypes.FixedUpdate;
import me.kaiyan.realisticvehicles.DataTypes.TrailerHitch;
import me.kaiyan.realisticvehicles.DataTypes.VehicleInterface;
import me.kaiyan.realisticvehicles.ModelHandlers.Model;
import me.kaiyan.realisticvehicles.RealisticVehicles;
import me.kaiyan.realisticvehicles.Vehicles.Settings.TrailerSettings;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Map;

public class Trailer implements FixedUpdate {
    private Location loc;
    private TrailerHitch hitched;
    float yaw = 0;

    private final Model model;

    private Player hitcher;
    private boolean hitchingMode;

    //Point3D A = new Point3D { X = 0, Y = 0, Z = 0 };
    //Point3D B = new Point3D { X = 1, Y = 1, Z = 1 };
    //
    //Double distanceToAdjust = 0.5;
    //
    //Point3D newCoordinate = new Point3D {
    //                                        A.X + ((B.X - A.X) * distanceToAdjust),
    //                                        A.Y + ((B.Y - A.Y) * distanceToAdjust),
    //                                        A.Z + ((B.Z - A.Z) * distanceToAdjust)
    //                                    }


    public Trailer(Location loc, TrailerSettings settings) {
        this.loc = loc;
        model = new Model(null, new Vector(), 3f);
        for (Map.Entry<int[], Integer> entry : settings.getModels().entrySet()){
            model.addCorner(entry.getKey(), (ArmorStand) RealisticVehicles.setTexture((LivingEntity) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND), entry.getValue()));
        }
    }

    public static float getYawDifference(float start, float end){
        float result = (end - start) % 360;
        if(result < 0) result += 360;
        result -= (result > 180 ? 360 : 0);
        return result;
    }

    int loops = 0;
    @Override
    public void OnFixedUpdate() {
        loc = hitched.getLoc().clone();
        if (hitched != null) {
            yaw += (getYawDifference(yaw, hitched.getpYaw()) * 0.25 * Math.min(hitched.getParentSpeed() / 5, 1));
        }

        if (hitchingMode && loops%5 == 0){
            attemptHitch(hitcher);
        } else if (hitchingMode){
            loops++;
        }

        model.updatePositions(loc, 0,yaw,0);
    }

    public void attemptHitch(Player player){
        for (FixedUpdate update : Updates.fixedUpdates){
            if (update instanceof VehicleInterface inter){
                if (inter.getTrailerHitches().size() == 0) continue;
                for (TrailerHitch hitch : inter.getTrailerHitches()){
                    if (loc.distanceSquared(hitch.getLoc()) < 4){
                        hitched = hitch;
                        player.sendMessage(ChatColor.GREEN+"HITCHED TRAILER!");
                        hitchingMode = false;
                        return;
                    }
                }
            }
        }
        player.sendMessage(ChatColor.RED +"Failed Hitch!");
    }

    public Location getLoc() {
        return loc;
    }

    public TrailerHitch getHitched() {
        return hitched;
    }

    public float getYaw() {
        return yaw;
    }

    public Model getModel() {
        return model;
    }

    public boolean isHitchingMode() {
        return hitchingMode;
    }
}
