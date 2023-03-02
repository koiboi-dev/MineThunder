package me.kaiyan.realisticvehicles.DataTypes;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record FuelType(String getFuelName, double getFuelDensity, double getFuelExchangeRate, Material getIcon) {
    public static final List<FuelType> fuelTypes = new ArrayList<>();

    public void registerFuelType() {
        fuelTypes.add(this);
    }

    public static FuelType getTypeFromName(String name){
        for (FuelType type : fuelTypes){
            if (Objects.equals(type.getFuelName, name)){
                return type;
            }
        }
        return null;
    }
}
