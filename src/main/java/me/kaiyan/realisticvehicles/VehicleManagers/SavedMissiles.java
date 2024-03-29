package me.kaiyan.realisticvehicles.VehicleManagers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.kaiyan.realisticvehicles.DataTypes.MissileSettings;
import me.kaiyan.realisticvehicles.Models.MissileHolder;
import me.kaiyan.realisticvehicles.Models.MissileSlot;
import org.bukkit.Location;

import java.io.Serializable;

public class SavedMissiles implements Serializable {
    private final MissileSettings[] missiles;

    public SavedMissiles(MissileHolder holder) {
        missiles = new MissileSettings[holder.getMissiles().size()];
        int loops = 0;
        for (MissileSlot slot : holder.getMissiles()){
            if (slot.getSettings() == null){
                continue;
            }
            missiles[loops] = slot.getSettings();
            loops++;
        }
    }

    public void updateMissileHolder(MissileHolder holder, Location spawnLoc){
        int loops = 0;
        for (MissileSettings set : missiles){
            if (set != null){
                holder.reloadMissile(loops, set, spawnLoc);
            }
            loops++;
        }
    }

    public String toJson(){
        return new GsonBuilder().serializeSpecialFloatingPointValues().create().toJson(this);
    }
    public String toString() {return toJson();}

    public static SavedMissiles fromJson(String json){
        return new GsonBuilder().serializeSpecialFloatingPointValues().create().fromJson(json, SavedMissiles.class);
    }
}
