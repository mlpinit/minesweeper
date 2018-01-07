package com.mlpinit.models;

public class MouseButtonEvent {
    private Coordinate coordinate;
    private int buttonNR;
    private int buttonID;

    public MouseButtonEvent(Coordinate coordinate, int buttonNR, int buttonID) {
        this.coordinate = coordinate;
        this.buttonNR = buttonNR;
        this.buttonID = buttonID;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public int getX() {
        return coordinate.getX();
    }

    public int getY() {
        return coordinate.getY();
    }

    public int getButtonNR() {
        return buttonNR;
    }

    public int getButtonID() {
        return buttonID;
    }

}
