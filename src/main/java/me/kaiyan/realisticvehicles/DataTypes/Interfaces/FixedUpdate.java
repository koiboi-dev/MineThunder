package me.kaiyan.realisticvehicles.DataTypes.Interfaces;

import me.kaiyan.realisticvehicles.Counters.Updates;

public interface FixedUpdate {
    void OnFixedUpdate();
    default void flashModel() {}
    default void OnClose(){}

    /**
     * Closes this update
     * @param standAction 0 = Dont do anything, 1 = Scrap, 2 = Delete
     */
    default void closeThis(int standAction){
        Updates.toBeRemoved.add(this);
        this.OnClose();
    }
    default void start(){
        Updates.toBeAdded.add(this);
    }
}
