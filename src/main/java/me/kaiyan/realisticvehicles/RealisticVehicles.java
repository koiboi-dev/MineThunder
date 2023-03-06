package me.kaiyan.realisticvehicles;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import me.kaiyan.realisticvehicles.Commands.TestCommands;
import me.kaiyan.realisticvehicles.DataTypes.Enums.*;
import me.kaiyan.realisticvehicles.DataTypes.Interfaces.FixedUpdate;
import me.kaiyan.realisticvehicles.Counters.TPSCounter;
import me.kaiyan.realisticvehicles.Counters.Updates;
import me.kaiyan.realisticvehicles.DamageModel.DamageModel;
import me.kaiyan.realisticvehicles.DamageModel.Dimensions.Rect;
import me.kaiyan.realisticvehicles.DamageModel.Hitboxes.ArmourPlate;
import me.kaiyan.realisticvehicles.DamageModel.Hitboxes.Component;
import me.kaiyan.realisticvehicles.DamageModel.Projectiles.Shell;
import me.kaiyan.realisticvehicles.DataTypes.FuelType;
import me.kaiyan.realisticvehicles.DataTypes.Interfaces.Sleepable;
import me.kaiyan.realisticvehicles.DataTypes.Interfaces.VehicleInterface;
import me.kaiyan.realisticvehicles.DataTypes.MissileSettings;
import me.kaiyan.realisticvehicles.Models.InventoryHandler;
import me.kaiyan.realisticvehicles.Models.MissileSlot;
import me.kaiyan.realisticvehicles.Models.Model;
import me.kaiyan.realisticvehicles.Physics.GroundVehicle;
import me.kaiyan.realisticvehicles.VehicleManagers.VehicleSaver;
import me.kaiyan.realisticvehicles.Vehicles.Aircraft;
import me.kaiyan.realisticvehicles.Vehicles.Car;
import me.kaiyan.realisticvehicles.Vehicles.Settings.AirVehicles.AirVehicleSettings;
import me.kaiyan.realisticvehicles.Vehicles.Settings.GroundVehicles.CarSettings;
import me.kaiyan.realisticvehicles.Vehicles.Settings.GroundVehicles.TankSettings;
import me.kaiyan.realisticvehicles.Vehicles.Settings.TrailerSettings;
import me.kaiyan.realisticvehicles.Vehicles.Settings.VehicleLoaderJSON;
import me.kaiyan.realisticvehicles.Vehicles.Settings.VehicleSettings;
import me.kaiyan.realisticvehicles.Vehicles.Tank;
import me.kaiyan.realisticvehicles.Vehicles.Trailer;
import me.kaiyan.realisticvehicles.VersionHandler.VersionHandler;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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

import java.util.*;
import java.util.logging.Logger;

public class RealisticVehicles extends JavaPlugin {
    public static NamespacedKey SCRAPKEY;
    public static NamespacedKey SLEEPKEY;
    public static NamespacedKey LANDINGKEY;

    public static int VEHICLESLEEPTIME = 400;

    public static RealisticVehicles plugin;
    public static ProtocolManager protocolManager;

    private static Economy econ = null;

    @Override
    public void onEnable() {

        plugin = this;
        double time = System.currentTimeMillis();
        getLogger().info("Launching Mine Thunder!");
        getLogger().info("Loading NameKeys...");
        SCRAPKEY = new NamespacedKey(RealisticVehicles.getInstance(), "scrap");
        SLEEPKEY = new NamespacedKey(RealisticVehicles.getInstance(), "sleep");
        LANDINGKEY = new NamespacedKey(RealisticVehicles.getInstance(), "landingGear");
        getLogger().info("Registering commands...");
        Objects.requireNonNull(getCommand("realvehicles")).setExecutor(new TestCommands());
        getLogger().info("Beginning TPS Counter...");
        new TPSCounter().runTaskTimer(this, 0, 1);
        getLogger().info("Opening Protocol Lib Library...");
        protocolManager = ProtocolLibrary.getProtocolManager();

        getLogger().info("Loading Config...");
        saveDefaultConfig();
        debugMode = getConfig().getBoolean("debug-mode");

        VEHICLESLEEPTIME = (int) Math.floor(getConfig().getDouble("sleep-time")*60*20);

        getLogger().info("Loading Fuel Types...");
        loadFuelTypes();

        getLogger().info("Getting Version...");
        VersionHandler.setVersion();

        getLogger().info("Loading Vehicles...");
        setupVehicles();

        getLogger().info("Loading Inventory Handler...");
        InventoryHandler.loadHandler();

        /*getLogger().info("Queueing Wait Loop...");
        new BukkitRunnable(){
            @Override
            public void run() {
                wakeVehicles();
            }
        }.runTaskLater(this, 0);*/

        getLogger().info("Registering Events...");
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        getServer().getPluginManager().registerEvents(new MenuFunctionListener(), this);

        getLogger().info("Starting Update Loop...");
        new BukkitRunnable(){
            @Override
            public void run() {
                Updates.triggerFixedUpdate();
            }
        }.runTaskTimer(this, 20, 2);
        startFlasher();

        getLogger().info("Connecting to Vault...");
        setupEconomy();

        if (getConfig().getBoolean("auto-wakeup")){
            new BukkitRunnable(){
                @Override
                public void run() {
                    wakeVehicles();
                }

                @Override
                public synchronized void cancel() throws IllegalStateException {
                    super.cancel();
                    for (FixedUpdate inter : Updates.fixedUpdates){
                        if (inter instanceof Sleepable sleep){
                            sleep.sleep();
                        } else {
                            inter.closeThis(1);
                        }
                    }
                }
            }.runTaskTimer(this, 20,getConfig().getInt("auto-wakeup-time"));
        }

        getLogger().info(String.format("=== Successfully Loaded Mine Thunder in %.4fs (%.4fms)! ===", (System.currentTimeMillis()-time)/1000, System.currentTimeMillis()-time));

        //WorldServer server = (WorldServer) getServer().getWorlds().get(0);
        //Gets holder for EntityTracker
        //server.k().a;
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        econ = rsp.getProvider();
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

    public void setupVehicles() {
        //region Tanks
        //region Challenger II
        TankSettings tankSettings = new TankSettings("Challenger II", 500, 500, "Tanks");
        tankSettings.setVehicleData(0.15, 0.1, 3f, 0.01, 0.75, 0.075, 0.1, 2, GroundVehicle.SteerType.TANK, Traversable.BLOCK);
        tankSettings.setTankData(5, 1, -10, 15, 0.06f, 0.6f);
        tankSettings.setSize(1, 3);

        DamageModel tankDamageModel = new DamageModel(new Rect(0, 0.75, 0.5, 8, 1.5, 9, true), new Rect(0, 2.75, 0.5, 8, 2, 9, true), 5);

        //Front Plating
        tankDamageModel.addArmour(new ArmourPlate(0.75, 0, 1, 3, 1, 1, 1.5, true, 0.8, false));
        tankDamageModel.addArmour(new ArmourPlate(1.25, 0, 1, 2, 3, 1, 1, true, 0.8, false));
        //Side Plating
        tankDamageModel.addArmour(new ArmourPlate(0.25, 1.3, 0.5, 0, 0.5, 1, 4, true, 0.8, false));
        tankDamageModel.addArmour(new ArmourPlate(0.25, -1.3, 0.5, 0, 0.5, 1, 4, true, 0.8, false));
        //Rear Plating
        tankDamageModel.addArmour(new ArmourPlate(0.25, 0, 1, -2, 3, 1, 0.5, true, 0.8, false));
        //Rear Roof Plating
        tankDamageModel.addArmour(new ArmourPlate(0.25, 0, 1.55, -1.25, 3, 0.25, 1, true, 0.8, false));

        //Turret Plating
        //Side plating
        tankDamageModel.addArmour(new ArmourPlate(0.1, 1, 2, -0.5, 0.5, 0.8, 3, true, 0.8, true));
        tankDamageModel.addArmour(new ArmourPlate(0.1, -1, 2, -0.5, 0.5, 0.8, 3, true, 0.8, true));
        //Front Plating
        tankDamageModel.addArmour(new ArmourPlate(2, 0, 2, 1, 2, 1, 1, true, 0.8, true));
        //Rear Plating
        tankDamageModel.addArmour(new ArmourPlate(0.5, 0, 2, -1.5, 2, 1, 0.5, true, 0.8, true));
        //Roof Plating
        tankDamageModel.addArmour(new ArmourPlate(0.5, 0, 2.75, -0.5, 2, 0.5, 3, true, 0.8, true));
        //Components
        //Engine
        tankDamageModel.addComponent(new Component(ComponentType.ENGINE, 5, 1, 0.7, 0.25, false, 0, 1, -1.5, 2, 1, 1, true, false, Particle.SMOKE_NORMAL, Particle.CAMPFIRE_COSY_SMOKE, Particle.LAVA));
        tankDamageModel.addComponent(new Component(ComponentType.AMMOSTOWAGE, 0.25, 1.5, 0.4, 0.2, true, -0.75, 2, -0.75, 0.4, 0.5, 0.5, true, true, Particle.CAMPFIRE_COSY_SMOKE, Particle.CRIT, Particle.LAVA));
        tankDamageModel.addComponent(new Component(ComponentType.AMMOSTOWAGE, 0.25, 1.5, 0.4, 0.2, true, 0.75, 2, -0.75, 0.4, 0.5, 0.5, true, true, Particle.CAMPFIRE_COSY_SMOKE, Particle.CRIT, Particle.LAVA));
        tankDamageModel.addComponent(new Component(ComponentType.AMMOSTOWAGE, 0.25, 1.5, 0.4, 0.2, true, 0.5, 0.75, 1.25, 0.75, 0.75, 0.75, true, true, Particle.CAMPFIRE_COSY_SMOKE, Particle.CRIT, Particle.LAVA));
        tankDamageModel.addComponent(new Component(ComponentType.GUNBARREL, 4, 3, 2, 1, false, 0, 2, 2.5, 0.5, 0.5, 2.5, true, true, Particle.ASH, Particle.CRIT, Particle.SMOKE_NORMAL));
        tankDamageModel.addComponent(new Component(ComponentType.TURRETRING, 2, 2, 0.4, 0.3, false, 0, 1.5, 0, 1.5, 0.25, 1.5, true, true, Particle.ASH, Particle.CRIT, Particle.CRIT_MAGIC));
        tankDamageModel.addComponent(new Component(ComponentType.GUNLOADER, 3, 0.5, 0.4, 0.3, false, 0, 2, 0.5, 0.6, 0.5, 0.6, true, true, Particle.ASH, Particle.CRIT, Particle.CRIT_MAGIC));
        tankDamageModel.addComponent(new Component(ComponentType.FUEL, 0.5, 2, 1.8, 1.5, true, 0.5, 1, -0.5, 0.5, 0.5, 0.75, true, false, Particle.DRIP_LAVA, Particle.FLAME, Particle.LAVA));
        tankDamageModel.addComponent(new Component(ComponentType.FUEL, 0.5, 2, 1.8, 1.5, true, -0.5, 1, -0.5, 0.5, 0.5, 0.75, true, false, Particle.DRIP_LAVA, Particle.FLAME, Particle.LAVA));
        tankDamageModel.addComponent(new Component(ComponentType.PLAYER, 0, 20, 1.8, 1.5, true, 0.1, 1, -0.3, 0.75, 1.25, 0.75, true, true, Particle.ASH, Particle.ASH, Particle.ASH));

        tankDamageModel.finish();

        tankSettings.setDamageModel(tankDamageModel);
        tankSettings.setPositions(new Vector(-0.85, -0.5, -0.2), new Vector(-0.85, 0.4, -0.2), new Vector(0, 1.5, 3.5), new Vector(0.5, 1.5, 2));
        Shell apShell = new Shell(5, 1.5, true, false, false, false, Material.GLASS, Collections.singletonList("&eArmour Piercing Sabot Round"), 4, true, 3f, 100, 0.05, 1);
        Shell heshShell = new Shell(3, 6, false, false, true, false, Material.TNT, List.of("&eHigh Explosive Squash Head", "Low penetration but extremely damaging to armour"), 2, false, 6f, 400, 0.4, 1);
        Shell heatShell = new Shell(5, 0.25, false, false, false, true, Material.HOPPER, Collections.singletonList("&eHigh Explosive Anti-Tank"), 5, true, 3.5f, 500, 0, 1);
        tankSettings.setShellData(apShell, heshShell, heatShell);
        tankSettings.setFuelData(100, 0.05f, 0, 2000, 0.05f);

        tankSettings.register();
        //endregion
        //region Leopard 2A7
        TankSettings leopardSettings = new TankSettings("Leopard 2A7", 504, 500, "Tanks");
        leopardSettings.setVehicleData(0.075, 0.1, 4f, 0.03, 1, 0.04, 0.075, 0.4, GroundVehicle.SteerType.TANK, Traversable.BLOCK);
        leopardSettings.setTankData(7, 3, -5, 15, 0.12f, 0.6f);
        leopardSettings.setSize(2, 4);

        leopardSettings.setPositions(new Vector(-0.85, -0.5, -0.2), new Vector(-0.85, 0.4, -0.2), new Vector(0, 1.5, 3.5), new Vector(0.5, 1.5, 2));
        //Shell apShell = new Shell(5, 1.5, true, false, false,false, Material.GLASS, Collections.singletonList("&eArmour Piercing Sabot Round"),4, true, 3f, 100, 0.05, 1);
        //Shell heshShell = new Shell(3, 6, false, false, true,false, Material.TNT, List.of("&eHigh Explosive Squash Head", "Low penetration but extremely damaging to armour"),2, false, 6f, 400, 0.4, 1);
        //Shell heatShell = new Shell(5, 0.25, false, false, false, true, Material.HOPPER, Collections.singletonList("&eHigh Explosive Anti-Tank"), 5, true, 3.5f, 500, 0, 1);
        leopardSettings.setShellData(apShell, heshShell, heatShell);
        leopardSettings.setFuelData(100, 0.05f, 0, 2000, 0.05f);

        //leopardSettings.register();
        //endregion
        //endregion

        //region Planes
        //region MIG 31
        DamageModel migModel = new DamageModel(new Rect(0, 0, 0, 10, 10, 10, true), null, 6);

        migModel.addComponent(new Component(ComponentType.WING, 0.05, 5, 3.5, 2.5, false, 2, 1.5, 0, 2, 0.2, 2, true, false, Particle.CLOUD, Particle.VILLAGER_ANGRY, Particle.LAVA));
        migModel.addComponent(new Component(ComponentType.WING, 0.05, 5, 3.5, 2.5, false, -2, 1.5, 0, 2, 0.2, 2, true, false, Particle.CLOUD, Particle.VILLAGER_ANGRY, Particle.LAVA));
        migModel.addComponent(new Component(ComponentType.FUEL, 0.05, 5, 3.5, 2.5, false, 2, 1.5, 0, 1, 0.15, 1.5, true, false, Particle.LAVA, Particle.DRIP_LAVA, Particle.VILLAGER_ANGRY));
        migModel.addComponent(new Component(ComponentType.FUEL, 0.05, 5, 3.5, 2.5, false, -2, 1.5, 0, 1, 0.15, 1.5, true, false, Particle.LAVA, Particle.DRIP_LAVA, Particle.VILLAGER_ANGRY));
        migModel.addComponent(new Component(ComponentType.AILERON, 0.05, 5, 3.5, 2.5, false, 1.75, 1.5, -1, 1.75, 0.15, 0.4, true, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD));
        migModel.addComponent(new Component(ComponentType.AILERON, 0.05, 5, 3.5, 2.5, false, -1.75, 1.5, -1, 1.75, 0.15, 0.4, true, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD));
        migModel.addComponent(new Component(ComponentType.ELEVATOR, 0.05, 5, 3.5, 2.5, false, 1.75, 1, -1.5, 1.5, 0.15, 1, true, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD));
        migModel.addComponent(new Component(ComponentType.ELEVATOR, 0.05, 5, 3.5, 2.5, false, -1.75, 1, -1.5, 1.5, 0.15, 1, true, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD));
        migModel.addComponent(new Component(ComponentType.RUDDER, 0.05, 5, 3.5, 2.5, false, 1.25, 2.5, -1.5, 0.25, 1.25, 1.25, true, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD));
        migModel.addComponent(new Component(ComponentType.RUDDER, 0.05, 5, 3.5, 2.5, false, -1.25, 2.5, -1.5, 0.25, 1.25, 1.25, true, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD));
        migModel.addComponent(new Component(ComponentType.ENGINE, 0.25, 3, 2, 1, false, 0, 1.25, -1.25, 0.75, 1.25, 1.25, true, false, Particle.LAVA, Particle.LAVA, Particle.VILLAGER_ANGRY));
        migModel.addComponent(new Component(ComponentType.LANDINGGEAR, 0.25, 3, 2, 1, false, 0, 0.5, 1, 0.5, 1, 0.5, true, false, Particle.LAVA, Particle.LAVA, Particle.VILLAGER_ANGRY));

        migModel.finish();

        AirVehicleSettings migSettings = new AirVehicleSettings("MIG 31", 601, 500, 1.5f, false, "Planes");
        migSettings.setSeatPos(new Vector(0, 0, 0));
        migSettings.setControlData(1.5, 2, 3, 1.5, 1.75);
        migSettings.setFlightData(6, 4, 0.025, 0.002, 1, 2.75);
        migSettings.setSize(1, 3);
        migSettings.setDamageModel(migModel);
        migSettings.setFireRate(0);
        migSettings.setBullet(new Shell(0.2, 1, false, false, false, false, Material.REDSTONE_TORCH, Collections.singletonList("Fires the planes bullet"), 10, true, 0, 5, 0.25, 50));
        migSettings.addGunPosition(new Vector(0.5, -0.1, 0.25));
        migSettings.setFuelData(20, 0.1f, 0, 150, 0.05f);

        migSettings.addModelSegment(new int[]{0, 0}, 601, 602);

        migSettings.addMissileSlot(new MissileSlot(new Vector(2, 1.75, 0), "Left Missile Slot"));
        migSettings.addMissileSlot(new MissileSlot(new Vector(-2, 1.75, 0), "Right Missile Slot"));
        migSettings.addMissile(new MissileSettings(6, 6, 2.5f, 100, 0.05f, TrackingType.ACTIVE, "R-40 Interceptor", 701, 40, 1000));
        migSettings.setRadar(60, 1000);

        migSettings.register();
        //endregion
        //region F-15
        DamageModel f15Model = new DamageModel(new Rect(0, 0, 0, 15, 15, 15, true), null, 10);

        f15Model.addComponent(new Component(ComponentType.ENGINE, 1, 2, 1, 1, false, -0.85, 0.88, 2.12, 1.75, 0.87, 2.62, false, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD));
        f15Model.addComponent(new Component(ComponentType.FUEL, 1, 1, 1, 1, false, -3.06, 1.42, 1.14, 2.04, 0.09, 1.57, false, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD));
        f15Model.addComponent(new Component(ComponentType.WING, 1, 1, 1, 1, false, -4.52, 1.38, 1.14, 3.5, 0.16, 2.15, false, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD));
        f15Model.addComponent(new Component(ComponentType.ELEVATOR, 1, 1, 1, 1, false, -2.92, 1.38, 3.91, 1.9, 0.16, 1.86, false, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD));
        f15Model.addComponent(new Component(ComponentType.ELEVATOR, 1, 1, 1, 1, false, 0.87, 1.38, 3.91, 1.9, 0.16, 1.86, false, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD));
        f15Model.addComponent(new Component(ComponentType.RUDDER, 1, 1, 1, 1, false, -1.02, 1.68, 3.32, 0.15, 2.2, 1.86, false, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD));
        f15Model.addComponent(new Component(ComponentType.RUDDER, 1, 1, 1, 1, false, 0.84, 1.68, 3.32, 0.15, 2.2, 1.86, false, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD));
        f15Model.addComponent(new Component(ComponentType.WING, 1, 1, 1, 1, false, 1.02, 1.38, 1.14, 3.5, 0.16, 2.15, false, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD));
        f15Model.addComponent(new Component(ComponentType.WING, 1, 1, 1, 1, false, 1.02, 1.38, -1.05, 1.02, 0.16, 2.15, false, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD));
        f15Model.addComponent(new Component(ComponentType.WING, 1, 1, 1, 1, false, -2.04, 1.38, -1.05, 1.02, 0.16, 2.15, false, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD));
        f15Model.addComponent(new Component(ComponentType.LANDINGGEAR, 1, 1, 1, 1, false, -1.31, 0.07, 0.99, 1.02, 0.74, 0.55, false, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD));
        f15Model.addComponent(new Component(ComponentType.LANDINGGEAR, 1, 1, 1, 1, false, 0.29, 0.07, 0.99, 1.02, 0.74, 0.55, false, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD));
        f15Model.addComponent(new Component(ComponentType.LANDINGGEAR, 1, 1, 1, 1, false, -0.44, -0.07, -4.55, 0.87, 0.74, 0.55, false, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD));
        f15Model.addComponent(new Component(ComponentType.AILERON, 1, 1, 1, 1, false, -3.5, 1.31, 2.89, 2.48, 0.31, 0.69, false, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD));
        f15Model.addComponent(new Component(ComponentType.AILERON, 1, 1, 1, 1, false, 1.02, 1.31, 2.89, 2.48, 0.31, 0.69, false, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD));
        f15Model.addComponent(new Component(ComponentType.FUEL, 1, 1, 1, 1, false, 1.28, 1.42, 1.14, 2.04, 0.09, 1.57, false, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD));
        f15Model.addComponent(new Component(ComponentType.PLAYER, 1, 1, 1, 1, false, -0.33, 0.84, -4.4, 0.58, 1.11, 0.69, false, false, Particle.CLOUD, Particle.CLOUD, Particle.CLOUD));

        f15Model.finish();

        AirVehicleSettings f15Settings = new AirVehicleSettings("F-15", 603, 500, 3, true, "Planes");
        f15Settings.setSeatPos(new Vector(0, 0.4, -0.8));
        f15Settings.setControlData(2.5, 2, 4.5, 2, 2.5);
        f15Settings.setFlightData(4, 4, 0.03, 0.0025, 1.5, 2);
        f15Settings.setSize(1, 3);
        f15Settings.setDamageModel(f15Model);
        f15Settings.setFireRate(0);
        f15Settings.setBullet(new Shell(0.2, 1, false, false, false, false, Material.REDSTONE_TORCH, Collections.singletonList("Fires the planes bullet"), 10, true, 0, 5, 0.25, 50));
        f15Settings.addGunPosition(new Vector(0.5, -0.1, 0.25));
        f15Settings.setFuelData(50, 0.2f, 0, 300, 0.3f);

        f15Settings.addModelSegment(new int[]{0, 0}, 604, 603);
        f15Settings.addModelSegment(new int[]{0, -1}, 606, 605);
        f15Settings.addModelSegment(new int[]{-1, -1}, 608, 607);

        f15Settings.addMissileSlot(new MissileSlot(new Vector(2, 1.75, 0), "Left Missile Slot"));
        f15Settings.addMissileSlot(new MissileSlot(new Vector(-2, 1.75, 0), "Right Missile Slot"));
        f15Settings.addMissile(new MissileSettings(6, 6, 2.5f, 100, 0.05f, TrackingType.ACTIVE, "R-40 Interceptor", 701, 40, 1000));
        f15Settings.setRadar(60, 1000);

        f15Settings.register();
        //endregion
        //endregion

        //region Civilian
        //region S3-X Truck
        CarSettings trucksettings = new CarSettings("Bessie Trucking S3-X Truck", 800, 400, 3, "Tanks");
        trucksettings.setSeatPos(new Vector(0.8, 1.3, -0.45));
        DamageModel truckModel = new DamageModel(new Rect(0, 0, 0, 6, 6, 6, true), null, 5);
        truckModel.addComponent(new Component(ComponentType.ENGINE, 5, 2, 1.5, 1, false, 0, 0.75, -0.2, 1.25, 0.75, 0.5, true, false, Particle.CAMPFIRE_SIGNAL_SMOKE, Particle.VILLAGER_ANGRY, Particle.LAVA));
        truckModel.finish();
        trucksettings.setDamageModel(truckModel);
        trucksettings.setVehicleData(0.025, 0.02, 1.5f, 0.015, 1.3, 0.005, 0.01, 0.2, GroundVehicle.SteerType.REGULAR, Traversable.SLAB);
        trucksettings.setFuelData(100, 0.2f, 0, 1000, 1);
        trucksettings.setSize(3, 2f);
        trucksettings.addTrailerHitches(new Vector(0, 1, -4.75), TrailerTypes.TRUCK);
        //BlockHarvester harvester = new BlockHarvester(new Vector(0, 3, -2));
        //harvester.addBreakables(Material.GRASS_BLOCK);
        //harvester.addBreakPoints(new Vector(-1, 0, 0.25), new Vector(0, 0, 0.25));
        //trucksettings.setHarvester(harvester);
        trucksettings.register();
        //endregion
        //region Model 2 Tractor
        CarSettings tractorSettings = new CarSettings("Jerry Deer's Model 2 Tractor", 801, 800, -0.5f, "Tanks");
        tractorSettings.setSeatPos(new Vector(0, 1.5, -0.6));
        DamageModel tractorModel = new DamageModel(new Rect(0, 0, 0, 5, 5, 5, true), null, 4);
        tractorModel.addComponent(new Component(ComponentType.ENGINE, 3, 2, 1.5, 1, false, 0, 1.25, 0.75, 0.75, 1, 1, true, false, Particle.CAMPFIRE_SIGNAL_SMOKE, Particle.VILLAGER_ANGRY, Particle.LAVA));
        tractorModel.finish();
        tractorSettings.setDamageModel(tractorModel);
        tractorSettings.setVehicleData(0.1, 0.2, 2f, 0.05, 1, 0.05, 0.075, 0.5, GroundVehicle.SteerType.REGULAR, Traversable.BLOCK);
        tractorSettings.setFuelData(50, 0.2f, 0, 300, 1);
        tractorSettings.setSize(1, 1.5f);
        tractorSettings.addTrailerHitches(new Vector(0, 0.25, -2.5), TrailerTypes.TRACTOR);
        tractorSettings.register();
        //endregion
        //region T5 Dry Trailer
        TrailerSettings trailerSettings = new TrailerSettings("Bessie Trucking T5 Dry Trailer", 9f, 216, TrailerTypes.TRUCK);
        trailerSettings.addModelSegment(new int[]{0, 1}, 850);
        trailerSettings.addModelSegment(new int[]{0, 0}, 851);
        trailerSettings.register();
        //endregion
        //endregion

        //Load custom vehicles
        VehicleLoaderJSON.loadVehicles();
    }

    @Override
    public void onDisable() {
        for (FixedUpdate inter : Updates.fixedUpdates){
            if (inter instanceof VehicleInterface vinter){
                if (vinter.getSeatedPlayer() != null) {
                    if (vinter instanceof Aircraft craft) {
                        craft.getSeatedPlayer().getInventory().setContents(craft.getPlayerInv());
                    } else if (vinter instanceof Tank tank) {
                        tank.getSeatedPlayer().getInventory().setContents(tank.getPlayerInv());
                    }
                }
            }
            if (inter instanceof Sleepable sleep){
                sleep.sleep();
            } else {
                inter.closeThis(1);
            }
        }
        Updates.onClose();
        getLogger().info("Disabled Mine Thunder.");
    }

    public static void spawnParticle(Location loc, Particle.DustOptions options){
        Objects.requireNonNull(loc.getWorld()).spawnParticle(Particle.REDSTONE, loc,0,0,0,0,4, options, true);
    }

    public static void spawnParticle(Location loc, Particle particle){
        Objects.requireNonNull(loc.getWorld()).spawnParticle(particle, loc,0,0,0,0,2, null, true);
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
    public static boolean debugMode = false;
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

    public static Entity setTexture(LivingEntity ent, int data, int landing) {
        ent.setPersistent(true);
        ent.setInvulnerable(true);
        ent.setGravity(false);
        ent.setInvisible(true);
        ItemStack helmet = new ItemStack(Material.WOODEN_HOE);
        ItemMeta meta = helmet.getItemMeta();
        assert meta != null;
        meta.setCustomModelData(data);
        helmet.setItemMeta(meta);
        ent.getPersistentDataContainer().set(LANDINGKEY, PersistentDataType.STRING, data+":"+landing);
        Objects.requireNonNull(ent.getEquipment()).setHelmet(helmet);
        ((ArmorStand) ent).addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
        return ent;
    }

    public static Entity setSeatTexture(LivingEntity ent, int data, int landing, VehicleType type) {
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
        meta.getPersistentDataContainer().set(LANDINGKEY, PersistentDataType.STRING, data+":"+landing);
        ((ArmorStand) ent).addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
        ent.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
        return ent;
    }

    public static Logger getStaticLogger(){
        return getInstance().getServer().getLogger();
    }

    public static void wakeVehicles(){
        getStaticLogger().info("Getting all sleeping vehicles...");
        HashMap<String, List<ArmorStand>> sleeps = new HashMap<>();
        for (World world : getInstance().getServer().getWorlds()){
            for (Entity en : world.getEntities()){
                if (en instanceof ArmorStand stand){
                    if (en.getPersistentDataContainer().has(SLEEPKEY, PersistentDataType.STRING)){
                        String id = en.getPersistentDataContainer().get(SLEEPKEY, PersistentDataType.STRING).split(";")[0];
                        RealisticVehicles.debugLog("Got Key: "+id);
                        if (!sleeps.containsKey(id)) {
                            sleeps.put(id, new ArrayList<>());
                        }
                        sleeps.get(id).add(stand);
                    }
                }
            }
        }
        RealisticVehicles.debugLog(sleeps);
        getStaticLogger().info("Waking sleeping vehicles...");
        for (Map.Entry<String, List<ArmorStand>> entry : sleeps.entrySet()){
            ArmorStand en = entry.getValue().get(0);
            // 0       ; 1      ; 2      ; 3      ; 4                   ; 5                 ; 6 (0 or 1 value); 7
            //sleepID+";"+type+";"+name+";"+data+";"+stand.getKey()[0]+";"+stand.getKey()[1];isSeatEnt; yaw
            String[] info = en.getPersistentDataContainer().get(RealisticVehicles.SLEEPKEY, PersistentDataType.STRING).split(";");
            RealisticVehicles.debugLog(info[1]);
            switch (VehicleType.valueOf(info[1])){
                case CAR -> {
                    Location loc = en.getLocation().clone();
                    if (info[6].equals("1")){
                        loc.subtract(new Vector(Float.parseFloat(info[4]), 0, Float.parseFloat(info[5])));
                    } else if (en.getUniqueId().equals(en.getUniqueId())){
                        loc.subtract(new Vector(Integer.parseInt(info[4]), 0, Integer.parseInt(info[5])).multiply(Model.GRID_OFFSET));
                    }

                    for (ArmorStand stand : entry.getValue()){
                        stand.remove();
                    }

                    Car car = (Car) VehicleSaver.fromJson(info[3].split("@")[0]).createCraft(loc);
                    car.setYaw(Float.parseFloat(info[7]));
                }
                case TANK -> {
                    // 0  ; 1           ; 2                                 ; 3  ; 4
                    //id+";"+getType()+";"+new VehicleSaver(this).toJson()+";seat;"+getVehicleYaw()
                    ArmorStand seat = null;
                    ArmorStand turret = null;
                    ArmorStand gun = null;
                    ArmorStand base = null;
                    Location loc = en.getLocation().clone();
                    boolean enIsSeat = false;
                    for (ArmorStand stand : entry.getValue()){
                        String data = stand.getPersistentDataContainer().get(RealisticVehicles.SLEEPKEY, PersistentDataType.STRING);
                        stand.getPersistentDataContainer().remove(RealisticVehicles.SLEEPKEY);
                        if (Objects.equals(data.split(";")[0], info[0])) {
                            RealisticVehicles.debugLog(data);
                            if (data.contains("gun")) gun = stand;
                            else if (data.contains("turret")) turret = stand;
                            else if (data.contains("base")) base = stand;
                            else if (data.contains("seat")) {
                                seat = stand;
                                enIsSeat = en.getUniqueId() == stand.getUniqueId();
                            }
                        }
                    }
                    Tank tank = (Tank) VehicleSaver.fromJson(info[2]).createCraft(loc);
                    tank.resetModels(seat, base, gun, turret);
                    if (enIsSeat){
                        loc.subtract(tank.getSeatPos());
                    }
                    tank.setLoc(loc);
                    tank.setYaw(Float.parseFloat(info[4]));
                }
                case AIR -> {
                    // 0       ; 1      ; 2      ; 3      ; 4                   ; 5                 ; 6 (0 or 1 value); 7
                    //sleepID+";"+type+";"+name+";"+data+";"+stand.getKey()[0]+";"+stand.getKey()[1];isSeatEnt        ; yaw
                    Location loc = en.getLocation().clone();
                    if (info[6].equals("1")){
                        if (en.getUniqueId().equals(en.getUniqueId()))
                            loc.subtract(new Vector(Float.parseFloat(info[4]), 0, Float.parseFloat(info[5])));
                        continue;
                    } else if (en.getUniqueId().equals(en.getUniqueId())){
                        loc.subtract(new Vector(Integer.parseInt(info[4]), 0, Integer.parseInt(info[5])).multiply(Model.GRID_OFFSET));
                    }

                    for (ArmorStand stand : entry.getValue()){
                        stand.remove();
                    }
                    Aircraft plane = (Aircraft) VehicleSaver.fromJson(info[3]).createCraft(loc);
                    plane.setYaw(Float.parseFloat(info[7]));
                }
                case TRAILER -> {
                    // 0       ; 1      ; 2      ; 3      ; 4                   ; 5                 ; 6 (0 or 1 value); 7
                    //sleepID+";"+type+";"+name+";"+data+";"+stand.getKey()[0]+";"+stand.getKey()[1];isSeatEnt       ; yaw
                    Location loc = en.getLocation();//.clone();

                    loc.subtract(new Vector(Integer.parseInt(info[4]), 0, Integer.parseInt(info[5])).multiply(Model.GRID_OFFSET));

                    for (ArmorStand stand : entry.getValue()){
                        stand.remove();
                    }
                    // 0       ; 1      ; 2      ; 3      ; 4                   ; 5                 ; 6 (0 or 1 value); 7
                    //sleepID+";"+type+";"+name+";"+data+";"+stand.getKey()[0]+";"+stand.getKey()[1];isSeatEnt       ; yaw
                    String[] sinfo = en.getPersistentDataContainer().get(RealisticVehicles.SLEEPKEY, PersistentDataType.STRING).split(";");
                    Trailer trailer = new Trailer(loc, TrailerSettings.getTrailerSettings(sinfo[2]));

                    trailer.setYaw(Float.parseFloat(sinfo[7]));
                }
            }
        }
    }

    //public void effectExplosion
}
