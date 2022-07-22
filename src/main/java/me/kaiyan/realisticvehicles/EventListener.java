package me.kaiyan.realisticvehicles;

import me.kaiyan.realisticvehicles.Counters.Updates;
import me.kaiyan.realisticvehicles.DataTypes.VehicleInterface;
import me.kaiyan.realisticvehicles.Menus.VehicleMenu;
import me.kaiyan.realisticvehicles.VehicleManagers.ItemGenerator;
import me.kaiyan.realisticvehicles.Vehicles.Aircraft;
import me.kaiyan.realisticvehicles.Vehicles.Tank;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.checkerframework.checker.units.qual.C;

import java.util.Objects;

public class EventListener implements Listener {
    public static final NamespacedKey vehicleType = new NamespacedKey(RealisticVehicles.getInstance(), "vehicleTypes");

    @EventHandler
    public void onPlayerInteract(PlayerInteractAtEntityEvent event){
        event.setCancelled(fireWeapons(event.getPlayer().getInventory().getItemInMainHand(), event.getPlayer(), Action.RIGHT_CLICK_AIR));

        Entity en = event.getRightClicked();
        if (en.getPersistentDataContainer().has(vehicleType, PersistentDataType.STRING) && en instanceof ArmorStand stand){
            event.setCancelled(true);
            //String type = en.getPersistentDataContainer().get(vehicleType, PersistentDataType.STRING);

            VehicleInterface inter = Updates.getVehicleFromStand(stand);

            if (inter instanceof Tank tank){
                if (event.getPlayer().isSneaking()){
                    VehicleMenu.showMenu(tank, event.getPlayer());
                } else {
                    tank.playerEnteredVehicle(event.getPlayer());
                    inter.getBaseSeat().addPassenger(event.getPlayer());
                    event.getPlayer().setInvulnerable(true);
                }
            } else if (inter instanceof Aircraft craft){
                if (event.getPlayer().isSneaking()){
                    VehicleMenu.showMenu(craft, event.getPlayer());
                } else {
                    inter.getBaseSeat().addPassenger(event.getPlayer());
                    inter.playerEnteredVehicle(event.getPlayer());
                }
            }
        } else if (en.getPersistentDataContainer().has(RealisticVehicles.SCRAPKEY, PersistentDataType.INTEGER)){
            event.getPlayer().sendMessage(ChatColor.GOLD+"Salvaging Wreck, Moving will cancel it!");
            new BukkitRunnable() {
                int loops = 0;
                final int maxTime = 100;
                final Location startLoc = event.getPlayer().getLocation();
                @Override
                public void run() {
                    ComponentBuilder builder = new ComponentBuilder();
                    builder.append("[");
                    for (int i = 1; i <= 10; i++){
                        if ((float) loops/maxTime >= 0.1*i){
                            builder.append(ChatColor.GREEN+"█");
                        } else {
                            builder.append(ChatColor.GRAY+"█");
                        }
                    }
                    builder.append("]");
                    event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, builder.create());

                    if (startLoc.distanceSquared(event.getPlayer().getLocation()) > 1){
                        event.getPlayer().sendMessage(ChatColor.RED+"Failed salvage: You moved.");
                        cancel();
                    }

                    if (loops > maxTime) {
                        RealisticVehicles.getEconomy().depositPlayer(event.getPlayer(), en.getPersistentDataContainer().get(RealisticVehicles.SCRAPKEY, PersistentDataType.INTEGER));
                        en.remove();
                        event.getPlayer().sendMessage(ChatColor.GREEN+"Salvaged!");
                        cancel();
                    }
                    loops++;
                }
            }.runTaskTimer(RealisticVehicles.getInstance(), 0, 1);
        }
    }

    @EventHandler
    public void onModelDamageEvent(EntityDamageEvent event){
        if (event.getEntity() instanceof ArmorStand stand) {
            event.setCancelled(Updates.getVehicleFromStand(stand) != null);
        }
    }

    @EventHandler
    public void onPlayerFire(PlayerInteractEvent e){
        e.setCancelled(fireWeapons(e.getItem(), e.getPlayer(), e.getAction()));
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        VehicleInterface vehicle = Updates.getPlayerVehicle((Player) e.getWhoClicked());
        if (vehicle != null) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e){
        VehicleInterface vehicle = Updates.getPlayerVehicle(e.getPlayer());
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
    public void onPlayerInteract(PlayerInteractEvent e){
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getItem() != null && e.getItem().getItemMeta().getPersistentDataContainer().has(ItemGenerator.JSONSAVE, PersistentDataType.STRING)) {
            RayTraceResult hit = e.getPlayer().getWorld().rayTraceBlocks(e.getPlayer().getEyeLocation(), e.getPlayer().getLocation().getDirection(), 4.5);
            if (hit != null && hit.getHitBlock() != null){
                ItemGenerator.spawnVehicleFromItem(e.getItem(), hit.getHitPosition().toLocation(e.getPlayer().getWorld()));
                e.getPlayer().getInventory().remove(e.getItem());
            }
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
            event.setDeathMessage(Updates.expectedDeaths.get(event.getEntity()).getMessage());
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

    public boolean fireWeapons(ItemStack heldItem, Player player, Action action){
        if (heldItem == null) return false;
        VehicleInterface inter = Updates.getPlayerVehicle(player);
        if (inter != null){
            if (inter instanceof Tank tank) {
                if (tank.getShells()[0].item.isSimilar(heldItem)) {
                    tank.fireShell(0);
                }
                if (tank.getShells()[1] != null) {
                    if (tank.getShells()[1].item.isSimilar(heldItem)) {
                        tank.fireShell(1);
                    }
                }
                if (tank.getShells()[2] != null) {
                    if (tank.getShells()[2].item.isSimilar(heldItem)) {
                        tank.fireShell(2);
                    }
                }
                if (heldItem.getType() == Material.IRON_TRAPDOOR) {
                    tank.setRaised(!tank.isRaised());
                }
                if (heldItem.getType() == Material.GREEN_STAINED_GLASS_PANE) {
                    tank.toggleZoom();
                }
            } else if (inter instanceof Aircraft aircraft){
                if (heldItem.getType() == Material.LEVER){
                    aircraft.setFiring(true);
                }  else if (heldItem.getType() == Material.WOODEN_HOE){
                    //TODO Fix Missiles Not Firing, Fix Breaking Armour Stand.
                    if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK){
                        aircraft.cyclePylon();
                        aircraft.updateMissileItem();
                    } else if (action == Action.RIGHT_CLICK_AIR){
                        aircraft.attemptFireMissile();
                        aircraft.cyclePylon();
                        aircraft.updateMissileItem();
                    }
                }
            }
            return true;
        }
        return false;
    }
}
