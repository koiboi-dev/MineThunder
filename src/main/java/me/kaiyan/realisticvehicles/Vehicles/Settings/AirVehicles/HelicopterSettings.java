package me.kaiyan.realisticvehicles.Vehicles.Settings.AirVehicles;

import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.Vehicles.Settings.VehicleSettings;
import net.minecraft.util.Tuple;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HelicopterSettings extends VehicleSettings {
    private float climbRate;
    private float fallRate;
    private float moveSpeed;
    private float moveAccel;

    private float yawSpeed;

    private List<Vector> gunPositions = new ArrayList<>();
    private float fireRate;

    private final HashMap<int[], Tuple<Integer, Integer>> models = new HashMap<>();
    private final float midOffset;
    private final boolean shiftGrid;

    public HelicopterSettings(String type, int textureID, VehicleType vtype, float price, float midOffset, boolean shiftGrid) {
        super(type, textureID, vtype, price);
        this.midOffset = midOffset;
        this.shiftGrid = shiftGrid;
    }

    public float getClimbRate() {
        return climbRate;
    }

    public float getFallRate() {
        return fallRate;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public float getMoveAccel() {
        return moveAccel;
    }

    public float getYawSpeed() {
        return yawSpeed;
    }

    public List<Vector> getGunPositions() {
        return gunPositions;
    }

    public float getFireRate() {
        return fireRate;
    }

    public HashMap<int[], Tuple<Integer, Integer>> getModels() {
        return models;
    }

    public float getMidOffset() {
        return midOffset;
    }

    public boolean isShiftGrid() {
        return shiftGrid;
    }
}
