package com.mlpinit.models;

import com.mlpinit.utils.Log;
import rx.subjects.PublishSubject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Board implements BoardInterface {
    private static final String TAG = "[Board]";
    public State state = State.NOT_STARTED;

    private int height;
    private int width;
    private int nrOfMines;
    private Cell[][] board = null;

    private PublishSubject<Cell> openCellPublishSubject;
    private PublishSubject<Cell> markCellPublishSubject;

    public Board(PublishSubject<Cell> openCellPublishSubject, PublishSubject<Cell> markCellPublishSubject,
                 int height, int width, int nrOfMines) {
        this.height = height;
        this.width = width;
        this.nrOfMines = nrOfMines;
        this.openCellPublishSubject = openCellPublishSubject;
        this.markCellPublishSubject = markCellPublishSubject;
    }

    public Board(PublishSubject<Cell> openCellPublishSubject, PublishSubject<Cell> markCellPublishSubject) {
        // default settings
        this.height = 16;
        this.width = 30;
        this.nrOfMines = 100;

        this.openCellPublishSubject = openCellPublishSubject;
        this.markCellPublishSubject = markCellPublishSubject;
    }

    public void execute(BoardRequest boardRequest) {
        if (boardRequest.getActionType() == BoardAction.OPEN) {
            open(boardRequest.getX(), boardRequest.getY());
        } else if (boardRequest.getActionType() == BoardAction.MARK) {
            toggleMark(boardRequest.getX(), boardRequest.getY());
        } else if (boardRequest.getActionType() == BoardAction.OPEN_NEIGHBOURS) {
            Log.info(TAG, "Opening Neighbours from: " + boardRequest.getCoordinate() + ".");
            openNeighbours(boardRequest.getX(), boardRequest.getY());
        }
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
        if (state == State.GAME_OVER) return;
        cell.open();
        Log.info(TAG, "--> Cell: " + cell);
        openCellPublishSubject.onNext(cell);
        if (cell.isMine()) {
            state = State.GAME_OVER;
            Log.info(TAG, "GAME OVER");
        } else {
            Log.info(TAG, "Opening Cell at: " + cell.getCoordinate() + ".");
            if (cell.isEmpty()) {
                List<Cell> neighbours = neighbours(cell.getX(), cell.getY());
                for (Cell neighbour : neighbours) {
                    if (!neighbour.isOpened()) open(neighbour);
                }
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
    public void toggleMark(int x, int y) {
        Cell cell = board[x][y];
        if (cell == null) return;
        if (cell.isMarked()) {
            Log.info(TAG, "Removing mark for cell at: " + cell.getCoordinate() + ".");
            cell.unsetMark();
        } else {
            Log.info(TAG, "Setting mark for cell at: " + cell.getCoordinate() + ".");
            cell.setMark();
        }
        markCellPublishSubject.onNext(cell);
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
            Coordinate coordinate = new Coordinate(row, column);
            board[row][column] = new Cell(coordinate, Cell.MINE);
        }
        // set value to the count of neighbouring mines
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (board[i][j] == null) {
                    Coordinate coordinate = new Coordinate(i, j);
                    board[i][j] = new Cell(coordinate, neighboursCount(i, j));
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