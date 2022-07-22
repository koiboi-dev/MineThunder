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

    public Component(ComponentType type, double penDefence, double health, double damageHealth, double criticalHealth, boolean destroys, double x, double y, double z, double xsize, double ysize, double zsize, boolean centered, boolean upper,Particle damageParticle, Particle critParticle, Particle destParticle, boolean isAmmo) {
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
        this.isAmmo = isAmmo;
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
