package me.kaiyan.realisticvehicles.DataTypes;

import me.kaiyan.realisticvehicles.DamageModel.DamageModel;
import me.kaiyan.realisticvehicles.DamageModel.Projectiles.Shell;
import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.Physics.GroundVehicle;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface VehicleInterface {
    Location getLoc();
    float getVehicleYaw();
    float getVehiclePitch();
    DamageModel getDamageModel();
    default double getTurretYaw(){return 0;}
    Player getSeatedPlayer();
    void explode();
    void fizzleAmmo(int ammoIndex);
    FuelTank getFuelTank();
    VehicleType getType();
    String getNameType();
    String getID();
    void displayActionBar();

    Entity getBaseSeat();

    Shell[] getShells();
    int[] getShellsAmmo();
    void addShells(int shellIndex, int amount);
    void playerEnteredVehicle(Player p);
    void playerExitedVehicle(boolean skipEject);
}
