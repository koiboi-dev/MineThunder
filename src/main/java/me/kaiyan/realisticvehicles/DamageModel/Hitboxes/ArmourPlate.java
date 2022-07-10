package me.kaiyan.realisticvehicles.DamageModel.Hitboxes;

import me.kaiyan.realisticvehicles.DamageModel.Dimensions.Rect;

public class ArmourPlate extends Rect implements Cloneable {
    public double penDef;
    public double weakness = 0;
    public double weaknessMult;

    public ArmourPlate(double penDefence,double x, double y, double z, double xsize, double ysize, double zsize, boolean centered, double weaknessMult) {
        super(x, y, z, xsize, ysize, zsize, centered);
        penDef = penDefence;
        this.weaknessMult = weaknessMult;
    }

    @Override
    public String toString() {
        return "ArmourPlate{" +
                "penDef=" + penDef +
                ", weakness=" + weakness +
                ", weaknessMult=" + weaknessMult +
                '}';
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
