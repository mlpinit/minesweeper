package com.mlpinit.models;

import com.mlpinit.utils.Log;
import rx.Observable;
import rx.subjects.PublishSubject;

import java.awt.event.MouseEvent;

public class BoardActionInterpreter {
    private static final String TAG = "[BoardActionInterpreter]";
    private static BoardActionInterpreter instance = null;

    private Coordinate actionableCoordinate = null;
    private Integer penUltimatePress = null;
    private Integer ultimatePress = null;

    /* Private subjects */
    private PublishSubject<BoardRequest> observedByBoard = PublishSubject.create();

    /* Public observables */
    public Observable<BoardRequest> boardRequestObservable = observedByBoard.share();

    private BoardActionInterpreter() {
    }

    public static BoardActionInterpreter create() {
        if (instance == null) {
            instance = new BoardActionInterpreter();
        }
        return instance;
    }

    public void addEvent(MouseButtonEvent event) {
        if (event.getButtonID() == MouseEvent.MOUSE_PRESSED || event.getButtonID() == MouseEvent.MOUSE_ENTERED) {
            this.actionableCoordinate = event.getCoordinate();
        }
        if (event.getButtonID() == MouseEvent.MOUSE_PRESSED) {
            penUltimatePress = ultimatePress;
            ultimatePress = event.getButtonNR();
        }
        if (event.getButtonID() == MouseEvent.MOUSE_RELEASED) {
            if (ultimatePress != null || penUltimatePress != null) {
                BoardAction actionType = null;
                if (isOpenNeighbours()) {
                    actionType = BoardAction.OPEN_NEIGHBOURS;
                } else if (isMark()) {
                    actionType = BoardAction.MARK;
                } else if (isOpenCell()) {
                    actionType = BoardAction.OPEN;
                }
                Log.info(TAG, "Sending request to " + actionType + ". Cell: " + actionableCoordinate);
                observedByBoard.onNext(new BoardRequest(actionableCoordinate, actionType));
                penUltimatePress = null;
                ultimatePress = null;
            }
        }
    }

    private Boolean isOpenNeighbours() {
        return penUltimatePress != null && ultimatePress != null
                && penUltimatePress + ultimatePress == MouseEvent.BUTTON1 + MouseEvent.BUTTON3;
    }

    private Boolean isMark() {
        return ultimatePress == MouseEvent.BUTTON3;
    }

    private Boolean isOpenCell() {
        return ultimatePress == MouseEvent.BUTTON1;
    }

}

