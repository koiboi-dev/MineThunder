package me.kaiyan.realisticvehicles.DataTypes;

import me.kaiyan.realisticvehicles.DataTypes.Enums.TrackingType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MissileSettings {
    public static final List<MissileSettings> register = new ArrayList<>();

    private final float power;
    private final float speed;
    private final float turnRate;
    private final float startFuel;
    private final float burnRate;
    private final TrackingType type;
    private final String name;
    private final int texID;

    public MissileSettings(float power, float speed, float turnRate, float startFuel, float burnRate, TrackingType type, String name, int texID) {
        this.power = power;
        this.speed = speed;
        this.turnRate = turnRate;
        this.startFuel = startFuel;
        this.burnRate = burnRate;
        this.type = type;
        this.name = name;
        this.texID = texID;
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

    public static MissileSettings getMissileFromSettings(String name){
        Optional<MissileSettings> settings = register.stream().filter(missileSettings -> missileSettings.name.equals(name)).findFirst();
        return settings.orElse(null);
    }

    public float getStartFuel() {
        return startFuel;
    }

    public void register(){
        register.add(this);
    }


}
