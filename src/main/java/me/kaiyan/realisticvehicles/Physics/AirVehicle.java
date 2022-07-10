package me.kaiyan.realisticvehicles.Physics;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.kaiyan.realisticvehicles.RealisticVehicles;
import me.kaiyan.realisticvehicles.Vehicles.Settings.AirVehicles.AirVehicleSettings;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Random;

public class AirVehicle {
    private Location loc;
    private final World world;

    private double pitch = 0;
    private double yaw = 0;
    private double roll = 0;

    private Entity seatEnt;

    // In m/t
    protected int speed = 0;

    public final AirVehicleSettings settings;

    public AirVehicle(Location loc, AirVehicleSettings settings) {
        this.settings = settings;
        world = loc.getWorld();
        this.loc = loc;
    }

    private boolean acceling = false;
    public void setup(Entity seatEnt, @Nullable GroundVehicle.VehiclePacket packetSender){
        this.seatEnt = seatEnt;
        if (packetSender == null) {
            RealisticVehicles.protocolManager.addPacketListener(new PacketAdapter(RealisticVehicles.getInstance(),
                    ListenerPriority.HIGH,
                    PacketType.Play.Client.STEER_VEHICLE) {
                @Override
                public void onPacketReceiving(PacketEvent event) {
                    if (seatEnt.getPassengers().size() != 0) {
                        if (seatEnt.getPassengers().get(0) == event.getPlayer()) {
                            acceling = false;
                            if (event.getPacket().getFloat().getValues().get(1) > 0) {
                                accelerate(settings.getEnginePower());
                                acceling = true;
                            } else if (event.getPacket().getFloat().getValues().get(1) < 0) {
                                accelerate(-settings.getEnginePower());
                                acceling = true;
                            }
                            if (event.getPacket().getFloat().getValues().get(0) > 0) {
                                yaw -= 1;
                            } else if (event.getPacket().getFloat().getValues().get(0) < 0) {
                                yaw += 1;
                            }
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
                    packetSender.OnPacketGet(event);
                    if (seatEnt.getPassengers().size() != 0) {
                        if (seatEnt.getPassengers().get(0) == event.getPlayer()) {
                            acceling = false;
                            if (event.getPacket().getFloat().getValues().get(1) > 0) {
                                accelerate(settings.getEnginePower());
                                acceling = true;
                            } else if (event.getPacket().getFloat().getValues().get(1) < 0) {
                                accelerate(-settings.getEnginePower());
                                acceling = true;
                            }
                            if (event.getPacket().getFloat().getValues().get(0) > 0) {
                                yaw -= Math.min(1, getSpeed());
                            } else if (event.getPacket().getFloat().getValues().get(0) < 0) {
                                yaw += Math.min(1, getSpeed());
                            }
                        }
                    }
                }
            });
        }
    }

    public void accelerate(double speed){
        addSpeed(speed);
    }
    double yawVelo = 0;
    public void calRotate(Entity seated){
        calRotate(seated, 1, false);
    }
    public void calRotate(Entity seated, boolean skipRoll){
        calRotate(seated, 1, skipRoll);
    }
    public void calRotate(Entity seated, float mult, boolean skipRoll){
        double pYaw = seated.getLocation().getYaw();
        double pPitch = seated.getLocation().getPitch();

        double mod = Math.min(getSpeed()/settings.getFullYawSpeed(), 1);
        mod = Math.max(-0.1,mod);

        if (collided)
            return;

        if (yaw > pYaw) {
            if (yaw > 90 && pYaw < -90) {
                yawVelo += settings.getYawSpeed()*mod;
                //yaw += settings.getYawSpeed()*mod;
                //roll = -currentRoll;
            } else {
                yawVelo -= settings.getYawSpeed()*mod;
                //yaw -= settings.getYawSpeed()*mod;
                //roll = currentRoll;
            }
        } else if (yaw < pYaw) {
            if (yaw < -90 && pYaw > 90) {
                yawVelo -= settings.getYawSpeed()*mod;
                //yaw -= settings.getYawSpeed()*mod;
                //roll = currentRoll;
            } else {
                yawVelo += settings.getYawSpeed()*mod;
                //yaw += settings.getYawSpeed()*mod;
                //roll = -currentRoll;
            }
        }

        yawVelo = Math.min(settings.getMaxYawSpeed(), Math.max(-settings.getMaxYawSpeed(), yawVelo));
        //roll = Math.min(15, Math.max(-15, roll));
        yawVelo *= mult;

        yaw += yawVelo;

        if (yaw < pYaw + settings.getYawSpeed()+0.5 && yaw > pYaw - settings.getYawSpeed()+0.5) {
            yaw = pYaw;
            if (roll < 1 && roll > -1) {
                roll = 0;
            } else {
                roll *= 0.75;
            }
            yawVelo = 0;
        } else if (!skipRoll){
            roll = (yawVelo/settings.getMaxYawSpeed())*20;
        }

        mod = Math.min(getSpeed()/settings.getFullPitchSpeed(), 1);

        if (pPitch > pitch){
            pitch += settings.getPitchSpeed()*mod*mult;
        } else if (pPitch < pitch){
            pitch -= settings.getPitchSpeed()*mod*mult;
        }

        if (pitch < pPitch + settings.getPitchSpeed()+0.5 && pitch > pPitch - settings.getYawSpeed()+0.5){
            pitch = pPitch;
        }

        if (yaw > 180)
            yaw = -180;
        else if (yaw < -180)
            yaw = 180;
    }

    private float crashingHealth;
    boolean stalling = false;
    Random rand = new Random();
    public void update(){
        if (getSpeed() > 0) {
            addSpeed(-((getSpeed() * getSpeed()) * settings.getDragCoefficient()));
        }
        //System.out.println(pitch+" : "+yaw);

        stalling = false;

        //Crashing move system
        if (crashing){
            addSpeed((pitch / 90) * 0.075);

            roll -= (rand.nextFloat()-0.5)*3;

            if (roll > 0){
                yaw += rand.nextFloat()*3;
            } else if (roll != 0) {
                yaw -= rand.nextFloat()*3;
            }
            pitch += (Math.abs(roll/90)*2);

            Vector moveBy = new Vector(0, 0, 1);
            moveBy.rotateAroundX(Math.toRadians(pitch));
            moveBy.rotateAroundY(-Math.toRadians(yaw));
            moveBy.multiply(getSpeed());
            moveBy.subtract(new Vector(0, 0.1, 0));

            loc.add(moveBy);

            if (crashingHealth <= 0){
                crashed = true;
            }
            if (seatEnt.getPassengers().size() != 0){
                Entity seated = seatEnt.getPassengers().get(0);
                double pYaw = seated.getLocation().getYaw();
                if (yaw > pYaw) {
                    if (yaw > 90 && pYaw < -90) {
                        roll += 1;
                    } else {
                        roll -= 1;
                    }
                } else if (yaw < pYaw) {
                    if (yaw < -90 && pYaw > 90) {
                        roll -= 1;
                    } else {
                        roll += 1;
                    }
                }
            }
            return;
        }

        if (!collided) {
            addSpeed((pitch / 90) * 0.075);
            float yFall = (float) (0.75f-((getSpeed()/settings.getLiftSpeed()*0.6)));
            yFall = Math.max(0, yFall);

            if (getSpeed() < settings.getStallSpeed() && pitch < 80) {
                yFall *= 1.25;
                pitch += ((settings.getStallSpeed() - getSpeed()) / settings.getStallSpeed()) * 4;
                stalling = true;
            }
            loc.subtract(new Vector(0, yFall, 0));
        } else {
            if (getSpeed() > settings.getLiftSpeed()){
                pitch -= 1;
            } else {
                if (pitch < -1){
                    pitch += 5;
                } else {
                    pitch = 0;
                }
            }
        }

        Vector moveBy = new Vector(0, 0, 1);
        moveBy.rotateAroundX(Math.toRadians(pitch));
        moveBy.rotateAroundY(-Math.toRadians(yaw));
        moveBy.multiply(getSpeed());

        ySpeed = moveBy.getY();

        loc.add(moveBy);

        if (seatEnt.getPassengers().size() != 0){
            Entity seated = seatEnt.getPassengers().get(0);
            if (!stalling) calRotate(seated);
        }

        collide();
    }

    double ySpeed = 0;
    
    private boolean collided = false;
    private boolean crashing = false;
    private boolean crashed = false;
    public void collide(){
        collided = false;
        RayTraceResult hit = world.rayTraceBlocks(loc.clone().add(new Vector(0, 1.25, 0)), new Vector(0, -1, 0), 1.5);
        if (hit != null){
            //On impact code.
            if (Objects.requireNonNull(hit.getHitBlock()).getType().isSolid()){
                loc = hit.getHitPosition().toLocation(world);
                collided = true;
                //TODO damage landing gear on harsh roll at impact
                if (ySpeed > 0.3){
                    startCrashing();
                }
                roll = 0;
                //Severely damage gear
                //else if (ySpeed > 0.15){
            }
        }
    }

    public void startCrashing(){
        crashing = true;
        crashingHealth = (float) (getSpeed()*1.25);
    }

    public Location getLoc() {
        return loc;
    }

    public double getPitch() {
        return pitch;
    }
    public void addPitch(float amount){
        pitch -= amount;
    }

    public double getYaw() {
        return yaw;
    }

    public double getRoll() {
        return roll;
    }
    public void multiplyRoll(float mult){
        roll *= mult;
    }

    public World getWorld() {
        return world;
    }

    public double getSpeed() {
        return speed/1000f;
    }

    public void addSpeed(double add){
        speed += add*1000;
    }

    public boolean isCrashing() {
        return crashing;
    }

    public void setCrashing(boolean crashing) {
        this.crashing = crashing;
    }

    public boolean isAcceling() {
        return acceling;
    }

    public void setAcceling(boolean acceling) {
        this.acceling = acceling;
    }

    public float getCrashingHealth() {
        return crashingHealth;
    }

    public void setCrashingHealth(float crashingHealth) {
        this.crashingHealth = crashingHealth;
    }

    public void removeCrashingHealth(float amt){
        this.crashingHealth -= amt;
    }

    public boolean isCollided() {
        return collided;
    }

    public void setCollided(boolean collided) {
        this.collided = collided;
    }

    public boolean isCrashed() {
        return crashed;
    }

    public void setCrashed(boolean crashed) {
        this.crashed = crashed;
    }
}
