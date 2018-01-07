package com.mlpinit.models;

public interface BoardInterface {
    Cell[][] getBoard();
    State getState();
    int open(int x, int y);
    void open(Cell cell);
    boolean openNeighbours(int x, int y);
    void toggleMark(int x, int y);

    enum State {
        NOT_STARTED, STARTED, GAME_OVER
    }
}
