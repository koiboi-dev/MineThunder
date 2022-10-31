package me.kaiyan.realisticvehicles.Vehicles.Settings.GroundVehicles;

import me.kaiyan.realisticvehicles.DamageModel.DamageModel;
import me.kaiyan.realisticvehicles.DamageModel.Projectiles.Shell;
import me.kaiyan.realisticvehicles.DataTypes.Enums.VehicleType;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TankSettings extends GroundVehicleSettings{
    public static List<TankSettings> register = new ArrayList<>();

    Vector seatRaisedPos;
    Vector gunBarrelEnd;
    Vector machineGunPos;

    float traverseSpeed;
    float elevateSpeed;
    float minPitch;
    float maxPitch;

    float gunRecoilCooldown;
    float gunRecoilSetback;

    DamageModel damageModel;

    Shell[] shells = new Shell[3];

    /**
     *  @param type Tank Name
     * @param textureID Tanks numerical ID, this is used for knowing what model to display.
     *                  It tries to make a wooden hoe with overrides
     *                  body: texID
     *                  turret: texID+1
     *                  gun: texID+2
     */
    public TankSettings(String type, int textureID, float price){
        super(type, VehicleType.TANK,textureID, price);
    }

    /**
     *
     * @param seatPos Pos of the seat armour stand
     * @param seatRaisedPos Raised Pos of the seat armour stand
     * @param gunBarrelEndPos Gun barrel used for vfx
     * @param machineGunPos Gun barrel of the machine gun
     */
    public void setPositions(Vector seatPos, Vector seatRaisedPos, Vector gunBarrelEndPos, Vector machineGunPos){
        setSeatPos(seatPos);
        this.seatRaisedPos = seatRaisedPos;
        this.gunBarrelEnd = gunBarrelEndPos;
        this.machineGunPos = machineGunPos;
    }

    public void setTankData(float traverseSpeed, float elevateSpeed, float minPitch, float maxPitch, float gunRecoilCooldown, float gunRecoilSetback){
        this.traverseSpeed = traverseSpeed;
        this.elevateSpeed = elevateSpeed;
        this.minPitch = -minPitch;
        this.maxPitch = -maxPitch;
        this.gunRecoilCooldown = gunRecoilCooldown;
        this.gunRecoilSetback = gunRecoilSetback;
    }

    public void setShellData(@Nonnull Shell fshell, @Nullable Shell sshell, @Nullable Shell tshell){
        shells[0] = fshell;
        shells[1] = sshell;
        shells[2] = tshell;
    }
    public void setShellData(@Nonnull Shell fshell, @Nullable Shell sshell){
        setShellData(fshell, sshell, null);
    }

    public void setShellData(@Nonnull Shell fshell){
        setShellData(fshell, null, null);
    }

    public void setDamageModel(DamageModel model){
        damageModel = model.clone();
    }

    public void register(){
        register.add(this);
    }

    public static TankSettings getTankSettings(String type){
        for (TankSettings settings : register){
            if (Objects.equals(settings.getType(), type)){
                return settings;
            }
        }
        return null;
    }

    public static List<TankSettings> getRegister() {
        return register;
    }

    public Vector getSeatRaisedPos() {
        return seatRaisedPos;
    }

    public Vector getGunBarrelEnd() {
        return gunBarrelEnd;
    }

    public Vector getMachineGunPos() {
        return machineGunPos;
    }

    public float getTraverseSpeed() {
        return traverseSpeed;
    }

    public float getElevateSpeed() {
        return elevateSpeed;
    }

    public float getMinPitch() {
        return minPitch;
    }

    public float getMaxPitch() {
        return maxPitch;
    }

    public DamageModel getDamageModel() {
        return damageModel;
    }

    public Shell[] getShells() {
        return shells;
    }

    public float getGunRecoilCooldown() {
        return gunRecoilCooldown;
    }

    public float getGunRecoilSetback() {
        return gunRecoilSetback;
    }
}
