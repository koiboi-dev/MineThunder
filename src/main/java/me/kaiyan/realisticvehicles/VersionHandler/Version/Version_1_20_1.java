package me.kaiyan.realisticvehicles.VersionHandler.Version;

import org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class Version_1_20_1 implements Version{
    @Override
    public void teleport(Entity stand, Vector loc, float yaw, float pitch) {
        //((org.bukkit.craftbukkit.v1_18_R2.entity.CraftArmorStand) stand).getHandle().b(loc.getX(), loc.getY(), loc.getZ(), yaw, pitch);
        if (stand.getPassengers().size() == 0) {
            stand.teleport(loc.toLocation(stand.getWorld()));
            stand.setRotation(yaw, pitch);
        } else {
            ((CraftLivingEntity) stand).getHandle().b(loc.getX(), loc.getY(), loc.getZ(), yaw, pitch);
        }
    }
}
