package me.kaiyan.realisticvehicles.DataTypes;

public class DeathMessage {
    private String message;
    private int aliveTime = 0;

    public DeathMessage(String message) {
        this.message = message;
    }
    public DeathMessage(String message, int aliveTime) {
        this.message = message;
        this.aliveTime = aliveTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getAliveTime() {
        return aliveTime;
    }

    public void setAliveTime(int aliveTime) {
        this.aliveTime = aliveTime;
    }

    public void addAliveTime(int amount){
        this.aliveTime += amount;
    }
}
