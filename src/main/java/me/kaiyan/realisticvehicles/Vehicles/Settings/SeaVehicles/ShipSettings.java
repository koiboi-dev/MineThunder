package me.kaiyan.realisticvehicles.Vehicles.Settings.SeaVehicles;

import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.Vehicles.Settings.VehicleSettings;

public class ShipSettings extends VehicleSettings {
    private float acceleration;
    private float drag;
    private float maxSpeed;
    private float health;

    private float length;
    private float width;

    public ShipSettings(String type, int textureID, VehicleType vtype, float price) {
        super(type, textureID, vtype, price);
    }

    public float getAcceleration() {
        return acceleration;
    }

    public float getDrag() {
        return drag;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public float getHealth() {
        return health;
    }
}
