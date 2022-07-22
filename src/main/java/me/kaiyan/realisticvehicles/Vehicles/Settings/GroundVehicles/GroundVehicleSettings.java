package me.kaiyan.realisticvehicles.Vehicles.Settings.GroundVehicles;

import me.kaiyan.realisticvehicles.DataTypes.Enums.Traversable;
import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.DataTypes.TrailerHitch;
import me.kaiyan.realisticvehicles.ModelHandlers.Model;
import me.kaiyan.realisticvehicles.Physics.GroundVehicle;
import me.kaiyan.realisticvehicles.Vehicles.Settings.VehicleSettings;
import org.bukkit.util.Vector;

import java.util.List;

public class GroundVehicleSettings extends VehicleSettings {
    double acceleration;
    double brakeForce;
    float turnSpeed;
    double turnDeaccel;
    double maxSpeed;
    double drag;
    double reverseAccel;
    double reverseMax;

    GroundVehicle.SteerType steerType;
    Traversable traversable;

    List<Vector> hitches;

    public GroundVehicleSettings(String type, int textureID) {
        super(type, textureID, VehicleType.TANK);
    }

    public void setVehicleData(double acceleration, double brakeForce, float turnSpeed, double turnDeaccel, double maxSpeed, double drag, double reverseAccel, double reverseMax, GroundVehicle.SteerType steerType, Traversable traversable) {
        this.acceleration = acceleration;
        this.brakeForce = brakeForce;
        this.turnSpeed = turnSpeed;
        this.turnDeaccel = turnDeaccel;
        this.maxSpeed = maxSpeed;
        this.drag = drag;
        this.reverseAccel = reverseAccel;
        this.reverseMax = reverseMax;
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

    public GroundVehicle.SteerType getSteerType() {
        return steerType;
    }

    public Traversable getTraversable() {
        return traversable;
    }

    public List<Vector> getHitches() {
        return hitches;
    }

    public void addTrailerHitches(Vector pos){
        hitches.add(pos);
    }

}
