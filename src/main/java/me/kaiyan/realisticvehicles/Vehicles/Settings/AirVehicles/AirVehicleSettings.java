package me.kaiyan.realisticvehicles.Vehicles.Settings.AirVehicles;

import me.kaiyan.realisticvehicles.DamageModel.Projectiles.Shell;
import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import me.kaiyan.realisticvehicles.DataTypes.MissileSettings;
import me.kaiyan.realisticvehicles.Menus.PurchaseMenu;
import me.kaiyan.realisticvehicles.Models.MissileSlot;
import me.kaiyan.realisticvehicles.Vehicles.Settings.VehicleSettings;
import net.minecraft.util.Tuple;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class AirVehicleSettings extends VehicleSettings {
    public static final List<AirVehicleSettings> registers = new ArrayList<>();

    // kg
    private double weight;
    private double explodeSpeed;
    private double dragCoefficient;
    private double stallSpeed;
    private double liftSpeed;
    private double enginePower;

    private double yawSpeed;
    private double maxYawSpeed;
    private double fullYawSpeed;
    private double pitchSpeed;
    private double fullPitchSpeed;

    private final List<MissileSettings> missiles = new ArrayList<>();

    private List<Vector> gunPositions = new ArrayList<>();
    private int fireRate;

    private final HashMap<int[], Tuple<Integer, Integer>> models = new HashMap<>();

    private boolean hasRadar = false;
    private int scanAngle;
    private float scanDistance;

    private final float midOffset;
    private final boolean shiftGrid;

    public AirVehicleSettings(String type, int textureID, float price, float midOffset, boolean shiftGrid, String shopGroup) {
        super(type, textureID, VehicleType.AIR, price, shopGroup);
        this.midOffset = midOffset;
        this.shiftGrid = shiftGrid;
    }

    public void setFlightData(double weight, double explodeSpeed, double enginePower, double dragCoefficient, double stallSpeed, double liftSpeed){
        this.weight = weight;
        this.explodeSpeed = explodeSpeed;
        this.enginePower = enginePower;
        this.dragCoefficient = dragCoefficient;
        this.stallSpeed = stallSpeed;
        this.liftSpeed = liftSpeed;
    }

    public void setControlData(double yawSpeed, double fullYawSpeed, double maxYawSpeed,double pitchSpeed, double fullPitchSpeed){
        this.yawSpeed = yawSpeed;
        this.fullYawSpeed = fullYawSpeed*fullYawSpeed;
        this.pitchSpeed = pitchSpeed;
        this.fullPitchSpeed = fullPitchSpeed*fullPitchSpeed;
        this.maxYawSpeed = maxYawSpeed;
    }

    public void addModelSegment(int[] coords, int extID, int retID){
        models.put(coords, new Tuple<>(extID, retID));
    }

    public HashMap<int[], Tuple<Integer, Integer>> getModels() {
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
        PurchaseMenu.addVehicleToShopList(this);
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

    public boolean hasRadar() {
        return hasRadar;
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

    public float getMidOffset() {
        return midOffset;
    }

    public double getExplodeSpeed() {
        return explodeSpeed;
    }

    public boolean isShiftGrid() {
        return shiftGrid;
    }
}
