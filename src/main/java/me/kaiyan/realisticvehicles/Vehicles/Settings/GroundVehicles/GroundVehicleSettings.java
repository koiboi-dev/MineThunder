package me.kaiyan.realisticvehicles.Vehicles.Settings.GroundVehicles;

import me.kaiyan.realisticvehicles.DataTypes.Enums.Traversable;
import me.kaiyan.realisticvehicles.Physics.GroundVehicle;
import me.kaiyan.realisticvehicles.Vehicles.Settings.VehicleSettings;

public class GroundVehicleSettings extends VehicleSettings {
    double acceleration;
    double brakeForce;
    float turnSpeed;
    double turnDeaccel;
    double maxSpeed;
    double drag;
    double reverseAccel;
    double reverseMax;
    double width;
    double length;

    GroundVehicle.SteerType steerType;
    Traversable traversable;

    public GroundVehicleSettings(String type, int textureID) {
        super(type, textureID);
    }

    public void setVehicleData(double acceleration, double brakeForce, float turnSpeed, double turnDeaccel, double maxSpeed, double drag, double reverseAccel, double reverseMax, double width, double length, GroundVehicle.SteerType steerType, Traversable traversable) {
        this.acceleration = acceleration;
        this.brakeForce = brakeForce;
        this.turnSpeed = turnSpeed;
        this.turnDeaccel = turnDeaccel;
        this.maxSpeed = maxSpeed;
        this.drag = drag;
        this.reverseAccel = reverseAccel;
        this.reverseMax = reverseMax;
        this.width = width;
        this.length = length;
        this.steerType = steerType;
        this.traversable = traversable;
    }

    public double getAcceleration() {
        return acceleration;
    }

    public double getBrakeForce() {
        return brakeForce;
    }

    public float getTurnSpeed() {
        return turnSpeed;
    }

    public double getTurnDeceleration() {
        return turnDeaccel;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getDrag() {
        return drag;
    }

    public double getReverseAccel() {
        return reverseAccel;
    }

    public double getReverseMax() {
        return reverseMax;
    }

    public double getWidth() {
        return width;
    }

    public double getLength() {
        return length;
    }

    public GroundVehicle.SteerType getSteerType() {
        return steerType;
    }

    public Traversable getTraversable() {
        return traversable;
    }
}
