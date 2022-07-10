package me.kaiyan.realisticvehicles.Counters;

import me.kaiyan.realisticvehicles.DamageModel.DamageModel;

public interface FixedUpdate {
    void OnFixedUpdate();
    default void flashModel() {}
    default void OnClose(){}
    default void closeThis(){
        Updates.fixedUpdates.remove(this);
        this.OnClose();
    }
    default void start(){
        Updates.fixedUpdates.add(this);
    }
}
