package me.kaiyan.realisticvehicles.DamageModel.Dimensions;

import org.bukkit.util.Vector;

public class VectorD implements Cloneable{
    public double x;
    public double y;
    public double z;

    public VectorD(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public VectorD add(double xt, double yt, double zt){
        x+=xt;
        y+=yt;
        z+=zt;
        return this;
    }

    public VectorD add(VectorD v){
        x+=v.x;
        y+=v.y;
        z+=v.z;
        return this;
    }

    public VectorD subtract(double xt, double yt, double zt){
        x-=xt;
        y-=yt;
        z-=zt;
        return this;
    }

    public VectorD subtract(VectorD v){
        x-=v.x;
        y-=v.y;
        z-=v.z;
        return this;
    }

    public VectorD multiply(double mult){
        x*=mult;
        y*=mult;
        z*=mult;
        return this;
    }

    public VectorD multiply(VectorD v){
        x*=v.x;
        y*=v.y;
        z*=v.z;
        return this;
    }

    public VectorD divide(double mult){
        x/=mult;
        y/=mult;
        z/=mult;
        return this;
    }

    public VectorD divide(VectorD v){
        x/=v.x;
        y/=v.y;
        z/=v.z;
        return this;
    }

    public double length(){
        return Math.sqrt((x*x)+(y*y)+(z*z));
    }

    public VectorD normalize(){
        divide(length());
        return this;
    }

    public VectorD clone(){
        VectorD clone = null;
        try {
            clone = (VectorD) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return clone;
    }

    public Vector toVector(){
        return new Vector(x, y, z);
    }

    public String toString(){
        return x+","+y+","+z;
    }
}
