package com.mlpinit.models;

import com.mlpinit.controllers.MainController;
import org.junit.Before;
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
    private PublishSubject<Cell> incorrectMarkCellSubject = PublishSubject.create();
    private PublishSubject<Cell> openMineCellSubject = PublishSubject.create();
    private Board board;


    @Before
    public void setup() {
        this.board = new Board(openCellSubject, markCellSubject, incorrectMarkCellSubject, openMineCellSubject);
    }

    @Test
    public void testThatItSetsUpAnInitialBoardOnFirstOpenClick() {
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
            Board board = new Board(openCellSubject, markCellSubject, incorrectMarkCellSubject, openMineCellSubject);
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
        openMineCellSubject.subscribe(subscriber);
        board.open(mine.getX(), mine.getY());
        assertEquals(Board.State.GAME_OVER, board.getState());
        assertEquals(Cell.State.OPENED, mine.getState());
        subscriber.assertValueCount(100);
    }

    @Test
    public void testCanNotOpenAnotherCellAfterGameOver() {
        TestSubscriber<Cell> subscriber = TestSubscriber.create();
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

        board.open(mine.getX(), mine.getY());
        openCellSubject.subscribe(subscriber);
        board.open(notMine.getX(), notMine.getY());
        assertEquals(Board.State.GAME_OVER, board.getState());
        assertEquals(Cell.State.OPENED, mine.getState());
        subscriber.assertNoValues();
    }

    @Test
    public void testOpeningEmptyCellOpensAllNeighbours() {
        Cell[][] cells = loadCellsFromTemplate();
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
        TestSubscriber<Cell> subscriber = new TestSubscriber<>();
        openCellSubject.subscribe(subscriber);
        Cell[][] cells = loadCellsFromTemplate();
        board.setBoard(cells);
        board.open(0, 3);
        subscriber.assertValues(cells[0][3], cells[0][2], cells[0][4], cells[0][5], cells[0][6], cells[1][4], cells[1][5],
                cells[1][6], cells[1][3], cells[1][2], cells[2][2], cells[2][3], cells[2][4]);
    }

    @Test
    public void testToggleMarkTogglesCell() {
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
        board.open(0, 0);
        board.toggleMark(0, 0);
        assertEquals(Cell.State.OPENED, board.getBoard()[0][0].getState());
    }


    @Test
    public void testCanNotMarkOpenedCell() {
        TestSubscriber<Cell> subscriber = new TestSubscriber<>();
        board.open(0, 0);
        Cell[][] cells = board.getBoard();
        Cell cell = null;

        outerboard:
        for (int i = height - 1; i >= 0; i--) {
            for (int j = width - 1; j >= 0; j--) {
                cell = cells[i][j];
                if (!cell.isMine()) {
                    board.open(cell.getX(), cell.getY());
                    break outerboard;
                }
            }
        }
        markCellSubject.subscribe(subscriber);
        board.toggleMark(cell.getX(), cell.getY());
        subscriber.assertNoValues();
    }

    @Test
    public void testOpeningNeighboursWithBombsEndsTheGame() {
        Cell[][] cells = loadCellsFromTemplate();
        board.setBoard(cells);
        board.open(1,3);
        board.toggleMark(3,2);
        board.toggleMark(2,1);
        board.openNeighbours(2,2);
        assertTrue("Game should have ended", board.getState() == Board.State.GAME_OVER);
    }

    @Test
    public void testIfCellIsAlreadyOpenItShouldNotReopen() {
    }

    @Test
    public void testAllOpenedNeighboursAreMarkedOpen() {
        Cell[][] cells = loadCellsFromTemplate();
        board.setBoard(cells);
        board.open(1,1);
        board.toggleMark(0,0);
        board.toggleMark(0,1);
        board.openNeighbours(1,1);
        assertTrue(cells[0][2].isOpened());
        assertTrue(cells[1][0].isOpened());
        assertTrue(cells[1][1].isOpened());
        assertTrue(cells[1][2].isOpened());
        assertTrue(cells[2][0].isOpened());
        assertTrue(cells[2][1].isOpened());
        assertTrue(cells[2][2].isOpened());
    }

    @Test
    public void testTogglingMarkOnCellBeforeGameStartedShouldNotBreakTheGame() {
        TestSubscriber<Cell> subscriber = new TestSubscriber<>();
        // to be implemented
    }

    @Test
    public void testShouldNotBeAllowedToToggleMarkIfGameIsOver() {
        TestSubscriber<Cell> subscriber = new TestSubscriber<>();
        Cell[][] cells = loadCellsFromTemplate();
        board.setBoard(cells);
        board.open(1,1);
        board.open(0,0);
        markCellSubject.subscribe(subscriber);
        board.toggleMark(15,29);
        subscriber.assertNoValues();
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
