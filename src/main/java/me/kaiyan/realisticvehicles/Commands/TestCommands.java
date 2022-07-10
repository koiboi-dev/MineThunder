package me.kaiyan.realisticvehicles.Commands;

import me.kaiyan.realisticvehicles.DataTypes.Exceptions.InvalidTypeException;
import me.kaiyan.realisticvehicles.Physics.ProjectileShell;
import me.kaiyan.realisticvehicles.RealisticVehicles;
import me.kaiyan.realisticvehicles.Vehicles.Aircraft;
import me.kaiyan.realisticvehicles.Vehicles.Car;
import me.kaiyan.realisticvehicles.Vehicles.Tank;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.Objects;

public class TestCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        switch (strings[0]) {
            case "tank" -> {
                try {
                    new Tank(((Player) commandSender).getLocation(), "Challenger II");
                } catch (InvalidTypeException e) {
                    e.printStackTrace();
                }
            }
            case "flash" -> {
                RealisticVehicles.flashing = !RealisticVehicles.flashing;
            }
            case "car" -> {
                try {
                    new Car(((Player) commandSender).getLocation(), (Player) commandSender, "ahmad");
                } catch (InvalidTypeException e) {
                    e.printStackTrace();
                }
            }
            case "plane" -> {
                new Aircraft(((Player) commandSender).getLocation(), "MIG 31");
            }
            case "fire" -> {
                Player player = (Player)commandSender;
                new ProjectileShell(player.getEyeLocation(), player.getLocation().getYaw(), player.getLocation().getPitch(), 5, true, 5, 2, false, false, false,false, player, Material.GLASS, Collections.singletonList(""), 0.5, 0, 0, 1);
            }
            case "test" -> {
                Player player = (Player)commandSender;
                ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
            }
        }
        return false;
    }
}
