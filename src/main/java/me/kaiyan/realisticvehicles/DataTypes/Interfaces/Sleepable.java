package me.kaiyan.realisticvehicles.DataTypes.Interfaces;

public interface Sleepable {
    int getTicksSinceLastWake();
    void setWakeTicks(int amount);
    void sleep();
}
