package me.kaiyan.realisticvehicles.Models;

import me.kaiyan.realisticvehicles.RealisticVehicles;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationOptions;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class InventoryHandler {
    public static File invFile;
    public static FileConfiguration invConfig;

    public static void loadHandler(){
        invFile = new File(RealisticVehicles.getInstance().getDataFolder()+"/inventories.yml");
        if (!invFile.exists()){
            try {
                invFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        invConfig = YamlConfiguration.loadConfiguration(invFile);
    }

    /*public static String saveInventories(Inventory[] invs){
        StringBuilder out = new StringBuilder();
        for (Inventory inv : invs){
            out.append("--").append(inventoryToBase64(inv));
        }
        String key = UUID.randomUUID().toString();
        invConfig.set(key, out);
        return key;
    }*/
    /*public static Inventory[] loadInventory(String key){
        String invs = invConfig.getString(key);
        String[] invAr = invs.split("--");
        Inventory[] finv = new Inventory[invAr.length];
        int loops = 0;
        for (String str : invAr){
            try {
                finv[loops] = inventoryFromBase64(str);
            } catch (IOException e) {
                e.printStackTrace();
            }
            loops++;
        }
        return finv;
    }*/



}
