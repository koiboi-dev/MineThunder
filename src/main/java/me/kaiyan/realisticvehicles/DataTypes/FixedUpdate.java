package me.kaiyan.realisticvehicles.DataTypes;

import me.kaiyan.realisticvehicles.Counters.Updates;
import me.kaiyan.realisticvehicles.DamageModel.DamageModel;

public interface FixedUpdate {
    void OnFixedUpdate();
    default void flashModel() {}
    default void OnClose(){}
    default void closeThis(boolean clearStands){
        Updates.fixedUpdates.remove(this);
        this.OnClose();
    }
    default void start(){
        Updates.fixedUpdates.add(this);
    }
}
