package me.kaiyan.realisticvehicles.VersionHandler.Version;

import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class Version_1_19 implements Version{
    @Override
    public void teleport(Entity stand, Vector loc, float yaw, float pitch) {
        ((org.bukkit.craftbukkit.v1_19_R1.entity.CraftArmorStand) stand).getHandle().b(loc.getX(), loc.getY(), loc.getZ(), yaw, pitch);
    }
}
