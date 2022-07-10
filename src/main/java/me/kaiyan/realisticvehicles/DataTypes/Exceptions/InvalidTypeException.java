package me.kaiyan.realisticvehicles.DataTypes.Exceptions;

public class InvalidTypeException extends Exception {
    public InvalidTypeException(String type) {
        super("Invalid Vehicle Type: "+type);
    }
}
