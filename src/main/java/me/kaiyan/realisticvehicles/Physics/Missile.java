package me.kaiyan.realisticvehicles.Physics;

import me.kaiyan.realisticvehicles.DataTypes.Interfaces.FixedUpdate;
import me.kaiyan.realisticvehicles.Counters.Updates;
import me.kaiyan.realisticvehicles.DataTypes.*;
import me.kaiyan.realisticvehicles.DataTypes.Enums.TrackingType;
import me.kaiyan.realisticvehicles.DataTypes.Interfaces.RadarTarget;
import me.kaiyan.realisticvehicles.DataTypes.Interfaces.VehicleInterface;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.Objects;

public class Missile implements FixedUpdate {
    private final RadarTarget shooter;
    private Location loc;
    private float yaw;
    private float pitch;
    private float fuel;
    private final MissileSettings settings;
    private final ArmorStand stand;
    private final Player player;

    public Missile(Location loc, float yaw, float pitch, MissileSettings settings, ArmorStand stand, float speed, RadarTarget shooter, Player player) {
        this.loc = loc;
        this.yaw = yaw;
        this.pitch = pitch;
        this.settings = settings;
        fuel = settings.getStartFuel();
        this.stand = stand;
        this.shooter = shooter;
        this.player = player;

        this.speed = speed;
        start();
    }

    float speed;

    @Override
    public void OnFixedUpdate() {
        if (loc.getBlock().getType().isSolid()){
            explode();
            return;
        }

        if (settings.getType() == TrackingType.ACTIVE){
            double highestSig = 0;
            RadarTarget target = null;
            for (FixedUpdate update : Updates.fixedUpdates) {
                if (update instanceof RadarTarget inter) {
                    if (inter == shooter){
                        continue;
                    }
                    float angle = (float) (Math.toDegrees(Math.atan2(inter.getLoc().getX()-loc.getX(), inter.getLoc().getZ()-loc.getZ())));
                    if (Math.abs(getYawDifference(yaw, angle)) < getSettings().getPassiveScanAngle()) {
                        if (highestSig < inter.getLoc().distanceSquared(loc)){
                            target = inter;
                            highestSig = inter.getLoc().distanceSquared(loc);
                        }
                    }
                    if (loc.distanceSquared(inter.getLoc()) < 144){
                        explode();
                    }
                }
            }
            if (target != null) {
                calRotate(target);
            }
        } else if (settings.getType() == TrackingType.BOMB){
            loc.subtract(new Vector(0, 0.25f, 0));
            pitch += 1;
            pitch = Math.max(pitch, 90);
            speed += (pitch / 90) * 0.075;
        }

        loc.add(new Vector(0, 0, 1).rotateAroundX(Math.toRadians(pitch)).rotateAroundY(Math.toRadians(yaw)).multiply(getSettings().getSpeed()));

        fuel -= getSettings().getBurnRate();

        Objects.requireNonNull(loc.getWorld()).spawnParticle(Particle.REDSTONE, loc, 5, 1.5, 1.5, 1.5, 0.5, new Particle.DustOptions(Color.fromRGB(200, 200, 200), 8), true);

        loc.setYaw(yaw);
        stand.teleport(loc.clone().subtract(0, 1.9f, 0));
        stand.setHeadPose(new EulerAngle(Math.toRadians(pitch), 0, 0));
    }

    public void explode(){
        Objects.requireNonNull(loc.getWorld()).createExplosion(loc, settings.getPower());
        for (VehicleInterface veh : Updates.getActiveVehicles()){
            if (veh.getLoc().distanceSquared(loc) < getSettings().getPower()*getSettings().getPower()){
                ImpactOutData data = veh.getDamageModel().explosionImpact(settings.getPower(), getLoc().getX(), getLoc().getY(), getLoc().getZ(), veh.getLoc().getX(), veh.getLoc().getY(), veh.getLoc().getZ(), player);
                if (data.getPlayerDamage() >= veh.getSeatedPlayer().getHealth()){
                    Updates.expectedDeaths.put(veh.getSeatedPlayer(), new DeathMessage(veh.getSeatedPlayer().getName()+" was killed by a missile fired by "+player.getName()));
                }
            }
        }
        closeThis(0);
    }

    @Override
    public void closeThis(int actionType) {
        FixedUpdate.super.closeThis(actionType);
        stand.remove();
    }

    public void calRotate(RadarTarget target){
        float angle = (float) (Math.toDegrees(Math.atan2(target.getLoc().getX()-loc.getX(), target.getLoc().getZ()-loc.getZ())));

        yaw += Math.min(getYawDifference(yaw, angle)/10, 1)*settings.getTurnRate();

        if (target.getLoc().getY() < loc.getY()){
            pitch += settings.getTurnRate();
        } else if (target.getLoc().getY() > loc.getY()){
            pitch -= settings.getTurnRate();
        }
        if (target.getLoc().getY()+0.5 > loc.getY() && target.getLoc().getY()-0.5 < loc.getY()){
            pitch *= 0.5;
        }
    }

    public Location getLoc() {
        return loc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getFuel() {
        return fuel;
    }

    public void setFuel(float fuel) {
        this.fuel = fuel;
    }

    public MissileSettings getSettings() {
        return settings;
    }

    public static float getYawDifference(float start, float end){
        float result = (end - start) % 360;
        if(result < 0) result += 360;
        result -= (result > 180 ? 360 : 0);
        return result;
    }
}
