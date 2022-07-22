package me.kaiyan.realisticvehicles.Vehicles.Settings.AirVehicles;

import me.kaiyan.realisticvehicles.DamageModel.DamageModel;
import me.kaiyan.realisticvehicles.DamageModel.Projectiles.Shell;
import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.DataTypes.MissileSettings;
import me.kaiyan.realisticvehicles.ModelHandlers.MissileSlot;
import me.kaiyan.realisticvehicles.Vehicles.Settings.VehicleSettings;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class AirVehicleSettings extends VehicleSettings {
    public static List<AirVehicleSettings> registers = new ArrayList<>();

    // kg
    private double weight;
    private double dragCoefficient;
    private double stallSpeed;
    private double liftSpeed;
    private double enginePower;

    private double yawSpeed;
    private double maxYawSpeed;
    private double fullYawSpeed;
    private double pitchSpeed;
    private double fullPitchSpeed;

    private double heatSignature;

    private final List<MissileSettings> missiles = new ArrayList<>();

    private List<Vector> gunPositions = new ArrayList<>();
    private int fireRate;

    private final HashMap<int[], Integer> models = new HashMap<>();

    private boolean hasRadar = false;
    private int scanAngle;
    private float scanDistance;

    public AirVehicleSettings(String type, int textureID) {
        super(type, textureID, VehicleType.AIR);
    }

    public void setFlightData(double weight, double enginePower, double dragCoefficient, double stallSpeed, double liftSpeed, double heatSignature){
        this.weight = weight;
        this.enginePower = enginePower;
        this.dragCoefficient = dragCoefficient;
        this.stallSpeed = stallSpeed;
        this.liftSpeed = liftSpeed;
        this.heatSignature = heatSignature;
    }

    public void setControlData(double yawSpeed, double fullYawSpeed, double maxYawSpeed,double pitchSpeed, double fullPitchSpeed){
        this.yawSpeed = yawSpeed;
        this.fullYawSpeed = fullYawSpeed*fullYawSpeed;
        this.pitchSpeed = pitchSpeed;
        this.fullPitchSpeed = fullPitchSpeed*fullPitchSpeed;
        this.maxYawSpeed = maxYawSpeed;
    }

    public void addModelSegment(int[] coords, int modelID){
        models.put(coords, modelID);
    }

    public HashMap<int[], Integer> getModels() {
        return models;
    }

    public double getWeight() {
        return weight;
    }

    public double getYawSpeed() {
        return yawSpeed;
    }

    public double getPitchSpeed() {
        return pitchSpeed;
    }

    public double getFullYawSpeed() {
        return fullYawSpeed;
    }

    public double getFullPitchSpeed() {
        return fullPitchSpeed;
    }

    public double getEnginePower() {
        return enginePower;
    }

    public double getDragCoefficient() {
        return dragCoefficient;
    }

    public static AirVehicleSettings getAirVehicleSettings(String type){
        for (AirVehicleSettings settings : registers){
            if (Objects.equals(settings.getType(), type)){
                return settings;
            }
        }
        return null;
    }

    public void register(){
        registers.add(this);
    }

    public double getStallSpeed() {
        return stallSpeed;
    }

    public void setStallSpeed(double stallSpeed) {
        this.stallSpeed = stallSpeed;
    }

    public double getLiftSpeed() {
        return liftSpeed;
    }

    public void setLiftSpeed(double liftSpeed) {
        this.liftSpeed = liftSpeed;
    }

    public List<Vector> getGunPositions() {
        return gunPositions;
    }

    public void setGunPositions(List<Vector> gunPositions) {
        this.gunPositions = gunPositions;
    }

    public void addGunPosition(Vector vec){
        gunPositions.add(vec);
    }

    private Shell shell;
    public void setBullet(Shell bullet){
        shell = bullet;
    }

    public Shell getBullet(){
        return shell;
    }

    public int getFireRate() {
        return fireRate;
    }

    public void setFireRate(int fireRate) {
        this.fireRate = fireRate;
    }

    public double getMaxYawSpeed() {
        return maxYawSpeed;
    }

    public void setMaxYawSpeed(double maxYawSpeed) {
        this.maxYawSpeed = maxYawSpeed;
    }

    public List<MissileSettings> getMissiles() {
        return missiles;
    }

    public void setRadar(int scanAngle, float scanDistance){
        this.scanAngle = scanAngle;
        this.scanDistance = scanDistance;
        hasRadar = true;
    }

    public boolean hasRadar() {
        return hasRadar;
    }

    public int getScanAngle() {
        return scanAngle;
    }

    public float getScanDistance() {
        return scanDistance;
    }

    public void addMissile(MissileSettings settings){
        missiles.add(settings);
    }
    private final List<MissileSlot> missileSlots = new ArrayList<>();
    public void addMissileSlot(MissileSlot slot){
        missileSlots.add(slot);
    }

    public List<MissileSlot> getMissileSlots() {
        return missileSlots;
    }
}
