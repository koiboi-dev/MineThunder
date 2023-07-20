package me.kaiyan.realisticvehicles.DamageModel;

import me.kaiyan.realisticvehicles.Models.Model;
import org.bukkit.util.EulerAngle;
import org.joml.Quaternionf;

import java.util.Arrays;

public class Test {
    public static void main(String[] args){
        System.out.println(Arrays.toString(getQuaternionFromAngles(45, 60, 30)));

    }

    public static double[] getQuaternionFromAngles(float yaw, float pitch, float roll){
        // Thanks to Amir!
        // https://math.stackexchange.com/questions/2975109/how-to-convert-euler-angles-to-quaternions-and-get-the-same-euler-angles-back-fr
        double qx = Math.sin(Math.toRadians(roll/2)) * Math.cos(Math.toRadians(pitch/2)) * Math.cos(Math.toRadians(yaw/2)) - Math.cos(Math.toRadians(roll/2)) * Math.sin(Math.toRadians(pitch/2)) * Math.sin(Math.toRadians(yaw/2));
        double qy = Math.cos(Math.toRadians(roll/2)) * Math.sin(Math.toRadians(pitch/2)) * Math.cos(Math.toRadians(yaw/2)) + Math.sin(Math.toRadians(roll/2)) * Math.cos(Math.toRadians(pitch/2)) * Math.sin(Math.toRadians(yaw/2));
        double qz = Math.cos(Math.toRadians(roll/2)) * Math.cos(Math.toRadians(pitch/2)) * Math.sin(Math.toRadians(yaw/2)) - Math.sin(Math.toRadians(roll/2)) * Math.sin(Math.toRadians(pitch/2)) * Math.cos(Math.toRadians(yaw/2));
        double qw = Math.cos(Math.toRadians(roll/2)) * Math.cos(Math.toRadians(pitch/2)) * Math.cos(Math.toRadians(yaw/2)) + Math.sin(Math.toRadians(roll/2)) * Math.sin(Math.toRadians(pitch/2)) * Math.sin(Math.toRadians(yaw/2));
        return new double[]{qx, qy, qz, qw};
    }
}
