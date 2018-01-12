package com.mlpinit.controllers;

import com.mlpinit.models.*;
import com.mlpinit.views.BoardFrame;
import rx.Observable;

import java.awt.event.MouseEvent;
import java.util.HashSet;

public class BoardController {
    public static final HashSet<Integer> observedMouseEvents;
    static {
        observedMouseEvents = new HashSet<>();
        observedMouseEvents.add(MouseEvent.MOUSE_PRESSED);
        observedMouseEvents.add(MouseEvent.MOUSE_RELEASED);
        observedMouseEvents.add(MouseEvent.MOUSE_ENTERED);
    }
    private int selectedHeight;
    private int selectedWidth;
    private int selectedNrOfMines;
    private BoardFrame boardFrame;
    private BoardActionInterpreter boardActionInterpreter;
    private MinesweeperTimer minesweeperTimer;

    private Observable<MouseButtonEvent> cellButtonBoardRequestObservable;
    private Observable<MouseEvent> restartGameObservable;
    private Board board;

    public BoardController(int height, int width, int nrOfMines) {
        this.selectedHeight = height;
        this.selectedWidth = width;
        this.selectedNrOfMines = nrOfMines;
        createNewGame();
        setupObservables();
    }

    private void createNewGame() {
        this.boardActionInterpreter = BoardActionInterpreter.create();
        this.minesweeperTimer = new MinesweeperTimer();
        this.board = new Board(selectedHeight, selectedWidth, selectedNrOfMines);
        this.boardFrame = new BoardFrame(
                board.openCellObservable,
                board.markCellObservable,
                board.incorrectCellMarkObservable,
                board.openMineCellObservable,
                board.removeCellMarkObservable,
                board.remainingMinesObservable,
                minesweeperTimer.elapsedTimeObservable,
                selectedHeight,
                selectedWidth,
                selectedNrOfMines
        );
        this.cellButtonBoardRequestObservable = boardFrame.getCellButtonBoardRequestObservable();
        this.restartGameObservable = boardFrame.getRestartGameObservable();
    }

    private void setupObservables() {
        this.board.gameIsRunningObservable.subscribe(gameIsRunning -> {
            if (gameIsRunning) {
                minesweeperTimer.startTimer();
            } else {
                minesweeperTimer.stopTimer();
            }
        });
        cellButtonBoardRequestObservable
                .filter(mouseButtonEvent -> observedMouseEvents.contains(mouseButtonEvent.getButtonID()))
                .subscribe(boardActionInterpreter::addEvent);
        boardActionInterpreter.boardRequestObservable.subscribe(board::execute);
        restartGameObservable.map(event -> event.getID() == MouseEvent.MOUSE_CLICKED)
                .filter(value -> value == true)
                .subscribe(event -> {
                    this.boardFrame.setVisible(false);
                    this.boardFrame.dispose();
                    this.boardFrame = null;
                    createNewGame();
                    setupObservables();
                });
    }
}
