package com.proyecto.uis.uismaps.mapview;

/**
 * Created by cheloreyes on 19/03/15.
 */
public class NearbyPlace {

    private String label;
    private double distance;
    private boolean isInside = false;
    private boolean leftHand = false;
    private boolean rightHand = false;
    private boolean ahead = false;
    private boolean behind = false;

    public NearbyPlace() {
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setInside(boolean isInside) {
        this.isInside = isInside;
    }

    public void setLeftHand(boolean leftHand) {
        this.leftHand = leftHand;
    }

    public void setRightHand(boolean rightHand) {
        this.rightHand = rightHand;
    }

    public void setAhead(boolean ahead) {
        this.ahead = ahead;
    }

    public void setBehind(boolean behind) {
        this.behind = behind;
    }

    public String getLabel() {
        return label;
    }

    public double getDistance() {
        return distance;
    }

    public boolean isInside() {
        return isInside;
    }

    public boolean isLeftHand() {
        return leftHand;
    }

    public boolean isRightHand() {
        return rightHand;
    }

    public boolean isAhead() {
        return ahead;
    }

    public boolean isBehind() {
        return behind;
    }
}
