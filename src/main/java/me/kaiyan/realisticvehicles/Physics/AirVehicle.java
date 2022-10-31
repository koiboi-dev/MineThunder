package me.kaiyan.realisticvehicles.Physics;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.kaiyan.realisticvehicles.DamageModel.DamageModel;
import me.kaiyan.realisticvehicles.DamageModel.Hitboxes.Component;
import me.kaiyan.realisticvehicles.DataTypes.Enums.ComponentType;
import me.kaiyan.realisticvehicles.DataTypes.Exceptions.InvalidTypeException;
import me.kaiyan.realisticvehicles.RealisticVehicles;
import me.kaiyan.realisticvehicles.Vehicles.Settings.AirVehicles.AirVehicleSettings;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.List;
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

    private DamageModel damageModel;

    public final AirVehicleSettings settings;
    private boolean hasFuel;

    public AirVehicle(Location loc, AirVehicleSettings settings) throws InvalidTypeException {
        if (settings == null){
            throw new InvalidTypeException("AIR");
        }
        this.settings = settings;
        damageModel = settings.getDamageModel().clone();
        world = loc.getWorld();
        this.loc = loc;
    }

    private boolean acceling = false;
    public void setup(Entity seatEnt, @Nullable GroundVehicle.VehiclePacket packetSender){
        this.seatEnt = seatEnt;
        if (packetSender == null) {
            RealisticVehicles.protocolManager.addPacketListener(new PacketAdapter(RealisticVehicles.getInstance(),
                    ListenerPriority.MONITOR,
                    PacketType.Play.Client.STEER_VEHICLE) {
                @Override
                public void onPacketReceiving(PacketEvent event) {
                    if (seatEnt.getPassengers().size() != 0) {
                        if (seatEnt.getPassengers().get(0) == event.getPlayer()) {
                            acceling = false;
                            if (event.getPacket().getFloat().getValues().get(1) > 0 && hasFuel) {
                                accelerate(settings.getEnginePower());
                                acceling = true;
                            } else if (event.getPacket().getFloat().getValues().get(1) < 0) {
                                accelerate(-settings.getEnginePower()/4);
                                acceling = true;
                            }
                            if (event.getPacket().getFloat().getValues().get(0) > 0) {
                                yaw -= Math.min(1, getSpeed())*damageModel.getComponentDamagePercent(ComponentType.RUDDER);
                            } else if (event.getPacket().getFloat().getValues().get(0) < 0) {
                                yaw += Math.min(1, getSpeed())*damageModel.getComponentDamagePercent(ComponentType.RUDDER);
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
                            if (event.getPacket().getFloat().getValues().get(1) > 0 && hasFuel) {
                                accelerate(settings.getEnginePower());
                                acceling = true;
                            } else if (event.getPacket().getFloat().getValues().get(1) < 0) {
                                accelerate(-settings.getEnginePower()*0.25);
                                acceling = true;
                            }
                            if (event.getPacket().getFloat().getValues().get(0) > 0) {
                                yaw -= Math.min(1, getSpeed())*damageModel.getComponentDamagePercent(ComponentType.RUDDER);
                            } else if (event.getPacket().getFloat().getValues().get(0) < 0) {
                                yaw += Math.min(1, getSpeed())*damageModel.getComponentDamagePercent(ComponentType.RUDDER);
                            }
                        }
                    }
                }
            });
        }
    }

    public void updateSeat(ArmorStand seat){
        seatEnt = seat;
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
                yawVelo += settings.getYawSpeed() * damageModel.getComponentDamagePercent(ComponentType.AILERON);
                //yaw += settings.getYawSpeed()*mod;
                //roll = -currentRoll;
            } else {
                yawVelo -= settings.getYawSpeed() * damageModel.getComponentDamagePercent(ComponentType.AILERON);
                //yaw -= settings.getYawSpeed()*mod;
                //roll = currentRoll;
            }
        } else if (yaw < pYaw) {
            if (yaw < -90 && pYaw > 90) {
                yawVelo -= settings.getYawSpeed() * damageModel.getComponentDamagePercent(ComponentType.AILERON);
                //yaw -= settings.getYawSpeed()*mod;
                //roll = currentRoll;
            } else {
                yawVelo += settings.getYawSpeed() * damageModel.getComponentDamagePercent(ComponentType.AILERON);
                //yaw += settings.getYawSpeed()*mod;
                //roll = -currentRoll;
            }
        }

        yawVelo = Math.min(settings.getMaxYawSpeed(), Math.max(-settings.getMaxYawSpeed(), yawVelo))*mod*damageModel.getComponentDamagePercent(ComponentType.AILERON);
        //roll = Math.min(15, Math.max(-15, roll));
        yawVelo *= mult;

        yaw += yawVelo;

        if (yaw < pYaw + settings.getMaxYawSpeed()+0.5 && yaw > pYaw - settings.getMaxYawSpeed()+0.5) {
            yaw = pYaw;
            if (roll < 1 && roll > -1) {
                roll = 0;
            } else {
                roll *= 0.75;
            }
            yawVelo = 0;
        } else if (!skipRoll){
            roll = (yawVelo/settings.getFullYawSpeed())*20;
        }

        mod = Math.min(getSpeed()/settings.getFullPitchSpeed(), 1);

        float elevatorHealth = damageModel.getComponentDamagePercent(ComponentType.ELEVATOR);
        if (pPitch > pitch){
            pitch += settings.getPitchSpeed()*mod*mult*elevatorHealth;
        } else if (pPitch < pitch){
            pitch -= settings.getPitchSpeed()*mod*mult*elevatorHealth;
        }
        if (elevatorHealth < 0.5){
            pitch += 0.6*((Math.random()-0.5)*2);
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
    public void update(boolean extendedGear){
        if (getSpeed() > 0) {
            addSpeed(-((getSpeed() * getSpeed()) * settings.getDragCoefficient()));
        }
        //System.out.println(pitch+" : "+yaw);

        stalling = false;

        //Crashing move system
        if (crashing){
            addSpeed((pitch / 90) * 0.15);

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
            float yFall = (float) (0.75f-((getSpeed()/settings.getLiftSpeed()*0.6)));
            System.out.println(damageModel.getComponentDamagePercent(ComponentType.WING));
            yFall /= damageModel.getComponentDamagePercent(ComponentType.WING);
            yFall = Math.max(0, yFall);

            moveBy.subtract(new Vector(0, yFall, 0));

            if (!getWorld().isChunkLoaded((int) Math.floor(loc.clone().add(moveBy).getX()/16), (int) Math.floor(loc.clone().add(moveBy).getZ()/16))){
                return;
            }

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
            addSpeed((pitch / 90) * 0.25);

            float yFall = (float) (0.75f-((getSpeed()/settings.getLiftSpeed()*0.6)));
            yFall /= damageModel.getComponentDamagePercent(ComponentType.WING);

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
        if (extendedGear) {
            collide();
        }
    }

    protected boolean invulnerable = true;

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
                if (ySpeed >= 0.5){
                    for (Component comp : damageModel.getComponents()) {
                        if (comp.type == ComponentType.LANDINGGEAR){
                            comp.health -= ySpeed*2;
                        }
                    }
                }
                System.out.println(damageModel.getComponentActivePercent(ComponentType.LANDINGGEAR));
                if (damageModel.getComponentActivePercent(ComponentType.LANDINGGEAR) <= 0 && !invulnerable){
                    startCrashing();
                }
                if (roll > 1){
                    for (Integer anInt : damageModel.getComponentIndices(ComponentType.LANDINGGEAR)) {
                        Component comp = damageModel.getComponents().get(anInt);
                        comp.health -= (1 - (roll / 45)) * 0.1;
                        if (comp.health <= 0){
                            comp.destroyed = true;
                        }
                    }
                }
                if (ySpeed < -0.2 && !invulnerable){
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

    public float getRoll() {
        return (float) roll;
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

    public DamageModel getDamageModel() {
        return damageModel;
    }

    public void setDamageModel(DamageModel damageModel) {
        this.damageModel = damageModel;
    }

    public boolean isHasFuel() {
        return hasFuel;
    }

    public void setHasFuel(boolean hasFuel) {
        this.hasFuel = hasFuel;
    }

    public void setYaw(float yaw){
        this.yaw = yaw;
    }

    public AirVehicleSettings getSettings() {
        return settings;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }
}
