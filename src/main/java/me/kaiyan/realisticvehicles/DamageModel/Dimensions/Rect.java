package me.kaiyan.realisticvehicles.DamageModel.Dimensions;

import java.util.Random;
import java.util.Vector;

public class Rect implements Cloneable{
    public final double x;
    public final double y;
    public final double z;

    public final double xsize;
    public final double ysize;
    public final double zsize;

    public Rect(double x, double y, double z, double xsize, double ysize, double zsize, boolean centered){
        if (!centered) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.xsize = xsize;
            this.ysize = ysize;
            this.zsize = zsize;
        } else {
            this.x = x-xsize/2;
            this.y = y-ysize/2;
            this.z = z-zsize/2;
            this.xsize = xsize;
            this.ysize = ysize;
            this.zsize = zsize;
        }
    }

    public boolean isPointInsideBox(double px, double py, double pz){
        return (px >= x && px <= xsize + x) && (py >= y && py <= ysize + y) && (pz >= z && pz <= zsize + z);
    }

    public VectorD getRandomPoint(){
        return getRandomPoint(new Random());
    }

    public VectorD getRandomPoint(Random rand){
        double rx = x+rand.nextDouble()*xsize;
        double ry = y+rand.nextDouble()*ysize;
        double rz = z+rand.nextDouble()*zsize;
        return new VectorD(rx, ry, rz);
    }

    @Override
    public Rect clone() {
        try {
            return (Rect) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
