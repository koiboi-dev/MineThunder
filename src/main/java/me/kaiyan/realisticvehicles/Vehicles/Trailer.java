package me.kaiyan.realisticvehicles.Vehicles;

import me.kaiyan.realisticvehicles.Counters.Updates;
import me.kaiyan.realisticvehicles.DataTypes.Enums.TrailerTypes;
import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.DataTypes.Interfaces.FixedUpdate;
import me.kaiyan.realisticvehicles.DataTypes.Interfaces.Sleepable;
import me.kaiyan.realisticvehicles.DataTypes.TrailerHitch;
import me.kaiyan.realisticvehicles.DataTypes.Interfaces.VehicleInterface;
import me.kaiyan.realisticvehicles.Models.Harvester.BlockHarvester;
import me.kaiyan.realisticvehicles.Models.Model;
import me.kaiyan.realisticvehicles.RealisticVehicles;
import me.kaiyan.realisticvehicles.Vehicles.Settings.TrailerSettings;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.Vector;

import java.util.Map;

public class Trailer implements FixedUpdate, Sleepable {
    private final Location loc;
    private TrailerHitch hitched;
    private TrailerHitch hitch;
    private final TrailerSettings settings;
    private float yaw = 0;

    private final Model model;

    private Player hitcher;
    private boolean hitchingMode;

    private Inventory[] inventory;

    private final BlockHarvester harvester;

    public Trailer(Location loc, TrailerSettings settings) {
        this.loc = loc;
        model = new Model(null, new Vector(), RealisticVehicles.setTexture((ItemDisplay) loc.getWorld().spawnEntity(loc, EntityType.ITEM_DISPLAY), settings.getDisplayID(), settings.getDisplayID()), true, new Vector(1, 1, 1));
        this.settings = settings;
        int invs = (int) Math.ceil(settings.getMaxItems()/54f);
        inventory = new Inventory[invs];
        for (int i = 0; i < invs; i++) {
            inventory[i] = Bukkit.createInventory(null, 54, "Trailer Inventory");
        }
        if (settings.getTrailerHitchLoc() != null){
            hitch = new TrailerHitch(settings.getTrailerHitchLoc(), TrailerTypes.TRUCK);
        }
        if (settings.getHarvester() != null) {
            harvester = settings.getHarvester().clone();
        } else {
            harvester = null;
        }
        start();
    }

    int loops = 0;
    @Override
    public void OnFixedUpdate() {
        if (hitched != null) {
            //yaw += (getYawDifference(yaw, hitched.getpYaw()) * 0.25 * Math.min(hitched.getParentSpeed() / 5, 1));
            //Move to be in range of hitch point.
            Vector dir = loc.clone().subtract(hitched.getLoc().clone().subtract(0, 1, 0)).toVector().normalize();
            double dist = loc.distance(hitched.getLoc());
            double distper = 1-((dist)/settings.getLength());
            loc.add(dir.clone().multiply(dist*distper));
            loc.setDirection(dir.multiply(-1));
            yaw = loc.getYaw();
        } else {
            sleepTicks++;
            if (sleepTicks >= RealisticVehicles.VEHICLESLEEPTIME){
                sleep();
            }
        }

        if (harvester != null){
            harvester.update(loc, yaw, getHitch().isActivated());
        }

        if (hitchingMode && loops%5 == 0){
            attemptHitch(hitcher);
        } else if (hitchingMode){
            if (loops > 600){
                hitchingMode = false;
                loops = 0;
            }
        }
        loops++;

        model.updatePositions(loc, 0,yaw,0);
    }

    public void hitchInteract(Player player){
        if (hitched == null){
            player.sendMessage(ChatColor.GREEN+"Hitching mode engaged for 30 seconds!");
            hitchingMode = true;
            hitcher = player;
        } else {
            player.sendMessage(ChatColor.RED+"Detached Trailer!");
            hitched = null;
        }
    }

    private void attemptHitch(Player player){
        for (FixedUpdate update : Updates.fixedUpdates){
            if (update instanceof VehicleInterface inter){
                if (inter.getTrailerHitches().size() == 0) continue;
                for (TrailerHitch hitch : inter.getTrailerHitches()){
                    hitch.getLoc().getWorld().spawnParticle(Particle.REDSTONE, hitch.getLoc(), 5, 0, 0, 0,10, new Particle.DustOptions(Color.YELLOW, 5));
                    if (loc.clone().add(new Vector(0, 0, settings.getLength()).rotateAroundY(Math.toRadians(yaw))).distanceSquared(hitch.getLoc()) < 16 && settings.getType() == hitch.getType()){
                        hitched = hitch;
                        player.sendMessage(ChatColor.GREEN+"HITCHED TRAILER!");
                        hitchingMode = false;
                        return;
                    }
                }
            } else if (update instanceof Trailer trailer){
                if (trailer.getHitch() != null){
                    hitch.getLoc().getWorld().spawnParticle(Particle.REDSTONE, hitch.getLoc(), 5, 0, 0, 0,10, new Particle.DustOptions(Color.YELLOW, 5));
                    if (loc.clone().add(new Vector(0, 0, settings.getLength()).rotateAroundY(Math.toRadians(yaw))).distanceSquared(hitch.getLoc()) < 16 && settings.getType() == hitch.getType()){
                        hitched = hitch;
                        player.sendMessage(ChatColor.GREEN+"HITCHED TRAILER!");
                        hitchingMode = false;
                        return;
                    }
                }
            }
        }
    }

    public TrailerHitch getHitch() {
        return hitch;
    }

    public Location getLoc() {
        return loc;
    }

    public TrailerHitch getHitched() {
        return hitched;
    }

    public float getYaw() {
        return yaw;
    }

    public Model getModel() {
        return model;
    }

    public boolean isHitchingMode() {
        return hitchingMode;
    }

    public Inventory[] getInventory() {
        return inventory;
    }
    public void setInventory(Inventory[] inventories) {
        this.inventory = inventories;
    }

    public TrailerSettings getSettings() {
        return settings;
    }
    int sleepTicks;
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

    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }
}
