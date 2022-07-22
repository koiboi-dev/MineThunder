package me.kaiyan.realisticvehicles.Vehicles.Settings;

import java.util.HashMap;

public class TrailerSettings {
    private final HashMap<int[], Integer> models = new HashMap<>();

    public void addModelSegment(int[] coords, int modelID){
        models.put(coords, modelID);
    }

    public HashMap<int[], Integer> getModels() {
        return models;
    }
}
