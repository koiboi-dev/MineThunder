package me.kaiyan.realisticvehicles.Counters;

import me.kaiyan.realisticvehicles.DamageModel.Dimensions.Rect;
import me.kaiyan.realisticvehicles.DamageModel.Hitboxes.Component;
import me.kaiyan.realisticvehicles.DataTypes.DeathMessage;
import me.kaiyan.realisticvehicles.DataTypes.Enums.ComponentType;
import me.kaiyan.realisticvehicles.DataTypes.Interfaces.FixedUpdate;
import me.kaiyan.realisticvehicles.DataTypes.ImpactOutData;
import me.kaiyan.realisticvehicles.DataTypes.Interfaces.VehicleInterface;
import me.kaiyan.realisticvehicles.Physics.ProjectileShell;
import me.kaiyan.realisticvehicles.RealisticVehicles;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

public class Updates {
    public static final List<FixedUpdate> fixedUpdates = new ArrayList<>();

    public static void addListener(FixedUpdate update){
        fixedUpdates.add(update);
    }
    public static final HashMap<Player, DeathMessage> expectedDeaths = new HashMap<>();
    public static List<FixedUpdate> toBeRemoved = new ArrayList<>();
    public static List<FixedUpdate> toBeAdded = new ArrayList<>();

    public static void triggerFixedUpdate(){
        try {
            float time = System.nanoTime();
            List<ProjectileShell> shells = new ArrayList<>();
            List<VehicleInterface> vehicles = new ArrayList<>();
            for (FixedUpdate update : fixedUpdates) {
                if (update != null) {
                    update.OnFixedUpdate();
                    if (update instanceof ProjectileShell shell){
                        shells.add(shell);
                    } else if (update instanceof VehicleInterface vehicle){
                        vehicles.add(vehicle);
                    }
                }
            }
            fixedUpdates.removeAll(toBeRemoved);
            fixedUpdates.addAll(toBeAdded);
            toBeRemoved = new ArrayList<>();
            toBeAdded = new ArrayList<>();
            List<Player> remove = new ArrayList<>();
            for (Map.Entry<Player, DeathMessage> message: expectedDeaths.entrySet()){
                if (message.getValue().getAliveTime() > 2){
                    remove.add(message.getKey());
                }
                message.getValue().addAliveTime(1);
            }
            for (Player player : remove){
                expectedDeaths.remove(player);
            }
            calculateShellImpacts(vehicles, shells);
            calculateCollisions(vehicles);
            //RealisticVehicles.debugLog("UpdateSpeed: "+((System.nanoTime()-time)/1000000)+"ms");
        } catch (ConcurrentModificationException e){
            e.printStackTrace();
        }
    }

    public static void onClose(){
        for (FixedUpdate update : fixedUpdates){
            update.OnClose();
        }
    }

    public static List<VehicleInterface> getActiveVehicles(){
        List<VehicleInterface> vehicles = new ArrayList<>();
        for (FixedUpdate update : fixedUpdates){
            if (update instanceof VehicleInterface inter){
                vehicles.add(inter);
            }
        }
        return vehicles;
    }
    public static void calculateShellImpacts(List<VehicleInterface> vehicles, List<ProjectileShell> shells){
        for (VehicleInterface vehicle : vehicles){
            /*List<ProjectileShell> boundingShells = shells.stream().filter(shell -> shell.player != vehicle.getSeatedPlayer() && (vehicle.getDamageModel().isInBoundingBox(shell.loc, vehicle.getLoc()) || vehicle.getDamageModel().isInBoundingBox(shell.prevLoc.clone().add(shell.loc.clone().subtract(shell.prevLoc).multiply(0.5)), vehicle.getLoc()))).toList();
            if (boundingShells.size() == 0){
                continue;
            }*/
            Rect box = vehicle.getDamageModel().getTotalBox();
            List<ProjectileShell> boundingShells = shells.stream().filter(shell -> {
                Vector L1 = shell.prevLoc.toVector().subtract(vehicle.getLoc().toVector());
                Vector L2 = shell.loc.toVector().subtract(vehicle.getLoc().toVector());
                if (L2.getX() < box.x && L1.getX() < box.x) return false;
                if (L2.getX() > box.x+box.xsize && L1.getX() > box.x+box.xsize) return false;
                if (L2.getY() < box.y && L1.getY() < box.y) return false;
                if (L2.getY() > box.y+box.ysize && L1.getY() > box.y+box.ysize) return false;
                if (L2.getZ() < box.z && L1.getZ() < box.z) return false;
                if (L2.getZ() > box.z+box.zsize && L1.getZ() > box.z+box.zsize) return false;

                for (int i = 0; i < Math.floor(shell.power); i++) {
                    if (vehicle.getDamageModel().isInBoundingBox(shell.prevLoc.clone().add(shell.loc.clone().subtract(shell.prevLoc).multiply(i/10f)), vehicle.getLoc())){
                        shell.loc = shell.prevLoc.clone().add(shell.loc.clone().subtract(shell.prevLoc).multiply(i/shell.power));
                        return true;
                    }
                }
                return false;
            }).toList();
            for (ProjectileShell shell : boundingShells){
                RealisticVehicles.debugLog("Hit");
                ImpactOutData data = vehicle.getDamageModel().shellImpact(shell, vehicle.getLoc(), shell.loc, vehicle.getVehicleYaw(), (float) vehicle.getTurretYaw(), shell.getYaw(), shell.getPitch(), shell.loc.getWorld(), 0, true, shell.player);

                if (!data.getGoneThrough()) {
                    if (vehicle.getSeatedPlayer() != null) {
                        shell.player = vehicle.getSeatedPlayer();
                    }
                    shell.closeThis(1);
                }

                if (data.getPlayerDamage() != 0 && vehicle.getSeatedPlayer() != null){
                    if (!(vehicle.getSeatedPlayer().getHealth() - data.getPlayerDamage() > 0)) {
                        expectedDeaths.put(vehicle.getSeatedPlayer(), new DeathMessage(vehicle.getSeatedPlayer().getName() + " was killed in a " + vehicle.getNameType() + " by " + shell.player.getName() + " in a " + Objects.requireNonNull(getPlayerVehicle(shell.player)).getNameType()));
                    }
                    vehicle.getSeatedPlayer().damage(data.getPlayerDamage());
                }
                if (data.getDestroyedIndex() == -1){
                    continue;
                }
                Component comp = vehicle.getDamageModel().getComponents().get(data.getDestroyedIndex());
                if (comp.type == ComponentType.FUEL){
                    vehicle.explode();
                } else if (comp.type == ComponentType.AMMOSTOWAGE){
                    vehicle.fizzleAmmo(data.getDestroyedIndex());
                }
            }
        }
    }

    public static void calculateCollisions(List<VehicleInterface> vehicles){
        for (VehicleInterface vehicle : vehicles){
            for (VehicleInterface oV : vehicles){
                if (vehicle != oV && vehicle.getLoc().distanceSquared(oV.getLoc()) < vehicle.getDamageModel().getCollisionSphere() * vehicle.getDamageModel().getCollisionSphere()){
                    vehicle.crash();
                    oV.crash();
                }
            }
        }
    }

    /*public static void getShellImpact(VehicleInterface vehicle){
        for (FixedUpdate update : fixedUpdates){
            if (update instanceof ProjectileShell shell){
                if (vehicle.getDamageModel().isInBoundingBox(shell.loc, vehicle.getLoc()) && vehicle.getSeatedPlayer() != shell.player) {
                    int ammo = vehicle.getDamageModel().shellImpact(shell, vehicle.getLoc(), shell.loc, vehicle.getVehicleYaw(), (float) vehicle.getTurretYaw(), shell.yaw, -shell.pitch, shell.loc.getWorld(), 0, true, shell.player);
                    shell.closeThis();
                    if (ammo == -2){
                        vehicle.explode();
                    } else if (ammo != -1) {
                        vehicle.fizzleAmmo(ammo);
                    }
                }
            }
        }
    }*/

    public static VehicleInterface getVehicleFromStand(Interaction seatEnt){
        for (FixedUpdate update : fixedUpdates){
            if (update instanceof VehicleInterface inter){
                if (inter.getBaseSeat().getUniqueId() == seatEnt.getUniqueId() || inter.containsSeat(seatEnt)){
                    return inter;
                }
            }
        }
        return null;
    }

    public static VehicleInterface getPlayerVehicle(Player player) {
        for (VehicleInterface vehicle : getActiveVehicles()) {
            //RealisticVehicles.debugLog("Vehicle:"+vehicle);
            if (vehicle.getSeatedPlayer() == player) {
                return vehicle;
            }
        }
        return null;
    }
}
