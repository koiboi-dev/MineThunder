package me.kaiyan.realisticvehicles.Vehicles.Settings.GroundVehicles;


import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.Menus.PurchaseMenu;
import me.kaiyan.realisticvehicles.Models.Harvester.BlockHarvester;
import org.bukkit.util.Vector;

import java.util.*;

public class CarSettings extends GroundVehicleSettings{
    public static final List<CarSettings> register = new ArrayList<>();
    private final HashMap<int[], Integer> models = new HashMap<>();
    private final List<Vector> seats = new ArrayList<>();

    private BlockHarvester harvester;

    private final float offset;

    public CarSettings(String type, int textureID, float price, float offset, String shopGroup) {
        super(type, VehicleType.CAR,textureID, price, shopGroup);
        models.put(new int[] {0, 0}, textureID);
        this.offset = offset;
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
    public void addSeat(Vector... vecs){
        seats.addAll(Arrays.stream(vecs).toList());
    }

    public HashMap<int[], Integer> getModels() {
        return models;
    }

    public void register(){
        register.add(this);
        PurchaseMenu.addVehicleToShopList(this);
    }

    public static List<CarSettings> getRegister() {
        return register;
    }

    public static CarSettings getCarSettings(String type){
        for (CarSettings settings : register){
            if (Objects.equals(settings.getType(), type)){
                return settings;
            }
        }
        return null;
    }

    public float getOffset() {
        return offset;
    }

    public List<Vector> getSeats() {
        return seats;
    }
}
