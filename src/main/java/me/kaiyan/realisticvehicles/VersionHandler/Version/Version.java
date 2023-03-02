package me.kaiyan.realisticvehicles.VersionHandler.Version;

import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public interface Version {
    void teleport(Entity stand, Vector loc, float yaw, float pitch);
}
