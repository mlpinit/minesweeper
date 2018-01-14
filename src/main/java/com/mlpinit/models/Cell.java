package com.mlpinit.models;

public class Cell {
    public static final int MINE = -1;
    public static final int EMPTY = 0;

    private State state;
    private int value;
    private Coordinate coordinate;

    public Cell(Coordinate coordinate, int value) {
        this.coordinate = coordinate;
        this.value = value;
        this.state = State.CLOSED;
    }

    public void open() {
        this.state = State.OPENED;
    }

    public int getValue() {
        return value;
    }

    public String getDisplayValue() {
        if (value == MINE) return "*";
        if (value == EMPTY) return "";
        return "" + value;
    }

    public int getX() {
        return coordinate.getX();
    }

    public int getY() {
        return coordinate.getY();
    }

    public State getState() {
        return state;
    }

    public void setMark() {
        if (state == State.CLOSED) state = State.MARKED;
    }

    public void unsetMark() {
        if (state == State.MARKED) state = State.CLOSED;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public boolean isMarked() {
        return state == State.MARKED;
    }

    public boolean isOpened() {
        return state == State.OPENED;
    }

    public boolean isClosed() {
        return state == State.CLOSED;
    }

    public boolean isEmpty() {
        return value == EMPTY;
    }

    public boolean isMine() {
        return value == MINE;
    }

    public String toString() {
        return state + " with value: " + value + " ";
    }

    public enum State {
        CLOSED, OPENED, MARKED
    }
}

