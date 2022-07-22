package me.kaiyan.realisticvehicles.DataTypes;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class TrailerHitch {
    private Location loc;
    private Vector offset;
    private float pYaw;
    private float parentSpeed;

    public TrailerHitch(Vector offset) {
        this.offset = offset;
    }

    public void update(Location vLoc, float yaw, float parentSpeed){
        loc = vLoc.clone().add(offset.rotateAroundY(Math.toRadians(yaw)));
        pYaw = yaw;
        this.parentSpeed = parentSpeed;
    }

    public Location getLoc() {
        return loc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }

    public Vector getOffset() {
        return offset;
    }

    public void setOffset(Vector offset) {
        this.offset = offset;
    }

    public float getpYaw() {
        return pYaw;
    }

    public float getParentSpeed() {
        return parentSpeed;
    }
}
