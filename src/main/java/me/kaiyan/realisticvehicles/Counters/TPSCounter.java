package me.kaiyan.realisticvehicles.Counters;

import org.bukkit.scheduler.BukkitRunnable;

public class TPSCounter extends BukkitRunnable {
    double lasttime = Integer.MAX_VALUE;
    static double tps = 20;

    @Override
    public void run() {
        double diff = System.currentTimeMillis()-lasttime;
        tps = 1000/diff;
        lasttime = System.currentTimeMillis();
    }

    public static double getTPS(){
        return tps;
    }
}
