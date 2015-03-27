package com.proyecto.uis.uismaps.mapview;

/**
 * Created by cheloreyes on 19/03/15.
 */
public class NearbyPlace {

    public static final int INSIDE = 0;
    public static final int SOUTH = 1;
    public static final int NORTH = 2;
    public static final int EAST = 3;
    public static final int WEST = 4;

    private String label;
    private double distance;
    private int where;

    public NearbyPlace() {
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setWhere(int where) {
        this.where = where;
    }

    public String getLabel() {
        return label;
    }

    public double getDistance() {
        return distance;
    }

    public int getWhere() {
        return where;
    }
}
