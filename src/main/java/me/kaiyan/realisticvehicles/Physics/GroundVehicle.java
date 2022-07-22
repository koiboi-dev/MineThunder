package me.kaiyan.realisticvehicles.Physics;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.kaiyan.realisticvehicles.DataTypes.Enums.Traversable;
import me.kaiyan.realisticvehicles.RealisticVehicles;
import me.kaiyan.realisticvehicles.Vehicles.Settings.GroundVehicles.GroundVehicleSettings;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.Objects;


public class GroundVehicle {
    private Location loc;
    private double yaw = 0;
    private double speed = 0;

    private final GroundVehicleSettings settings;

    private boolean onGround = true;
    private boolean hasFuel = true;

    private Entity baseSeat;

    public VehiclePacket vPacket;

    public boolean engineActive = true;

    public GroundVehicle(Location loc, GroundVehicleSettings settings){
        this.loc = loc;
        this.settings = settings;
    }

    public void setup(Entity seat, @Nullable VehiclePacket packetSender){
        baseSeat = seat;
        if (packetSender == null) {
            RealisticVehicles.protocolManager.addPacketListener(new PacketAdapter(RealisticVehicles.getInstance(),
                    ListenerPriority.HIGH,
                    PacketType.Play.Client.STEER_VEHICLE) {
                @Override
                public void onPacketReceiving(PacketEvent event) {
                    if (baseSeat.getPassengers().size() != 0 && engineActive && hasFuel) {
                        if (event.getPlayer() == baseSeat.getPassengers().get(0)) {
                            steer(event.getPacket().getFloat().read(0));
                            getPedal(event.getPacket().getFloat().read(1));
                        }
                    }
                }
            });
        } else {
            RealisticVehicles.protocolManager.addPacketListener(new PacketAdapter(RealisticVehicles.getInstance(),
                    ListenerPriority.HIGH,
                    PacketType.Play.Client.STEER_VEHICLE) {
                @Override
                public void onPacketReceiving(PacketEvent event) {
                    if (baseSeat.getPassengers().size() != 0 && engineActive) {
                        if (event.getPlayer() == baseSeat.getPassengers().get(0)) {
                            steer(event.getPacket().getFloat().read(0));
                            getPedal(event.getPacket().getFloat().read(1));
                            packetSender.OnPacketGet(event);
                        }
                    }
                }
            });
        }
    }

    public void update(){
        if (speed > 0) {
            speed -= settings.getDrag();
        } else if (speed < 0){
            speed += settings.getDrag();
        }
        if (speed < settings.getDrag() && speed > -settings.getDrag()){
            speed = 0;
        }
        if (yaw > 180){
            yaw = -180;
        } else if (yaw < -180){
            yaw = 180;
        }

        loc.add(new Vector(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw))).normalize().multiply(speed));

        int loops = 0;
        // Old Hit Code? Maybe...
        /*Vector[] checks = new Vector[] {
                new Vector(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw))),
                new Vector(-Math.sin(Math.toRadians(yaw+90)), 0, Math.cos(Math.toRadians(yaw+90))),
                new Vector(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw))),
                new Vector(-Math.sin(Math.toRadians(yaw-90)), 0, Math.cos(Math.toRadians(yaw-90)))
        };
        Vector sideVect = new Vector(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw))).multiply(settings.getLength()*0.8);

        for (Vector vec : checks){
            RayTraceResult hit;
            if (loops == 1 || loops == 3) {
                hit = base.getWorld().rayTraceBlocks(loc.clone().add(sideVect), vec, settings.getWidth(), FluidCollisionMode.NEVER, true);
            } else {
                hit = base.getWorld().rayTraceBlocks(loc, vec, settings.getLength(), FluidCollisionMode.NEVER, true);
            }
            if (hit != null && hit.getHitBlock() != null){
                if (hit.getHitBlock().getRelative(BlockFace.UP).getType() != Material.AIR && hit.getHitBlock().getRelative(BlockFace.UP).getType().isSolid()) {
                    loc.subtract(vec.multiply(settings.getWidth() - loc.clone().add(sideVect).toVector().distance(hit.getHitPosition())));
                    if (loops == 0){
                        speed = 0;
                    }
                }
            }
            loops++;
        }*/

        Vector dir;
        if (speed >= 0){
            dir = new Vector(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)));
        } else {
            dir = new Vector(-Math.sin(Math.toRadians(yaw+180)), 0, Math.cos(Math.toRadians(yaw+180)));
        }
        RayTraceResult motionCheck = Objects.requireNonNull(loc.getWorld()).rayTraceBlocks(loc, dir, settings.getLength(), FluidCollisionMode.NEVER);

        if (motionCheck != null && motionCheck.getHitBlock() != null) {
            if (getTraversable() == Traversable.BLOCK && (motionCheck.getHitBlock().getRelative(BlockFace.UP).getType().isSolid())) {
                pushAgainstHit(dir, motionCheck);
            } else if (getTraversable() == Traversable.SLAB && (!motionCheck.getHitBlock().getType().toString().contains("SLAB") || motionCheck.getHitBlock().getRelative(BlockFace.UP).getType().isSolid())){
                pushAgainstHit(dir, motionCheck);
            } else if (getTraversable() == Traversable.NONE){
                pushAgainstHit(dir, motionCheck);
            }
        }

        RayTraceResult gravhit = Objects.requireNonNull(loc.getWorld()).rayTraceBlocks(loc.clone().add(new Vector(0, 1, 0)), new Vector(0, -1, 0), 2, FluidCollisionMode.NEVER, true);

        if (gravhit != null && gravhit.getHitBlock() != null) {
            loc.setY(gravhit.getHitPosition().getY());
            onGround = true;
        } else {
            loc.add(new Vector(0, -0.5, 0));
            onGround = false;
        }
    }

    public void pushAgainstHit(Vector dir, RayTraceResult motionCheck){
        loc.subtract(dir.clone().multiply(settings.getLength() - loc.distance(motionCheck.getHitPosition().toLocation(baseSeat.getWorld()))));
    }

    public void steer(double amount){
        if (settings.getSteerType() == SteerType.REGULAR){
            if (speed > 0.1 && onGround) {
                speed -= settings.getTurnDeceleration() * Math.abs(amount);
                yaw -= settings.getTurnSpeed() * amount;
            }
        } else if (settings.getSteerType() == SteerType.TANK){
            if (speed > 0.1 && onGround) {
                speed -= settings.getTurnDeceleration() * Math.abs(amount);
                yaw -= settings.getTurnSpeed() * amount;
            } else if (onGround){
                yaw -= (settings.getTurnSpeed()/2) * amount;
            }
        }
    }

    public void getPedal(float amount){
        if (amount > 0){
            accelerate(amount);
        } else {
            if (speed > settings.getDrag()) {
                brake(amount);
            } else {
                reverse(amount);
            }
        }
    }

    public void accelerate(float amount){
        if (speed < settings.getMaxSpeed() && onGround) {
            speed += amount * settings.getAcceleration();
        }
    }

    public void brake(float amount){
        if (speed > 0 && onGround) {
            speed += amount * settings.getBrakeForce();
        }
    }

    public void reverse(float amount){
        if (speed > -settings.getReverseMax()){
            speed += amount * settings.getReverseAccel();
        }
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }

    public enum SteerType {
        REGULAR,
        TANK
    }

    public interface VehiclePacket{
        void OnPacketGet(PacketEvent event);
    }

    public Location getLoc() {
        return loc;
    }

    public double getYaw() {
        return yaw;
    }

    public double getSpeed() {
        return speed;
    }

    public GroundVehicleSettings getSettings() {
        return settings;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public boolean isHasFuel() {
        return hasFuel;
    }

    public void setHasFuel(boolean hasFuel) {
        this.hasFuel = hasFuel;
    }

    public SteerType getSteerType() {
        return settings.getSteerType();
    }

    public Traversable getTraversable() {
        return settings.getTraversable();
    }

    public Entity getBaseSeat() {
        return baseSeat;
    }
}
