package me.kaiyan.realisticvehicles;

import me.kaiyan.realisticvehicles.Counters.Updates;
import me.kaiyan.realisticvehicles.DataTypes.VehicleInterface;
import me.kaiyan.realisticvehicles.Menus.VehicleMenu;
import me.kaiyan.realisticvehicles.Vehicles.Aircraft;
import me.kaiyan.realisticvehicles.Vehicles.Car;
import me.kaiyan.realisticvehicles.Vehicles.Tank;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class EventListener implements Listener {
    public static final NamespacedKey vehicleType = new NamespacedKey(RealisticVehicles.getInstance(), "vehicleTypes");

    @EventHandler
    public void onPlayerInteract(PlayerInteractAtEntityEvent event){
        fireWeapons(event.getPlayer().getInventory().getItemInMainHand(), event.getPlayer(), event);

        Entity en = event.getRightClicked();
        if (en.getPersistentDataContainer().has(vehicleType, PersistentDataType.STRING)){
            //String type = en.getPersistentDataContainer().get(vehicleType, PersistentDataType.STRING);

            VehicleInterface inter = Updates.getVehicleFromSeat(en);

            if (inter instanceof Tank tank){
                if (event.getPlayer().isSneaking()){
                    VehicleMenu.showMenu(tank, event.getPlayer());
                } else {
                    tank.playerEnteredVehicle(event.getPlayer());
                    inter.getBaseSeat().addPassenger(event.getPlayer());
                    event.getPlayer().setInvulnerable(true);
                }
            }
            else if (inter instanceof Car car){
                if (event.getPlayer().isSneaking()){
                    VehicleMenu.showMenu(car, event.getPlayer());
                } else {
                    inter.getBaseSeat().addPassenger(event.getPlayer());
                }
            } else if (inter instanceof Aircraft craft){
                if (event.getPlayer().isSneaking()){
                    VehicleMenu.showMenu(craft, event.getPlayer());
                } else {
                    inter.getBaseSeat().addPassenger(event.getPlayer());
                    inter.playerEnteredVehicle(event.getPlayer());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerFire(PlayerInteractEvent e){
        fireWeapons(e.getItem(), e.getPlayer(), e);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        VehicleInterface vehicle = Updates.getPlayerVehicle((Player) e.getWhoClicked());
        if (vehicle != null) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        VehicleInterface vehicle = Updates.getPlayerVehicle(event.getPlayer());
        if (vehicle != null){
            event.getPlayer().setInvulnerable(false);
            vehicle.playerExitedVehicle(false);
            vehicle.getBaseSeat().eject();
        }
    }

    @EventHandler
    public void onPlayerDie(PlayerDeathEvent event){
        VehicleInterface vehicle = Updates.getPlayerVehicle(event.getEntity());
        if (vehicle != null){
            event.getEntity().setInvulnerable(false);
            vehicle.playerExitedVehicle(false);
            vehicle.getBaseSeat().eject();
        }
        if (Updates.expectedDeaths.containsKey(event.getEntity())){
            event.setDeathMessage(Updates.expectedDeaths.get(event.getEntity()));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        event.getPlayer().sendMessage(ChatColor.GREEN+
                "========================================\n" +
                "Installing resource pack in 10 seconds, if you do not get it please use <PLACEHOLDER>\n" +
                "========================================");

        new BukkitRunnable(){
            @Override
            public void run() {
                System.out.println("Loaded Player: "+Objects.requireNonNull(RealisticVehicles.getInstance().getConfig().getString("resource-pack")));
                event.getPlayer().setResourcePack(Objects.requireNonNull(RealisticVehicles.getInstance().getConfig().getString("resource-pack")));
            }
        }.runTaskLater(RealisticVehicles.getInstance(), 200);
    }

    public void fireWeapons(ItemStack heldItem, Player player, Cancellable event){
        if (heldItem == null) return;
        VehicleInterface inter = Updates.getPlayerVehicle(player);
        if (inter != null){
            event.setCancelled(true);
            if (inter instanceof Tank tank) {
                if (tank.shells[0].item.isSimilar(heldItem)) {
                    tank.fireShell(0);
                }
                if (tank.shells[1] != null) {
                    if (tank.shells[1].item.isSimilar(heldItem)) {
                        tank.fireShell(1);
                    }
                }
                if (tank.shells[2] != null) {
                    if (tank.shells[2].item.isSimilar(heldItem)) {
                        tank.fireShell(2);
                    }
                }
                if (heldItem.getType() == Material.IRON_TRAPDOOR) {
                    tank.raised = !tank.raised;
                }
                if (heldItem.getType() == Material.GREEN_STAINED_GLASS_PANE) {
                    tank.toggleZoom();
                }
            } else if (inter instanceof Aircraft aircraft){
                if (heldItem.getType() == Material.LEVER){
                    aircraft.setFiring(true);
                }
            }
        }
    }
}
