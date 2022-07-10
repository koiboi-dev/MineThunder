package me.kaiyan.realisticvehicles.Vehicles.Settings;

import me.kaiyan.realisticvehicles.DataTypes.FuelTank;
import org.bukkit.util.Vector;

public class VehicleSettings {
    final String type;
    final int textureID;
    Vector seatPos;

    private double startFuel;
    private double fuelConsumptionRate;
    private double idleFuelConsumptionRate;
    private double maxFuel;
    private double leakFuelAmount;

    public VehicleSettings(String type, int textureID) {
        this.type = type;
        this.textureID = textureID;
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
}
