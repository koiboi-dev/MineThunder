package me.kaiyan.realisticvehicles.DamageModel.Hitboxes;

import com.google.gson.Gson;
import me.kaiyan.realisticvehicles.DamageModel.Dimensions.Rect;
import me.kaiyan.realisticvehicles.DataTypes.Enums.ComponentType;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.*;

public class Component extends Rect implements Cloneable{
    public final double penDef;
    public double health;
    public final double maxHealth;
    public final double damageHealth;
    public final double criticalHealth;
    public boolean destroyed;
    public final boolean destroys;
    public ComponentType type;

    public final boolean upper;

    final Particle damParticle;
    final Particle crtParticle;
    final Particle desParticle;

    public final boolean isAmmo;

    /**
     * Creates a new component object
     * @param type Type of comp
     * @param penDefence Difficulty for shells to pass through
     * @param health Health
     * @param damageHealth Bar where it plays damageParticle
     * @param criticalHealth Bar where it plays criticalParticle
     * @param destroys Destroys vehicle on break
     * @param x x pos
     * @param y y pos
     * @param z z pos
     * @param xsize x size
     * @param ysize y size
     * @param zsize z size
     * @param centered apply size from corner or center
     * @param upper in the upper part of the damage model (turret)
     * @param damageParticle particle to play when damaged
     * @param critParticle particle to play when critical
     * @param destParticle particle to play when destroyed
     */
    public Component(ComponentType type, double penDefence, double health, double damageHealth, double criticalHealth, boolean destroys, double x, double y, double z, double xsize, double ysize, double zsize, boolean centered, boolean upper,Particle damageParticle, Particle critParticle, Particle destParticle) {
        super(x, y, z, xsize, ysize, zsize, centered);
        penDef = penDefence;
        this.health = health;
        this.type = type;
        this.damageHealth = damageHealth;
        this.criticalHealth = criticalHealth;
        this.destroys = destroys;
        this.damParticle = damageParticle;
        this.crtParticle = critParticle;
        this.desParticle = destParticle;
        this.upper = upper;
        this.isAmmo = type == ComponentType.AMMOSTOWAGE;
        maxHealth = health;
    }

    public void spawnDamageParticle(World world, Random rand, double vYaw, Vector vPos){
        if (health < damageHealth){
            createRandomParticle(rand, world, vYaw, vPos, damParticle);
        }
        if (health < criticalHealth){
            createRandomParticle(rand, world, vYaw, vPos, crtParticle);
        }
        if (health <= 0){
            createRandomParticle(rand, world, vYaw, vPos, desParticle);
        }
    }

    public void createRandomParticle(Random rand, World world, double vYaw, Vector vPos, Particle part){
        Vector spawnPos = getRandomPoint(rand).toVector().rotateAroundY(Math.toRadians(-vYaw));
        spawnPos.add(vPos);
        world.spawnParticle(part, spawnPos.toLocation(world), 0, 0, 0.05, 0, 5, null, true);
    }

    @Override
    public Component clone() {
        try {
            Component clone = (Component) super.clone();
            clone.type = type;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public Vector getCornerVector(){
        return new Vector(x,y,z);
    }
    public Vector getCenterVector(){
        return new Vector(x+xsize/2,y+ysize/2,z+zsize/2);
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
