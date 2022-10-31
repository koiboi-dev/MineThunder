package me.kaiyan.realisticvehicles.DataTypes.Interfaces;

import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import org.bukkit.Location;

public interface RadarTarget {
    Location getLoc();
    VehicleType getVehicleType();
    VehicleInterface getVehicleInterface();
}
