package me.kaiyan.realisticvehicles.Vehicles.Settings.GroundVehicles;

import me.kaiyan.realisticvehicles.DataTypes.Enums.TrailerTypes;
import me.kaiyan.realisticvehicles.DataTypes.Enums.Traversable;
import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.Physics.GroundVehicle;
import me.kaiyan.realisticvehicles.Vehicles.Settings.VehicleSettings;
import net.minecraft.util.Tuple;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class GroundVehicleSettings extends VehicleSettings {

    private double acceleration;
    private double brakeForce;
    private float turnSpeed;
    private double turnDeaccel;
    private double maxSpeed;
    private double drag;
    private double reverseAccel;
    private double reverseMax;

    GroundVehicle.SteerType steerType;
    Traversable traversable;

    List<Tuple<Vector, TrailerTypes>> hitches = new ArrayList<>();

    public GroundVehicleSettings(String type, VehicleType vType,int textureID, float price) {
        super(type, textureID, vType, price);
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

    public List<Tuple<Vector, TrailerTypes>> getHitches() {
        return hitches;
    }

    public void addTrailerHitches(Vector pos, TrailerTypes type){
        hitches.add(new Tuple<>(pos, type));
    }

}
