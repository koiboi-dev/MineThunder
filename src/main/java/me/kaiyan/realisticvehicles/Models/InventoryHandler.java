package me.kaiyan.realisticvehicles.Models;

import me.kaiyan.realisticvehicles.RealisticVehicles;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class InventoryHandler {
    public static File invFile;
    public static FileConfiguration invConfig;

    public static void loadHandler(){
        invFile = new File(RealisticVehicles.getInstance().getDataFolder()+"/inventories.yml");
        if (!invFile.exists()){
            try {
                boolean none = invFile.createNewFile();
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
