package me.kaiyan.realisticvehicles.Vehicles;

import me.kaiyan.realisticvehicles.Counters.FixedUpdate;
import me.kaiyan.realisticvehicles.Counters.Updates;
import me.kaiyan.realisticvehicles.DamageModel.DamageModel;
import me.kaiyan.realisticvehicles.DamageModel.Projectiles.Shell;
import me.kaiyan.realisticvehicles.DataTypes.Exceptions.InvalidTypeException;
import me.kaiyan.realisticvehicles.DataTypes.FuelTank;
import me.kaiyan.realisticvehicles.DataTypes.VehicleInterface;
import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.Physics.GroundVehicle;
import me.kaiyan.realisticvehicles.RealisticVehicles;
import me.kaiyan.realisticvehicles.Vehicles.Settings.GroundVehicles.CarSettings;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.*;

public class Car extends GroundVehicle implements VehicleInterface, FixedUpdate {
    public static final NamespacedKey ownerKey = new NamespacedKey(RealisticVehicles.getInstance(), "ownerUUID");
    public static List<Car> cars = new ArrayList<>();

    public String id;

    public DamageModel damageModel;
    public Entity baseEntity;
    public Entity driverSeat;

    public Player seatedPlayer;
    private final FuelTank fuelTank;

    public Car(Location loc,Player player, String type) throws InvalidTypeException {
        super(loc, CarSettings.getCarSettings(type));
        CarSettings settings = CarSettings.getCarSettings(type);
        id = UUID.randomUUID().toString();
        baseEntity = RealisticVehicles.setTexture((LivingEntity) Objects.requireNonNull(loc.getWorld()).spawnEntity(loc, EntityType.ARMOR_STAND), 400);
        baseEntity.setCustomName(id + "_BASE");
        driverSeat = RealisticVehicles.setSeat((LivingEntity) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND), VehicleType.GROUND);
        driverSeat.setCustomName(id+"_SEAT");
        driverSeat.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, player.getUniqueId().toString());

        fuelTank = new FuelTank(settings);
        setup(baseEntity, driverSeat, null);

        start();
    }

    @Override
    public void OnFixedUpdate() {
        if (driverSeat.getPassengers().size() != 0) {
            seatedPlayer = (Player) driverSeat.getPassengers().get(0);
        } else {
            seatedPlayer = null;
        }

        update();

        ((CraftArmorStand) baseEntity).getHandle().a(getLoc().getX(), getLoc().getY(), getLoc().getZ(), (float) getYaw(), 0);

        Vector mseatcoord = getSettings().getSeatPos().clone();
        mseatcoord.rotateAroundY(-Math.toRadians(getYaw()));
        mseatcoord.add(getLoc().toVector());

        ((CraftArmorStand) driverSeat).getHandle().a(mseatcoord.getX(), mseatcoord.getY(), mseatcoord.getZ());

        displayActionBar();
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
        return null;
    }

    @Override
    public Player getSeatedPlayer() {
        return null;
    }

    @Override
    public void explode() {
        driverSeat.getWorld().createExplosion(getLoc(), 6, true, true);
        Random rand = new Random();
        for (int i = 0; i < 100; i++) {
            driverSeat.getWorld().spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, getLoc(), 0, getRand(rand, 1), Math.abs(getRand(rand, 0.25)), getRand(rand, 1), 0.25, null, true);
            driverSeat.getWorld().spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, getLoc(), 0, getRand(rand, 0.5), Math.abs(getRand(rand, 1.25)), getRand(rand, 0.5), 0.25, null, true);
            driverSeat.getWorld().spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, getLoc(), 0, getRand(rand, 0.5), Math.abs(getRand(rand, 1.25)), getRand(rand, 0.5), 0.25, null, true);
            driverSeat.getWorld().spawnParticle(Particle.FLAME, getLoc(), 0, getRand(rand, 0.25), Math.abs(getRand(rand, 0.25)), getRand(rand, 0.25), 1, null, true);
        }
        driverSeat.remove();
        baseEntity.remove();
        cars.remove(this);
        this.closeThis();
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
        return VehicleType.GROUND;
    }

    @Override
    public String getNameType() {
        return getSettings().getType();
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public void displayActionBar() {
        if (seatedPlayer != null) {
            BaseComponent[] comps = new ComponentBuilder(ChatColor.YELLOW + "Fuel: " + Math.round(getFuelTank().getFuel())).create();
            seatedPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, comps);
        }
    }

    @Override
    public Shell[] getShells() {
        return new Shell[0];
    }

    @Override
    public int[] getShellsAmmo() {
        return new int[0];
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

    public static double getRand(Random rand, double mult){
        return (((rand.nextDouble()-0.5)*2)*mult);
    }

    @Override
    public void flashModel() {
        damageModel.flashAll(driverSeat.getWorld(), getLoc());
    }


}
