package com.mlpinit.models;

import com.mlpinit.controllers.MainController;
import org.junit.Test;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

public class BoardTest {

    private int height = 16;
    private int width = 30;
    private int nrOfMines = 100;
    private int row = 1;
    private int column = 2;
    private PublishSubject<Cell> openCellSubject = PublishSubject.create();
    private PublishSubject<Cell> markCellSubject = PublishSubject.create();

    @Test
    public void testThatItSetsUpAnInitialBoardOnFirstOpenClick() {
        Board board = new Board(openCellSubject, markCellSubject);
        board.open(row,column);
        Cell[][] bd = board.getBoard();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                boolean betweenRange = bd[i][j].getValue() >= -1 && bd[i][j].getValue() <= 8;
                assertTrue("Value must be between -1 and 8 inclusive.", betweenRange);
            }
        }
    }

    @Test
    public void testThatItSetsCorrectNumberOfMines() {
        Board board = new Board(openCellSubject, markCellSubject);
        int counter = 0;
        board.open(row, column);
        Cell[][] bd = board.getBoard();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (bd[i][j].getValue() == Cell.MINE) {
                    counter++;
                }
            }
        }
        assertEquals(nrOfMines, counter);
    }

    @Test
    public void testThatFirstClickIsNotAMine() {
        int cellIsAMineCounter = 0;
        for (int i = 0; i < 100; i++) {
            Board board = new Board(openCellSubject, markCellSubject);
            board.open(row, column);
            if (board.getBoard()[row][column].getValue() == Cell.MINE) {
                cellIsAMineCounter++;
            }
        }
        assertEquals(0, cellIsAMineCounter);
    }

    @Test
    public void testOpeningMineEndsTheGame() {
        TestSubscriber<Cell> subscriber = TestSubscriber.create();
        Board board = new Board(openCellSubject, markCellSubject);
        board.open(1,2);
        Cell[][] bd = board.getBoard();
        Cell mine = null;
        outerboard:
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (bd[i][j].isMine()) {
                    mine = bd[i][j];
                    break outerboard;
                }
            }
        }
        openCellSubject.subscribe(subscriber);
        board.open(mine.getX(), mine.getY());
        assertEquals(Board.State.GAME_OVER, board.getState());
        assertEquals(Cell.State.OPENED, mine.getState());
        subscriber.assertNoValues();
    }

    @Test
    public void testCanNotOpenAnotherCellAfterGameOver() {
        TestSubscriber<Cell> subscriber = TestSubscriber.create();
        Board board = new Board(openCellSubject, markCellSubject);
        board.open(1,2);
        Cell[][] bd = board.getBoard();
        Cell mine = null;
        Cell notMine = null;
        outerboard:
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (bd[i][j].isMine()) {
                    mine = bd[i][j];
                } else {
                    if (i != 1 || j != 2) notMine = bd[i][j];
                }
                if (mine != null && notMine != null) break outerboard;
            }
        }

        openCellSubject.subscribe(subscriber);
        board.open(mine.getX(), mine.getY());
        board.open(notMine.getX(), notMine.getY());
        assertEquals(Board.State.GAME_OVER, board.getState());
        assertEquals(Cell.State.OPENED, mine.getState());
        subscriber.assertNoValues();
    }

    @Test
    public void testOpeningEmptyCellOpensAllNeighbours() {
        Cell[][] cells = loadCellsFromTemplate();
        Board board = new Board(openCellSubject, markCellSubject);
        board.setBoard(cells);
        board.open(11, 21);
        int openedCellsCounter = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (cells[i][j].isOpened()) openedCellsCounter++;
            }
        }
        assertEquals(40, openedCellsCounter);
    }

    @Test
    public void testThatItPublishesOpenedCells() {
        MainController mainController = new MainController();
        TestSubscriber<Cell> subscriber = new TestSubscriber<>();
        mainController.openCellsObservable.subscribe(subscriber);
        Cell[][] cells = loadCellsFromTemplate();
        mainController.getBoard().setBoard(cells);
        mainController.getBoard().open(0, 3);
        subscriber.assertValues(cells[0][3], cells[0][2], cells[0][4], cells[0][5], cells[0][6], cells[1][4], cells[1][5],
                cells[1][6], cells[1][3], cells[1][2], cells[2][2], cells[2][3], cells[2][4]);
    }

    @Test
    public void testToggleMarkTogglesCell() {
        Board board = new Board(openCellSubject, markCellSubject);
        TestSubscriber<Cell> subscriber = new TestSubscriber<>();
        board.open(0, 0);
        markCellSubject.subscribe(subscriber);
        board.toggleMark(15, 29);
        assertEquals(Cell.State.MARKED, board.getBoard()[15][29].getState());
        board.toggleMark(15, 29);
        assertEquals(Cell.State.CLOSED, board.getBoard()[15][29].getState());
        Cell[][] cells = board.getBoard();
        subscriber.assertValues(cells[15][29], cells[15][29]);
    }

    @Test
    public void testToggleMarkDoesNotToggleOpenCell() {
        Board board = new Board(openCellSubject, markCellSubject);
        board.open(0, 0);
        board.toggleMark(0, 0);
        assertEquals(Cell.State.OPENED, board.getBoard()[0][0].getState());
    }

    private Cell[][] loadCellsFromTemplate() {
        Cell[][] cells = new Cell[height][width];
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("board_template.txt").getFile());
            Scanner scanner = new Scanner(file);
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    Coordinate coordinate = new Coordinate(i, j);
                    cells[i][j] = new Cell(coordinate, scanner.nextInt());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return cells;
    }
}