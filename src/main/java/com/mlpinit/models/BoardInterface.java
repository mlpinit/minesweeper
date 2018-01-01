package com.mlpinit.models;

public interface BoardInterface {
    Cell[][] getBoard();
    int open(int x, int y);
    void open(Cell cell);
    boolean openNeighbours(int x, int y);
    boolean setMark(int x, int y);
    State getState();

    enum State {
        NOT_STARTED, STARTED, GAME_OVER
    }
}
