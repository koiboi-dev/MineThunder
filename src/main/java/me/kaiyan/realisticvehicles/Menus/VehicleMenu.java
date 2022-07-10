package me.kaiyan.realisticvehicles.Menus;

import me.kaiyan.realisticvehicles.DamageModel.Hitboxes.ArmourPlate;
import me.kaiyan.realisticvehicles.DamageModel.Hitboxes.Component;
import me.kaiyan.realisticvehicles.DamageModel.Projectiles.Shell;
import me.kaiyan.realisticvehicles.DataTypes.FuelType;
import me.kaiyan.realisticvehicles.DataTypes.VehicleInterface;
import me.kaiyan.realisticvehicles.RealisticVehicles;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;
import org.ipvp.canvas.slot.ClickOptions;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.type.ChestMenu;

import java.util.Arrays;

public class VehicleMenu {
    public static void showMenu(VehicleInterface vehicle, Player player){
        Menu menu = ChestMenu.builder(1).title(vehicle.getNameType()).redraw(false).build();
        Slot fuelSlot = menu.getSlot(1);
        ItemStack fuelItem = new ItemStack(getMatFromFuel(vehicle.getFuelTank().getFuel(), vehicle.getFuelTank().getMaxFuel()));
        ItemMeta meta = fuelItem.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.GOLD+"Fuel Amount");
        meta.setLore(Arrays.asList(ChatColor.GREEN+"The amount of fuel left in the vehicle",
                ChatColor.GREEN+"Percent: "+(vehicle.getFuelTank().getFuel()/vehicle.getFuelTank().getMaxFuel())*100+"%",
                ChatColor.GREEN+"Fuel: "+vehicle.getFuelTank().getFuel(),
                ChatColor.GREEN+"Max Fuel: "+vehicle.getFuelTank().getMaxFuel(),
                ChatColor.GREEN+"Fuel Type: "+vehicle.getFuelTank().getLoadedFuelType().getFuelName(),
                ChatColor.GREEN+"Click to refuel "+vehicle.getFuelTank().getMaxFuel()/10)
        );
        fuelItem.setItemMeta(meta);
        fuelSlot.setItem(fuelItem);
        fuelSlot.setClickHandler(((gplayer, clickInformation) -> displayFuelMenu(player, vehicle)));

        for (int i = 0; i < 3; i++) {
            Slot ammoSlot = menu.getSlot(i+3);
            Shell shell = vehicle.getShells()[i];
            if (shell != null) {
                ItemStack item = new ItemStack(shell.item);
                ItemMeta imeta = item.getItemMeta();
                assert imeta != null;
                imeta.setDisplayName(shell.getAbbreviation());
                imeta.setLore(Arrays.asList("Current Amount: "+vehicle.getShellsAmmo()[i], "Buys 1 shell at "+shell.cost));
                item.setItemMeta(imeta);
                ammoSlot.setItem(item);
                int finalI = i;
                ammoSlot.setClickHandler((player1, clickInformation) -> {
                    if (RealisticVehicles.getEconomy().getBalance(player) >= shell.cost){
                        RealisticVehicles.getEconomy().withdrawPlayer(player, shell.cost);
                        vehicle.addShells(finalI, 1);
                        player.sendMessage(ChatColor.GREEN+"Restocked a shell, Shell count: " +vehicle.getShellsAmmo()[finalI]);
                        menu.close(player);
                        showMenu(vehicle,player);
                    } else {
                        player.sendMessage(ChatColor.RED+"Not enough money to restock.");
                    }
                });
            }
        }

        Slot repairSlot = menu.getSlot(7);
        ItemStack repairItem = new ItemStack(Material.BOOK);
        ItemMeta rmeta = repairItem.getItemMeta();
        assert rmeta != null;
        rmeta.setDisplayName(ChatColor.GREEN+"Repair Tank");
        double armourRepairCost = 0;
        for (ArmourPlate armour : vehicle.getDamageModel().getArmour()){
            armourRepairCost += Math.round(armour.weakness*RealisticVehicles.getInstance().getConfig().getDouble("armour-repair-cost"));
        }
        double componentRepairCost = 0;
        for (Component comp : vehicle.getDamageModel().getComponents()){
            componentRepairCost += Math.round(comp.health*RealisticVehicles.getInstance().getConfig().getDouble("components-repair-costs"));
        }
        rmeta.setLore(Arrays.asList(ChatColor.GOLD+"Right Click - Repair Armour, Costs: " + armourRepairCost,ChatColor.GOLD+"Left Click - Repair Components, Costs: " +componentRepairCost));
        repairItem.setItemMeta(rmeta);
        repairSlot.setItem(repairItem);

        repairSlot.setClickOptions(ClickOptions.builder().allow(ClickType.LEFT, ClickType.RIGHT).build());
        double finalArmourRepairCost = armourRepairCost;
        double finalComponentRepairCost = componentRepairCost;
        repairSlot.setClickHandler((rplayer, clickInformation) -> {
            if (clickInformation.getClickType() == ClickType.RIGHT && finalArmourRepairCost != 0){
                if (RealisticVehicles.getEconomy().has(rplayer, finalArmourRepairCost)) {
                    for (ArmourPlate armour : vehicle.getDamageModel().getArmour()) {
                        armour.weakness = 0;
                    }
                    rplayer.sendMessage(ChatColor.GREEN+"Repaired armour for : " + finalArmourRepairCost);
                } else {
                    rplayer.sendMessage(ChatColor.RED+"Not enough money to repair armour.");
                }
            } else if (finalComponentRepairCost != 0){
                if (RealisticVehicles.getEconomy().has(rplayer, finalComponentRepairCost)) {
                    if (RealisticVehicles.getEconomy().has(rplayer, finalComponentRepairCost)) {
                        for (Component comp : vehicle.getDamageModel().getComponents()) {
                            comp.health = comp.maxHealth;
                        }
                        rplayer.sendMessage(ChatColor.GREEN+"Repaired components for : " + finalArmourRepairCost);

                    } else {
                        rplayer.sendMessage(ChatColor.RED+"Not enough money to repair components.");
                    }
                }
            }
        });

        Mask mask = BinaryMask.builder(menu).pattern("101000101").item(new ItemStack(Material.GRAY_STAINED_GLASS_PANE)).build();
        mask.apply(menu);

        menu.open(player);
    }

    public static void displayFuelMenu(Player player, VehicleInterface vtype){
        Menu menu = ChestMenu.builder((int)Math.ceil(FuelType.fuelTypes.size()/9f)).redraw(true).title("Fuel Types").build();
        int loops = 0;
        for (FuelType type : FuelType.fuelTypes){
            ItemStack item = new ItemStack(type.getIcon());
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(type.getFuelName());
            meta.setLore(Arrays.asList("Density: "+type.getFuelDensity(), "Exchange Rate: "+type.getFuelExchangeRate(), "Cost: "+vtype.getFuelTank().getMaxFuel()/10*type.getFuelExchangeRate()));
            item.setItemMeta(meta);

            Slot slot = menu.getSlot(loops);
            slot.setItem(item);
            slot.setClickHandler(((gplayer, clickInformation) -> {
                //System.out.println("Refueling...");
                Economy econ = RealisticVehicles.getEconomy();
                if (econ.getBalance(player) >= vtype.getFuelTank().getMaxFuel()/10*type.getFuelExchangeRate()) {
                    econ.withdrawPlayer(player, vtype.getFuelTank().getMaxFuel()/10*type.getFuelExchangeRate());
                    float refund = vtype.getFuelTank().refuelAmountOfFuel(type, vtype.getFuelTank().getMaxFuel() / 10);
                    econ.depositPlayer(player, refund);
                    player.sendMessage(ChatColor.GREEN+"Refueled "+vtype.getFuelTank().getMaxFuel()/10+" "+vtype.getFuelTank().getLoadedFuelType().getFuelName()+"! \nCurrent Fuel Amount: "+vtype.getFuelTank().getFuel()+"\nCost: "+(vtype.getFuelTank().getMaxFuel()/10*type.getFuelExchangeRate())+refund);
                } else {
                    player.sendMessage(ChatColor.RED+"Not enough money to refuel.");
                }
            }));

            loops++;
        }

        menu.open(player);
    }

    public static Material getMatFromFuel(double fuel, double maxFuel){
        if (fuel < maxFuel/4){
            return Material.RED_STAINED_GLASS_PANE;
        } else if (fuel < maxFuel/2){
            return Material.YELLOW_STAINED_GLASS_PANE;
        } else {
            return Material.GREEN_STAINED_GLASS_PANE;
        }
    }
}
