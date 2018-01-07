package com.mlpinit.models;

import org.junit.Test;
import rx.observers.TestSubscriber;

import java.awt.event.MouseEvent;

import static org.junit.Assert.assertTrue;

public class BoardActionInterpreterTest {
    private static Coordinate coordinate = new Coordinate(0, 0);
    private static Coordinate movedCoordinate = new Coordinate(0, 1);

    @Test
    public void sendsBoardRequestWithOpenCellAction() {
        TestSubscriber<BoardRequest> subscriber = TestSubscriber.create();
        BoardActionInterpreter interpreter = BoardActionInterpreter.create();
        interpreter.boardRequestObservable.subscribe(subscriber);
        interpreter.addEvent(new MouseButtonEvent(coordinate, MouseEvent.BUTTON1, MouseEvent.MOUSE_PRESSED));
        interpreter.addEvent(new MouseButtonEvent(coordinate, MouseEvent.BUTTON1, MouseEvent.MOUSE_RELEASED));
        BoardRequest boardRequest = subscriber.getOnNextEvents().remove(0);

        assertTrue("Must be an Open Cell Board Action.",
                boardRequestOpenCellCondition(boardRequest, coordinate));
    }

    @Test
    public void sendsBoardRequestWithMarkCellAction() {
        TestSubscriber<BoardRequest> subscriber = TestSubscriber.create();
        BoardActionInterpreter interpreter = BoardActionInterpreter.create();
        interpreter.boardRequestObservable.subscribe(subscriber);
        interpreter.addEvent(new MouseButtonEvent(coordinate, MouseEvent.BUTTON3, MouseEvent.MOUSE_PRESSED));
        interpreter.addEvent(new MouseButtonEvent(coordinate, MouseEvent.BUTTON3, MouseEvent.MOUSE_RELEASED));
        BoardRequest boardRequest = subscriber.getOnNextEvents().remove(0);

        assertTrue("Must be a Mark Cell Board Action.",
                boardRequestMarkCellCondition(boardRequest, coordinate));
    }

    @Test
    public void sendsBoardRequestWithMovedOpenCellAction() {
        TestSubscriber<BoardRequest> subscriber = TestSubscriber.create();
        BoardActionInterpreter interpreter = BoardActionInterpreter.create();
        interpreter.boardRequestObservable.subscribe(subscriber);
        interpreter.addEvent(new MouseButtonEvent(coordinate, MouseEvent.BUTTON1, MouseEvent.MOUSE_PRESSED));
        interpreter.addEvent(new MouseButtonEvent(movedCoordinate, MouseEvent.BUTTON1, MouseEvent.MOUSE_ENTERED));
        interpreter.addEvent(new MouseButtonEvent(coordinate, MouseEvent.BUTTON1, MouseEvent.MOUSE_RELEASED));
        BoardRequest boardRequest = subscriber.getOnNextEvents().remove(0);

        assertTrue("Must be an Open Cell Board Action.",
                boardRequestOpenCellCondition(boardRequest, movedCoordinate));
    }

    @Test
    public void sendsBoardRequestWithMovedMarkCellAction() {
        TestSubscriber<BoardRequest> subscriber = TestSubscriber.create();
        BoardActionInterpreter interpreter = BoardActionInterpreter.create();
        interpreter.boardRequestObservable.subscribe(subscriber);
        interpreter.addEvent(new MouseButtonEvent(coordinate, MouseEvent.BUTTON3, MouseEvent.MOUSE_PRESSED));
        interpreter.addEvent(new MouseButtonEvent(movedCoordinate, MouseEvent.BUTTON3, MouseEvent.MOUSE_ENTERED));
        interpreter.addEvent(new MouseButtonEvent(coordinate, MouseEvent.BUTTON3, MouseEvent.MOUSE_RELEASED));
        BoardRequest boardRequest = subscriber.getOnNextEvents().remove(0);

        assertTrue("Must be an Mark Cell Board Action.",
                boardRequestMarkCellCondition(boardRequest, movedCoordinate));
    }

    @Test
    public void sendsBoardRequestWithOpenNeighboursActionWhenButtonOneReleased() {
        TestSubscriber<BoardRequest> subscriber = TestSubscriber.create();
        BoardActionInterpreter interpreter = BoardActionInterpreter.create();
        interpreter.boardRequestObservable.subscribe(subscriber);
        interpreter.addEvent(new MouseButtonEvent(coordinate, MouseEvent.BUTTON1, MouseEvent.MOUSE_PRESSED));
        interpreter.addEvent(new MouseButtonEvent(coordinate, MouseEvent.BUTTON3, MouseEvent.MOUSE_PRESSED));
        interpreter.addEvent(new MouseButtonEvent(coordinate, MouseEvent.BUTTON1, MouseEvent.MOUSE_RELEASED));
        BoardRequest boardRequest = subscriber.getOnNextEvents().remove(0);

        assertTrue("Must be an Open Cell Board Action.",
                boardRequestOpenNeighboursCondition(boardRequest, coordinate));
    }

    @Test
    public void sendsBoardRequestWithOpenNeighboursActionWhenButtonThreeReleased() {
        TestSubscriber<BoardRequest> subscriber = TestSubscriber.create();
        BoardActionInterpreter interpreter = BoardActionInterpreter.create();
        interpreter.boardRequestObservable.subscribe(subscriber);
        interpreter.addEvent(new MouseButtonEvent(coordinate, MouseEvent.BUTTON1, MouseEvent.MOUSE_PRESSED));
        interpreter.addEvent(new MouseButtonEvent(coordinate, MouseEvent.BUTTON3, MouseEvent.MOUSE_PRESSED));
        interpreter.addEvent(new MouseButtonEvent(coordinate, MouseEvent.BUTTON3, MouseEvent.MOUSE_RELEASED));
        BoardRequest boardRequest = subscriber.getOnNextEvents().remove(0);

        assertTrue("Must be an Open Cell Board Action.",
                boardRequestOpenNeighboursCondition(boardRequest, coordinate));
    }

    @Test
    public void unknownActionDoesNotTriggerError() {
        TestSubscriber<BoardRequest> subscriber = TestSubscriber.create();
        BoardActionInterpreter interpreter = BoardActionInterpreter.create();
        interpreter.boardRequestObservable.subscribe(subscriber);
        interpreter.addEvent(new MouseButtonEvent(coordinate, MouseEvent.BUTTON1, MouseEvent.MOUSE_RELEASED));
        interpreter.addEvent(new MouseButtonEvent(coordinate, MouseEvent.BUTTON3, MouseEvent.MOUSE_RELEASED));
        interpreter.addEvent(new MouseButtonEvent(coordinate, MouseEvent.BUTTON2, MouseEvent.MOUSE_RELEASED));
        subscriber.assertNoValues();
    }

    private Boolean boardRequestOpenCellCondition(BoardRequest boardRequest, Coordinate coordinate) {
        if (boardRequest.getActionType() != BoardAction.OPEN) return false;
        if (!boardRequest.getCoordinate().toString().equals(coordinate.toString())) return false;
        return true;
    }

    private Boolean boardRequestMarkCellCondition(BoardRequest boardRequest, Coordinate coordinate) {
        if (boardRequest.getActionType() != BoardAction.MARK) return false;
        if (!boardRequest.getCoordinate().toString().equals(coordinate.toString())) return false;
        return true;
    }

    private Boolean boardRequestOpenNeighboursCondition(BoardRequest boardRequest, Coordinate coordinate) {
        if (boardRequest.getActionType() != BoardAction.OPEN_NEIGHBOURS) return false;
        if (!boardRequest.getCoordinate().toString().equals(coordinate.toString())) return false;
        return true;
    }
}
