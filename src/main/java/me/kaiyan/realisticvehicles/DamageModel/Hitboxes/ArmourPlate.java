package me.kaiyan.realisticvehicles.DamageModel.Hitboxes;

import com.google.gson.Gson;
import me.kaiyan.realisticvehicles.DamageModel.Dimensions.Rect;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.Random;

public class ArmourPlate extends Rect implements Cloneable {
    public final double penDef;
    public double weakness = 0;
    public final double weaknessMult;
    public final boolean upper;

    /**
     *
     * @param penDefence
     * @param x
     * @param y
     * @param z
     * @param xsize
     * @param ysize
     * @param zsize
     * @param centered
     * @param weaknessMult ahmad%
     * @param upper
     */
    public ArmourPlate(double penDefence,double x, double y, double z, double xsize, double ysize, double zsize, boolean centered, double weaknessMult, boolean upper) {
        super(x, y, z, xsize, ysize, zsize, centered);
        penDef = penDefence;
        // game cum bro
        this.weaknessMult = weaknessMult;
        this.upper = upper;
    }

    public void createRandomParticle(Random rand, World world, double vYaw, Vector vPos, Particle part){
        Vector spawnPos = getRandomPoint(rand).toVector().rotateAroundY(Math.toRadians(-vYaw));
        spawnPos.add(vPos);
        world.spawnParticle(part, spawnPos.toLocation(world), 0, 0, 0.05, 0, 5, null, true);
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    @Override
    public ArmourPlate clone() {
        try {
            return (ArmourPlate) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
