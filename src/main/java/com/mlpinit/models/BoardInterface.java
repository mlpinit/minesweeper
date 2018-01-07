package com.mlpinit.models;

public interface BoardInterface {
    Cell[][] getBoard();
    State getState();
    void open(int x, int y);
    void openNeighbours(int x, int y);
    void toggleMark(int x, int y);

    enum State {
        NOT_STARTED, STARTED, GAME_OVER
    }
}
