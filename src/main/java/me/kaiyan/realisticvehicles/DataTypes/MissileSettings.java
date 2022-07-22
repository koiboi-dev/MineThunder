package me.kaiyan.realisticvehicles.DataTypes;

import com.google.gson.Gson;
import me.kaiyan.realisticvehicles.DataTypes.Enums.TrackingType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MissileSettings {
    private final float power;
    private final float speed;
    private final float turnRate;
    private final float startFuel;
    private final float burnRate;
    private final TrackingType type;
    private final String name;
    private final int texID;
    private final double passiveScanAngle;
    private final double passiveScanDistance;

    public MissileSettings(float power, float speed, float turnRate, float startFuel, float burnRate, TrackingType type, String name, int texID, double passiveScanAngle, double passiveScanDistance) {
        this.power = power;
        this.speed = speed;
        this.turnRate = turnRate;
        this.startFuel = startFuel;
        this.burnRate = burnRate;
        this.type = type;
        this.name = name;
        this.texID = texID;
        this.passiveScanAngle = passiveScanAngle;
        this.passiveScanDistance = passiveScanDistance;
    }

    public double getPassiveScanDistance() {
        return passiveScanDistance;
    }

    public double getPassiveScanAngle() {
        return passiveScanAngle;
    }

    public int getTexID() {
        return texID;
    }

    public float getPower() {
        return power;
    }

    public float getSpeed() {
        return speed;
    }

    public float getTurnRate() {
        return turnRate;
    }

    public float getBurnRate() {
        return burnRate;
    }

    public TrackingType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public float getStartFuel() {
        return startFuel;
    }

    public String toJson(){
        return new Gson().toJson(this);
    }

    public String toString(){
        return toJson();
    }

    public MissileSettings fromJson(String json){
        return new Gson().fromJson(json, MissileSettings.class);
    }
}
