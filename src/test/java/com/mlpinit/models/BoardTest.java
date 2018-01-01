package com.mlpinit.models;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

public class BoardTest {

    private int height = 16;
    private int width = 30;
    private int nrOfMines = 100;
    private int row = 1;
    private int column = 2;

    @Test
    public void testThatItSetsUpAnInitialBoardOnFirstOpenClick() {
        Board board = new Board(height, width, nrOfMines);
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
        Board board = new Board(height, width, nrOfMines);
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
            Board board = new Board(height, width, nrOfMines);
            board.open(row, column);
            if (board.getBoard()[row][column].getValue() == Cell.MINE) {
                cellIsAMineCounter++;
            }
        }
        assertEquals(0, cellIsAMineCounter);
    }

    @Test
    public void testOpeningMineEndsTheGame() {
        Board board = new Board(height, width, nrOfMines);
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
        board.open(mine.getX(), mine.getY());
        assertEquals(Board.State.GAME_OVER, board.getState());
        assertEquals(Cell.State.OPENED, mine.getState());
    }

    @Test
    public void testOpeningEmptyCellOpensAllNeighbours() {
        Cell[][] cells = new Cell[height][width];
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("board_template.txt").getFile());
            Scanner scanner = new Scanner(file);
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    cells[i][j] = new Cell(scanner.nextInt(), i, j);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Board board = new Board(height, width, nrOfMines);
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
}
