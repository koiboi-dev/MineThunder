package me.kaiyan.realisticvehicles.Models;

import me.kaiyan.realisticvehicles.DataTypes.MissileSettings;
import me.kaiyan.realisticvehicles.DataTypes.Interfaces.RadarTarget;
import me.kaiyan.realisticvehicles.Physics.Missile;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class MissileHolder implements Cloneable{
    private final List<MissileSlot> missiles;

    public MissileHolder(){
        missiles = new ArrayList<>();
    }

    public MissileHolder(List<MissileSlot> slots){
        missiles = slots;
    }


    public void updateHeldMissilePosition(Location loc, float yaw, float pitch, float roll){
        for (MissileSlot slot : missiles){
            if (slot.getStand() == null){
                continue;
            }
            Vector offset = slot.getPos().clone();
            offset.rotateAroundZ(Math.toRadians(roll)).rotateAroundZ(Math.toRadians(pitch)).rotateAroundY(Math.toRadians(-yaw));
            offset.subtract(new Vector(0, 1.9, 0));
            Location oLoc = loc.clone();
            oLoc.add(offset);
            oLoc.setYaw(yaw);
            slot.getStand().teleport(oLoc);
            slot.getStand().setHeadPose(new EulerAngle(Math.toRadians(pitch), 0, Math.toRadians(-roll)));
        }
    }

    public Location getSlotLoc(MissileSlot slot, Location loc, float yaw, float roll, float pitch){
        loc = loc.clone();
        Vector offset = slot.getPos().clone();
        offset.rotateAroundZ(Math.toRadians(roll)).rotateAroundZ(Math.toRadians(pitch)).rotateAroundY(Math.toRadians(yaw));
        offset.subtract(new Vector(0, 1.9, 0));
        loc.add(offset);
        loc.setYaw(yaw);
        return loc;
    }

    public void fireMissile(int id, Location loc, float yaw, float pitch, float roll, float speed, RadarTarget shooter, Player player){
        if (id == -1){
            return;
        }
        try {
            loc = loc.clone();
            MissileSlot slot = missiles.get(id);

            Vector offset = slot.getPos().clone();
            offset.rotateAroundZ(Math.toRadians(roll)).rotateAroundZ(Math.toRadians(pitch)).rotateAroundY(Math.toRadians(yaw));
            offset.subtract(new Vector(0, 1.9, 0));

            new Missile(loc.add(offset), yaw, pitch, slot.getSettings(), slot.getStand(), speed, shooter, player);
            slot.setStand(null);
            slot.setSettings(null);
        } catch (IndexOutOfBoundsException ignore){

        }
    }

    public void reloadMissile(int id, MissileSettings type, Location loc){
        MissileSlot slot = missiles.get(id);
        slot.setSettings(type);
        slot.generateArmourStand(loc);
    }

    public void addMissileSlot(MissileSlot slot){
        missiles.add(slot);
    }
    public void addMissileSlots(List<MissileSlot> slots){
        missiles.addAll(slots);
    }

    public List<MissileSlot> getMissiles() {
        return missiles;
    }

    public MissileHolder clone(){
        try {
            return (MissileHolder) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public MissileHolder deepClone(){
        List<MissileSlot> slots = new ArrayList<>();
        for (MissileSlot slot : missiles){
            slots.add(slot.clone());
        }
        return new MissileHolder(slots);
    }
}
