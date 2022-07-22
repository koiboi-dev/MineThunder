package me.kaiyan.realisticvehicles.ModelHandlers;

import me.kaiyan.realisticvehicles.DataTypes.FixedUpdate;
import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.DataTypes.RadarTarget;
import me.kaiyan.realisticvehicles.DataTypes.VehicleInterface;
import org.bukkit.Location;
import org.bukkit.Particle;

import java.util.Objects;

public class FakeRadarTarget implements RadarTarget, FixedUpdate {
    Location loc;

    public FakeRadarTarget(Location loc) {
        this.loc = loc;
        start();
    }

    @Override
    public Location getLoc() {
        return loc;
    }

    @Override
    public VehicleType getVehicleType() {
        return VehicleType.AIR;
    }

    @Override
    public VehicleInterface getVehicleInterface() {
        return null;
    }

    @Override
    public void OnFixedUpdate() {
        Objects.requireNonNull(loc.getWorld()).spawnParticle(Particle.CRIT, loc, 1);
    }
}
