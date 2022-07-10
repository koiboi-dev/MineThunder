package me.kaiyan.realisticvehicles.Vehicles.Settings.GroundVehicles;

import me.kaiyan.realisticvehicles.DamageModel.DamageModel;
import me.kaiyan.realisticvehicles.Vehicles.Car;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CarSettings extends GroundVehicleSettings {
    DamageModel damageModel;

    public CarSettings(String type, int textureID) {
        super(type, textureID);
    }

    public static List<CarSettings> register = new ArrayList<>();

    public void register(){
        register.add(this);
    }

    public void setDamageModel(DamageModel model){
        damageModel = model.clone();
    }

    public static CarSettings getCarSettings(String type){
        for (CarSettings settings : register){
            if (Objects.equals(settings.getType(), type)){
                return settings;
            }
        }
        return null;
    }
}
