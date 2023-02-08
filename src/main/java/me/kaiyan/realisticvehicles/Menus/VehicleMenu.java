package me.kaiyan.realisticvehicles.Menus;

import me.kaiyan.realisticvehicles.DamageModel.Hitboxes.ArmourPlate;
import me.kaiyan.realisticvehicles.DamageModel.Hitboxes.Component;
import me.kaiyan.realisticvehicles.DamageModel.Projectiles.Shell;
import me.kaiyan.realisticvehicles.DataTypes.FuelType;
import me.kaiyan.realisticvehicles.DataTypes.MissileSettings;
import me.kaiyan.realisticvehicles.DataTypes.Interfaces.VehicleInterface;
import me.kaiyan.realisticvehicles.Models.MissileHolder;
import me.kaiyan.realisticvehicles.Models.MissileSlot;
import me.kaiyan.realisticvehicles.RealisticVehicles;
import me.kaiyan.realisticvehicles.VehicleManagers.ItemGenerator;
import me.kaiyan.realisticvehicles.Vehicles.Car;
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
import java.util.List;

public class VehicleMenu {
    public static void showMenu(VehicleInterface vehicle, Player player){
        Menu menu = ChestMenu.builder(2).title(vehicle.getNameType()).redraw(false).build();
        Slot fuelSlot = menu.getSlot(1+9);
        ItemStack fuelItem = new ItemStack(getMatFromFuel(vehicle.getFuelTank().getFuel(), vehicle.getFuelTank().getMaxFuel()));
        ItemMeta meta = fuelItem.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.GOLD+"Fuel Amount");
        meta.setLore(List.of(
                ChatColor.GREEN + "The amount of fuel left in the vehicle",
                String.format(ChatColor.GREEN + "Percent: %.2f", (vehicle.getFuelTank().getFuel() / vehicle.getFuelTank().getMaxFuel()) * 100) + "%",
                String.format(ChatColor.GREEN + "Fuel: %.2f", vehicle.getFuelTank().getFuel()),
                String.format(ChatColor.GREEN + "Max Fuel: %.2f", vehicle.getFuelTank().getMaxFuel()),
                String.format(ChatColor.GREEN + "Fuel Type: %s", vehicle.getFuelTank().getLoadedFuelType().getFuelName()),
                String.format(ChatColor.GREEN + "Click to refuel %.2f", vehicle.getFuelTank().getMaxFuel() / 10))
        );
        fuelItem.setItemMeta(meta);
        fuelSlot.setItem(fuelItem);
        fuelSlot.setClickHandler(((gplayer, clickInformation) -> displayFuelMenu(player, vehicle)));

        for (int i = 0; i < 3; i++) {
            Slot ammoSlot = menu.getSlot(i+3+9);
            Shell shell = vehicle.getShells()[i];
            if (shell != null) {
                ItemStack item = new ItemStack(shell.item);
                ItemMeta imeta = item.getItemMeta();
                assert imeta != null;
                imeta.setDisplayName(shell.getAbbreviation());
                imeta.setLore(Arrays.asList("Current Amount: "+vehicle.getShellsAmmo()[i], "Buys "+shell.buyAmount+" shell at "+shell.cost));
                item.setItemMeta(imeta);
                ammoSlot.setItem(item);
                int finalI = i;
                ammoSlot.setClickHandler((player1, clickInformation) -> {
                    if (RealisticVehicles.getEconomy().getBalance(player) >= shell.cost){
                        RealisticVehicles.getEconomy().withdrawPlayer(player, shell.cost);
                        vehicle.addShells(finalI, shell.buyAmount);
                        player.sendMessage(ChatColor.GREEN+"Restocked a shell, Shell count: " +vehicle.getShellsAmmo()[finalI]);
                        menu.close(player);
                        showMenu(vehicle,player);
                    } else {
                        player.sendMessage(ChatColor.RED+"Not enough money to restock.");
                    }
                });
            }
        }

        Slot repairSlot = menu.getSlot(7+9);
        ItemStack repairItem = new ItemStack(Material.BOOK);
        ItemMeta rmeta = repairItem.getItemMeta();
        assert rmeta != null;
        rmeta.setDisplayName(ChatColor.GREEN+"Repair Vehicle");
        double armourRepairCost = 0;
        for (ArmourPlate armour : vehicle.getDamageModel().getArmour()){
            armourRepairCost += Math.round(armour.weakness*RealisticVehicles.getInstance().getConfig().getDouble("armor-repair-cost"));
        }
        double componentRepairCost = 0;
        for (Component comp : vehicle.getDamageModel().getComponents()){
            componentRepairCost += Math.round((comp.maxHealth-comp.health)*RealisticVehicles.getInstance().getConfig().getDouble("component-repair-cost"));
        }
        rmeta.setLore(Arrays.asList(ChatColor.GOLD+"Left Click - Repair Components, Costs: " +componentRepairCost, ChatColor.GOLD+"Right Click - Repair Armour, Costs: " + armourRepairCost));
        repairItem.setItemMeta(rmeta);
        repairSlot.setItem(repairItem);

        repairSlot.setClickOptions(ClickOptions.builder().allow(ClickType.LEFT, ClickType.RIGHT).build());
        double finalArmourRepairCost = armourRepairCost;
        double finalComponentRepairCost = componentRepairCost;
        repairSlot.setClickHandler((rplayer, clickInformation) -> {
            if (clickInformation.getClickType() == ClickType.RIGHT){
                if (RealisticVehicles.getEconomy().has(rplayer, finalArmourRepairCost)) {
                    for (ArmourPlate armour : vehicle.getDamageModel().getArmour()) {
                        armour.weakness = 0;
                    }
                    rplayer.sendMessage(ChatColor.GREEN+"Repaired armour for : " + finalArmourRepairCost);
                } else {
                    rplayer.sendMessage(ChatColor.RED+"Not enough money to repair armour.");
                }
            } else {
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

        if (vehicle.getMissileHolder() != null){
            Slot missileSlot = menu.getSlot(1);
            ItemStack missileItem = new ItemStack(Material.GOLDEN_HOE);
            ItemMeta missileMeta = missileItem.getItemMeta();
            assert missileMeta != null;
            missileMeta.setDisplayName("Configure Missile Setup");
            missileItem.setItemMeta(missileMeta);
            missileSlot.setItem(missileItem);
            missileSlot.setClickHandler((ply, info) -> generateEquipmentMenu(vehicle.getMissileHolder(), vehicle, player).open(player));
        }

        Slot deleteSlot = menu.getSlot(7);
        ItemStack deleteItem = new ItemStack(Material.BARRIER);
        ItemMeta deleteMeta = deleteItem.getItemMeta();
        assert deleteMeta != null;
        deleteMeta.setDisplayName("Pickup Vehicle");
        deleteMeta.setLore(Arrays.asList("Picks up the vehicle as a item", ChatColor.DARK_RED+"WILL DELETE ANY ITEMS/FLUIDS IN THE VEHICLE"));
        deleteItem.setItemMeta(deleteMeta);
        deleteSlot.setItem(deleteItem);
        deleteSlot.setClickHandler((ply, info) -> {
            ply.getWorld().dropItem(ply.getLocation(), ItemGenerator.getItemFromVehicle(vehicle));
            vehicle.scrap(true);
            menu.close();
        });
        if (vehicle instanceof Car car){
            Slot inv = menu.getSlot(3);
            ItemStack invItem = new ItemStack(Material.CHEST);
            ItemMeta invMeta = invItem.getItemMeta();
            invMeta.setDisplayName("Open Inventory");
            invMeta.setLore(List.of(ChatColor.GRAY+"Opens this vehicles inventory", ChatColor.GRAY+"Temp inventory for storing / placing items"));
            invItem.setItemMeta(invMeta);
            inv.setItem(invItem);

            inv.setClickHandler((p, info) -> {
                if (car.getHarvester() != null){
                    p.openInventory(car.getHarvester().getInv());
                }
            });

            Mask mask = BinaryMask.builder(menu).pattern("101011101").pattern("101000101").item(new ItemStack(Material.GRAY_STAINED_GLASS_PANE)).build();
            mask.apply(menu);
        } else {
            Mask mask = BinaryMask.builder(menu).pattern("101111101").pattern("101000101").item(new ItemStack(Material.GRAY_STAINED_GLASS_PANE)).build();
            mask.apply(menu);
        }

        menu.open(player);
    }
    public static Menu generateEquipmentMenu(MissileHolder holder, VehicleInterface inter, Player player){
        Menu menu = ChestMenu.builder(3).build();

        int loops = 0;
        for (MissileSlot slots : holder.getMissiles()){
            Slot slot = menu.getSlot(loops);
            ItemStack item;
            if (slots.getSettings() == null) {
                item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            } else {
                item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            }
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(slots.getName());
            meta.setLore(List.of("Missile attachment point.", "Replacing the missile inside WILL DELETE IT."));
            item.setItemMeta(meta);
            slot.setItem(item);
            slot.setClickHandler((player1, clickInformation) -> generateMissileMenu(slots, inter).open(player));
            loops++;
        }

        return menu;
    }

    public static Menu generateMissileMenu(MissileSlot mSlot, VehicleInterface inter){
        Menu menu = ChestMenu.builder(3).build();

        int loops = 0;
        for (MissileSettings settings : inter.getValidMissiles()){
            Slot slot = menu.getSlot(loops);
            ItemStack item = new ItemStack(Material.WOODEN_HOE);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(settings.getName());
            meta.setLore(Arrays.asList(String.format("Power: %f-Speed: %f-Turn Rate:%f-Tracking Type: %s-Fuel: %f", settings.getPower(), settings.getSpeed(), settings.getTurnRate(), settings.getType(), settings.getStartFuel()).split("-")));
            meta.setCustomModelData(settings.getTexID());
            item.setItemMeta(meta);
            slot.setItem(item);

            slot.setClickHandler((player, info) -> {
                if (mSlot.getSettings() != null){
                    mSlot.setSettings(null);
                    mSlot.getStand().remove();
                }
                mSlot.setSettings(settings);
                mSlot.generateArmourStand(inter.getLoc());
                menu.close();
            });

            loops++;
        }
        return menu;
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
