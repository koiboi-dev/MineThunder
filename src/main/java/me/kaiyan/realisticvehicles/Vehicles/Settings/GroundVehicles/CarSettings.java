package me.kaiyan.realisticvehicles.Vehicles.Settings.GroundVehicles;

import me.kaiyan.realisticvehicles.ModelHandlers.Model;

import java.util.HashMap;

public class CarSettings extends GroundVehicleSettings{
    private final Model model;
    private final HashMap<int[], Integer> models = new HashMap<>();

    public CarSettings(String type, int textureID, Model model) {
        super(type, textureID);
        this.model = model;
    }

    public Model getModel() {
        return model;
    }

    public void addModelSegment(int[] coords, int modelID){
        models.put(coords, modelID);
    }

    public HashMap<int[], Integer> getModels() {
        return models;
    }
}
