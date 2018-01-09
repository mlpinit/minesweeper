package com.mlpinit.controllers;

import com.mlpinit.models.BoardActionInterpreter;
import com.mlpinit.models.Board;
import com.mlpinit.models.MouseButtonEvent;
import com.mlpinit.models.Cell;
import com.mlpinit.views.BoardFrame;
import rx.Observable;
import rx.subjects.PublishSubject;

import java.awt.event.MouseEvent;
import java.util.HashSet;

public class MainController {
    public static final HashSet<Integer> observedMouseEvents;
    static {
        observedMouseEvents = new HashSet<>();
        observedMouseEvents.add(MouseEvent.MOUSE_PRESSED);
        observedMouseEvents.add(MouseEvent.MOUSE_RELEASED);
        observedMouseEvents.add(MouseEvent.MOUSE_ENTERED);
    }
    public static final int defaultHeight = 16;
    public static final int defaultWidth = 30;
    public static final int defaultNrOfMines = 100;

    private BoardFrame boardFrame;
    private BoardActionInterpreter boardActionInterpreter;

    private Observable<MouseButtonEvent>[][] cellButtonBoardRequestObservables;
    private Observable<MouseEvent> restartGameObservable;
    private Board board;

    /* Private subjects */
    private PublishSubject<Cell> openCellsSubject = PublishSubject.create();
    private PublishSubject<Cell> markCellsSubject = PublishSubject.create();
    private PublishSubject<Cell> incorrectMarkCellsSubject = PublishSubject.create();
    private PublishSubject<Cell> openMineCelSubject = PublishSubject.create();

    /* Public observables */
    public Observable<Cell> openCellsObservable = openCellsSubject.share();
    public Observable<Cell> markCellsObservable = markCellsSubject.share();
    public Observable<Cell> incorrectMarkCellsObservable = incorrectMarkCellsSubject.share();
    public Observable<Cell> openMineCellObservable = openMineCelSubject.share();


    public MainController() {
        createNewGame();
        setupObservables();
    }

    public static void main(String[] args) {
        new MainController();
    }

    private void setupObservables() {
        for (int i = 0; i < defaultHeight; i++) {
            for (int j = 0; j < defaultWidth; j++) {
                cellButtonBoardRequestObservables[i][j]
                        .filter(mouseButtonEvent -> observedMouseEvents.contains(mouseButtonEvent.getButtonID()))
                        .subscribe(boardActionInterpreter::addEvent);
            }
        }
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

    private void createNewGame() {
        this.boardActionInterpreter = BoardActionInterpreter.create();
        this.board = new Board(openCellsSubject, markCellsSubject, incorrectMarkCellsSubject, openMineCelSubject);
        this.boardFrame = new BoardFrame(
                openCellsObservable, markCellsObservable, incorrectMarkCellsObservable, openMineCellObservable);
        this.cellButtonBoardRequestObservables = boardFrame.getCellButtonBoardRequestObservables();
        this.restartGameObservable = boardFrame.getRestartGameObservable();
    }

    // Use for testing only.
    public Board getBoard() {
        return board;
    }
}
