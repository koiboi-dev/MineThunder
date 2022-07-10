package me.kaiyan.realisticvehicles.ModelHandlers;

import me.kaiyan.realisticvehicles.Counters.Updates;
import me.kaiyan.realisticvehicles.DataTypes.VehicleInterface;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Radar {
    private VehicleInterface lockedTarget;
    //Radius of cone
    private float scanAngle;
    private float scanDistance;

    private final int maxTargets = 6;
    private List<VehicleInterface> pingedTargets;

    //x = the tip of the cone
    //dir = the normalized axis vector, pointing from the tip to the base
    //h = height
    //r = base radius
    //
    //p = point to test
    // https://stackoverflow.com/questions/12826117/how-can-i-detect-if-a-point-is-inside-a-cone-or-not-in-3d-space

    public Radar(float maxAngle, float maxDistance){
        maxAngle = maxAngle/2;
        scanAngle = (float) (Math.tan(Math.toRadians(maxAngle))*maxDistance);
    }

    public void update(Location loc, float yaw, float pitch){
        pingedTargets = new ArrayList<>();

        Vector dir = new Vector(0, 0, 1).rotateAroundX(Math.toRadians(pitch)).rotateAroundY(Math.toRadians(yaw));
        dir.normalize();
        Vector pos = loc.toVector();
        for (VehicleInterface inter : Updates.getActiveVehicles()){
            float cone_dist = (float) dir.clone().dot(inter.getLoc().toVector().subtract(pos));
            float cone_radius = (cone_dist/scanDistance) * scanAngle;
            float orth_distance = (float) inter.getLoc().toVector().subtract(pos).subtract(dir.multiply(cone_dist)).length();

            if (orth_distance < cone_radius && pingedTargets.size() < maxTargets){
                pingedTargets.add(inter);
            }
        }
    }
}
