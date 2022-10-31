package me.kaiyan.realisticvehicles.Vehicles;

import me.kaiyan.realisticvehicles.Counters.Updates;
import me.kaiyan.realisticvehicles.DataTypes.Enums.TrailerTypes;
import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.DataTypes.Interfaces.FixedUpdate;
import me.kaiyan.realisticvehicles.DataTypes.Interfaces.Sleepable;
import me.kaiyan.realisticvehicles.DataTypes.TrailerHitch;
import me.kaiyan.realisticvehicles.DataTypes.Interfaces.VehicleInterface;
import me.kaiyan.realisticvehicles.ModelHandlers.Harvester.BlockHarvester;
import me.kaiyan.realisticvehicles.ModelHandlers.Model;
import me.kaiyan.realisticvehicles.RealisticVehicles;
import me.kaiyan.realisticvehicles.Vehicles.Settings.TrailerSettings;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public class Trailer implements FixedUpdate, Sleepable {
    private Location loc;
    private TrailerHitch hitched;
    private TrailerHitch hitch;
    private final TrailerSettings settings;
    float yaw = 0;

    private final Model model;

    private Player hitcher;
    private boolean hitchingMode;

    private final Inventory[] inventory;

    private final BlockHarvester harvester;

    public Trailer(Location loc, TrailerSettings settings) {
        this.loc = loc;
        model = new Model(null, new Vector(), 0, true);
        for (Map.Entry<int[], Integer> entry : settings.getModels().entrySet()){
            model.addCorner(entry.getKey(), (ArmorStand) RealisticVehicles.setTexture((LivingEntity) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND), entry.getValue(), entry.getValue()));
        }
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

    public TrailerSettings getSettings() {
        return settings;
    }

    public boolean hasArmourStand(ArmorStand stand) {
        return model.containsStand(stand);
    }


    //Credit to matanos https://www.spigotmc.org/members/matanos.6931/
    public static String inventoryToBase64(Inventory inventory) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(inventory.getSize());

            for (int i = 0; i < inventory.getSize(); i++) {
                dataOutput.writeObject(inventory.getItem(i));
            }

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());

            //Converts the inventory and its contents to base64, This also saves item meta-data and inventory type
        } catch (Exception e) {
            throw new IllegalStateException("Could not convert inventory to base64.", e);
        }
    }

    //Credit to matanos https://www.spigotmc.org/members/matanos.6931/
    public static Inventory inventoryFromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            Inventory inventory = Bukkit.createInventory(null, dataInput.readInt());

            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, (ItemStack) dataInput.readObject());
            }

            dataInput.close();
            return inventory;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new IOException("Could not decode inventory.", e);
        }
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
        StringBuilder data = new StringBuilder();
        for (Inventory inv : inventory){
            data.append(";").append(inventoryToBase64(inv));
        }
        model.sleepStands(VehicleType.TRAILER,getSettings().getName(), yaw, data.toString());
    }
}
