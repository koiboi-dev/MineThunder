package me.kaiyan.realisticvehicles.Commands;

import me.kaiyan.realisticvehicles.DataTypes.Interfaces.FixedUpdate;
import me.kaiyan.realisticvehicles.Counters.Updates;
import me.kaiyan.realisticvehicles.DataTypes.Enums.TrackingType;
import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.DataTypes.Exceptions.InvalidTypeException;
import me.kaiyan.realisticvehicles.DataTypes.MissileSettings;
import me.kaiyan.realisticvehicles.Menus.PurchaseMenu;
import me.kaiyan.realisticvehicles.ModelHandlers.FakeRadarTarget;
import me.kaiyan.realisticvehicles.Physics.Missile;
import me.kaiyan.realisticvehicles.Physics.ProjectileShell;
import me.kaiyan.realisticvehicles.RealisticVehicles;
import me.kaiyan.realisticvehicles.VehicleManagers.ItemGenerator;
import me.kaiyan.realisticvehicles.Vehicles.Aircraft;
import me.kaiyan.realisticvehicles.Vehicles.Tank;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestCommands implements CommandExecutor {

    @Override
    public boolean onCommand(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String s, String[] strings) {
        switch (strings[0]) {
            case "tank" -> {
                try {
                    new Tank(((Player) commandSender).getLocation(), "Challenger II");
                } catch (InvalidTypeException e) {
                    e.printStackTrace();
                }
            }
            case "flash" -> RealisticVehicles.flashing = !RealisticVehicles.flashing;
            case "plane" -> {
                try {
                    new Aircraft(((Player) commandSender).getLocation(), "MIG 31");
                } catch (InvalidTypeException e) {
                    e.printStackTrace();
                }
            }
            case "fire" -> {
                Player player = (Player)commandSender;
                new ProjectileShell(player.getEyeLocation(), player.getLocation().getYaw(), player.getLocation().getPitch(), 5, true, 5, 2, false, false, false,false, player, Material.GLASS, Collections.singletonList(""), 0.5, 0, 0, 1);
            }
            case "faketarget" -> new FakeRadarTarget(((Player)commandSender).getLocation());
            case "killall" -> {
                List<FixedUpdate> updates = new ArrayList<>(Updates.fixedUpdates);
                for (FixedUpdate update : updates){
                    update.closeThis(2);
                }
            }
            case "missile" -> {
                Player player = (Player)commandSender;
                new Missile(
                        player.getLocation(),
                        -player.getLocation().getYaw(),
                        player.getLocation().getPitch(),
                        new MissileSettings(6, 6, 2.5f, 100, 0.05f, TrackingType.ACTIVE, "R-40 Interceptor", 701, 40, 1000),
                        (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND),
                        5,
                        null,
                        player

                );
            }
            case "giveitem" -> {
                ItemStack item = new ItemStack(Material.CRAFTING_TABLE);
                ItemMeta meta = item.getItemMeta();
                meta.setCustomModelData(200);
                item.setItemMeta(meta);
                Player player = (Player)commandSender;
                player.getInventory().addItem(item);
            }
            case "tanki" -> {
                Player player = (Player)commandSender;
                player.getInventory().addItem(ItemGenerator.generateNewVehicleItem("Challenger II", VehicleType.TANK));
            }
            case "planei" -> {
                Player player = (Player)commandSender;
                player.getInventory().addItem(ItemGenerator.generateNewVehicleItem("MIG 31", VehicleType.AIR));
            }
            case "truck" -> {
                Player player = (Player) commandSender;
                player.getInventory().addItem(ItemGenerator.generateNewVehicleItem("Bessie Trucking S3-X Truck", VehicleType.CAR));
            }
            case "trailer" -> {
                Player player = (Player) commandSender;
                player.getInventory().addItem(ItemGenerator.generateNewVehicleItem("Bessie Trucking T5 Dry Trailer", VehicleType.TRAILER));
            }
            case "crowbar" -> {
                Player player = (Player) commandSender;
                player.getInventory().addItem(ItemGenerator.getCrowbar());
            }
            case "shop" -> {
                Player player = (Player) commandSender;
                PurchaseMenu.openPurchaseMenu(player);
            }
        }
        return false;
    }
}
