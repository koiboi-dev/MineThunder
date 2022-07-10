package me.kaiyan.realisticvehicles.DamageModel;

import me.kaiyan.realisticvehicles.DamageModel.Dimensions.Rect;
import me.kaiyan.realisticvehicles.DamageModel.Dimensions.VectorD;
import me.kaiyan.realisticvehicles.DamageModel.Hitboxes.ArmourPlate;
import me.kaiyan.realisticvehicles.DamageModel.Projectiles.Shell;
import me.kaiyan.realisticvehicles.RealisticVehicles;

import javax.swing.text.Style;
import java.util.Scanner;

public class Test {
    public static void main(String[] args){
        boolean active = true;
        while (active) {
            Scanner scanner = new Scanner(System.in);

            System.out.println("Input X: ");
            double x = scanner.nextDouble();
            System.out.println("X : " + x);
            System.out.println("Input Y: ");
            double y = scanner.nextDouble();
            System.out.println("Y : " + y);

            System.out.println("Yaw: " + Math.toDegrees(Math.atan2(y, x)));

            System.out.println("Continue:");
            scanner.nextBoolean();
        }
    }
}
