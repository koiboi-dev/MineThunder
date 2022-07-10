package me.kaiyan.realisticvehicles.Vehicles;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import me.kaiyan.realisticvehicles.Counters.FixedUpdate;
import me.kaiyan.realisticvehicles.Counters.Updates;
import me.kaiyan.realisticvehicles.DataTypes.Enums.ComponentType;
import me.kaiyan.realisticvehicles.DataTypes.FuelTank;
import me.kaiyan.realisticvehicles.DataTypes.VehicleInterface;
import me.kaiyan.realisticvehicles.DamageModel.DamageModel;
import me.kaiyan.realisticvehicles.DamageModel.Projectiles.Shell;
import me.kaiyan.realisticvehicles.DataTypes.Exceptions.InvalidTypeException;
import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.Physics.GroundVehicle;
import me.kaiyan.realisticvehicles.Physics.ProjectileShell;
import me.kaiyan.realisticvehicles.RealisticVehicles;
import me.kaiyan.realisticvehicles.Vehicles.Settings.GroundVehicles.TankSettings;
import net.bytebuddy.implementation.bind.annotation.DefaultCall;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.minecraft.network.protocol.game.PacketPlayOutEntity;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftArmorStand;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.checkerframework.checker.units.qual.A;

import java.util.*;
import java.util.List;

public class Tank extends GroundVehicle implements FixedUpdate, VehicleInterface {
    public static List<Tank> tanks = new ArrayList<>();

    public DamageModel damageModel;
    public Entity baseEntity;
    public Entity turretEnt;
    public Entity gunEnt;
    public Entity driverSeat;

    public String type;
    public String id;
    public float turretYaw = 0;
    public float traverseSpeed;
    public float turretPitch = 0;
    public float elevateSpeed;
    public float maxPitch;
    public float minPitch;

    final Vector seatPos;
    final Vector seatRaisedPos;
    final Vector gunBarrelPos;
    final Vector machineGunPos;
    public boolean raised = true;

    Player seatedPlayer = null;
    ItemStack[] playerInv = null;
    public final Shell[] shells;
    public int[] shellAmmo = new int[]{5, 5, 5};

    public boolean reloading;
    private final FuelTank fuelTank;

    final Inventory inv;
    String reloadState = "|";

    public Tank(Location loc, String type) throws InvalidTypeException {
        super(loc, TankSettings.getTankSettings(type));

        TankSettings settings = TankSettings.getTankSettings(type);
        if (settings != null) {
            World world = loc.getWorld();
            assert world != null;
            loc = loc.clone();

            id = UUID.randomUUID().toString();

            loc.setPitch(0);
            loc.setYaw(0);
            this.seatPos = settings.getSeatPos();
            this.seatRaisedPos = settings.getSeatRaisedPos();

            baseEntity = RealisticVehicles.setTexture((LivingEntity) world.spawnEntity(loc, EntityType.ARMOR_STAND), settings.getTextureID());
            baseEntity.setCustomName(id + "_BASE");
            turretEnt = RealisticVehicles.setTexture((LivingEntity) world.spawnEntity(loc, EntityType.ARMOR_STAND), settings.getTextureID() + 1);
            turretEnt.setCustomName(id + "_TURRET");
            gunEnt = RealisticVehicles.setTexture((LivingEntity) world.spawnEntity(loc, EntityType.ARMOR_STAND), settings.getTextureID() + 2);
            gunEnt.setCustomName(id + "_GUN");

            Vector vec = seatRaisedPos.clone();
            vec.rotateAroundY(-Math.toRadians(turretYaw));
            vec.add(loc.toVector());

            driverSeat = RealisticVehicles.setSeat((LivingEntity) world.spawnEntity(vec.toLocation(world), EntityType.ARMOR_STAND), VehicleType.GROUND);
            driverSeat.setCustomName(id + "_SEAT");

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

            setup(baseEntity,driverSeat,packet -> {
                if (packet.getPacket().getBooleans().read(1)) {
                    playerExitedVehicle(false);
                }
                firingMG = packet.getPacket().getBooleans().read(0);
            });
            tanks.add(this);
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
        Vector moveBy = new Vector(0, 0, 1).rotateAroundY(Math.toRadians(-turretYaw)).rotateAroundZ(Math.toRadians(-turretPitch));
        RayTraceResult hit = driverSeat.getWorld().rayTrace(vec.toLocation(driverSeat.getWorld()), moveBy, 50, FluidCollisionMode.NEVER, true, 1, null);

        driverSeat.getWorld().playSound(getLoc(), Sound.ENTITY_GENERIC_EXPLODE, 10, 2);

        Vector lastPos = vec.clone();
        for (int i = 0; i < 50; i++){
            lastPos.add(moveBy);
            driverSeat.getWorld().spawnParticle(Particle.REDSTONE, lastPos.toLocation(baseEntity.getWorld()), 0, 0, 0,0,0, new Particle.DustOptions(Color.YELLOW, 3),true);
        }
        if (hit != null) {
            if (hit.getHitEntity() != null) {
                if (!(hit.getHitEntity() instanceof ArmorStand) && hit.getHitEntity() instanceof LivingEntity){
                    ((LivingEntity) hit.getHitEntity()).damage(5, seatedPlayer);
                }
            }
        }
    }

    public void setActiveComps() {
        engineActive = damageModel.getComponentActivePercent(ComponentType.ENGINE) > 0.5;
    }

    boolean exited = false;

    @Override
    public void playerEnteredVehicle(Player p) {
        RealisticVehicles.debugLog("Enter");
        playerInv = p.getInventory().getContents();
        p.getInventory().clear();
        p.getInventory().setContents(inv.getContents());
        exited = false;
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
                        if (driverSeat.getPassengers().get(0) != null) {
                            driverSeat.eject();
                        }
                    }
                }.runTaskLater(RealisticVehicles.getInstance(), 1);
            }
            playerInv = null;
        }
    }

    int loops = 0;

    @Override
    public void OnFixedUpdate() {
        if (driverSeat.getPassengers().size() != 0) {
            seatedPlayer = (Player) driverSeat.getPassengers().get(0);
        } else {
            seatedPlayer = null;
        }

        setActiveComps();

        update();

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

        if (fuelTank.getFuel() <= 0){
            fuelTank.setFuel(0);
            setHasFuel(false);
        } else {
            setHasFuel(true);
        }

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
        if (!reloading) {
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
            driverSeat.getWorld().playSound(getLoc(), Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 10, 1);

            if (damageModel.areAllCompsActive(ComponentType.GUNBARREL) && damageModel.areAllCompsActive(ComponentType.GUNLOADER)) {
                new ProjectileShell(vec.toLocation(driverSeat.getWorld()), turretYaw, turretPitch, seatedPlayer, shells[shellType]);
            } else {
                driverSeat.getWorld().playSound(getLoc(), Sound.BLOCK_CHEST_CLOSE, SoundCategory.HOSTILE, 10, 0.5f);
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
                driverSeat.getWorld().playSound(getLoc(), Sound.BLOCK_FIRE_EXTINGUISH, 20, 0.75f);
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
        return VehicleType.GROUND;
    }

    @Override
    public String getNameType() {
        return type;
    }

    @Override
    public String getID() {
        return id;
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
        driverSeat.remove();
        baseEntity.remove();
        gunEnt.remove();
        turretEnt.remove();
        tanks.remove(this);
        this.closeThis();
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
}
