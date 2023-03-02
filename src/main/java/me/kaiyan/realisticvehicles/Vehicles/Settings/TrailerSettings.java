package me.kaiyan.realisticvehicles.Vehicles.Settings;

import me.kaiyan.realisticvehicles.DataTypes.Enums.TrailerTypes;
import me.kaiyan.realisticvehicles.Menus.PurchaseMenu;
import me.kaiyan.realisticvehicles.Models.Harvester.BlockHarvester;
import org.bukkit.util.Vector;

import java.util.*;

public class TrailerSettings {
    public static final List<TrailerSettings> register = new ArrayList<>();

    private final HashMap<int[], Integer> models = new HashMap<>();
    private final String name;
    private final float length;
    private final TrailerTypes type;
    private int displayID;

    private final int maxItems;

    private org.bukkit.util.Vector trailerHitchLoc = null;
    private BlockHarvester harvester = null;

    /**
     * Creates a new trailer to be added
     * @param name Name of the trailer
     * @param length Distance from the connection point the trailer should stay
     * @param storageSpace Storage slots available in the trailer (Chest is 27)
     * @param type Type of hitch to use
     */
    public TrailerSettings(String name, float length, int storageSpace, TrailerTypes type){
        this.length = length;
        this.type = type;
        this.name = name;
        maxItems = storageSpace;
    }

    public BlockHarvester getHarvester() {
        return harvester;
    }

    public void setHarvester(BlockHarvester harvester) {
        this.harvester = harvester;
    }

    public void addModelSegment(int[] coords, int modelID){
        models.put(coords, modelID);
    }

    public void setTrailerHitchPoint(org.bukkit.util.Vector point){
        this.trailerHitchLoc = point;
    }

    public HashMap<int[], Integer> getModels() {
        return models;
    }

    public float getLength(){
        return length;
    }

    public TrailerTypes getType() {
        return type;
    }

    public int getMaxItems() {
        return maxItems;
    }

    public String getName() {
        return name;
    }

    public int getDisplayID() {
        return displayID;
    }

    public static List<TrailerSettings> getRegister() {
        return register;
    }

    public void register(){
        register.add(this);
    }

    public static TrailerSettings getTrailerSettings(String type){
        for (TrailerSettings settings : register){
            if (Objects.equals(settings.getName(), type)){
                return settings;
            }
        }
        return null;
    }

    public Vector getTrailerHitchLoc() {
        return trailerHitchLoc;
    }
}
