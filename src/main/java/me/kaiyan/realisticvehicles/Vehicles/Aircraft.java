package me.kaiyan.realisticvehicles.Vehicles;

import me.kaiyan.realisticvehicles.Counters.FixedUpdate;
import me.kaiyan.realisticvehicles.DamageModel.DamageModel;
import me.kaiyan.realisticvehicles.DamageModel.Projectiles.Shell;
import me.kaiyan.realisticvehicles.DataTypes.Enums.ComponentType;
import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.DataTypes.FuelTank;
import me.kaiyan.realisticvehicles.DataTypes.VehicleInterface;
import me.kaiyan.realisticvehicles.ModelHandlers.Model;
import me.kaiyan.realisticvehicles.Physics.AirVehicle;
import me.kaiyan.realisticvehicles.Physics.ProjectileShell;
import me.kaiyan.realisticvehicles.RealisticVehicles;
import me.kaiyan.realisticvehicles.Vehicles.Settings.AirVehicles.AirVehicleSettings;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Aircraft extends AirVehicle implements VehicleInterface, FixedUpdate{
    DamageModel damageModel;

    //private final Entity baseEnt;
    private Model model;
    private final Entity seatEnt;
    private Player seatedPlayer;

    private final String id;

    private final FuelTank fuelTank;

    private int bullets = 50;

    public Aircraft(Location loc, String type) {
        super(loc, Objects.requireNonNull(AirVehicleSettings.getAirVehicleSettings(type)));
        World world = loc.getWorld();

        id = UUID.randomUUID().toString();

        assert world != null;
        //this.baseEnt = RealisticVehicles.setTexture((LivingEntity) world.spawnEntity(loc, EntityType.ARMOR_STAND), settings.getTextureID());
        seatEnt = RealisticVehicles.setSeat((LivingEntity) world.spawnEntity(loc, EntityType.ARMOR_STAND), VehicleType.AIR);

        model = new Model((ArmorStand) seatEnt, new Vector(), 3f);
        for (Map.Entry<int[], Integer> entry : settings.getModels().entrySet()){
            model.addCorner(entry.getKey(), (ArmorStand) RealisticVehicles.setTexture((LivingEntity) world.spawnEntity(loc, EntityType.ARMOR_STAND), entry.getValue()));
        }

        //baseEnt.setRotation(0, 0);
        seatEnt.setRotation(0, 0);

        Inventory inv = Bukkit.createInventory(null, InventoryType.PLAYER);

        ItemStack trigger = new ItemStack(Material.LEVER);
        ItemMeta meta = trigger.getItemMeta();
        assert meta != null;
        meta.setDisplayName("Gun Trigger");
        meta.setLore(Collections.singletonList("Used to shoot with the plane"));
        trigger.setItemMeta(meta);
        inv.setItem(0, trigger);

        this.inv = inv;

        damageModel = settings.getDamageModel().clone();

        fuelTank = new FuelTank(settings);

        setup(seatEnt, event -> {
            if (event.getPacket().getBooleans().read(1)) {
                playerExitedVehicle(false);
            }
        });
        start();
    }

    @Override
    public void OnFixedUpdate() {
        if (seatEnt.getPassengers().size() != 0){
            seatedPlayer = (Player) seatEnt.getPassengers().get(0);
        } else {
            seatedPlayer = null;
        }

        //((CraftArmorStand) baseEnt).getHandle().b(getLoc().getX(), getLoc().getY(), getLoc().getZ(), getVehicleYaw(), 0);
        //((CraftArmorStand) seatEnt).getHandle().b(getLoc().getX(), getLoc().getY()-0.8, getLoc().getZ(), getVehicleYaw(), 0);
        //((CraftArmorStand) baseEnt).setHeadPose(new EulerAngle(Math.toRadians(getVehiclePitch()), 0, -Math.toRadians(getRoll())));
        //((CraftArmorStand) seatEnt).setHeadPose(new EulerAngle(Math.toRadians(getVehiclePitch()), 0, -Math.toRadians(getRoll())));

        model.updatePositions(getLoc(), getVehiclePitch(), getVehicleYaw(), (float) getRoll());

        update();

        Vector vec = new Vector(0, 0, 1).rotateAroundZ(Math.toRadians(getPitch())).rotateAroundY(Math.toRadians(getYaw()));
        RayTraceResult otherHit = getWorld().rayTraceBlocks(getLoc().clone().add(new Vector(0, 0.5, 0)), vec, getSpeed()+0.5);
        if (otherHit != null){
            if (Objects.requireNonNull(otherHit.getHitBlock()).getType().isSolid()){
                if (isCrashing()){
                    float crashChange = (float) (getSpeed()*Math.max(0.25,Math.random()*0.5));
                    removeCrashingHealth(crashChange);
                    addSpeed(-crashChange);
                    addPitch((float) ((getSpeed()/0.75)*5));
                    multiplyRoll(0.5f);
                    getWorld().createExplosion(getLoc(), (float) (settings.getWeight()+(getSpeed()/settings.getWeight())));
                    if (getSpeed() < 0.5) {
                        explode();
                    }
                } else if (getSpeed() > settings.getStallSpeed()) {
                    startCrashing();
                    getWorld().createExplosion(getLoc(), (float) (settings.getWeight()+(getSpeed()/settings.getWeight())));
                }
            } else if (otherHit.getHitBlock().isLiquid()){
                explode();
            }
        }
        if (isCrashed()){
            getWorld().createExplosion(getLoc(), (float) (settings.getWeight()*2));
            closeThis();
        }

        fuelTank.removeFuelWithDensity(getFuelTank().getIdleFuelConsumptionRate());
        if (isAcceling()){
            fuelTank.removeFuelWithDensity(getFuelTank().getFuelConsumptionRate());
        }
        for (int i : damageModel.getComponentIndices(ComponentType.FUEL)){
            if (damageModel.getComponents().get(i).health < damageModel.getComponents().get(i).damageHealth){
                fuelTank.removeFuel(getFuelTank().getFuelLeakAmount()/2);
            } else if (damageModel.getComponents().get(i).health < damageModel.getComponents().get(i).criticalHealth){
                fuelTank.removeFuel(getFuelTank().getFuelLeakAmount());
            }
        }

        tryFiring();
        displayActionBar();
        damageModel.showDamageParticles(getWorld(), getVehicleYaw(), getVehiclePitch(), getLoc().toVector());
    }

    private boolean firing = false;
    public void setFiring(boolean firing){
        this.firing = firing;
    }

    public boolean getFiring() {
        return firing;
    }

    int lastFired = 0;
    int lastFiredTick = 0;
    public void tryFiring(){
        if (lastFiredTick > settings.getFireRate()){
            if (firing) {
                lastFired++;
                if (lastFired >= settings.getGunPositions().size()) {
                    lastFired = 0;
                }
                Vector fireFrom = settings.getGunPositions().get(lastFired).clone();
                fireFrom.rotateAroundX(Math.toRadians(getRoll())).rotateAroundZ(Math.toRadians(getPitch())).rotateAroundY(Math.toRadians(getYaw()));
                fireFrom.add(getLoc().toVector());
                setFiring(false);
                if (bullets <= 0){
                    seatedPlayer.getWorld().playSound(fireFrom.toLocation(getWorld()), Sound.ENTITY_ITEM_BREAK, 30, 0.5f);
                    return;
                }
                bullets--;
                System.out.println(fireFrom);
                new ProjectileShell(fireFrom.toLocation(getWorld()), (float) getYaw(), (float) getPitch(), seatedPlayer, settings.getBullet());
                seatedPlayer.getWorld().playSound(fireFrom.toLocation(getWorld()), Sound.ENTITY_GENERIC_EXPLODE, 30, 2);
            }
            lastFiredTick = 0;
        }
        lastFiredTick++;
    }

    ItemStack[] playerInv;
    final Inventory inv;
    boolean exited = true;

    @Override
    public void playerEnteredVehicle(Player p) {
        exited = false;
        RealisticVehicles.debugLog("Enter");
        if (seatedPlayer == null) {
            playerInv = p.getInventory().getContents();
            p.getInventory().clear();
            p.getInventory().setContents(inv.getContents());
        }
    }

    @Override
    public void playerExitedVehicle(boolean skipEject) {
        RealisticVehicles.debugLog("Exit");
        if (seatedPlayer != null && !exited) {
            exited = true;
            seatedPlayer.setInvulnerable(false);
            seatedPlayer.getInventory().clear();
            seatedPlayer.getInventory().setContents(playerInv);
            if (!skipEject) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (seatEnt.getPassengers().get(0) != null) {
                            seatEnt.eject();
                        }
                    }
                }.runTaskLater(RealisticVehicles.getInstance(), 1);
            }
            playerInv = null;
        }
    }

    @Override
    public float getVehicleYaw() {
        return (float) getYaw();
    }

    @Override
    public float getVehiclePitch() {
        return (float) getPitch();
    }

    @Override
    public DamageModel getDamageModel() {
        return damageModel;
    }

    @Override
    public double getTurretYaw() {
        return 0;
    }

    @Override
    public Player getSeatedPlayer() {
        return seatedPlayer;
    }

    @Override
    public void explode() {
        getWorld().createExplosion(getLoc(), (float) (settings.getWeight()+(getSpeed()/settings.getWeight())));
        playerExitedVehicle(false);
        closeThis();
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
        return VehicleType.AIR;
    }

    @Override
    public String getNameType() {
        return settings.getType();
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public void displayActionBar() {
        if (seatedPlayer != null)
            seatedPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR,new ComponentBuilder().append(String.format("Fuel: %.1f",getFuelTank().getFuel())).append(" Bullets: "+bullets).append(String.format(" Speed: %.3f", (float) getSpeed())).create());
    }

    @Override
    public Entity getBaseSeat() {
        return seatEnt;
    }

    @Override
    public Shell[] getShells() {
        return new Shell[] {settings.getBullet(), null, null};
    }

    @Override
    public int[] getShellsAmmo() {
        return new int[] {bullets, 0, 0};
    }

    @Override
    public void addShells(int shellIndex, int amount) {
        bullets += amount;
    }

    @Override
    public void flashModel() {
        damageModel.flashAll(seatEnt.getWorld(), getLoc());
    }
}
