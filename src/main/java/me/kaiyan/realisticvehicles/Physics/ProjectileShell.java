package me.kaiyan.realisticvehicles.Physics;

import me.kaiyan.realisticvehicles.DataTypes.Interfaces.FixedUpdate;
import me.kaiyan.realisticvehicles.DamageModel.Projectiles.Shell;
import me.kaiyan.realisticvehicles.RealisticVehicles;
import me.kaiyan.realisticvehicles.VersionHandler.VersionHandler;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Objects;

public class ProjectileShell extends Shell implements FixedUpdate {
    public final Location loc;
    public Location prevLoc;
    public final Vector moveBy;
    public final Player player;

    public static final float gravity = 0.05f;

    /**
     * Creates a new projectile
     * @param loc Location of the shell
     * @param yaw Yaw to fire from
     * @param pitch Pitch to fire from
     * @param power Power to fire at (Speed)
     * @param tracer Red or White?
     * @param penScore Penetration score
     * @param shellDamage Damage Score
     * @param capped Capped?
     * @param sabot Sabot round?
     * @param explosive Explosive?
     * @param heat HEAT Round?
     * @param player Fired Player
     * @param mat Icon Mat
     * @param lore Lore
     * @param reloadTime Reload Time
     * @param cost Cost
     * @param weaknessDamage Damage to apply to plates
     * @param buyAmount Amount to buy
     */
    public ProjectileShell(Location loc, float yaw, float pitch, float power, boolean tracer, double penScore, double shellDamage, boolean capped, boolean sabot, boolean explosive, boolean heat, Player player, Material mat, List<String> lore, double reloadTime, double cost, double weaknessDamage, int buyAmount) {
        super(penScore, shellDamage, capped, sabot, explosive, heat, mat, lore, power, tracer, (float) reloadTime, (float) cost, (float) weaknessDamage, buyAmount);
        this.loc = loc;
        moveBy = new Vector(0, 0, 1).rotateAroundX(Math.toRadians(pitch)).rotateAroundY(-Math.toRadians(yaw)).multiply(power);
        this.tracer = tracer;
        this.player = player;

        start();
    }

    public ProjectileShell(Location loc, float yaw, float pitch, Player player, Shell shell, float speed) {
        super(shell.penScore, shell.shellDamage, shell.capped, shell.sabot, shell.explosive, shell.heat, shell.item.getType(), shell.shellLore, shell.power+speed, shell.tracer, shell.reloadTime, shell.cost, shell.weaknessDamage,shell.buyAmount);
        long startTime = System.currentTimeMillis();
        RealisticVehicles.debugLog("Creating Shell...");
        this.loc = loc;
        moveBy = new Vector(0, 0, 1).rotateAroundX(Math.toRadians(pitch)).rotateAroundY(-Math.toRadians(yaw)).multiply(power);
        this.player = player;

        start();
        RealisticVehicles.debugLog("Created Shell! " + (System.currentTimeMillis() - startTime));
    }

    int loops = 0;

    @Override
    public void OnFixedUpdate() {
        if (loops >= 60) {
            closeThis(0);
            return;
        }

        loc.add(moveBy);
        moveBy.subtract(new Vector(0, gravity, 0));


        if (tracer) {
            RealisticVehicles.spawnParticle(loc, new Particle.DustOptions(Color.RED, 6));
        } else {
            RealisticVehicles.spawnParticle(loc, new Particle.DustOptions(Color.GRAY, 3));
        }

        if (Objects.requireNonNull(loc.getWorld()).getBlockAt(loc).getType().isSolid()) {
            loc.getWorld().createExplosion(loc, 2, false, false);
            closeThis(0);
        }
        /*if(!loc.getChunk().isLoaded()) {
            closeThis();
        }*/

        prevLoc = loc;
        loops++;
    }

    //pitch = asin(-d.Y);
    //yaw = atan2(d.X, d.Z)

    /**
     * Calculates yaw of shell
     */
    public float getYaw(){
        Vector temp = moveBy.clone().normalize();
        return (float) -Math.toDegrees(Math.atan2(temp.getX(), temp.getZ()));
    }
    /**
     * Calculates pitch of shell
     */
    public float getPitch(){
        return (float) Math.toDegrees(Math.asin(-moveBy.clone().normalize().getY()));
    }
}
