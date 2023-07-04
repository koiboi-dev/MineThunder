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
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class GroundVehicleRevised {
    private Location loc;
    private double yaw = 0;
    private double pitch = 0;
    private double roll = 0;
    private double speed = 0;

    private final GroundVehicleSettings settings;

    private boolean onGround = true;
    private boolean hasFuel = true;

    private Entity baseSeat;

    public VehiclePacket vPacket;

    public boolean engineActive = true;

    public static List<Vector> contactPoints = new ArrayList<>();

    public GroundVehicleRevised(Location loc, GroundVehicleSettings settings){
        this.loc = loc;
        this.settings = settings;
        contactPoints = List.of(
                new Vector(2, 0, 2),
                new Vector(2, 0, 1),
                new Vector(2, 0, 0),
                new Vector(2, 0, -1),
                new Vector(2, 0, -2),
                new Vector(-2, 0, 2),
                new Vector(-2, 0, 1),
                new Vector(-2, 0, 0),
                new Vector(-2, 0, -1),
                new Vector(-2, 0, -2)
        );
    }

    public void updateSeat(Entity seat){
        baseSeat = seat;
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
                    if (baseSeat.getPassengers().size() != 0 && engineActive && hasFuel) {
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

        for (Vector vec : contactPoints){
            loc.getWorld().spawnParticle(Particle.REDSTONE, loc.add(vec.rotateAroundZ(Math.toRadians(pitch)).rotateAroundX(Math.toRadians(roll)).rotateAroundY(Math.toRadians(yaw))), 1);
        }

    }

    public void pushAgainstHit(Vector dir, RayTraceResult motionCheck){
        loc.subtract(
                dir.clone().multiply(settings.getLength() - loc.distance(motionCheck.getHitPosition().toLocation(baseSeat.getWorld())))
        );
    }

    public void steer(double amount){
        if (settings.getSteerType() == GroundVehicle.SteerType.REGULAR){
            if ((speed > 0.1 || speed < -0.1) && onGround) {
                speed -= settings.getTurnDeceleration() * Math.abs(amount);
                if (speed < -0.1){
                    yaw += settings.getTurnSpeed() * amount * (getSpeed()/settings.getMaxSpeed());
                } else {
                    yaw -= settings.getTurnSpeed() * amount * (getSpeed()/settings.getMaxSpeed());
                }
            }
        } else if (settings.getSteerType() == GroundVehicle.SteerType.TANK){
            if ((speed > 0.1 || speed < -0.1) && onGround) {
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

    public GroundVehicle.SteerType getSteerType() {
        return settings.getSteerType();
    }

    public Traversable getTraversable() {
        return settings.getTraversable();
    }

    public Entity getBaseSeat() {
        return baseSeat;
    }

    public void setYaw(float yaw){
        this.yaw = yaw;
    }
}
