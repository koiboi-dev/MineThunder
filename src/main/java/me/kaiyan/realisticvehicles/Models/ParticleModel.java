package me.kaiyan.realisticvehicles.Models;

import me.kaiyan.realisticvehicles.Vehicles.Tank;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.Random;

public enum ParticleModel {
    TANKSHOTLARGE((world, loc, yaw, pitch) -> {
        Random rand = new Random();
        for (int i = 0; i < 20; i++) {
            world.spawnParticle(
                    Particle.SMOKE_LARGE,
                    loc.toLocation(world),
                    0,
                    (Math.sin(-Math.toRadians(yaw+90)) + Tank.getRand(rand, 0.25)) * rand.nextDouble()*0.1,
                    (Math.sin(-Math.toRadians(pitch)) + Tank.getRand(rand, 0.25)) * rand.nextDouble()*0.2,
                    (Math.cos(Math.toRadians(yaw+90)) + Tank.getRand(rand, 0.25)) *rand.nextDouble()*0.1,
                    5,
                    null,
                    true
            );
        }
        for (int i = 0; i < 20; i++) {
            world.spawnParticle(
                    Particle.SMOKE_LARGE,
                    loc.toLocation(world),
                    0,
                    (Math.sin(-Math.toRadians(yaw-90)) + Tank.getRand(rand, 0.25)) * rand.nextDouble()*0.1,
                    (Math.sin(-Math.toRadians(pitch)) + Tank.getRand(rand, 0.25)) * rand.nextDouble()*0.2,
                    (Math.cos(Math.toRadians(yaw-90)) + Tank.getRand(rand, 0.25)) * rand.nextDouble()*0.1,
                    5,
                    null,
                    true
            );
        }
        for (int i = 0; i < 40; i++) {
            world.spawnParticle(
                    Particle.SMOKE_LARGE,
                    loc.toLocation(world),
                    0,
                    (Math.sin(-Math.toRadians(yaw)) + Tank.getRand(rand, 0.25)) * rand.nextDouble()*0.2,
                    (Math.sin(-Math.toRadians(pitch)) + Tank.getRand(rand, 0.25)) * rand.nextDouble()*0.3,
                    (Math.cos(Math.toRadians(yaw)) + Tank.getRand(rand, 0.25)) * rand.nextDouble()*0.2,
                    5,
                    null,
                    true
            );
        }
        for (int i = 0; i < 15; i++) {
            world.spawnParticle(
                    Particle.FLAME,
                    loc.toLocation(world),
                    0,
                    (Math.sin(-Math.toRadians(yaw)) + Tank.getRand(rand, 0.25)) * rand.nextDouble()*0.05,
                    (Math.sin(-Math.toRadians(pitch)) + Tank.getRand(rand, 0.25)) * rand.nextDouble()*0.05,
                    (Math.cos(Math.toRadians(yaw)) + Tank.getRand(rand, 0.25)) * rand.nextDouble()*0.05,
                    5,
                    null,
                    true
            );
        }
    });

    ParticleEffect effect;
    ParticleModel(ParticleEffect effect){
        this.effect = effect;
    }

    public void spawnGunSmoke(World world, Vector loc, double yaw, double pitch){
        effect.spawnParticle(world, loc, yaw, pitch);
    }
}

interface ParticleEffect {
    void spawnParticle(World world, Vector loc, double yaw, double pitch);
}
