package com.mlpinit.models;

import org.omg.CORBA.CODESET_INCOMPATIBLE;

import java.awt.*;

public class Cell {
    public static Color[] colors = {
            Color.white, // not used
            new Color(11,36,251),        // => 1
            new Color(14,122,17),        // => 2
            new Color(252,13,27),        // => 3
            new Color(2,11,121)  ,       // => 4
            new Color(163, 141, 28),     // => 5
            new Color(169,169,169),      // => 6
            new Color(255,140,0),        // => 7
            new Color(0,0,0)             // => 8
    };

    public static final int MINE = -1;
    public static final int EMPTY = 0;

    private State state;
    private int value;
    private Coordinate coordinate;
    private Color color;

    public Cell(Coordinate coordinate, int value) {
        this.coordinate = coordinate;
        this.value = value;
        this.state = State.CLOSED;
    }

    public void open() {
        state = State.OPENED;
    }

    public int getValue() {
        return value;
    }

    public String getDisplayValue() {
        return value == MINE ? "*" : "" + value;
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

    public boolean setMark() {
        if (state == State.CLOSED) {
            state = State.MARKED;
            return true;
        }
        return false;
    }

    public boolean unsetMark() {
        if (state == State.MARKED) {
            state = State.CLOSED;
            return true;
        }
        return false;
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

    public boolean isEmpty() {
        return value == EMPTY;
    }

    public boolean isMine() {
        return value == MINE;
    }

    public Color getForegroundColor() {
        return value == MINE ? Color.white : colors[value];
    }

    public String toString() {
        return state + " with value: " + value + " ";
    }

    public enum State {
        CLOSED, OPENED, MARKED
    }
}

