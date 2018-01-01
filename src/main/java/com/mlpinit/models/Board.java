package com.mlpinit.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Board implements BoardInterface {
    public State state = State.NOT_STARTED;

    private int height;
    private int width;
    private int nrOfMines;
    private Cell[][] board = null;

    public Board(int height, int width, int nrOfMines) {
        this.height = height;
        this.width = width;
        this.nrOfMines = nrOfMines;
    }

    @Override
    public int open(int x, int y) {
        if (state == State.NOT_STARTED) setupBoard(x, y);
        Cell cell = cellAtPosition(x, y);
        open(cell);
        return cell.getValue();
    }

    @Override
    public void open(Cell cell) {
        cell.open();
        if (cell.isMine()) {
            state = State.GAME_OVER;
        } else if (cell.isEmpty()) {
            List<Cell> neighbours = neighbours(cell.getX(), cell.getY());
            for (Cell neighbour : neighbours) {
                if (!neighbour.isOpened()) open(neighbour);
            }
        }
    }

    @Override
    public boolean openNeighbours(int x, int y) {
        Cell currentCell = cellAtPosition(x, y);
        if (!currentCell.isOpened()) {
            return false;
        }
        List<Cell> neighbours = neighbours(x, y);
        List<Cell> markedNeighbours = new ArrayList<>();
        List<Cell> nonMarkedNeighbours = new ArrayList<>();
        // separate marked neighbours from non marked neighbours
        for (Cell neighbour : neighbours) {
            if (neighbour.isMarked()) {
                markedNeighbours.add(neighbour);
            } else {
                nonMarkedNeighbours.add(neighbour);
            }
        }
        // only open neighbours if the correct number of mines has been marked around the current cell
        if (markedNeighbours.size() == currentCell.getValue()) {
            for (Cell neighbour : markedNeighbours) {
                if (!neighbour.isMine()) {
                    state = State.GAME_OVER;
                    neighbour.open();
                    return true;
                }
            }
            for (Cell neighbour : nonMarkedNeighbours) {
                open(neighbour.getX(), neighbour.getY());
            }
            return true;
        }
        return false;
    }
    @Override
    public boolean setMark(int x, int y) {
        return board[x][y].setMark();
    }

    @Override
    public Cell[][] getBoard() {
        return board;
    }

    @Override
    public State getState() {
        return state;
    }

    private void setupBoard(int x, int y) {
        board = new Cell[height][width];
        ArrayList<Integer> positions = new ArrayList<>(height * width);
        for (int i = 0; i < height * width; i++) {
            positions.add(i);
        }
        Collections.shuffle(positions);
        // Make sure that the first position clicked is never a mine.
        // Replace it with a different position if it is a mine.
        int firstClickedPosition = x * width + y;
        for (int i = 0; i < nrOfMines; i++) {
            if (positions.get(i) == firstClickedPosition) {
                positions.set(i, positions.get(nrOfMines));
            }
        }
        // set mines value to Cell.MINE
        for (int i = 0; i < nrOfMines; i++) {
            int position = positions.get(i);
            int row =  position / width;
            int column = position % width;
            board[row][column] = new Cell(Cell.MINE, row, column);
        }
        // set value to the count of neighbouring mines
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (board[i][j] == null) {
                    board[i][j] = new Cell(neighboursCount(i, j), i, j);
                }
            }
        }
        state = State.STARTED;
    }

    private List<Cell> neighbours(int x, int y) {
        ArrayList<Cell> neighbours = new ArrayList();
        addCell(x - 1, y - 1, neighbours);
        addCell(x - 1, y, neighbours);
        addCell(x - 1, y + 1, neighbours);
        addCell(x, y - 1, neighbours);
        addCell(x, y + 1, neighbours);
        addCell(x + 1, y - 1, neighbours);
        addCell(x + 1, y, neighbours);
        addCell(x + 1, y + 1, neighbours);
        return neighbours;
    }

    private void addCell(int x, int y, ArrayList<Cell> neighbours) {
        Cell cell = cellAtPosition(x , y);
        if (cell != null) neighbours.add(cell);
    }

    private int neighboursCount(int x, int y) {
        int counter = 0;
        for (Cell cell : neighbours(x, y)) {
            if (cell.isMine()) counter++;
        }
        return counter;
    }

    private Cell cellAtPosition(int x, int y) {
        if (x < 0) return null;
        if (y < 0) return null;
        if (x >= height) return null;
        if (y >= width) return null;
        return board[x][y];
    }

    // Used only for testing.
    public void setBoard(Cell[][] board) {
        state = State.STARTED;
        this.board = board;
    }

}
