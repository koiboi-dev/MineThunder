package me.kaiyan.realisticvehicles.VehicleManagers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.jna.platform.win32.VersionHelpers;
import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.DataTypes.Exceptions.InvalidTypeException;
import me.kaiyan.realisticvehicles.DataTypes.FuelType;
import me.kaiyan.realisticvehicles.DataTypes.Interfaces.VehicleInterface;
import me.kaiyan.realisticvehicles.RealisticVehicles;
import me.kaiyan.realisticvehicles.Vehicles.Aircraft;
import me.kaiyan.realisticvehicles.Vehicles.Car;
import me.kaiyan.realisticvehicles.Vehicles.Settings.GroundVehicles.CarSettings;
import me.kaiyan.realisticvehicles.Vehicles.Tank;
import org.bukkit.Location;
import org.bukkit.persistence.PersistentDataType;

import java.io.*;
import java.util.Objects;
import java.util.UUID;

public class VehicleSaver implements Serializable {
    private final String type;
    private final VehicleType vType;
    private final String savedFuelType;
    private final float savedFuel;
    private final int[] savedShells;
    private final SavedDamage damageModel;
    private final SavedMissiles missiles;
    private final String uuid;

    public VehicleSaver(VehicleInterface inter){
        type = inter.getNameType();
        vType = inter.getType();
        savedFuelType = inter.getFuelTank().getLoadedFuelType().getFuelName();
        savedFuel = (float) inter.getFuelTank().getFuel();
        savedShells = inter.getShellsAmmo();
        damageModel = new SavedDamage(inter.getDamageModel());
        uuid = UUID.randomUUID().toString();
        if (inter.getMissileHolder() != null) {
            missiles = new SavedMissiles(inter.getMissileHolder());
        } else {
            missiles = null;
        }
    }

    public VehicleInterface createCraft(Location loc){
        if (vType == VehicleType.AIR){
            try {
                RealisticVehicles.debugLog("Creating Craft...");
                Aircraft craft = new Aircraft(loc, type);
                RealisticVehicles.debugLog("Updating new vars");
                craft.getFuelTank().refuelAmountOfFuel(FuelType.getTypeFromName(savedFuelType), 0);
                craft.getFuelTank().setFuel(savedFuel);
                craft.setBullets(savedShells[0]);
                damageModel.updateDamageModel(craft.getDamageModel());
                if (craft.getMissileHolder() != null) {
                    missiles.updateMissileHolder(craft.getMissileHolder(), loc);
                }
                return craft;
            } catch (InvalidTypeException e) {
                RealisticVehicles.getInstance().getLogger().severe("UNKNOWN AIRCRAFT "+type+" WHEN PLACING NEW CRAFT!");
            }
        } else if (vType == VehicleType.TANK){
            try {
                Tank craft = new Tank(loc, type);
                craft.getFuelTank().setFuel(savedFuel);
                craft.getFuelTank().refuelAmountOfFuel(FuelType.getTypeFromName(savedFuelType), 0);
                craft.getShellsAmmo()[0] = savedShells[0];
                craft.getShellsAmmo()[1] = savedShells[1];
                craft.getShellsAmmo()[2] = savedShells[2];
                damageModel.updateDamageModel(craft.getDamageModel());
                if (craft.getMissileHolder() != null) {
                    missiles.updateMissileHolder(craft.getMissileHolder(), loc);
                }
                return craft;
            } catch (InvalidTypeException e) {
                RealisticVehicles.getInstance().getLogger().severe("UNKNOWN TANK "+type+" WHEN PLACING NEW CRAFT!");
            }
        } else if (vType == VehicleType.CAR){
            Car craft = new Car(loc, CarSettings.getCarSettings(type));
            craft.getFuelTank().setFuel(savedFuel);
            craft.getFuelTank().refuelAmountOfFuel(FuelType.getTypeFromName(savedFuelType), 0);
            damageModel.updateDamageModel(craft.getDamageModel());
            if (craft.getMissileHolder() != null) {
                missiles.updateMissileHolder(craft.getMissileHolder(), loc);
            }
            return craft;
        }
        return null;
    }

    public String getType() {
        return type;
    }

    public VehicleType getVType() {
        return vType;
    }

    public float getSavedFuel() {
        return savedFuel;
    }

    public int[] getSavedShells() {
        return savedShells;
    }

    public SavedDamage getDamageModel() {
        return damageModel;
    }

    public SavedMissiles getMissiles() {
        return missiles;
    }

    public String getSavedFuelType() {
        return savedFuelType;
    }

    public String toJson(){
        return new GsonBuilder().serializeSpecialFloatingPointValues().create().toJson(this);
    }

    public static VehicleSaver fromJson(String json){
        return new Gson().fromJson(json, VehicleSaver.class);
    }

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(this);
        oos.flush();
        return bos.toByteArray();
    }

    public static VehicleSaver fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        return (VehicleSaver) ois.readObject();
    }

    public String getUuid() {
        return uuid;
    }
}
