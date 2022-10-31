package me.kaiyan.realisticvehicles.Menus;

import me.kaiyan.realisticvehicles.Vehicles.Trailer;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;
import org.ipvp.canvas.paginate.PaginatedMenuBuilder;
import org.ipvp.canvas.slot.ClickOptions;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.slot.SlotSettings;
import org.ipvp.canvas.type.ChestMenu;

import java.util.Arrays;
import java.util.List;

public class TrailerMenu{
    public static void openTrailerInventory(Player player, Trailer trailer){
        Menu menu = ChestMenu.builder(3).title("Trailer Inventory").build();
        Mask mask = BinaryMask.builder(menu)
                .item(new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
                .pattern("111111111")
                .pattern("100000001")
                .pattern("111111111")
                .build();
        mask.apply(menu);

        int invs = (int) Math.ceil(trailer.getSettings().getMaxItems()/54f);
        for (int i = 0; i < invs; i++) {
            Slot slot = menu.getSlot(i+10);
            ItemStack menuItem = new ItemStack(Material.CHEST);
            ItemMeta meta = menuItem.getItemMeta();
            meta.setDisplayName("Open Section "+i);
            meta.setLore(List.of(ChatColor.GRAY+"This is a inventory in the trailer"));
            menuItem.setItemMeta(meta);
            slot.setItem(menuItem);
            int finalI = i;
            slot.setClickHandler((player1, info) -> player1.openInventory(trailer.getInventory()[finalI]));
        }
        menu.open(player);
    }
}
