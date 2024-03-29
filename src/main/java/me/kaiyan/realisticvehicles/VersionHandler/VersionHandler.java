package me.kaiyan.realisticvehicles.VersionHandler;

import me.kaiyan.realisticvehicles.RealisticVehicles;
import me.kaiyan.realisticvehicles.VersionHandler.Version.Version;
import me.kaiyan.realisticvehicles.VersionHandler.Version.Version_1_20_1;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class VersionHandler {
    public static String version;
    public static Version tele;

    public static void setVersion(){
        version = RealisticVehicles.getInstance().getServer().getClass().getPackage().getName().split("\\.")[3];
        RealisticVehicles.debugLog("Version: "+version);
        switch (version){
            case "v1_18_R2" -> tele = new Version_1_18();
            case "v1_19_R1" -> tele = new Version_1_19();
            case "v1_19_R2" -> tele = new Version_1_20_1();
            default -> {
                RealisticVehicles.getStaticLogger().severe("UNABLE TO LAUNCH REALISTIC VEHICLES, UNSUPPORTED MC VERSION");
                RealisticVehicles.getInstance().getServer().getPluginManager().disablePlugin(RealisticVehicles.getInstance());
            }
        }
    }

    public static void teleport(Entity stand, Vector loc, float yaw, float pitch){
        tele.teleport(stand, loc, yaw, pitch);
    }
}
