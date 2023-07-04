package me.kaiyan.realisticvehicles;

import me.kaiyan.realisticvehicles.Counters.Updates;
import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.DataTypes.Interfaces.FixedUpdate;
import me.kaiyan.realisticvehicles.DataTypes.Interfaces.VehicleInterface;
import me.kaiyan.realisticvehicles.Menus.TrailerMenu;
import me.kaiyan.realisticvehicles.Menus.VehicleMenu;
import me.kaiyan.realisticvehicles.VehicleManagers.ItemGenerator;
import me.kaiyan.realisticvehicles.VehicleManagers.VehicleSaver;
import me.kaiyan.realisticvehicles.Vehicles.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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

import java.util.*;

public class EventListener implements Listener {
    public static final NamespacedKey vehicleType = new NamespacedKey(RealisticVehicles.getInstance(), "vehicleTypes");

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntity(PlayerInteractAtEntityEvent event){
        event.setCancelled(fireWeapons(event.getPlayer().getInventory().getItemInMainHand(), event.getPlayer(), Action.RIGHT_CLICK_AIR));

        Entity en = event.getRightClicked();

        if (en.getPersistentDataContainer().has(RealisticVehicles.SLEEPKEY, PersistentDataType.STRING)){
            //RealisticVehicles.debugLog("Sleep Key'd");
            String[] info = en.getPersistentDataContainer().get(RealisticVehicles.SLEEPKEY, PersistentDataType.STRING).split(";");
            //RealisticVehicles.debugLog(info[0]);
            switch (VehicleType.valueOf(info[1])){
                case CAR -> {
                    RealisticVehicles.debugLog("Car");
                    // 0       ; 1      ; 2      ; 3      ; 4                   ; 5                 ; 6 (0 or 1 value); 7
                    //sleepID+";"+type+";"+name+";"+data+";"+stand.getKey()[0]+";"+stand.getKey()[1];isSeatEnt; yaw
                    ArmorStand seat = null;
                    HashMap<int[], ArmorStand> stands = new HashMap<>();
                    Location loc = en.getLocation().clone();
                    for (Entity ent : event.getPlayer().getWorld().getEntities()){
                        if (ent instanceof ArmorStand stand){
                            if (stand.getPersistentDataContainer().has(RealisticVehicles.SLEEPKEY, PersistentDataType.STRING)){
                                String[] sinfo = stand.getPersistentDataContainer().get(RealisticVehicles.SLEEPKEY, PersistentDataType.STRING).split(";");
                                RealisticVehicles.debugLog(sinfo[0]+" : "+sinfo[1]+" : "+sinfo[1]);
                                if (!Objects.equals(sinfo[0], info[0])){
                                    continue;
                                }
                                stand.getPersistentDataContainer().remove(RealisticVehicles.SLEEPKEY);
                                stand.remove();
                                /*if (sinfo[6].equals("1")){
                                    if (stand.getUniqueId().equals(en.getUniqueId()))
                                        loc.subtract(new Vector(Float.parseFloat(sinfo[4]), 0, Float.parseFloat(sinfo[5])));
                                    seat = stand;
                                    continue;
                                } else if (stand.getUniqueId().equals(en.getUniqueId())){
                                    loc.subtract(new Vector(Integer.parseInt(sinfo[4]), 0, Integer.parseInt(sinfo[5])).multiply(Model.GRID_OFFSET));
                                }
                                stands.put(new int[] {Integer.parseInt(sinfo[4]), Integer.parseInt(sinfo[5])}, stand);*/
                            }
                        }
                    }
                    Car car = (Car) VehicleSaver.fromJson(info[3].split("@")[0]).createCraft(loc);
                    //car.resetModels(seat);
                    car.setYaw(Float.parseFloat(info[7]));
                    car.getBaseSeat().addPassenger(event.getPlayer());
                }
                case TANK -> {
                    // 0  ; 1           ; 2                                 ; 3  ; 4
                    //id+";"+getType()+";"+new VehicleSaver(this).toJson()+";seat;"+getVehicleYaw()
                    ArmorStand seat = null;
                    ArmorStand turret = null;
                    ArmorStand gun = null;
                    ArmorStand base = null;
                    Location loc = en.getLocation().clone();
                    boolean enIsSeat = false;
                    for (Entity ent : event.getPlayer().getWorld().getEntities()){
                        if (ent instanceof ArmorStand stand){
                            if (stand.getPersistentDataContainer().has(RealisticVehicles.SLEEPKEY, PersistentDataType.STRING)) {
                                String data = stand.getPersistentDataContainer().get(RealisticVehicles.SLEEPKEY, PersistentDataType.STRING);
                                stand.getPersistentDataContainer().remove(RealisticVehicles.SLEEPKEY);
                                if (Objects.equals(data.split(";")[0], info[0])) {
                                    if (data.contains("gun")) gun = stand;
                                    else if (data.contains("turret")) turret = stand;
                                    else if (data.contains("base")) base = stand;
                                    else if (data.contains("seat")) {
                                        seat = stand;
                                        enIsSeat = en.getUniqueId() == stand.getUniqueId();
                                    }
                                }
                            }
                        }
                    }
                    Tank tank = (Tank) VehicleSaver.fromJson(info[2]).createCraft(loc);
                    tank.resetModels(seat, base, gun, turret);
                    if (enIsSeat){
                        loc.subtract(tank.getSeatPos());
                    }
                    tank.setLoc(loc);
                    tank.setYaw(Float.parseFloat(info[4]));
                    tank.getBaseSeat().addPassenger(event.getPlayer());
                }
                case AIR -> {
                    // 0       ; 1      ; 2      ; 3      ; 4                   ; 5                 ; 6 (0 or 1 value); 7
                    //sleepID+";"+type+";"+name+";"+data+";"+stand.getKey()[0]+";"+stand.getKey()[1];isSeatEnt; yaw
                    //ArmorStand seat = null;
                    //HashMap<int[], ArmorStand> stands = new HashMap<>();
                    Location loc = en.getLocation().clone();
                    for (Entity ent : event.getPlayer().getWorld().getEntities()){
                        if (ent instanceof ArmorStand stand){
                            if (stand.getPersistentDataContainer().has(RealisticVehicles.SLEEPKEY, PersistentDataType.STRING)){
                                String[] sinfo = stand.getPersistentDataContainer().get(RealisticVehicles.SLEEPKEY, PersistentDataType.STRING).split(";");
                                RealisticVehicles.debugLog(sinfo[0]+" : "+sinfo[1]+" : "+sinfo[2]);
                                if (!Objects.equals(sinfo[0], info[0])){
                                    continue;
                                }
                                //stand.getPersistentDataContainer().remove(RealisticVehicles.SLEEPKEY);
                                stand.remove();
                                /*if (sinfo[6].equals("1")){
                                    if (stand.getUniqueId().equals(en.getUniqueId()))
                                        loc.subtract(new Vector(Float.parseFloat(sinfo[4]), 0, Float.parseFloat(sinfo[5])));
                                    seat = stand;
                                    continue;
                                } else if (stand.getUniqueId().equals(en.getUniqueId())){
                                    loc.subtract(new Vector(Integer.parseInt(sinfo[4]), 0, Integer.parseInt(sinfo[5])).multiply(Model.GRID_OFFSET));
                                }
                                stands.put(new int[] {Integer.parseInt(sinfo[4]), Integer.parseInt(sinfo[5])}, stand);*/
                            }
                        }
                    }
                    Aircraft plane = (Aircraft) VehicleSaver.fromJson(info[3]).createCraft(loc.clone().add(new Vector(0, 1, 0)));
                    plane.setYaw(Float.parseFloat(info[7]));
                    //plane.getLoc().add(new Vector(0, 0.5, 0));
                    plane.getBaseSeat().addPassenger(event.getPlayer());
                }
            }
        }

        if (en.getPersistentDataContainer().has(vehicleType, PersistentDataType.STRING) && en instanceof ArmorStand stand){
            event.setCancelled(true);
            //String type = en.getPersistentDataContainer().get(vehicleType, PersistentDataType.STRING);

            VehicleInterface inter = Updates.getVehicleFromStand(stand);
            RealisticVehicles.debugLog(inter);

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
            } else if (inter instanceof Car car){
                if (event.getPlayer().isSneaking()){
                    VehicleMenu.showMenu(car, event.getPlayer());
                } else {
                    inter.getBaseSeat().addPassenger(event.getPlayer());
                    inter.playerEnteredVehicle(event.getPlayer());
                }
            }
        }
        else if (en.getPersistentDataContainer().has(RealisticVehicles.SCRAPKEY, PersistentDataType.INTEGER)){
            event.getPlayer().sendMessage(ChatColor.GOLD+"Salvaging Wreck, Moving will cancel it!");
            new BukkitRunnable() {
                int loops = 0;
                final int maxTime = 100;
                final Location startLoc = event.getPlayer().getLocation();
                @Override
                public void run() {
                    ComponentBuilder builder = new ComponentBuilder();
                    builder.append("[").color(net.md_5.bungee.api.ChatColor.BOLD);
                    for (int i = 1; i <= 10; i++){
                        if ((float) loops/maxTime >= 0.1*i){
                            builder.append(ChatColor.GREEN+"█");
                        } else {
                            builder.append(ChatColor.GRAY+"█");
                        }
                    }
                    builder.append("]").color(net.md_5.bungee.api.ChatColor.BOLD);
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

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent e){
        e.setCancelled(fireWeapons(e.getItem(), e.getPlayer(), e.getAction()));

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getItem() != null && e.getItem().getItemMeta().getPersistentDataContainer().has(ItemGenerator.JSONSAVE, PersistentDataType.BYTE_ARRAY)) {
            RayTraceResult hit = e.getPlayer().getWorld().rayTraceBlocks(e.getPlayer().getEyeLocation(), e.getPlayer().getLocation().getDirection(), 4.5);
            if (hit != null && hit.getHitBlock() != null){
                ItemGenerator.spawnVehicleFromItem(e.getItem(), hit.getHitPosition().toLocation(e.getPlayer().getWorld()));
                ItemStack[] inv = e.getPlayer().getInventory().getStorageContents();
                inv[e.getPlayer().getInventory().first(e.getItem())] = null;
                e.getPlayer().getInventory().setStorageContents(inv);
            }
        }

        if (e.getItem() != null && e.getItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD+""+ChatColor.ITALIC+"Crowbar")){
            RealisticVehicles.wakeVehicles();

            e.setCancelled(true);
            float minDist = 100;
            Trailer fTrailer = null;
            for (FixedUpdate update : Updates.fixedUpdates){
                if (update instanceof Trailer trailer){
                    if (e.getPlayer().getLocation().distanceSquared(trailer.getLoc()) < minDist){
                        minDist = (float) e.getPlayer().getLocation().distanceSquared(trailer.getLoc());
                        fTrailer = trailer;
                    }
                }
            }
            if (fTrailer != null) {
                if (e.getPlayer().isSneaking()) {
                    TrailerMenu.openTrailerInventory(e.getPlayer(), fTrailer);
                } else {
                    fTrailer.hitchInteract(e.getPlayer());
                }
            }
        }
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
        new BukkitRunnable(){
            @Override
            public void run() {
                event.getPlayer().sendMessage(ChatColor.GREEN+
                        "========================================\n" +
                        "Installing resource pack in 10 seconds, if you do not get it please use /mt resource\n" +
                        "========================================");
            }
        }.runTaskLater(RealisticVehicles.getInstance(), 5);

        new BukkitRunnable(){
            @Override
            public void run() {
                RealisticVehicles.debugLog("Loaded Player: "+Objects.requireNonNull(RealisticVehicles.getInstance().getConfig().getString("resource-pack")));
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
