package me.kaiyan.realisticvehicles.Vehicles;

import me.kaiyan.realisticvehicles.DataTypes.*;
import me.kaiyan.realisticvehicles.DataTypes.Enums.ComponentType;
import me.kaiyan.realisticvehicles.DamageModel.DamageModel;
import me.kaiyan.realisticvehicles.DamageModel.Projectiles.Shell;
import me.kaiyan.realisticvehicles.DataTypes.Exceptions.InvalidTypeException;
import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.ModelHandlers.MissileHolder;
import me.kaiyan.realisticvehicles.Physics.GroundVehicle;
import me.kaiyan.realisticvehicles.Physics.ProjectileShell;
import me.kaiyan.realisticvehicles.RealisticVehicles;
import me.kaiyan.realisticvehicles.Vehicles.Settings.GroundVehicles.TankSettings;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftArmorStand;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.List;

import static me.kaiyan.realisticvehicles.RealisticVehicles.SCRAPKEY;

public class Tank extends GroundVehicle implements FixedUpdate, VehicleInterface {
    private final DamageModel damageModel;
    private final Entity baseEntity;
    private final Entity turretEnt;
    private final Entity gunEnt;
    private final Entity driverSeat;

    private final String type;
    private float turretYaw = 0;
    private final float traverseSpeed;
    private float turretPitch = 0;
    private final float elevateSpeed;
    private final float maxPitch;
    private final float minPitch;

    private final Vector seatPos;
    private final Vector seatRaisedPos;
    private final Vector gunBarrelPos;
    private final Vector machineGunPos;
    private boolean raised = true;

    Player seatedPlayer = null;
    ItemStack[] playerInv = null;
    private final Shell[] shells;
    private int[] shellAmmo = new int[]{5, 5, 5};

    private boolean reloading;
    private final FuelTank fuelTank;

    private final Inventory inv;
    private String reloadState = "|";

    List<TrailerHitch> hitches = new ArrayList<>();

    public Tank(Location loc, String type) throws InvalidTypeException {
        super(loc, TankSettings.getTankSettings(type));

        TankSettings settings = TankSettings.getTankSettings(type);
        if (settings != null) {
            World world = loc.getWorld();
            assert world != null;
            loc = loc.clone();

            loc.setPitch(0);
            loc.setYaw(0);
            this.seatPos = settings.getSeatPos();
            this.seatRaisedPos = settings.getSeatRaisedPos();

            baseEntity = RealisticVehicles.setTexture((LivingEntity) world.spawnEntity(loc, EntityType.ARMOR_STAND), settings.getTextureID());
            turretEnt = RealisticVehicles.setTexture((LivingEntity) world.spawnEntity(loc, EntityType.ARMOR_STAND), settings.getTextureID() + 1);
            gunEnt = RealisticVehicles.setTexture((LivingEntity) world.spawnEntity(loc, EntityType.ARMOR_STAND), settings.getTextureID() + 2);

            Vector vec = seatRaisedPos.clone();
            vec.rotateAroundY(-Math.toRadians(turretYaw));
            vec.add(loc.toVector());

            driverSeat = RealisticVehicles.setSeat((LivingEntity) world.spawnEntity(vec.toLocation(world), EntityType.ARMOR_STAND), VehicleType.TANK);

            this.type = type;

            traverseSpeed = settings.getTraverseSpeed();
            elevateSpeed = settings.getElevateSpeed();
            minPitch = settings.getMinPitch();
            maxPitch = settings.getMaxPitch();
            gunBarrelPos = settings.getGunBarrelEnd();
            machineGunPos = settings.getMachineGunPos();

            fuelTank = new FuelTank(settings);

            shells = settings.getShells();
            RealisticVehicles.debugLog(Arrays.toString(shells));
            this.inv = Bukkit.createInventory(null, InventoryType.PLAYER);
            inv.setItem(0, shells[0].item);
            if (shells[1] != null)
                inv.setItem(1, shells[1].item);
            if (shells[2] != null)
                inv.setItem(2, shells[2].item);

            ItemStack raiseItem = new ItemStack(Material.IRON_TRAPDOOR);
            ItemMeta rmeta = raiseItem.getItemMeta();
            assert rmeta != null;
            rmeta.setDisplayName(ChatColor.GREEN + "Open/Close Hatch");
            raiseItem.setItemMeta(rmeta);

            inv.setItem(7, raiseItem);

            ItemStack zoomItem = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            ItemMeta zmeta = raiseItem.getItemMeta();
            assert zmeta != null;
            zmeta.setDisplayName(ChatColor.GREEN + "Toggle Zoom");
            zoomItem.setItemMeta(zmeta);

            inv.setItem(8, zoomItem);

            RealisticVehicles.debugLog(settings.getDamageModel());
            damageModel = settings.getDamageModel().clone();

            setup(driverSeat,packet -> {
                if (packet.getPacket().getBooleans().read(1)) {
                    playerExitedVehicle(false);
                }
                firingMG = packet.getPacket().getBooleans().read(0);
            });

            for (Vector hvec : settings.getHitches()){
                hitches.add(new TrailerHitch(hvec));
            }

            start();
        } else {
            throw new InvalidTypeException(type);
        }
    }

    boolean firingMG = false;
    public void fireMachineGun(){
        Vector vec = machineGunPos.clone();
        vec.rotateAroundAxis(new Vector(1, 0, 0), Math.toRadians(turretPitch));
        vec.rotateAroundY(Math.toRadians(-turretYaw));
        vec.add(getLoc().toVector());
        Vector moveBy = new Vector(0, 0, 1).rotateAroundX(Math.toRadians(seatedPlayer.getLocation().getPitch())).rotateAroundY(Math.toRadians(-turretYaw));
        RayTraceResult hit = driverSeat.getWorld().rayTrace(vec.toLocation(driverSeat.getWorld()), moveBy, 50, FluidCollisionMode.NEVER, true, 1, null);

        driverSeat.getWorld().playSound(getLoc(), Sound.ENTITY_GENERIC_EXPLODE, 2, 2);

        Vector lastPos = vec.clone();
        for (int i = 0; i < 50; i++){
            lastPos.add(moveBy);
            driverSeat.getWorld().spawnParticle(Particle.REDSTONE, lastPos.toLocation(baseEntity.getWorld()), 0, 0, 0,0,0, new Particle.DustOptions(Color.YELLOW, 3),true);
        }
        if (hit != null) {
            if (hit.getHitEntity() != null) {
                if (!(hit.getHitEntity() instanceof ArmorStand) && hit.getHitEntity() instanceof LivingEntity){
                    ((LivingEntity) hit.getHitEntity()).damage(10, seatedPlayer);
                }
            }
        }
    }

    public void setActiveComps() {
        engineActive = damageModel.getComponentActivePercent(ComponentType.ENGINE) > 0.5;
    }

    boolean exited = false;
    Player invOwner = null;
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
    public boolean wasCreative = false;
    @Override
    public void playerExitedVehicle(boolean skipEject) {
        exited = true;
        if (!skipEject) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (wasCreative) {
                        seatedPlayer.setGameMode(GameMode.CREATIVE);
                    }
                }
            }.runTaskLater(RealisticVehicles.getInstance(), 1);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (driverSeat.getPassengers().size() != 0) {
                        driverSeat.eject();
                    }
                }
            }.runTaskLater(RealisticVehicles.getInstance(), 1);
        }
    }

    @Override
    public List<MissileSettings> getValidMissiles() {
        return null;
    }

    @Override
    public MissileHolder getMissileHolder() {
        return null;
    }

    int loops = 0;

    @Override
    public void OnFixedUpdate() {
        if (driverSeat.getPassengers().size() != 0) {
            seatedPlayer = (Player) driverSeat.getPassengers().get(0);
        } else {
            if (invOwner != null && playerInv != null){
                invOwner.setInvulnerable(false);
                invOwner.getInventory().clear();
                invOwner.getInventory().setContents(playerInv);
                invOwner = null;
                playerInv = null;
            }
            seatedPlayer = null;
        }

        setActiveComps();

        update();
        for (TrailerHitch hitch : hitches){
            hitch.update(getLoc(), getVehicleYaw(), (float) getSpeed());
        }

        //((CraftArmorStand) gunEnt).getHandle().setHeadPose(new Vector3f((float) Math.toRadians(turretPitch), 0, 0));
        ((ArmorStand) gunEnt).setHeadPose(new EulerAngle(Math.toRadians(turretPitch), 0, 0));

        //baseEntity.teleport(loc)
        ((CraftArmorStand) baseEntity).getHandle().b(getLoc().getX(), getLoc().getY(), getLoc().getZ(), (float) getYaw(), 0);
        //turretEnt.teleport(baseEntity.getLocation());
        ((CraftArmorStand) turretEnt).getHandle().b(getLoc().getX(), getLoc().getY(), getLoc().getZ(), turretYaw, 0);
        //gunEnt.teleport(baseEntity.getLocation());
        ((CraftArmorStand) gunEnt).getHandle().b(getLoc().getX(), getLoc().getY(), getLoc().getZ(), turretYaw, turretPitch);

        Vector mseatcoord;
        if (!raised) {
            mseatcoord = seatPos.clone();
        } else {
            mseatcoord = seatRaisedPos.clone();
        }
        if (seatedPlayer == null) {
            mseatcoord = seatRaisedPos.clone();
        } else if (damageModel.areAllCompsActive(ComponentType.TURRETRING)){
            float playerYaw = seatedPlayer.getLocation().getYaw();
            if (turretYaw > playerYaw) {
                if (turretYaw > 90 && playerYaw < -90) {
                    turretYaw += traverseSpeed;
                } else turretYaw -= traverseSpeed;
            } else if (turretYaw < playerYaw) {
                if (turretYaw < -90 && playerYaw > 90) {
                    turretYaw -= traverseSpeed;
                } else turretYaw += traverseSpeed;
            }
            if (turretYaw < playerYaw + traverseSpeed && turretYaw > playerYaw - traverseSpeed) {
                turretYaw = playerYaw;
            }

            float playerPitch = seatedPlayer.getLocation().getPitch();
            if (turretPitch < playerPitch) {
                turretPitch += elevateSpeed;
            } else if (turretPitch > playerPitch) {
                turretPitch -= elevateSpeed;
            }

            if (turretPitch > playerPitch - elevateSpeed && turretPitch < playerPitch + elevateSpeed) {
                turretPitch = playerPitch;
            }

            if (turretPitch > minPitch) {
                turretPitch = minPitch;
            } else if (turretPitch < maxPitch) {
                turretPitch = maxPitch;
            }
        }

        mseatcoord.rotateAroundY(-Math.toRadians(turretYaw));
        mseatcoord.add(getLoc().toVector());
        if (seatedPlayer != null) {
            mseatcoord.add(zoomCheck());
        }

        ((CraftArmorStand) driverSeat).getHandle().b(mseatcoord.getX(), mseatcoord.getY(), mseatcoord.getZ(), 0, 0);

        if (turretYaw > 180) {
            turretYaw = -180;
        } else if (turretYaw < -180) {
            turretYaw = 180;
        }

        damageModel.showDamageParticles(driverSeat.getWorld(), getYaw(), turretYaw, getLoc().toVector());

        List<Entity> ents = (List<Entity>) Objects.requireNonNull(getLoc().getWorld()).getNearbyEntities(getLoc(), 3, 3, 3);
        for (Entity ent : ents) {
            if (ent.getType() == EntityType.ARROW) {
                ent.setVelocity(ent.getVelocity().clone().multiply(-1));
            }
        }

        fuelTank.removeFuelWithDensity(fuelTank.getIdleFuelConsumptionRate());
        if (getSpeed() != 0){
            fuelTank.removeFuelWithDensity(fuelTank.getFuelConsumptionRate());
        }
        for (int i : damageModel.getComponentIndices(ComponentType.FUEL)){
            if (damageModel.getComponents().get(i).health < damageModel.getComponents().get(i).damageHealth){
                fuelTank.removeFuel(fuelTank.getFuelLeakAmount()/2);
            } else if (damageModel.getComponents().get(i).health < damageModel.getComponents().get(i).criticalHealth){
                fuelTank.removeFuel(fuelTank.getFuelLeakAmount());
            }
        }
        setHasFuel(!(fuelTank.getFuel() <= 0));

        if (loops % 5 == 0) {
            displayActionBar();
            if (firingMG){
                fireMachineGun();
            }
        }
        loops++;
    }

    boolean isZooming;
    public void toggleZoom(){
        isZooming = !isZooming;
    }

    public Vector zoomCheck(){
        if (isZooming){
            seatedPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5, 500, false, false));
            return new Vector(-seatPos.getX(), 0, 2).rotateAroundY(Math.toRadians(-turretYaw));
        } else {
            return new Vector(0, 0, 0);
        }
    }
    public void fireShell(int shellType) {
        if (!reloading && shellAmmo[shellType] > 0) {
            Vector vec = gunBarrelPos.clone();
            vec.rotateAroundX(Math.toRadians(turretPitch));
            vec.rotateAroundY(Math.toRadians(-turretYaw));
            vec.add(getLoc().toVector());
            Random rand = new Random();
            new BukkitRunnable() {
                int loops = 0;

                @Override
                public void run() {
                    for (int i = 0; i < 2; i++) {
                        driverSeat.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, vec.toLocation(baseEntity.getWorld()), 0, (Math.sin(-Math.toRadians(turretYaw)) + getRand(rand, 0.25)) * 0.1, (Math.sin(-Math.toRadians(turretPitch)) + getRand(rand, 0.25)) * 0.1, (Math.cos(Math.toRadians(turretYaw)) + getRand(rand, 0.25)) * 0.1, 5, null, true);
                        driverSeat.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, vec.toLocation(baseEntity.getWorld()), 0, (Math.sin(-Math.toRadians(turretYaw)) + getRand(rand, 0.1)) * 0.3, (Math.sin(-Math.toRadians(turretPitch)) + getRand(rand, 0.1)) * 0.3, (Math.cos(Math.toRadians(turretYaw)) + getRand(rand, 0.1)) * 0.3, 5, null, true);
                        driverSeat.getWorld().spawnParticle(Particle.FLAME, vec.toLocation(baseEntity.getWorld()), 0, (Math.sin(-Math.toRadians(turretYaw)) + getRand(rand, 0.1)) * 0.3, (Math.sin(-Math.toRadians(turretPitch)) + getRand(rand, 0.1)) * 0.3, (Math.cos(Math.toRadians(turretYaw)) + getRand(rand, 0.1)) * 0.3, 5, null, true);
                    }
                    if (loops >= 5) {
                        this.cancel();
                    }
                    if (damageModel.areAllCompsActive(ComponentType.GUNLOADER)) {
                        turretPitch -= elevateSpeed;
                    }
                    loops++;
                }
            }.runTaskTimer(RealisticVehicles.getInstance(), 0, 1);
            driverSeat.getWorld().playSound(getLoc(), Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1, 1);

            if (damageModel.areAllCompsActive(ComponentType.GUNBARREL) && damageModel.areAllCompsActive(ComponentType.GUNLOADER)) {
                new ProjectileShell(vec.toLocation(driverSeat.getWorld()), turretYaw, turretPitch, seatedPlayer, shells[shellType]);
            } else {
                driverSeat.getWorld().playSound(getLoc(), Sound.BLOCK_CHEST_CLOSE, SoundCategory.HOSTILE, 1, 0.5f);
            }

            startReload(shellType);
        }
    }

    public void startReload(int shellType) {
        if (reloading) return;
        if (shellAmmo[shellType] <= 0) {
            if (seatedPlayer != null) {
                seatedPlayer.sendMessage(ChatColor.RED + "NO AMMO FOR THIS SHELL LEFT.");
            }
            return;
        }
        reloading = true;
        reloadState = "|";
        new BukkitRunnable() {
            @Override
            public void run() {
                finishReload(shellType);
            }
        }.runTaskLaterAsynchronously(RealisticVehicles.getInstance(), (long) (shells[shellType].reloadTime * 20));
    }

    public void finishReload(int shellType) {
        reloading = false;
        shellAmmo[shellType] -= 1;
    }

    @Override
    public void displayActionBar() {
        //TODO finish this

        if (seatedPlayer != null) {
            if (reloading) {
                switch (reloadState) {
                    case "|" -> reloadState = "/";
                    case "/" -> reloadState = "-";
                    case "-" -> reloadState = "\\";
                    case "\\" -> reloadState = "|";
                }
            } else {
                reloadState = "X";
            }

            BaseComponent[] comps = new ComponentBuilder(ChatColor.GOLD + "[" + reloadState + "]")
                    .append(ChatColor.GREEN + " Ammo: " + (colorFromAmmo(0) + "" + Math.min(99,shellAmmo[0])) + " " + (colorFromAmmo(1) + "" + Math.min(99,shellAmmo[1])) + " " + (colorFromAmmo(2) + "" + Math.min(99,shellAmmo[2])))
                    .append(ChatColor.YELLOW+" Fuel: "+Math.round(fuelTank.getFuel()))
                    .create();
            seatedPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, comps);
        }
    }

    public ChatColor colorFromAmmo(int ammoType) {
        if (shellAmmo[ammoType] > 25) {
            return ChatColor.GREEN;
        } else if (shellAmmo[ammoType] > 0) {
            return ChatColor.YELLOW;
        }
        return ChatColor.RED;
    }

    @Override
    public void fizzleAmmo(int ammoIndex){
        new BukkitRunnable(){
            int loops = 0;
            final Random rand = new Random();
            @Override
            public void run() {
                if (loops >= 80){
                    explode();
                    cancel();
                }
                driverSeat.getWorld().spawnParticle(Particle.LAVA, getLoc().clone().add(damageModel.getComponents().get(ammoIndex).getRandomPoint(rand).toVector().rotateAroundY(Math.toRadians(turretYaw))), 1);
                driverSeat.getWorld().spawnParticle(Particle.FLAME, getLoc().clone().add(damageModel.getComponents().get(ammoIndex).getRandomPoint(rand).toVector().rotateAroundY(Math.toRadians(turretYaw))), 0, 0, 0.2, 0);
                driverSeat.getWorld().spawnParticle(Particle.FLAME, getLoc().clone().add(damageModel.getComponents().get(ammoIndex).getRandomPoint(rand).toVector().rotateAroundY(Math.toRadians(turretYaw))), 0, 0, 0.2, 0);
                driverSeat.getWorld().spawnParticle(Particle.FLAME, getLoc().clone().add(damageModel.getComponents().get(ammoIndex).getRandomPoint(rand).toVector().rotateAroundY(Math.toRadians(turretYaw))), 0, 0, 0.2, 0);
                driverSeat.getWorld().playSound(getLoc(), Sound.BLOCK_FIRE_EXTINGUISH, 5, 0.75f);
                loops++;
            }
        }.runTaskTimer(RealisticVehicles.getInstance(), 0, 1);
    }

    @Override
    public FuelTank getFuelTank() {
        return fuelTank;
    }

    @Override
    public VehicleType getType() {
        return VehicleType.TANK;
    }

    @Override
    public String getNameType() {
        return type;
    }

    @Override
    public int getTexId() {
        return getSettings().getTextureID();
    }

    @Override
    public Shell[] getShells() {
        return shells;
    }

    @Override
    public int[] getShellsAmmo() {
        return shellAmmo;
    }

    @Override
    public void addShells(int shellIndex, int amount) {
        shellAmmo[shellIndex] += amount;
    }

    @Override
    public void explode(){
        playerExitedVehicle(false);
        driverSeat.getWorld().createExplosion(getLoc(), 6, true, true);
        Random rand = new Random();
        for (int i = 0; i < 100; i++) {
            driverSeat.getWorld().spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, getLoc(), 0, getRand(rand, 1), Math.abs(getRand(rand, 0.25)), getRand(rand, 1), 0.25, null, true);
            driverSeat.getWorld().spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, getLoc(), 0, getRand(rand, 0.5), Math.abs(getRand(rand, 1.25)), getRand(rand, 0.5), 0.25, null, true);
            driverSeat.getWorld().spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, getLoc(), 0, getRand(rand, 0.5), Math.abs(getRand(rand, 1.25)), getRand(rand, 0.5), 0.25, null, true);
            driverSeat.getWorld().spawnParticle(Particle.FLAME, getLoc(), 0, getRand(rand, 0.25), Math.abs(getRand(rand, 0.25)), getRand(rand, 0.25), 1, null, true);
        }
        this.closeThis(false);
    }

    @Override
    public void closeThis(boolean clearStands) {
        FixedUpdate.super.closeThis(false);
        if (invOwner != null) {
            invOwner.setInvulnerable(false);
            invOwner.getInventory().clear();
            invOwner.getInventory().setContents(playerInv);
        }
        driverSeat.remove();
        if (clearStands) {
            baseEntity.remove();
            turretEnt.remove();
            gunEnt.remove();
        } else {
            Random rand = new Random();
            baseEntity.getPersistentDataContainer().set(SCRAPKEY, PersistentDataType.INTEGER, (int) Math.round(rand.nextDouble()*RealisticVehicles.getInstance().getConfig().getDouble("scrap-reward")));
            turretEnt.getPersistentDataContainer().set(SCRAPKEY, PersistentDataType.INTEGER, (int) Math.round(rand.nextDouble()*RealisticVehicles.getInstance().getConfig().getDouble("scrap-reward")));
            gunEnt.getPersistentDataContainer().set(SCRAPKEY, PersistentDataType.INTEGER, (int) Math.round(rand.nextDouble()*RealisticVehicles.getInstance().getConfig().getDouble("scrap-reward")));
        }
    }

    @Override
    public void scrap(boolean delete) {
        closeThis(delete);
    }

    @Override
    public List<TrailerHitch> getTrailerHitches() {
        return hitches;
    }

    @Override
    public void flashModel() {
        damageModel.flashAll(baseEntity.getWorld(), getLoc());
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
    public void OnClose() {
        FixedUpdate.super.OnClose();
    }

    @Override
    public double getTurretYaw() {
        return turretYaw;
    }

    @Override
    public Player getSeatedPlayer() {
        return seatedPlayer;
    }

    public static double getRand(Random rand, double mult){
        return (((rand.nextDouble()-0.5)*2)*mult);
    }

    public Entity getBaseEntity() {
        return baseEntity;
    }

    public Entity getTurretEnt() {
        return turretEnt;
    }

    public Entity getGunEnt() {
        return gunEnt;
    }

    public Entity getDriverSeat() {
        return driverSeat;
    }

    public float getTraverseSpeed() {
        return traverseSpeed;
    }

    public float getTurretPitch() {
        return turretPitch;
    }

    public float getElevateSpeed() {
        return elevateSpeed;
    }

    public float getMaxPitch() {
        return maxPitch;
    }

    public float getMinPitch() {
        return minPitch;
    }

    public Vector getSeatPos() {
        return seatPos;
    }

    public Vector getSeatRaisedPos() {
        return seatRaisedPos;
    }

    public Vector getGunBarrelPos() {
        return gunBarrelPos;
    }

    public Vector getMachineGunPos() {
        return machineGunPos;
    }

    public boolean isRaised() {
        return raised;
    }

    public ItemStack[] getPlayerInv() {
        return playerInv;
    }

    public int[] getShellAmmo() {
        return shellAmmo;
    }

    public boolean isReloading() {
        return reloading;
    }

    public Inventory getInv() {
        return inv;
    }

    public String getReloadState() {
        return reloadState;
    }

    public boolean isFiringMG() {
        return firingMG;
    }

    public boolean isExited() {
        return exited;
    }

    public Player getInvOwner() {
        return invOwner;
    }

    public boolean isWasCreative() {
        return wasCreative;
    }

    public int getLoops() {
        return loops;
    }

    public boolean isZooming() {
        return isZooming;
    }

    public void setTurretYaw(float turretYaw) {
        this.turretYaw = turretYaw;
    }

    public void setTurretPitch(float turretPitch) {
        this.turretPitch = turretPitch;
    }

    public void setRaised(boolean raised) {
        this.raised = raised;
    }

    public void setSeatedPlayer(Player seatedPlayer) {
        this.seatedPlayer = seatedPlayer;
    }

    public void setPlayerInv(ItemStack[] playerInv) {
        this.playerInv = playerInv;
    }

    public void setShellAmmo(int[] shellAmmo) {
        this.shellAmmo = shellAmmo;
    }

    public void setReloading(boolean reloading) {
        this.reloading = reloading;
    }

    public void setReloadState(String reloadState) {
        this.reloadState = reloadState;
    }

    public void setFiringMG(boolean firingMG) {
        this.firingMG = firingMG;
    }

    public void setExited(boolean exited) {
        this.exited = exited;
    }

    public void setInvOwner(Player invOwner) {
        this.invOwner = invOwner;
    }

    public void setWasCreative(boolean wasCreative) {
        this.wasCreative = wasCreative;
    }

    public void setLoops(int loops) {
        this.loops = loops;
    }

    public void setZooming(boolean zooming) {
        isZooming = zooming;
    }
}
