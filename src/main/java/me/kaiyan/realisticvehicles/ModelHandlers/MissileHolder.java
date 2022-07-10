package me.kaiyan.realisticvehicles.ModelHandlers;

import me.kaiyan.realisticvehicles.DataTypes.MissileSettings;
import me.kaiyan.realisticvehicles.DataTypes.VehicleInterface;
import me.kaiyan.realisticvehicles.Physics.Missile;
import org.bukkit.Location;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MissileHolder {
    private final List<MissileSlot> missiles = new ArrayList<>();

    public void updateHeldMissilePosition(Location loc, float yaw, float pitch, float roll){
        for (MissileSlot slot : missiles){
            Vector offset = slot.getPos().clone();
            offset.rotateAroundZ(Math.toRadians(roll)).rotateAroundZ(Math.toRadians(pitch)).rotateAroundY(Math.toRadians(yaw));
            offset.subtract(new Vector(0, 1.9, 0));
            Location oLoc = loc.clone();
            oLoc.add(offset);
            oLoc.setYaw(yaw);
            slot.getStand().teleport(oLoc);
            slot.getStand().setHeadPose(new EulerAngle(Math.toRadians(pitch), 0, Math.toRadians(roll)));
        }
    }

    public void fireMissile(int id, Location loc, float yaw, float pitch, float roll){
        loc = loc.clone();
        MissileSlot slot = missiles.get(id);

        Vector offset = slot.getPos().clone();
        offset.rotateAroundZ(Math.toRadians(roll)).rotateAroundZ(Math.toRadians(pitch)).rotateAroundY(Math.toRadians(yaw));
        offset.subtract(new Vector(0, 1.9, 0));


        new Missile(loc.add(offset), yaw, pitch, slot.getSettings(), slot.getStand());
        slot.setStand(null);
        slot.setSettings(null);
    }

    public void reloadMissile(int id, MissileSettings type, Location loc){
        MissileSlot slot = missiles.get(id);
        slot.setSettings(type);
        slot.generateArmourStand(loc);
    }

    public void addMissileSlot(MissileSlot slot){
        missiles.add(slot);
    }
}
