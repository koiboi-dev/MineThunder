package me.kaiyan.realisticvehicles.Vehicles;

import me.kaiyan.realisticvehicles.DamageModel.DamageModel;
import me.kaiyan.realisticvehicles.DamageModel.Projectiles.Shell;
import me.kaiyan.realisticvehicles.DataTypes.*;
import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.ModelHandlers.MissileHolder;
import me.kaiyan.realisticvehicles.ModelHandlers.Model;
import me.kaiyan.realisticvehicles.Physics.GroundVehicle;
import me.kaiyan.realisticvehicles.RealisticVehicles;
import me.kaiyan.realisticvehicles.Vehicles.Settings.GroundVehicles.CarSettings;
import me.kaiyan.realisticvehicles.Vehicles.Settings.GroundVehicles.GroundVehicleSettings;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Car extends GroundVehicle implements FixedUpdate, VehicleInterface {
    private final Model model;
    private final Entity seatEnt;
    private final DamageModel damageModel;

    private final FuelTank fuelTank;

    private final World world;

    List<TrailerHitch> hitches = new ArrayList<>();

    public Car(Location loc, CarSettings settings) {
        super(loc, settings);
        world = loc.getWorld();

        damageModel = settings.getDamageModel().clone();
        fuelTank = new FuelTank(settings);

        seatEnt = RealisticVehicles.setSeat((LivingEntity) world.spawnEntity(loc, EntityType.ARMOR_STAND), VehicleType.CAR);

        model = new Model((ArmorStand) seatEnt, new Vector(), 3f);
        for (Map.Entry<int[], Integer> entry : settings.getModels().entrySet()){
            model.addCorner(entry.getKey(), (ArmorStand) RealisticVehicles.setTexture((LivingEntity) world.spawnEntity(loc, EntityType.ARMOR_STAND), entry.getValue()));
        }

        for (Vector vec : settings.getHitches()){
            hitches.add(new TrailerHitch(vec));
        }

        setup(seatEnt, null);
    }

    @Override
    public void OnFixedUpdate() {
        update();
        model.updatePositions(getLoc(), 0, (float) getYaw(), 0);

        setHasFuel(!(fuelTank.getFuel() <= 0));

        for (TrailerHitch hitch : hitches){
            hitch.update(getLoc(), getVehicleYaw(), (float) getSpeed());
        }
    }

    @Override
    public void flashModel() {
        FixedUpdate.super.flashModel();
    }

    @Override
    public void OnClose() {
        FixedUpdate.super.OnClose();
    }

    @Override
    public void closeThis(boolean clearStands) {
        FixedUpdate.super.closeThis(clearStands);
    }

    @Override
    public void start() {
        FixedUpdate.super.start();
    }


    @Override
    public float getVehicleYaw() {
        return (float) getYaw();
    }

    @Override
    public float getVehiclePitch() {
        return 0;
    }

    @Override
    public DamageModel getDamageModel() {
        return damageModel;
    }

    @Override
    public Player getSeatedPlayer() {
        if (seatEnt.getPassengers().size() != 0) {
            return (Player) seatEnt.getPassengers().get(0);
        }
        return null;
    }

    @Override
    public void explode() {
        world.createExplosion(getLoc(), (float) (getSpeed()*1.5f));
        scrap(false);
    }

    @Override
    public void fizzleAmmo(int ammoIndex) {
        explode();
    }

    @Override
    public FuelTank getFuelTank() {
        return fuelTank;
    }

    @Override
    public VehicleType getType() {
        return VehicleType.CAR;
    }

    @Override
    public String getNameType() {
        return getSettings().getType();
    }

    @Override
    public int getTexId() {
        return getSettings().getTextureID();
    }

    @Override
    public void displayActionBar() {

    }

    @Override
    public Shell[] getShells() {
        return new Shell[3];
    }

    @Override
    public int[] getShellsAmmo() {
        return new int[3];
    }

    @Override
    public void addShells(int shellIndex, int amount) {

    }

    @Override
    public void playerEnteredVehicle(Player p) {

    }

    @Override
    public void playerExitedVehicle(boolean skipEject) {

    }

    @Override
    public List<MissileSettings> getValidMissiles() {
        return null;
    }

    @Override
    public MissileHolder getMissileHolder() {
        return null;
    }

    @Override
    public void scrap(boolean delete) {
        if (delete){
            model.clearAll();
        } else {
            model.scrapStands();
        }
    }

    @Override
    public List<TrailerHitch> getTrailerHitches() {
        return hitches;
    }
}
