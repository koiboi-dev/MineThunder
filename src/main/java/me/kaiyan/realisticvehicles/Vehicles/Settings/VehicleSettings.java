package me.kaiyan.realisticvehicles.Vehicles.Settings;

import me.kaiyan.realisticvehicles.DamageModel.DamageModel;
import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.Vehicles.Settings.AirVehicles.AirVehicleSettings;
import me.kaiyan.realisticvehicles.Vehicles.Settings.GroundVehicles.CarSettings;
import me.kaiyan.realisticvehicles.Vehicles.Settings.GroundVehicles.TankSettings;
import org.bukkit.util.Vector;

public class VehicleSettings {
    final String type;
    final int textureID;
    Vector seatPos;
    final VehicleType vtype;
    private final float price;

    DamageModel damageModel;

    private double startFuel;
    private double fuelConsumptionRate;
    private double idleFuelConsumptionRate;
    private double maxFuel;
    private double leakFuelAmount;

    private double width = 1;
    private double length = 1;
    private final String shopGroup;
    private final Vector scale;

    public VehicleSettings(String type, int textureID, VehicleType vtype, float price, String shopGroup, Vector scale) {
        this.type = type;
        this.textureID = textureID;
        this.vtype = vtype;
        this.price = price;
        this.shopGroup = shopGroup;
        this.scale = scale;
    }

    public String getType() {
        return type;
    }

    public int getTextureID() {
        return textureID;
    }

    /**
     *
     * @param startFuel Amount of fuel to start off with
     * @param fuelConsumptionRate Amount of fuel to take when driving (Per tick)
     *                            0.05 = 1 fuel per second
     * @param idleFuelConsumption Amount of fuel to take at all times (Per tick)
     * @param maxFuel Max amount of fuel the vehicle can hold
     */
    public void setFuelData(float startFuel, float fuelConsumptionRate, float idleFuelConsumption, float maxFuel, float leakFuelAmount){
        this.startFuel = startFuel;
        this.fuelConsumptionRate = fuelConsumptionRate;
        this.idleFuelConsumptionRate = idleFuelConsumption;
        this.maxFuel = maxFuel;
        this.leakFuelAmount = leakFuelAmount;
    }

    public void setSize(float width, float length){
        this.width = width;
        this.length = length;
    }

    public Vector getSeatPos() {
        return seatPos;
    }

    public void setSeatPos(Vector seatPos) {
        this.seatPos = seatPos;
    }

    public double getStartFuel() {
        return startFuel;
    }

    public double getFuelConsumptionRate() {
        return fuelConsumptionRate;
    }

    public double getIdleFuelConsumptionRate() {
        return idleFuelConsumptionRate;
    }

    public double getMaxFuel() {
        return maxFuel;
    }

    public double getLeakFuelAmount() {
        return leakFuelAmount;
    }

    public double getWidth() {
        return width;
    }

    public double getLength() {
        return length;
    }

    public void setStartFuel(double startFuel) {
        this.startFuel = startFuel;
    }

    public void setFuelConsumptionRate(double fuelConsumptionRate) {
        this.fuelConsumptionRate = fuelConsumptionRate;
    }

    public void setIdleFuelConsumptionRate(double idleFuelConsumptionRate) {
        this.idleFuelConsumptionRate = idleFuelConsumptionRate;
    }

    public void setMaxFuel(double maxFuel) {
        this.maxFuel = maxFuel;
    }

    public void setLeakFuelAmount(double leakFuelAmount) {
        this.leakFuelAmount = leakFuelAmount;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public VehicleType getVtype() {
        return vtype;
    }

    public DamageModel getDamageModel() {
        return damageModel;
    }

    public void setDamageModel(DamageModel damageModel) {
        this.damageModel = damageModel;
    }

    public static VehicleSettings getSettingsFromType(String type){
        for (VehicleSettings settings : AirVehicleSettings.registers){
            if (settings.getType().equals(type)){
                return settings;
            }
        }
        for (VehicleSettings settings : TankSettings.getRegister()){
            if (settings.getType().equals(type)){
                return settings;
            }
        }
        for (VehicleSettings settings : CarSettings.getRegister()){
            if (settings.getType().equals(type)){
                return settings;
            }
        }
        return null;
    }

    public float getPrice() {
        return price;
    }

    public String getShopGroup() {
        return shopGroup;
    }

    public Vector getScale() {
        return scale;
    }
}
