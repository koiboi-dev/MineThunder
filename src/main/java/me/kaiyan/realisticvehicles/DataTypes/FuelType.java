package me.kaiyan.realisticvehicles.DataTypes;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public record FuelType(String getFuelName, double getFuelDensity, double getFuelExchangeRate, Material getIcon) {
    public static List<FuelType> fuelTypes = new ArrayList<>();

    public void registerFuelType() {
        fuelTypes.add(this);
    }
}
