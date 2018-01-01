package com.mlpinit.models;

class Cell {
    public static final int MINE = -1;
    public static final int EMPTY = 0;

    private State state;
    private int value;
    private int x;
    private int y;

    public Cell(int value, int x, int y) {
        this.x = x;
        this.y = y;
        this.value = value;
        this.state = State.CLOSED;
    }

    public void open() {
        state = State.OPENED;
    }

    public int getValue() {
        return value;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
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

    public String toString() {
        return "" + value + " ";
    }

    public enum State {
        CLOSED, OPENED, MARKED
    }
}

