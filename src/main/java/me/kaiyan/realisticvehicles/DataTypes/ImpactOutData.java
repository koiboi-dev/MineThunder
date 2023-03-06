package me.kaiyan.realisticvehicles.DataTypes;

public class ImpactOutData {
    private int destroyedIndex;
    private int playerDamage;
    private boolean goneThrough;

    public ImpactOutData(int destroyedIndex, int playerDamage, boolean goneThrough) {
        this.destroyedIndex = destroyedIndex;
        this.playerDamage = playerDamage;
        this.goneThrough = goneThrough;
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

    public boolean getGoneThrough() {
        return goneThrough;
    }

    public void setGoneThrough(boolean goneThrough) {
        this.goneThrough = goneThrough;
    }
}
