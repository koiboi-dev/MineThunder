package me.kaiyan.realisticvehicles.VersionHandler.Version;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class Version_1_18 implements Version{
    @Override
    public void teleport(Entity stand, Vector loc, float yaw, float pitch) {
        ((org.bukkit.craftbukkit.v1_18_R2.entity.CraftArmorStand) stand).getHandle().b(loc.getX(), loc.getY(), loc.getZ(), yaw, pitch);
    }
}
