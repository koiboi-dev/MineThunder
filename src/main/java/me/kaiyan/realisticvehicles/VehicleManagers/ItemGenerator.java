package me.kaiyan.realisticvehicles.VehicleManagers;

import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.DataTypes.Exceptions.InvalidTypeException;
import me.kaiyan.realisticvehicles.DataTypes.Interfaces.VehicleInterface;
import me.kaiyan.realisticvehicles.RealisticVehicles;
import me.kaiyan.realisticvehicles.Vehicles.Aircraft;
import me.kaiyan.realisticvehicles.Vehicles.Car;
import me.kaiyan.realisticvehicles.Vehicles.Settings.GroundVehicles.CarSettings;
import me.kaiyan.realisticvehicles.Vehicles.Settings.TrailerSettings;
import me.kaiyan.realisticvehicles.Vehicles.Settings.VehicleSettings;
import me.kaiyan.realisticvehicles.Vehicles.Tank;
import me.kaiyan.realisticvehicles.Vehicles.Trailer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class ItemGenerator {
    public static final NamespacedKey JSONSAVE = new NamespacedKey(RealisticVehicles.getInstance(), "vehicleJson");

    public static ItemStack generateNewVehicleItem(String type, VehicleType vtype){
        if (vtype == VehicleType.TRAILER){
            TrailerSettings settings = TrailerSettings.getTrailerSettings(type);
            ItemStack item = new ItemStack(Material.WOODEN_HOE);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(type);
            meta.setCustomModelData(settings.getDisplayID());
            meta.setLore(Collections.singletonList(ChatColor.translateAlternateColorCodes('&', "&ePlace this down on a flat surface to spawn a vehicle.")));
            meta.getPersistentDataContainer().set(JSONSAVE, PersistentDataType.STRING, "NEW:" + vtype + ":" + type);
            item.setItemMeta(meta);
            return item;
        } else {
            VehicleSettings settings = VehicleSettings.getSettingsFromType(type);
            ItemStack item = new ItemStack(Material.WOODEN_HOE);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(type);
            meta.setCustomModelData(settings.getTextureID());
            meta.setLore(Collections.singletonList(ChatColor.translateAlternateColorCodes('&', "&ePlace this down on a flat surface to spawn a vehicle.")));
            meta.getPersistentDataContainer().set(JSONSAVE, PersistentDataType.STRING, "NEW:" + vtype + ":" + type);
            item.setItemMeta(meta);
            return item;
        }
    }

    public static ItemStack getItemFromVehicle(VehicleInterface inter){
        ItemStack item = new ItemStack(Material.WOODEN_HOE);
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(inter.getTexId());
        VehicleSaver saver = new VehicleSaver(inter);
        meta.setDisplayName(inter.getNameType());
        meta.setLore(getItemLore(saver));
        meta.getPersistentDataContainer().set(JSONSAVE, PersistentDataType.STRING, saver.toJson());
        item.setItemMeta(meta);
        return item;
    }

    public static List<String> getItemLore(VehicleSaver info){
        List<String> out = new ArrayList<>();
        // Gold: &6
        // Yellow: &e
        // Green: &a
        out.add(getFormattedString("&6Name:&a %s", info.getType()));
        out.add(getFormattedString("&6Type:&a %s", info.getVType().toString().toLowerCase()));
        out.add(getFormattedString("&6Fuel:&a %.2f", info.getSavedFuel()));
        out.add(getFormattedString("&6Shells:&a [%d, %d, %d]", info.getSavedShells()[0], info.getSavedShells()[1], info.getSavedShells()[2]));
        out.add(getFormattedString("&ePlace to see armour damage and equipped missiles."));
        return out;
    }

    public static String getFormattedString(String inp, Object ...args){
        return ChatColor.translateAlternateColorCodes('&', String.format(inp, args));
    }

    /**
     * Creates an item from vehicle, checks if it's valid.
     * @param item item to check
     * @param loc location to place craft
     */
    public static void spawnVehicleFromItem(ItemStack item, Location loc){
        PersistentDataContainer cont = item.getItemMeta().getPersistentDataContainer();
        if (!cont.has(JSONSAVE, PersistentDataType.STRING)){
            return;
        }
        String json = cont.get(JSONSAVE, PersistentDataType.STRING);
        if (json.contains("NEW")){
            String[] split = json.split(":");
            VehicleType type = VehicleType.valueOf(split[1]);
            if (type == VehicleType.AIR){
                try {
                    new Aircraft(loc, split[2]);
                } catch (InvalidTypeException e) {
                    e.printStackTrace();
                }
            } else if (type == VehicleType.TANK){
                try {
                    new Tank(loc, split[2]);
                } catch (InvalidTypeException e) {
                    e.printStackTrace();
                }
            }else if (type == VehicleType.CAR){
                new Car(loc, CarSettings.getCarSettings(split[2]));
            } else if (type == VehicleType.TRAILER){
                new Trailer(loc, TrailerSettings.getTrailerSettings(split[2]));
            }
        } else {
            VehicleSaver saver = VehicleSaver.fromJson(cont.get(JSONSAVE, PersistentDataType.STRING));
            saver.createCraft(loc);
        }
    }

    public static ItemStack getCrowbar(){
        ItemStack crowbar = new ItemStack(Material.WOODEN_HOE);
        ItemMeta meta = crowbar.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD+""+ChatColor.ITALIC+"Crowbar");
        meta.setLore(List.of(ChatColor.GRAY+"Crowbar used too open trailers", ChatColor.GRAY+"Right click when near to a trailer to enter hitching mode.", ChatColor.GRAY+"Shift Right Click to open the inventory of the trailer"));
        meta.setCustomModelData(1000);
        crowbar.setItemMeta(meta);
        return crowbar;
    }
}
