package me.kaiyan.realisticvehicles.Menus;

import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.RealisticVehicles;
import me.kaiyan.realisticvehicles.VehicleManagers.ItemGenerator;
import me.kaiyan.realisticvehicles.Vehicles.Settings.GroundVehicles.TankSettings;
import me.kaiyan.realisticvehicles.Vehicles.Settings.TrailerSettings;
import me.kaiyan.realisticvehicles.Vehicles.Settings.VehicleSettings;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.type.ChestMenu;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurchaseMenu {
    public static HashMap<String, List<VehicleSettings>> extraVehicles = new HashMap<>();
    public static void openPurchaseMenu(Player player){
        Menu menu = ChestMenu.builder(3).title("Vehicle Shop").build();
        Mask mask = BinaryMask.builder(menu)
                .item(new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
                .pattern("111111111")
                .pattern("100000001")
                .pattern("111111111")
                .build();
        mask.apply(menu);
        /*generateFolderSlot(menu, 11, "Cars", 800, List.of());
        generateFolderSlot(menu, 12, "Planes", 601, List.of(
                VehicleSettings.getSettingsFromType("MIG 31"),
                VehicleSettings.getSettingsFromType("F-15")
        ));
        generateFolderSlot(menu, 13, "Trucks", 800, List.of(
                VehicleSettings.getSettingsFromType("Bessie Trucking S3-X Truck")
        ));
        generateFolderSlotTrailers(menu, 14, "Trailers", 0, List.of(
                TrailerSettings.getTrailerSettings("Bessie Trucking T5 Dry Trailer")
        ));
        generateFolderSlot(menu, 15, "Tractors", 801, List.of(
                VehicleSettings.getSettingsFromType("Jerry Deer's Model 2 Tractor")
        ));
        generateFolderSlot(menu, 16, "Tanks", 503, List.of(
                VehicleSettings.getSettingsFromType("Challenger II")
                //VehicleSettings.getSettingsFromType("Leopard 2A7")
        ));*/
        int loops = 0;
        for (Map.Entry<String, List<VehicleSettings>> map : extraVehicles.entrySet()){
            generateFolderSlot(menu, 10+loops, map.getKey(), map.getValue().get(0).getTextureID(), map.getValue());
            loops++;
        }
        menu.open(player);

    }

    public static void generateFolderSlot(Menu bMenu, int id, String name, int vId, List<VehicleSettings> vehicles){
        Slot slot = bMenu.getSlot(id);
        slot.setItem(getVehicleItem(name, vId));
        slot.setClickHandler((p, info) -> {
            Menu menu = ChestMenu.builder(3).title(name).build();
            int loops = 0;
            for (VehicleSettings setting : vehicles){
                Slot sSlot = menu.getSlot(loops);
                if (setting instanceof TankSettings) {
                    sSlot.setItem(getVehicleItem(setting.getType(), setting.getTextureID()+3));
                } else {
                    sSlot.setItem(getVehicleItem(setting.getType(), setting.getTextureID()));
                }
                sSlot.setClickHandler((pl, cinfo) -> pl.getWorld().dropItem(pl.getLocation(), ItemGenerator.generateNewVehicleItem(setting.getType(), setting.getVtype())));
                loops++;
            }
            menu.open(p);
        });
    }
    public static void generateFolderSlotTrailers(Menu bMenu, int id, String name, int vId, List<TrailerSettings> vehicles){
        Slot slot = bMenu.getSlot(id);
        slot.setItem(getVehicleItem(name, vId));
        slot.setClickHandler((p, info) -> {
            Menu menu = ChestMenu.builder(3).title(name).build();
            int loops = 0;
            for (TrailerSettings setting : vehicles){
                Slot sSlot = menu.getSlot(loops);
                ItemStack item = getVehicleItem(setting.getName(), setting.getDisplayID());
                item.setType(Material.CHEST);
                sSlot.setItem(item);
                sSlot.setClickHandler((pl, cinfo) -> pl.getWorld().dropItem(pl.getLocation(), ItemGenerator.generateNewVehicleItem(setting.getName(), VehicleType.TRAILER)));
                loops++;
            }
            menu.open(p);
        });
    }

    public static void addVehicleToShopList(@Nonnull VehicleSettings settings){
        if (PurchaseMenu.extraVehicles.containsKey(settings.getShopGroup())){
            PurchaseMenu.extraVehicles.get(settings.getShopGroup()).add(settings);
        } else {
            List<VehicleSettings> list = new ArrayList<>();
            list.add(settings);
            PurchaseMenu.extraVehicles.put(settings.getShopGroup(), list);
        }
        RealisticVehicles.debugLog("Added new vehicle: "+settings.getType()+" : "+settings.getShopGroup());
        RealisticVehicles.debugLog(extraVehicles);
    }

    public static ItemStack getVehicleItem(String name, int id){
        ItemStack item = new ItemStack(Material.WOODEN_HOE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD +name);
        meta.setCustomModelData(id);
        item.setItemMeta(meta);
        return item;
    }
}
