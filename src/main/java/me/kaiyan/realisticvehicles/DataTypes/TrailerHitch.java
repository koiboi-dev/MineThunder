package me.kaiyan.realisticvehicles.DataTypes;

import me.kaiyan.realisticvehicles.DataTypes.Enums.TrailerTypes;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class TrailerHitch {
    private Location loc;
    private Vector offset;
    private final TrailerTypes type;
    private boolean activated;

    public TrailerHitch(Vector offset, TrailerTypes type) {
        this.offset = offset;
        this.type = type;
    }

    public void update(Location vLoc, float yaw){
        loc = vLoc.clone().add(offset.clone().rotateAroundY(-Math.toRadians(yaw)));
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
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

    public TrailerTypes getType() {
        return type;
    }
}
