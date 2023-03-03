package me.kaiyan.realisticvehicles.Vehicles.Settings;

import me.kaiyan.realisticvehicles.DamageModel.DamageModel;
import me.kaiyan.realisticvehicles.DamageModel.Dimensions.Rect;
import me.kaiyan.realisticvehicles.DamageModel.Hitboxes.ArmourPlate;
import me.kaiyan.realisticvehicles.DamageModel.Hitboxes.Component;
import me.kaiyan.realisticvehicles.DamageModel.Projectiles.Shell;
import me.kaiyan.realisticvehicles.DataTypes.Enums.ComponentType;
import me.kaiyan.realisticvehicles.DataTypes.Enums.TrackingType;
import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.DataTypes.MissileSettings;
import me.kaiyan.realisticvehicles.Models.MissileSlot;
import me.kaiyan.realisticvehicles.RealisticVehicles;
import me.kaiyan.realisticvehicles.Vehicles.Settings.AirVehicles.AirVehicleSettings;
import me.kaiyan.realisticvehicles.Vehicles.Settings.GroundVehicles.TankSettings;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.util.Vector;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Used to load vehicles from JSON files.
 */
public class VehicleLoaderJSON {
    public static void loadVehicles(){
        File files = new File(RealisticVehicles.getInstance().getDataFolder()+"/vehicles");
        if (!files.exists()){
            files.mkdir();
            RealisticVehicles.getInstance().saveResource("vehicles/mig31.json", false);
        }
        for (File file : files.listFiles()){
            try {
                String content = new Scanner(file).useDelimiter("\\Z").next();
                settingsFromJSON(content);
            } catch (FileNotFoundException e) {
                RealisticVehicles.getInstance().getLogger().severe("FAILED TO LAUNCH: NO FILE FOUND WHILE READING VEHICLES.");
                throw new RuntimeException(e);
            }
        }
    }
    public static void settingsFromJSON(String settingJSON){
        JSONObject obj = new JSONObject(settingJSON);
        VehicleType type = VehicleType.valueOf((String) obj.get("vtype"));
        switch (type){
            case AIR -> {
                AirVehicleSettings settings = new AirVehicleSettings(
                        obj.getString("type"),
                        obj.getInt("texID"),
                        obj.getFloat("price"),
                        obj.getFloat("midoffset"),
                        obj.optBoolean("shift", false),
                        obj.getString("shopgroup")
                );
                JSONObject control = obj.getJSONObject("control");
                settings.setControlData(
                        control.getDouble("yawAccel"),
                        control.getDouble("fullYawSpeed"),
                        control.getDouble("maxYawSpeed"),
                        control.getDouble("pitchSpeed"),
                        control.getDouble("fullPitchSpeed")
                );
                JSONObject flight = obj.getJSONObject("flight");
                settings.setFlightData(
                        flight.getDouble("weight"),
                        flight.getDouble("explodeSpeed"),
                        flight.getDouble("enginePower"),
                        flight.getDouble("dragCoefficient"),
                        flight.getDouble("stallSpeed"),
                        flight.getDouble("liftSpeed")
                );
                settings.setBullet(getShellFromJSON(obj.getJSONObject("bullet")));
                settings.setFireRate(obj.getInt("firerate"));
                settings.setSize(obj.getFloat("width"), obj.getFloat("length"));
                settings.setSeatPos(vectorFromJson(obj.getJSONArray("seatpos")));

                for (Object pos : obj.getJSONArray("gunpositions")){
                    JSONArray ary = (JSONArray) pos;
                    settings.addGunPosition(new Vector(ary.getFloat(0), ary.getFloat(1), ary.getFloat(2)));
                }
                for (Object pos : obj.getJSONArray("models")){
                    JSONArray ary = (JSONArray) pos;
                    settings.addModelSegment(new int[] {ary.getInt(0), ary.getInt(1)}, ary.getInt(2), ary.getInt(3));
                }
                for (Object slotobj : obj.getJSONArray("missileSlots")){
                    JSONObject slot = (JSONObject) slotobj;
                    settings.addMissileSlot(new MissileSlot(
                            vectorFromJson(slot.getJSONArray("pos")),
                            slot.getString("name"),
                            null,
                            null
                    ));
                }
                for (Object misObj : obj.getJSONArray("missileSettings")){
                    JSONObject missile = (JSONObject) misObj;
                    settings.addMissile(new MissileSettings(
                            missile.getFloat("power"),
                            missile.getFloat("speed"),
                            missile.getFloat("turnRate"),
                            missile.getFloat("startFuel"),
                            missile.getFloat("burnRate"),
                            TrackingType.valueOf(missile.getString("trackingType")),
                            missile.getString("name"),
                            missile.getInt("texID"),
                            missile.getDouble("scanAngle"),
                            missile.getDouble("scanDistance")
                    ));
                }
                RealisticVehicles.debugLog(modelFromJSON(obj.getJSONObject("model")));
                settings.setDamageModel(modelFromJSON(obj.getJSONObject("model")));

                settings.setFuelData(
                        obj.getFloat("startFuel"),
                        obj.getFloat("fuelConsumptionRate"),
                        obj.getFloat("idleFuelConsumption"),
                        obj.getFloat("maxFuel"),
                        obj.getFloat("fuelLeakAmount")
                );

                settings.register();
            }
            case TANK -> {
                TankSettings settings = new TankSettings(obj.getString("type"), obj.getInt("texID"), obj.getFloat("price"), obj.getString("shopgroup"));
                settings.setPositions(
                        vectorFromJson(obj.getJSONArray("seatPos")),
                        vectorFromJson(obj.getJSONArray("seatRaisedPos")),
                        vectorFromJson(obj.getJSONArray("gunBarrelPos")),
                        vectorFromJson(obj.getJSONArray("machineGunPos"))
                );
                //settings.setTankData();
                settings.setDamageModel(modelFromJSON(obj.getJSONObject("model")));
                settings.register();
            }
        }
    }

    public static Shell getShellFromJSON(JSONObject obj){
        List<String> lore = new ArrayList<>();
        for (Object stb : obj.getJSONArray("lore")){
            lore.add((String) stb);
        }
        return new Shell(
                obj.getDouble("penScore"),
                obj.getDouble("damage"),
                obj.optBoolean("capped", false),
                obj.optBoolean("sabot", false),
                obj.optBoolean("explosive", false),
                obj.optBoolean("heat", false),
                Material.valueOf(obj.getString("iconItem")),
                lore,
                obj.getFloat("power"),
                obj.optBoolean("tracer", false),
                obj.getFloat("reloadTime"),
                obj.getFloat("cost"),
                obj.getDouble("weaknessDamage"),
                obj.getInt("buyStackSize")
        );
    }
    public static Rect rectFromJSON(JSONArray ary){
        return new Rect(
                ary.getDouble(0), ary.getDouble(1), ary.getDouble(2),
                ary.getDouble(3), ary.getDouble(4), ary.getDouble(5),
                ary.optBoolean(6, true)
        );
    }
    public static Vector vectorFromJson(JSONArray ary){
        return new Vector(ary.getFloat(0), ary.getFloat(1), ary.getFloat(2));
    }

    public static DamageModel modelFromJSON(JSONObject modelJSON){
        JSONArray armour = modelJSON.getJSONArray("armour");
        JSONArray comps = modelJSON.getJSONArray("comps");
        DamageModel out;
        if (modelJSON.has("upperHalf")) {
            out = new DamageModel(rectFromJSON(modelJSON.getJSONArray("lowerHalf")),
                    rectFromJSON(modelJSON.getJSONArray("upperhalf")),
                    modelJSON.getFloat("collision")
            );
        } else {
            out = new DamageModel(rectFromJSON(modelJSON.getJSONArray("lowerHalf")),
                    null,
                    modelJSON.getFloat("collision")
            );
        }

        for (Object obj : armour){
            JSONObject json = (JSONObject) obj;
            out.addArmour(new ArmourPlate(
                    json.getFloat("penDefence"),
                    json.getFloat("x"),
                    json.getFloat("y"),
                    json.getFloat("z"),
                    json.getFloat("xsize"),
                    json.getFloat("ysize"),
                    json.getFloat("zsize"),
                    json.optBoolean("centered", true),
                    json.getDouble("weakness"),
                    json.getBoolean("upper")
                    )
            );
        }
        for (Object obj : comps){
            JSONObject comp = (JSONObject) obj;
            out.addComponent(new Component(
                    ComponentType.valueOf(comp.getString("type")),
                    comp.getDouble("penDefence"),
                    comp.getDouble("health"),
                    comp.optDouble("damagedHealth", comp.getDouble("health")/2),
                    comp.optDouble("criticalHealth", comp.getDouble("health")/4),
                    comp.optBoolean("destroys", false),
                    comp.getFloat("x"),
                    comp.getFloat("y"),
                    comp.getFloat("z"),
                    comp.getFloat("xsize"),
                    comp.getFloat("ysize"),
                    comp.getFloat("zsize"),
                    comp.optBoolean("centered", true),
                    comp.getBoolean("upper"),
                    Particle.valueOf(comp.getString("damageParticle")),
                    Particle.valueOf(comp.getString("criticalParticle")),
                    Particle.valueOf(comp.getString("destroyedParticle"))
             ));
        }
        out.finish();
        return out;
    }
}
