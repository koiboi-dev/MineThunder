package me.kaiyan.realisticvehicles.DataTypes;

public class ImpactOutData {
    private int destroyedIndex;
    private int playerDamage;

    public ImpactOutData(int destroyedIndex, int playerDamage) {
        this.destroyedIndex = destroyedIndex;
        this.playerDamage = playerDamage;
    }

    public void setDestroyedIndex(int destroyedIndex) {
        this.destroyedIndex = destroyedIndex;
    }

    public void setPlayerDamage(int playerDamage) {
        this.playerDamage = playerDamage;
    }

    public int getDestroyedIndex() {
        return destroyedIndex;
    }

    public int getPlayerDamage() {
        return playerDamage;
    }
    public void addPlayerDamage(int damage){
        playerDamage += damage;
    }
}
