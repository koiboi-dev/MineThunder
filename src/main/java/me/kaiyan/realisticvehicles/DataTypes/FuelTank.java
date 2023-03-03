package me.kaiyan.realisticvehicles.DataTypes;

import com.google.gson.Gson;
import me.kaiyan.realisticvehicles.Vehicles.Settings.VehicleSettings;

public class FuelTank {
    private double fuel = 0;
    final double fuelConsumptionRate;
    final double idleFuelConsumptionRate;
    final double maxFuel;
    final double fuelLeakAmount;
    FuelType loadedFuelType = FuelType.fuelTypes.get(0);

    public FuelTank(double startFuel,double fuelConsumptionRate, double idleFuelConsumptionRate, double maxFuel, double fuelLeakAmount) {
        this.fuel = startFuel;
        this.fuelConsumptionRate = fuelConsumptionRate;
        this.idleFuelConsumptionRate = idleFuelConsumptionRate;
        this.maxFuel = maxFuel;
        this.fuelLeakAmount = fuelLeakAmount;
    }

    public FuelTank(VehicleSettings settings){
        fuel = settings.getStartFuel();
        fuelConsumptionRate = settings.getFuelConsumptionRate();
        idleFuelConsumptionRate = settings.getIdleFuelConsumptionRate();
        maxFuel = settings.getMaxFuel();
        fuelLeakAmount = settings.getLeakFuelAmount();
    }

    public double getFuel() {
        return fuel;
    }

    public void setFuel(double fuel) {
        this.fuel = fuel;
    }

    public void addFuel(double fuel){
        this.fuel += fuel;
    }
    public void removeFuel(double fuel){
        this.fuel -= fuel;
    }

    public float refuelAmountOfFuel(FuelType type, double amount) {
        float moneyEquiv;
        if (type != loadedFuelType){
            moneyEquiv = (float) (loadedFuelType.getFuelExchangeRate() * fuel);
            moneyEquiv += type.getFuelExchangeRate()*amount;
            loadedFuelType = type;
        } else {
            moneyEquiv = (float) (type.getFuelExchangeRate() * (fuel+amount));
        }
        fuel = (float) (moneyEquiv/type.getFuelExchangeRate());
        //RealisticVehicles.debugLog("Set Fuel : " + moneyEquiv/type.getFuelExchangeRate() + " : " + moneyEquiv);
        if (fuel > maxFuel){
            fuel = maxFuel;
            return (float) ((fuel-maxFuel)*type.getFuelExchangeRate());
        }
        return 0;
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

    public double getFuelLeakAmount() {
        return fuelLeakAmount;
    }

    public FuelType getLoadedFuelType() {
        return loadedFuelType;
    }

    public void removeFuelWithDensity(double fuel){
        this.fuel -= fuel/getLoadedFuelType().getFuelDensity();
    }

    public String toJson(){
        return new Gson().toJson(this);
    }
}
