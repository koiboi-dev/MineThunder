package me.kaiyan.realisticvehicles.DataTypes;

import me.kaiyan.realisticvehicles.DamageModel.DamageModel;
import me.kaiyan.realisticvehicles.DamageModel.Projectiles.Shell;
import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.ModelHandlers.MissileHolder;
import me.kaiyan.realisticvehicles.Vehicles.Trailer;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

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
    int getTexId();
    void displayActionBar();

    Entity getBaseSeat();

    Shell[] getShells();
    int[] getShellsAmmo();
    void addShells(int shellIndex, int amount);
    void playerEnteredVehicle(Player p);
    void playerExitedVehicle(boolean skipEject);

    List<MissileSettings> getValidMissiles();
    MissileHolder getMissileHolder();

    default boolean hasArmourStand(ArmorStand stand){
        return false;
    }

    void scrap(boolean delete);

    List<TrailerHitch> getTrailerHitches();
}
