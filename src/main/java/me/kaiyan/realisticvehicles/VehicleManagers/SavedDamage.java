package me.kaiyan.realisticvehicles.VehicleManagers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.kaiyan.realisticvehicles.DamageModel.DamageModel;
import me.kaiyan.realisticvehicles.DamageModel.Hitboxes.ArmourPlate;
import me.kaiyan.realisticvehicles.DamageModel.Hitboxes.Component;

import java.io.Serializable;
import java.util.List;

public class SavedDamage implements Serializable {
    final List<ArmourPlate> plates;
    final List<Component> comps;

    public SavedDamage(DamageModel model){
        plates = model.getArmour();
        comps = model.getComponents();
    }

    public void updateDamageModel(DamageModel model){
        model.setArmour(plates);
        model.setComponents(comps);
    }

    public static SavedDamage fromJson(String json){
        return new GsonBuilder().serializeSpecialFloatingPointValues().create().fromJson(json, SavedDamage.class);
    }

    public String toJson(){
        return new GsonBuilder().serializeSpecialFloatingPointValues().create().toJson(this);
    }

    public String toString() {return toJson();}
}
