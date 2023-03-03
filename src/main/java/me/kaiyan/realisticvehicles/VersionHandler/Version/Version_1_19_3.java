package me.kaiyan.realisticvehicles.VersionHandler.Version;

import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class Version_1_19_3 implements Version{
    @Override
    public void teleport(Entity stand, Vector loc, float yaw, float pitch) {
        //((org.bukkit.craftbukkit.v1_18_R2.entity.CraftArmorStand) stand).getHandle().b(loc.getX(), loc.getY(), loc.getZ(), yaw, pitch);
        if (stand.getPassengers().size() == 0) {
            stand.teleport(loc.toLocation(stand.getWorld()));
            stand.setRotation(yaw, pitch);
        } else {
            ((org.bukkit.craftbukkit.v1_19_R2.entity.CraftArmorStand) stand).getHandle().o(loc.getX(), loc.getY(), loc.getZ());
        }
    }
}
