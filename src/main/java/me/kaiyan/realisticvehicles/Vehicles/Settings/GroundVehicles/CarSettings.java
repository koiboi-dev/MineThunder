package me.kaiyan.realisticvehicles.Vehicles.Settings.GroundVehicles;


import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.Menus.PurchaseMenu;
import me.kaiyan.realisticvehicles.Models.Harvester.BlockHarvester;
import org.bukkit.util.Vector;

import java.util.*;

public class CarSettings extends GroundVehicleSettings{
    public static final List<CarSettings> register = new ArrayList<>();
    private final List<Vector> seats = new ArrayList<>();

    private BlockHarvester harvester;

    private final float offset;

    public CarSettings(String type, int textureID, float price, float offset, String shopGroup, Vector scale) {
        super(type, VehicleType.CAR,textureID, price, shopGroup, scale);
        this.offset = offset;
    }

    public BlockHarvester getHarvester() {
        return harvester;
    }

    public void setHarvester(BlockHarvester harvester) {
        this.harvester = harvester;
    }
    public void addSeat(Vector... vecs){
        seats.addAll(Arrays.stream(vecs).toList());
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
