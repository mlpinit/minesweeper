package com.mlpinit.models;

public class BoardRequest {
    private Coordinate coordinate;
    private BoardAction actionType;

    BoardRequest(Coordinate coordinate, BoardAction actionType) {
        this.coordinate = coordinate;
        this.actionType = actionType;
    }

    public int getX() {
        return coordinate.getX();
    }

    public int getY() {
        return coordinate.getY();
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public BoardAction getActionType() {
        return actionType;
    }

    public String toString() {
        return actionType.toString() + " --> " + coordinate.toString();
    }

}
