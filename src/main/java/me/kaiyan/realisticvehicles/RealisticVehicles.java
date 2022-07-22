package me.kaiyan.realisticvehicles;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import me.kaiyan.realisticvehicles.Commands.TestCommands;
import me.kaiyan.realisticvehicles.DataTypes.FixedUpdate;
import me.kaiyan.realisticvehicles.Counters.TPSCounter;
import me.kaiyan.realisticvehicles.Counters.Updates;
import me.kaiyan.realisticvehicles.DamageModel.DamageModel;
import me.kaiyan.realisticvehicles.DamageModel.Dimensions.Rect;
import me.kaiyan.realisticvehicles.DamageModel.Hitboxes.ArmourPlate;
import me.kaiyan.realisticvehicles.DamageModel.Hitboxes.Component;
import me.kaiyan.realisticvehicles.DamageModel.Projectiles.Shell;
import me.kaiyan.realisticvehicles.DataTypes.Enums.ComponentType;
import me.kaiyan.realisticvehicles.DataTypes.Enums.TrackingType;
import me.kaiyan.realisticvehicles.DataTypes.Enums.Traversable;
import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.DataTypes.FuelType;
import me.kaiyan.realisticvehicles.DataTypes.MissileSettings;
import me.kaiyan.realisticvehicles.DataTypes.VehicleInterface;
import me.kaiyan.realisticvehicles.ModelHandlers.MissileSlot;
import me.kaiyan.realisticvehicles.Physics.GroundVehicle;
import me.kaiyan.realisticvehicles.Vehicles.Settings.AirVehicles.AirVehicleSettings;
import me.kaiyan.realisticvehicles.Vehicles.Settings.GroundVehicles.TankSettings;
import me.kaiyan.realisticvehicles.Vehicles.Tank;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.ipvp.canvas.MenuFunctionListener;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RealisticVehicles extends JavaPlugin {
    public static NamespacedKey SCRAPKEY;

    public static RealisticVehicles plugin;
    public static ProtocolManager protocolManager;

    private static Economy econ = null;

    @Override
    public void onEnable() {
        plugin = this;
        SCRAPKEY = new NamespacedKey(RealisticVehicles.getInstance(), "scrap");
        Objects.requireNonNull(getCommand("realvehicles")).setExecutor(new TestCommands());
        new TPSCounter().runTaskTimer(this, 0, 1);
        protocolManager = ProtocolLibrary.getProtocolManager();

        setupVehicles();

        getServer().getPluginManager().registerEvents(new EventListener(), this);
        getServer().getPluginManager().registerEvents(new MenuFunctionListener(), this);
        System.out.println("Registered Events");

        new BukkitRunnable(){

            @Override
            public void run() {
                Updates.triggerFixedUpdate();
            }
        }.runTaskTimer(this, 20, 2);

        setupEconomy();

        saveDefaultConfig();

        loadFuelTypes();

        //WorldServer server = (WorldServer) getServer().getWorlds().get(0);
        //Gets holder for EntityTracker
        //server.k().a;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public void loadFuelTypes(){
        ConfigurationSection section = getConfig().getConfigurationSection("fuelTypes");
        assert section != null;
        for (String key : section.getKeys(false)){
            FuelType type = new FuelType(section.getString(key+".name"),
                    section.getDouble(key+".fuelDensity"),
                    section.getDouble(key+".fuelExchangeRate"),
                    Material.getMaterial(Objects.requireNonNull(section.getString(key+".matIcon"))));
            type.registerFuelType();
        }
    }

    public void setupVehicles(){
        TankSettings tankSettings = new TankSettings("Challenger II", 500);
        tankSettings.setVehicleData(0.05, 0.05, 3f, 0.01, 0.75, 0.03, 0.04, 2, GroundVehicle.SteerType.TANK, Traversable.BLOCK);
        tankSettings.setTankData(5, 1, -10, 15);
        tankSettings.setSize(1, 3);

        DamageModel damageModel = new DamageModel(new Rect(0, 0.75, 0.5,8, 2, 9, true), new Rect(0, 2.75, 0.5,8, 2, 9, true));

        //Front Plating
        damageModel.addArmour(new ArmourPlate(0.75, 0, 1, 3, 1, 1, 1.5, true, 0.8, false));
        damageModel.addArmour(new ArmourPlate(1.25, 0, 1, 2, 3, 1, 1, true, 0.8, false));
        //Side Plating
        damageModel.addArmour(new ArmourPlate(0.25, 1.3, 0.5, 0, 0.5, 1, 4, true, 0.8, false));
        damageModel.addArmour(new ArmourPlate(0.25, -1.3, 0.5, 0, 0.5, 1, 4, true, 0.8, false));
        //Rear Plating
        damageModel.addArmour(new ArmourPlate(0.25, 0, 1, -2, 3, 1, 0.5, true, 0.8, false));
        //Rear Roof Plating
        damageModel.addArmour(new ArmourPlate(0.25, 0, 1.55, -1.25, 3, 0.25, 1, true, 0.8, false));

        //Turret Plating
        //Side plating
        damageModel.addArmour(new ArmourPlate(0.1,1,2,-0.5,0.5,0.8,3,true, 0.8, true));
        damageModel.addArmour(new ArmourPlate(0.1,-1,2,-0.5,0.5,0.8,3,true, 0.8, true));
        //Front Plating
        damageModel.addArmour(new ArmourPlate(2,0,2,1,2,1,1,true, 0.8, true));
        //Rear Plating
        damageModel.addArmour(new ArmourPlate(0.5,0,2,-1.5,2,1,0.5,true, 0.8, true));
        //Roof Plating
        damageModel.addArmour(new ArmourPlate(0.5,0,2.75,-0.5,2,0.5,3,true, 0.8, true));
        //Components
        //Engine
        damageModel.addComponent(new Component(ComponentType.ENGINE, 5, 1,0.7,0.25,false, 0, 1, -1.5, 2, 1, 1, true,false, Particle.SMOKE_NORMAL, Particle.CAMPFIRE_COSY_SMOKE, Particle.LAVA, false));
        damageModel.addComponent(new Component(ComponentType.AMMOSTOWAGE, 0.25, 1.5,0.4,0.2,true, -0.75, 2, -0.75, 0.4, 0.5, 0.5, true,true, Particle.CAMPFIRE_COSY_SMOKE, Particle.CRIT, Particle.LAVA, true));
        damageModel.addComponent(new Component(ComponentType.AMMOSTOWAGE, 0.25, 1.5,0.4,0.2,true, 0.75, 2, -0.75, 0.4, 0.5, 0.5, true,true, Particle.CAMPFIRE_COSY_SMOKE, Particle.CRIT, Particle.LAVA, true));
        damageModel.addComponent(new Component(ComponentType.AMMOSTOWAGE, 0.25, 1.5,0.4,0.2,true, 0.5, 0.75, 1.25, 0.75, 0.75, 0.75, true,true, Particle.CAMPFIRE_COSY_SMOKE, Particle.CRIT, Particle.LAVA, true));
        damageModel.addComponent(new Component(ComponentType.GUNBARREL, 4, 3,2,1,false, 0, 2, 2.5, 0.5, 0.5, 2.5, true,true, Particle.ASH, Particle.CRIT, Particle.SMOKE_NORMAL, false));
        damageModel.addComponent(new Component(ComponentType.TURRETRING, 2, 2,0.4,0.3,false, 0, 1.5, 0, 1.5, 0.25, 1.5, true,true, Particle.ASH, Particle.CRIT, Particle.CRIT_MAGIC, false));
        damageModel.addComponent(new Component(ComponentType.GUNLOADER, 3, 0.5,0.4,0.3,false, 0,2, 0.5, 0.6, 0.5, 0.6, true,true, Particle.ASH, Particle.CRIT, Particle.CRIT_MAGIC, false));
        damageModel.addComponent(new Component(ComponentType.FUEL, 0.5, 2,1.8,1.5,true, 0.5,1, -0.5, 0.5, 0.5, 0.75, true,false, Particle.DRIP_LAVA, Particle.FLAME, Particle.LAVA, false));
        damageModel.addComponent(new Component(ComponentType.FUEL, 0.5, 2,1.8,1.5,true, -0.5,1, -0.5, 0.5, 0.5, 0.75, true,false, Particle.DRIP_LAVA, Particle.FLAME, Particle.LAVA, false));
        damageModel.addComponent(new Component(ComponentType.PLAYER, 0, 20,1.8,1.5,true, 0.1,1, -0.3, 0.75, 1.25, 0.75, true,true, Particle.ASH, Particle.ASH, Particle.ASH, false));

        damageModel.finish();

        tankSettings.setDamageModel(damageModel);
        tankSettings.setPositions(new Vector(-0.85, -0.5, -0.2),new Vector(-0.85, 0.4, -0.2), new Vector(0, 1.5, 3.5), new Vector(0.5, 1.5, 2));
        Shell apShell = new Shell(5, 1.5, true, false, false,false, Material.GLASS, Collections.singletonList("&eArmour Piercing Sabot Round"),4, true, 3f, 100, 0.05, 1);
        Shell heshShell = new Shell(3, 6, false, false, true,false, Material.TNT, List.of("&eHigh Explosive Squash Head", "Low penetration but extremely damaging to armour"),2, false, 6f, 400, 0.4, 1);
        Shell heatShell = new Shell(5, 0.25, false, false, false, true, Material.HOPPER, Collections.singletonList("&eHigh Explosive Anti-Tank"), 5, true, 3.5f, 500, 0, 1);
        tankSettings.setShellData(apShell, heshShell, heatShell);
        tankSettings.setFuelData(100, 0.05f, 0, 2000, 0.05f);

        tankSettings.register();

        DamageModel migModel = new DamageModel(new Rect(0, 0, 0, 10, 10, 10, true), null);

        migModel.addComponent(new Component(ComponentType.WING, 0.05, 5, 3.5, 2.5, false, 2, 1.5, 0, 2, 0.2, 2, true, false, Particle.CLOUD, Particle.VILLAGER_ANGRY, Particle.LAVA, false));
        migModel.addComponent(new Component(ComponentType.WING, 0.05, 5, 3.5, 2.5, false, -2, 1.5, 0, 2, 0.2, 2, true, false, Particle.CLOUD, Particle.VILLAGER_ANGRY, Particle.LAVA, false));
        migModel.addComponent(new Component(ComponentType.FUEL, 0.05, 5, 3.5, 2.5, false, 2, 1.5, 0, 1, 0.15, 1.5, true, false, Particle.LAVA, Particle.DRIP_LAVA, Particle.VILLAGER_ANGRY, false));
        migModel.addComponent(new Component(ComponentType.FUEL, 0.05, 5, 3.5, 2.5, false, -2, 1.5, 0, 1, 0.15, 1.5, true, false, Particle.LAVA, Particle.DRIP_LAVA, Particle.VILLAGER_ANGRY, false));
        migModel.addComponent(new Component(ComponentType.AILERON, 0.05, 5, 3.5, 2.5, false, 1.75, 1.5, -1, 1.75, 0.15, 0.4, true, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD, false));
        migModel.addComponent(new Component(ComponentType.AILERON, 0.05, 5, 3.5, 2.5, false, -1.75, 1.5, -1, 1.75, 0.15, 0.4, true, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD, false));
        migModel.addComponent(new Component(ComponentType.ELEVATOR, 0.05, 5, 3.5, 2.5, false, 1.75, 1, -1.5, 1.5, 0.15, 1, true, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD, false));
        migModel.addComponent(new Component(ComponentType.ELEVATOR, 0.05, 5, 3.5, 2.5, false, -1.75, 1, -1.5, 1.5, 0.15, 1, true, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD, false));
        migModel.addComponent(new Component(ComponentType.RUDDER, 0.05, 5, 3.5, 2.5, false, 1.25, 2.5, -1.5, 0.25, 1.25, 1.25, true, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD, false));
        migModel.addComponent(new Component(ComponentType.RUDDER, 0.05, 5, 3.5, 2.5, false, -1.25, 2.5, -1.5, 0.25, 1.25, 1.25, true, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD, false));
        migModel.addComponent(new Component(ComponentType.ENGINE, 0.25, 3, 2, 1, false, 0, 1.25, -1.25, 0.75, 1.25, 1.25, true, false, Particle.LAVA, Particle.LAVA, Particle.VILLAGER_ANGRY, false));
        migModel.addComponent(new Component(ComponentType.LANDINGGEAR, 0.25, 3, 2, 1, false, 0, 0.5, 1, 0.5, 1, 0.5, true, false, Particle.LAVA, Particle.LAVA, Particle.VILLAGER_ANGRY, false));

        migModel.finish();

        AirVehicleSettings planeSettings = new AirVehicleSettings("MIG 31", 601);
        planeSettings.setSeatPos(new Vector(0, -0.5, 0));
        planeSettings.setControlData(1, 2, 3, 1, 1.75);
        planeSettings.setFlightData(6, 0.035, 0.0025, 2, 2.75, 3);
        planeSettings.setSize(1, 3);
        planeSettings.setDamageModel(migModel);
        planeSettings.setFireRate(0);
        planeSettings.setBullet(new Shell(0.2, 1, false, false, false, false, Material.REDSTONE_TORCH, Collections.singletonList("Fires the planes bullet"), 10, true, 0, 5, 0.25, 50));
        planeSettings.addGunPosition(new Vector(0.5,-0.1,0.25));
        planeSettings.setFuelData(20, 0.1f, 0, 150, 0.05f);

        planeSettings.addModelSegment(new int[]{0, 0}, 601);

        planeSettings.addMissileSlot(new MissileSlot(new Vector(2, 1.75, 0), "Left Missile Slot"));
        planeSettings.addMissileSlot(new MissileSlot(new Vector(-2, 1.75, 0), "Right Missile Slot"));
        planeSettings.addMissile(new MissileSettings(6, 6, 2.5f, 100, 0.05f, TrackingType.ACTIVE, "R-40 Interceptor", 701, 40, 1000));
        planeSettings.setRadar(60, 1000);

        planeSettings.register();

        startFlasher();
    }
    //TODO Planes:
    //TODO DamageModel affecting flight
    //TODO Crashing when landing hard
    //TODO Planes having Ailerons, Elevators, Rudder (Same as ailerons?), Fuel, Flaps (Maybe?), Wings and Gun Damage
    //TODO CLEANUP!

    @Override
    public void onDisable() {
        for (FixedUpdate inter : Updates.fixedUpdates){
            inter.closeThis(true);
        }
        Updates.onClose();
    }

    public static void spawnParticle(Location loc, Particle.DustOptions options){
        Objects.requireNonNull(loc.getWorld()).spawnParticle(Particle.REDSTONE, loc,0,0,0,0,5, options, true);
    }

    public static void spawnParticle(Location loc, Particle particle){
        Objects.requireNonNull(loc.getWorld()).spawnParticle(particle, loc,0,0,0,0,10, null, true);
    }

    public static RealisticVehicles getInstance(){
        return plugin;
    }

    public static boolean flashing = false;
    public void startFlasher(){
        new BukkitRunnable() {
            //int loops = 0;
            @Override
            public void run() {
                for (FixedUpdate update : Updates.fixedUpdates){
                    if (flashing) {
                        update.flashModel();
                    }
                }
            }
        }.runTaskTimer(RealisticVehicles.getInstance(), 0, 10);
    }
    public static final boolean debugMode = true;
    public static void debugLog(String str){
        if (debugMode){
            getInstance().getLogger().info(str);
        }
    }
    public static void debugLog(float i){
        debugLog(String.valueOf(i));
    }
    public static void debugLog(Object i){
        debugLog(i.toString());
    }

    public static Entity setSeat(LivingEntity ent, VehicleType type) {
        ent.getPersistentDataContainer().set(EventListener.vehicleType, PersistentDataType.STRING, type.toString());
        ent.setGravity(false);
        ent.setPersistent(true);
        ent.setInvulnerable(true);

        return ent;
    }

    public static Entity setTexture(LivingEntity ent, int data) {
        ent.setPersistent(true);
        ent.setInvulnerable(true);
        ent.setGravity(false);
        ent.setInvisible(true);
        ItemStack helmet = new ItemStack(Material.WOODEN_HOE);
        ItemMeta meta = helmet.getItemMeta();
        assert meta != null;
        meta.setCustomModelData(data);
        helmet.setItemMeta(meta);
        Objects.requireNonNull(ent.getEquipment()).setHelmet(helmet);
        ((ArmorStand) ent).addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
        return ent;
    }

    public static Entity setSeatTexture(LivingEntity ent, int data, VehicleType type) {
        ent.getPersistentDataContainer().set(EventListener.vehicleType, PersistentDataType.STRING, type.toString());
        ent.setPersistent(true);
        ent.setInvulnerable(true);
        ent.setGravity(false);
        ent.setInvisible(true);
        ItemStack helmet = new ItemStack(Material.WOODEN_HOE);
        ItemMeta meta = helmet.getItemMeta();
        assert meta != null;
        meta.setCustomModelData(data);
        helmet.setItemMeta(meta);
        Objects.requireNonNull(ent.getEquipment()).setHelmet(helmet);
        ((ArmorStand) ent).addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
        ent.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
        return ent;
    }

    //TODO make effect explosions?
    //public void effectExplosion
}
