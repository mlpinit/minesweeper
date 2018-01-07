package com.mlpinit.controllers;

import com.mlpinit.models.BoardActionInterpreter;
import com.mlpinit.models.Board;
import com.mlpinit.models.MouseButtonEvent;
import com.mlpinit.models.Cell;
import com.mlpinit.views.BoardFrame;
import rx.Observable;
import rx.subjects.PublishSubject;

public class MainController {
    public static final int defaultHeight = 16;
    public static final int defaultWidth = 30;
    public static final int defaultNrOfMines = 100;

    private BoardFrame boardFrame;
    private BoardActionInterpreter boardActionInterpreter;

    private Observable<MouseButtonEvent>[][] cellButtonBoardRequestObservables;
    private Observable<Boolean> restartGameObservable;
    private Board board;

    /* Private subjects */
    private PublishSubject<Cell> openCellsSubject = PublishSubject.create();
    private PublishSubject<Cell> markCellsSubject = PublishSubject.create();

    /* Public observables */
    public Observable<Cell> openCellsObservable = openCellsSubject.share();
    public Observable<Cell> markCellsObservable = markCellsSubject.share();


    public MainController() {
        createNewGame();
        setupObservables();
    }

    public static void main(String[] args) {
        new MainController();
    }

    // used for testing
    public Board getBoard() {
        return board;
    }

    private void setupObservables() {
        for (int i = 0; i < defaultHeight; i++) {
            for (int j = 0; j < defaultWidth; j++) {
                cellButtonBoardRequestObservables[i][j].subscribe(boardActionInterpreter::addEvent);
            }
        }
        boardActionInterpreter.boardRequestObservable.subscribe(board::execute);
        restartGameObservable.subscribe(aVoid -> {
            this.boardFrame.setVisible(false);
            this.boardFrame.dispose();
            this.boardFrame = null;
            createNewGame();
            setupObservables();
        });
    }

    private void createNewGame() {
        this.boardActionInterpreter = BoardActionInterpreter.create();
        this.board = new Board(openCellsSubject, markCellsSubject);
        this.boardFrame = new BoardFrame(openCellsObservable, markCellsObservable);
        this.cellButtonBoardRequestObservables = boardFrame.getCellButtonBoardRequestObservables();
        this.restartGameObservable = boardFrame.getRestartGameObservable();
    }

}
