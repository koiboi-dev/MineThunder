package me.kaiyan.realisticvehicles.Vehicles;

import me.kaiyan.realisticvehicles.DamageModel.Hitboxes.Component;
import me.kaiyan.realisticvehicles.DataTypes.Interfaces.FixedUpdate;
import me.kaiyan.realisticvehicles.Counters.Updates;
import me.kaiyan.realisticvehicles.DamageModel.DamageModel;
import me.kaiyan.realisticvehicles.DamageModel.Projectiles.Shell;
import me.kaiyan.realisticvehicles.DataTypes.*;
import me.kaiyan.realisticvehicles.DataTypes.Enums.ComponentType;
import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.DataTypes.Exceptions.InvalidTypeException;
import me.kaiyan.realisticvehicles.DataTypes.Interfaces.RadarTarget;
import me.kaiyan.realisticvehicles.DataTypes.Interfaces.Sleepable;
import me.kaiyan.realisticvehicles.DataTypes.Interfaces.VehicleInterface;
import me.kaiyan.realisticvehicles.ModelHandlers.MissileHolder;
import me.kaiyan.realisticvehicles.ModelHandlers.Model;
import me.kaiyan.realisticvehicles.Physics.AirVehicle;
import me.kaiyan.realisticvehicles.Physics.Missile;
import me.kaiyan.realisticvehicles.Physics.ProjectileShell;
import me.kaiyan.realisticvehicles.RealisticVehicles;
import me.kaiyan.realisticvehicles.VehicleManagers.VehicleSaver;
import me.kaiyan.realisticvehicles.Vehicles.Settings.AirVehicles.AirVehicleSettings;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.minecraft.util.Tuple;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

public class Aircraft extends AirVehicle implements VehicleInterface, FixedUpdate, RadarTarget, Sleepable {

    //private final Entity baseEnt;
    private Model model;
    private Entity seatEnt;
    private Player seatedPlayer;

    private final FuelTank fuelTank;

    private int bullets = 50;

    private final MissileHolder missiles;

    private boolean landingGearExtended = true;

    public static final int TRIGGER_SLOT = 0;
    public static final int FIRE_MISSILE_SLOT = 2;

    public Aircraft(Location loc, String type) throws InvalidTypeException {
        super(loc, AirVehicleSettings.getAirVehicleSettings(type));
        World world = loc.getWorld();

        assert world != null;
        //this.baseEnt = RealisticVehicles.setTexture((LivingEntity) world.spawnEntity(loc, EntityType.ARMOR_STAND), settings.getTextureID());
        seatEnt = RealisticVehicles.setSeat((LivingEntity) world.spawnEntity(loc, EntityType.ARMOR_STAND), VehicleType.AIR);

        model = new Model((ArmorStand) seatEnt, settings.getSeatPos(), getSettings().getMidOffset(), settings.isShiftGrid(),false);
        for (Map.Entry<int[], Tuple<Integer, Integer>> entry : settings.getModels().entrySet()){
            model.addCorner(entry.getKey(), (ArmorStand) RealisticVehicles.setTexture((LivingEntity) world.spawnEntity(loc, EntityType.ARMOR_STAND), entry.getValue().a(), entry.getValue().b()));
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
        inv.setItem(TRIGGER_SLOT, trigger);

        ItemStack missile = new ItemStack(Material.WOODEN_HOE);
        ItemMeta mmeta = trigger.getItemMeta();
        assert mmeta != null;
        mmeta.setDisplayName("Selected: NONE");
        mmeta.setLore(List.of("Currently selected firing pylon.", "Fires the missile in the selected pylon", "When Right Clicked"));
        missile.setItemMeta(mmeta);
        inv.setItem(FIRE_MISSILE_SLOT, missile);

        this.inv = inv;

        MissileHolder missiles1 = new MissileHolder();
        //add missile slots
        missiles1.addMissileSlots(settings.getMissileSlots());
        missiles = missiles1.deepClone();

        fuelTank = new FuelTank(settings);

        setup(seatEnt, event -> {
            if (event.getPacket().getBooleans().read(1)) {
                playerExitedVehicle(false);
            }
            if (event.getPacket().getBooleans().read(0) && cooldown <= 0){
                landingGearExtended = !landingGearExtended;
                if (landingGearExtended) {
                    event.getPlayer().sendMessage(ChatColor.YELLOW+"Extended Landing Gear");
                } else {
                    event.getPlayer().sendMessage(ChatColor.GOLD+"Retracted Landing Gear");
                }
                System.out.println("Gear State: "+landingGearExtended);
                model.setGearState(landingGearExtended);
                cooldown = 40;
            }
        });
        start();

        setCrashing(false);
        new BukkitRunnable(){
            @Override
            public void run() {
                invulnerable = false;
            }
        }.runTaskLater(RealisticVehicles.getInstance(), 20);
    }

    public int cooldown = 40;

    @Override
    public void OnFixedUpdate() {
        try {
            if (seatEnt.getPassengers().size() != 0) {
                seatedPlayer = (Player) seatEnt.getPassengers().get(0);
                sleepTicks = 0;
                cooldown--;
            } else {
                if (invOwner != null && playerInv != null) {
                    invOwner.setInvulnerable(false);
                    invOwner.getInventory().clear();
                    invOwner.getInventory().setContents(playerInv);
                    invOwner = null;
                    playerInv = null;
                }
                seatedPlayer = null;
                sleepTicks++;
            }
            setHasFuel(!(fuelTank.getFuel() <= 0));

            //((CraftArmorStand) baseEnt).getHandle().b(getLoc().getX(), getLoc().getY(), getLoc().getZ(), getVehicleYaw(), 0);
            //((CraftArmorStand) seatEnt).getHandle().b(getLoc().getX(), getLoc().getY()-0.8, getLoc().getZ(), getVehicleYaw(), 0);
            //((CraftArmorStand) baseEnt).setHeadPose(new EulerAngle(Math.toRadians(getVehiclePitch()), 0, -Math.toRadians(getRoll())));
            //((CraftArmorStand) seatEnt).setHeadPose(new EulerAngle(Math.toRadians(getVehiclePitch()), 0, -Math.toRadians(getRoll())));

            if (Double.isInfinite(getLoc().getX()) || Double.isInfinite(getLoc().getY()) || Double.isInfinite(getLoc().getZ())) {
                RealisticVehicles.getInstance().getLogger().severe("WARNING: VEHICLE AT INFINITE COORDINATES, STOPPING VEHICLE.");
                closeThis(0);
            }

            model.updatePositions(getLoc(), getVehiclePitch(), getVehicleYaw(), getRoll());
            if (missiles != null) {
                missiles.updateHeldMissilePosition(getLoc(), getVehicleYaw(), getVehiclePitch(), getRoll());
            }

            update(landingGearExtended);

            if (!invulnerable) {
                Vector vec = new Vector(0, 0, 1).rotateAroundZ(Math.toRadians(getPitch())).rotateAroundY(Math.toRadians(getYaw()));
                RayTraceResult otherHit = getWorld().rayTraceBlocks(getLoc().clone().add(new Vector(0, 0.5, 0)), vec, getSpeed() + 0.5, FluidCollisionMode.ALWAYS);
                if (otherHit != null) {
                    System.out.println("Hit Thing.");
                    if (Objects.requireNonNull(otherHit.getHitBlock()).getType().isSolid()) {
                        if (isCrashing()) {
                            float crashChange = (float) (getSpeed() * Math.max(0.25, Math.random() * 0.5));
                            removeCrashingHealth(crashChange);
                            addSpeed(-crashChange);
                            addPitch((float) ((getSpeed() / 2) * 10));
                            multiplyRoll(1.25f);
                            getWorld().createExplosion(getLoc(), (float) ((settings.getExplodeSpeed() / getSpeed()) * settings.getWeight()));
                            for (Component comp : getDamageModel().getComponents()) {
                                comp.health -= 0.5;
                                if (comp.health <= 0) {
                                    comp.destroyed = true;
                                }
                            }
                            if (this.seatedPlayer != null) {
                                Updates.expectedDeaths.put(seatedPlayer, new DeathMessage(seatedPlayer.getName() + " crashed their plane!"));
                            }
                            if (getSpeed() < 1) {
                                explode();
                            }
                        } else if (getSpeed() > 1) {
                            startCrashing();
                            getWorld().createExplosion(getLoc(), (float) (settings.getWeight() + (getSpeed() / settings.getWeight())));
                        } else {
                            Vector dir;
                            if (speed >= 0) {
                                dir = new Vector(-Math.sin(Math.toRadians(getYaw())), 0, Math.cos(Math.toRadians(getYaw())));
                            } else {
                                dir = new Vector(-Math.sin(Math.toRadians(getYaw() + 180)), 0, Math.cos(Math.toRadians(getYaw() + 180)));
                            }

                            getLoc().add(dir.clone().multiply(settings.getLength() - getLoc().distance(otherHit.getHitPosition().toLocation(getWorld()))));
                        }
                    } else if (otherHit.getHitBlock().isLiquid()) {
                        explode();
                    }
                }
                if (isCrashed()) {
                    getWorld().createExplosion(getLoc(), (float) (settings.getWeight()));
                    closeThis(1);
                }
            }

            fuelTank.removeFuelWithDensity(getFuelTank().getIdleFuelConsumptionRate());
            if (isAcceling()) {
                fuelTank.removeFuelWithDensity(getFuelTank().getFuelConsumptionRate());
            }
            for (int i : getDamageModel().getComponentIndices(ComponentType.FUEL)) {
                if (getDamageModel().getComponents().get(i).health < getDamageModel().getComponents().get(i).damageHealth) {
                    fuelTank.removeFuel(getFuelTank().getFuelLeakAmount() / 2);
                } else if (getDamageModel().getComponents().get(i).health < getDamageModel().getComponents().get(i).criticalHealth) {
                    fuelTank.removeFuel(getFuelTank().getFuelLeakAmount());
                }
            }
            tryFiring();
            displayActionBar();
            getDamageModel().showDamageParticles(getWorld(), getVehicleYaw(), getVehiclePitch(), getLoc().toVector());

            if (sleepTicks >= RealisticVehicles.VEHICLESLEEPTIME) {
                sleep();
            }
        } catch (IllegalArgumentException e){
            e.printStackTrace();
            closeThis(1);
        }
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

    private int selectedPylon = -1;
    public void attemptFireMissile(){
        missiles.fireMissile(selectedPylon, getLoc(), getVehicleYaw(), getVehiclePitch(), getRoll(), (float) getSpeed(), this, seatedPlayer);
    }

    public void updateMissileItem(){
        if (seatedPlayer.getInventory().getItem(FIRE_MISSILE_SLOT) != null && missiles.getMissiles().get(selectedPylon).getSettings() != null){
            ItemStack item = seatedPlayer.getInventory().getItem(FIRE_MISSILE_SLOT);
            if (item == null){
                return;
            }
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName("Selected: "+missiles.getMissiles().get(selectedPylon).getSettings().getName());
            meta.setCustomModelData(missiles.getMissiles().get(selectedPylon).getSettings().getTexID());
            item.setItemMeta(meta);
            System.out.println("Updated!");
        }
    }

    Player invOwner = null;
    ItemStack[] playerInv;
    final Inventory inv;
    boolean exited = true;

    public int getSelectedPylon() {
        return selectedPylon;
    }

    public void setSelectedPylon(int selectedPylon) {
        this.selectedPylon = selectedPylon;
    }

    public void cyclePylon(){
        selectedPylon += 1;
        if (selectedPylon > missiles.getMissiles().size()-1){
            selectedPylon = 0;
        }
    }

    boolean wasCreative = false;
    @Override
    public void playerEnteredVehicle(Player p) {
        exited = false;
        RealisticVehicles.debugLog("Enter");
        if (seatedPlayer == null) {
            playerInv = p.getInventory().getContents();
            p.getInventory().clear();
            p.getInventory().setContents(inv.getContents());
            wasCreative = p.getGameMode() == GameMode.CREATIVE;
            p.setGameMode(GameMode.SURVIVAL);
            invOwner = p;
        }
    }

    @Override
    public void playerExitedVehicle(boolean skipEject) {
        /*if (seatedPlayer != null && !exited) {
            exited = true;
            RealisticVehicles.debugLog("Exit");
            seatedPlayer.setInvulnerable(false);
            seatedPlayer.getInventory().clear();
            seatedPlayer.getInventory().setContents(playerInv);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (wasCreative){
                        seatedPlayer.setGameMode(GameMode.CREATIVE);
                    }
                }
            }.runTaskLater(RealisticVehicles.getInstance(), 1);
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
            invOwner = null;
        }*/
        exited = true;
        if (!skipEject){
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (wasCreative && seatedPlayer != null){
                        seatedPlayer.setGameMode(GameMode.CREATIVE);
                    }
                }
            }.runTaskLater(RealisticVehicles.getInstance(), 1);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (seatEnt.getPassengers().size() != 0) {
                        seatEnt.eject();
                    }
                }
            }.runTaskLater(RealisticVehicles.getInstance(), 1);
        }
    }

    @Override
    public void closeThis(int delStands) {
        FixedUpdate.super.closeThis(delStands);
        if (invOwner != null) {
            invOwner.setInvulnerable(false);
            invOwner.getInventory().clear();
            invOwner.getInventory().setContents(playerInv);
        }
        if (delStands == 2) {
            model.clearAll();
        } else if (delStands == 1){
            model.scrapStands();
        }
    }

    @Override
    public void scrap(boolean delete) {
        if (delete) {
            closeThis(2);
        } else {
            closeThis(1);
        }
    }

    @Override
    public void crash() {
        startCrashing();
    }

    @Override
    public List<TrailerHitch> getTrailerHitches() {
        return null;
    }

    @Override
    public List<MissileSettings> getValidMissiles() {
        return settings.getMissiles();
    }

    @Override
    public MissileHolder getMissileHolder() {
        return missiles;
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
        return super.getDamageModel();
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
        closeThis(1);
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
    public int getTexId() {
        return settings.getTextureID();
    }

    @Override
    public void displayActionBar() {
        ComponentBuilder builder = new ComponentBuilder();
        boolean found = false;
        for (FixedUpdate update : Updates.fixedUpdates) {
            if (update instanceof RadarTarget inter) {
                double highestSig = 0;
                if (inter == this){
                    continue;
                }
                float angle = (float) (Math.toDegrees(Math.atan2(inter.getLoc().getX()-getLoc().getX(), inter.getLoc().getZ()-getLoc().getZ())));
                if (Math.abs(Missile.getYawDifference(getVehicleYaw(), angle)) < 45) {
                    if (highestSig < inter.getLoc().distanceSquared(getLoc())){
                        found = true;
                        break;
                    }
                }
                if (getLoc().distanceSquared(inter.getLoc()) < 144){
                    explode();
                }
            }
        }
        if (found){
            builder.append(ChatColor.GOLD+"■■■ ");
        }
        if (seatedPlayer != null)
            seatedPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR,builder.append(String.format("Fuel: %.1f",getFuelTank().getFuel())).append(" Bullets: "+bullets).append(String.format(" Speed: %.3f", (float) getSpeed())).create());
        if (found){
            builder.append(ChatColor.GOLD+" ■■■");
        }
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
        getDamageModel().flashAll(seatEnt.getWorld(), getLoc());
    }

    @Override
    public VehicleType getVehicleType() {
        return VehicleType.AIR;
    }

    @Override
    public VehicleInterface getVehicleInterface() {
        return this;
    }

    @Override
    public boolean hasArmourStand(ArmorStand stand) {
        return model.containsStand(stand);
    }

    public int getBullets() {
        return bullets;
    }

    public void setBullets(int bullets) {
        this.bullets = bullets;
    }

    int sleepTicks = 0;
    @Override
    public int getTicksSinceLastWake() {
        return sleepTicks;
    }

    @Override
    public void setWakeTicks(int amount) {
        sleepTicks = amount;
    }

    @Override
    public void sleep() {
        if (seatedPlayer != null){
            seatEnt.eject();
            seatedPlayer.getInventory().setContents(playerInv);
        }
        model.sleepStands(getType(), getNameType(), getVehicleYaw(), new VehicleSaver(this).toJson());
        closeThis(0);
    }

    public void resetModels(ArmorStand seatEnt) {
        //model.clearAll();
        //model = new Model(seatEnt, settings.getSeatPos(), settings.getMidOffset(), false);
        //this.seatEnt = seatEnt;
        //updateSeat(seatEnt);
        /*for (Map.Entry<int[], Tuple<Integer, Integer>> stand : settings.getModels().entrySet()){
            model.addCorner(
                    stand.getKey(),
                    (ArmorStand) RealisticVehicles.setTexture((LivingEntity) seatEnt.getWorld().spawnEntity(getLoc(), EntityType.ARMOR_STAND), stand.getValue().a(), stand.getValue().b())
            );
        }*/
    }
}
