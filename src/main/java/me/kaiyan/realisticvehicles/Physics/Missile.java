package me.kaiyan.realisticvehicles.Physics;

import me.kaiyan.realisticvehicles.Counters.FixedUpdate;
import me.kaiyan.realisticvehicles.DataTypes.MissileSettings;
import me.kaiyan.realisticvehicles.DataTypes.VehicleInterface;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.EulerAngle;

public class Missile implements FixedUpdate {
    private Location loc;
    private float yaw;
    private float pitch;
    private float fuel;
    private final MissileSettings settings;
    private final ArmorStand stand;

    public Missile(Location loc, float yaw, float pitch, MissileSettings settings, ArmorStand stand) {
        this.loc = loc;
        this.yaw = yaw;
        this.pitch = pitch;
        this.settings = settings;
        fuel = settings.getStartFuel();
        this.stand = stand;

        start();
    }

    @Override
    public void OnFixedUpdate() {
        //TODO Add Missiles Firing.
        loc.setYaw(yaw);
        stand.teleport(loc);
        stand.setHeadPose(new EulerAngle(Math.toRadians(pitch), 0, 0));
    }

    public Location getLoc() {
        return loc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getFuel() {
        return fuel;
    }

    public void setFuel(float fuel) {
        this.fuel = fuel;
    }

    public MissileSettings getSettings() {
        return settings;
    }
}
