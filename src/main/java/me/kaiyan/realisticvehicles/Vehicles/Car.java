package me.kaiyan.realisticvehicles.Vehicles;

import me.kaiyan.realisticvehicles.DamageModel.DamageModel;
import me.kaiyan.realisticvehicles.DamageModel.Projectiles.Shell;
import me.kaiyan.realisticvehicles.DataTypes.*;
import me.kaiyan.realisticvehicles.DataTypes.Enums.TrailerTypes;
import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.DataTypes.Interfaces.FixedUpdate;
import me.kaiyan.realisticvehicles.DataTypes.Interfaces.Sleepable;
import me.kaiyan.realisticvehicles.DataTypes.Interfaces.VehicleInterface;
import me.kaiyan.realisticvehicles.Models.Harvester.BlockHarvester;
import me.kaiyan.realisticvehicles.Models.InventoryHandler;
import me.kaiyan.realisticvehicles.Models.MissileHolder;
import me.kaiyan.realisticvehicles.Models.Model;
import me.kaiyan.realisticvehicles.Physics.GroundVehicle;
import me.kaiyan.realisticvehicles.RealisticVehicles;
import me.kaiyan.realisticvehicles.VehicleManagers.VehicleSaver;
import me.kaiyan.realisticvehicles.Vehicles.Settings.GroundVehicles.CarSettings;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.minecraft.util.Tuple;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.util.*;

public class Car extends GroundVehicle implements FixedUpdate, VehicleInterface, Sleepable {
    private Model model;
    private Entity seatEnt;
    private final DamageModel damageModel;
    private final CarSettings settings;

    private final FuelTank fuelTank;

    private final World world;

    List<TrailerHitch> hitches = new ArrayList<>();

    private final BlockHarvester harvester;
    private boolean trigger;

    public Car(Location loc, CarSettings settings) {
        super(loc, settings);
        this.settings = settings;
        world = loc.getWorld();

        damageModel = settings.getDamageModel().clone();
        fuelTank = new FuelTank(settings);

        seatEnt = RealisticVehicles.setSeat((LivingEntity) world.spawnEntity(loc, EntityType.ARMOR_STAND), VehicleType.CAR);

        model = new Model((ArmorStand) seatEnt, settings.getSeatPos(), settings.getOffset(), true);
        for (Map.Entry<int[], Integer> entry : settings.getModels().entrySet()){
            model.addCorner(entry.getKey(), (ArmorStand) RealisticVehicles.setTexture((LivingEntity) world.spawnEntity(loc, EntityType.ARMOR_STAND), entry.getValue(), entry.getValue()));
        }
        for (Vector vec : settings.getSeats()){
            model.addSeat(vec, (ArmorStand) RealisticVehicles.setSeat((LivingEntity) world.spawnEntity(loc, EntityType.ARMOR_STAND), VehicleType.CAR));
        }

        for (Tuple<Vector, TrailerTypes> vec : settings.getHitches()){
            hitches.add(new TrailerHitch(vec.a(), vec.b()));
        }
        if (settings.getHarvester() != null) {
            harvester = settings.getHarvester().clone();
        } else {
            harvester = null;
        }

        setup(seatEnt, (packet) -> {
            if (packet.getPacket().getBooleans().read(0) && cooldown <= 0){
                trigger = !trigger;
                cooldown = 40;
            }
        });
        start();
    }

    private int cooldown = 40;

    @Override
    public void OnFixedUpdate() {
        if (getSeatedPlayer() == null){
            sleepTicks++;
        } else {
            sleepTicks = 0;
        }

        cooldown--;

        update();
        model.updatePositions(getLoc(), 0, (float) getYaw(), 0);

        setHasFuel(!(fuelTank.getFuel() <= 0));

        for (TrailerHitch hitch : hitches){
            hitch.update(getLoc(), getVehicleYaw());
            hitch.setActivated(trigger);
        }
        if (trigger && harvester != null){
            harvester.update(getLoc(), getVehicleYaw(), trigger);
        }

        if (sleepTicks >= RealisticVehicles.VEHICLESLEEPTIME){
            sleep();
        }

        sleepTicks++;
        displayActionBar();
    }

    @Override
    public void flashModel() {
        damageModel.flashAll(world, getLoc());
        for (TrailerHitch hitch : getTrailerHitches()) {
            world.spawnParticle(Particle.CLOUD, hitch.getLoc(), 1);
        }
        harvester.flashPoints(getLoc(), getVehicleYaw());
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
        String harv = ChatColor.RED+"Disabled.";
        if (trigger){
            harv = ChatColor.GREEN+"Enabled.";
        }
        BaseComponent[] comps = new ComponentBuilder()
                .append(ChatColor.GREEN+String.format("Speed: %.2f", getSpeed()))
                .append(ChatColor.YELLOW+String.format(" Fuel: %.2f", fuelTank.getFuel()))
                .append(ChatColor.WHITE+" Harvesting: "+harv)
                .create();
        if (seatEnt.getPassengers().size() != 0) {
            ((Player)seatEnt.getPassengers().get(0)).spigot().sendMessage(ChatMessageType.ACTION_BAR, comps);
        }
    }

    public BlockHarvester getHarvester() {
        return harvester;
    }

    @Override
    public Shell[] getShells() {
        return new Shell[] {new Shell()};
    }

    @Override
    public int[] getShellsAmmo() {
        return new int[] {0, 0, 0};
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
    public void crash() {
        damageModel.explosionImpact(1, 0, 0, 0, 0, 0, 0, getSeatedPlayer());

    }

    @Override
    public void closeThis(int standAction) {
        FixedUpdate.super.closeThis(standAction);
        if (standAction == 1) {
            scrap(false);
        } else if (standAction == 2){
            scrap(true);
        }
    }

    @Override
    public List<TrailerHitch> getTrailerHitches() {
        return hitches;
    }

    @Override
    public Entity getBaseSeat() {
        return seatEnt;
    }
    @Override
    public boolean hasArmourStand(ArmorStand stand) {
        return model.containsStand(stand);
    }

    private int sleepTicks = 0;
    @Override
    public int getTicksSinceLastWake() {return sleepTicks;}

    @Override
    public void setWakeTicks(int amount) {
        sleepTicks = amount;
    }

    @Override
    public void sleep() {
        if (harvester != null) {
            model.sleepStands(getType(), getNameType(), getVehicleYaw(), new VehicleSaver(this).toJson());
            closeThis(0);
        } else {
            model.sleepStands(getType(), getNameType(), getVehicleYaw(), new VehicleSaver(this).toJson());
            closeThis(0);
        }
    }

    public CarSettings getSettings(){
        return settings;
    }
}
