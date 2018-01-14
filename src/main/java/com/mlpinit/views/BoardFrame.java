package com.mlpinit.views;

import com.mlpinit.models.*;

import rx.Observable;
import rx.observables.SwingObservable;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.MouseEvent;

public class BoardFrame extends JFrame {
    private static final String TAG = "[BoardFrame]";
    private int startingNrOfMines;
    private int height;
    private int width;
    private JTextField nrOfMinesTextField;
    private JTextField timerTextField;

    private Observable<MouseEvent> restartGameObservable;
    public Observable<MouseButtonEvent> cellButtonBoardRequestObservable;
    public JButton[][] cellButtons;

    public BoardFrame(Observable<Cell> openCellObservable, Observable<Cell> markCellObservable,
                      Observable<Cell> incorrectCellMarkObservable, Observable<Cell> openMineCellObservable,
                      Observable<Cell> removeCellMarkObservable, Observable<Integer> remainingMinesObservable,
                      Observable<Void> gameWonObservable, Observable<Integer> elapsedTimeObservable,
                      int height, int width, int startingNrOfMines)
    {
        super("Minesweeper");
        this.setResizable(false);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.height = height;
        this.width = width;
        this.cellButtons = new JButton[height][width];
        openCellObservable.subscribe(this::openCell);
        markCellObservable.subscribe(this::markCell);
        removeCellMarkObservable.subscribe(this::removeCellMark);
        incorrectCellMarkObservable.subscribe(this::updateCellMarkedIncorrectly);
        openMineCellObservable.subscribe(this::openMine);
        remainingMinesObservable.subscribe(this::updateNrOfMinesTextField);
        elapsedTimeObservable.subscribe(this::updateTimer);
        gameWonObservable.subscribe(this::gameWon);
        this.startingNrOfMines = startingNrOfMines;
        this.cellButtonBoardRequestObservable = Observable.empty();
        addComponentsToPane(this.getContentPane());
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public Observable<MouseEvent> getRestartGameObservable() {
        return restartGameObservable;
    }

    public Observable<MouseButtonEvent> getCellButtonBoardRequestObservable() {
        return cellButtonBoardRequestObservable;
    }

    private void addComponentsToPane(final Container pane) {
        final JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BorderLayout());
        JButton restartButton = new JButton("Restart");
        restartButton.setBorder(new LineBorder(BasicColor.baseColor, 3));
        restartButton.setPreferredSize(new Dimension(80, 50));
        menuPanel.add(restartButton, BorderLayout.CENTER);
        nrOfMinesTextField = new JTextField("" + startingNrOfMines + " ");
        nrOfMinesTextField.setFont(new Font("sans-serif", Font.PLAIN, 20));
        nrOfMinesTextField.setBorder(new LineBorder(BasicColor.baseColor, 3));
        nrOfMinesTextField.setEditable(false);
        nrOfMinesTextField.setBackground(Color.black);
        nrOfMinesTextField.setForeground(Color.white);
        nrOfMinesTextField.setPreferredSize(new Dimension(60, 50));
        nrOfMinesTextField.setHorizontalAlignment(SwingConstants.RIGHT);

        menuPanel.add(nrOfMinesTextField, BorderLayout.WEST);

        timerTextField = new JTextField("0 ");
        timerTextField.setBackground(Color.black);
        timerTextField.setForeground(Color.white);
        timerTextField.setFont(new Font("sans-serif", Font.PLAIN, 20));
        timerTextField.setEditable(false);
        timerTextField.setBorder(new LineBorder(BasicColor.baseColor, 3));
        timerTextField.setPreferredSize(new Dimension(60, 50));
        timerTextField.setHorizontalAlignment(SwingConstants.RIGHT);

        menuPanel.add(timerTextField, BorderLayout.EAST);
        restartGameObservable = SwingObservable.fromMouseEvents(restartButton);
        pane.add(menuPanel, BorderLayout.NORTH);

        final JPanel cellsPanel = new JPanel();
        cellsPanel.setLayout(new GridLayout(height,width));
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                final Coordinate coordinate = new Coordinate(i, j);
                JButton button = new JButton();
                button.setUI((ButtonUI) BasicButtonUI.createUI(button));
                button.setBackground(BasicColor.baseColor);
                button.setPreferredSize(new Dimension(25 ,25));
                button.setBorder(BorderFactory.createEtchedBorder());
                cellButtons[i][j] = button;
                Observable<MouseButtonEvent> observable = SwingObservable.fromMouseEvents(button)
                        .map(event -> new MouseButtonEvent(coordinate, event.getButton(), event.getID()));
                cellButtonBoardRequestObservable = cellButtonBoardRequestObservable.mergeWith(observable);
                cellsPanel.add(button);
            } }
        pane.add(cellsPanel, BorderLayout.CENTER);
    }

    private void openCell(Cell cell) {
        JButton button = cellButtons[cell.getX()][cell.getY()];
        button.setUI(getBasicButton().getUI());
        button.setBackground(BasicColor.openCellColor);
        button.setText("" + cell.getDisplayValue());
        button.setFont(button.getFont().deriveFont(Font.BOLD));
        button.setBorder(new LineBorder(BasicColor.openCellColor));
        button.setForeground(BasicColor.fromValue(cell.getValue()));
    }

    private void openMine(Cell cell) {
        JButton button = cellButtons[cell.getX()][cell.getY()];
        button.setBackground(BasicColor.mineColor);
        button.setText("*");
        button.setFont(button.getFont().deriveFont(Font.BOLD));
        button.setBorder(new LineBorder(BasicColor.openCellColor));
        button.setForeground(Color.white);
    }

    private void markCell(Cell cell) {
        JButton button = cellButtons[cell.getX()][cell.getY()];
        button.setBackground(BasicColor.markedCellBackgroundColor);
        button.setText("!");
        button.setFont(button.getFont().deriveFont(Font.BOLD));
        button.setForeground(Color.white);
    }

    private void removeCellMark(Cell cell) {
        JButton button = cellButtons[cell.getX()][cell.getY()];
        button.setBackground(BasicColor.baseColor);
        button.setText("");
        button.setFont(button.getFont().deriveFont(Font.BOLD));
    }

    private void updateCellMarkedIncorrectly(Cell cell) {
        JButton button = cellButtons[cell.getX()][cell.getY()];
        button.setText("!*");
        button.setForeground(Color.white);
        button.setBackground(BasicColor.incorrectCellMarkBackgroundColor);
    }

    private void updateNrOfMinesTextField(int nrOfMines) {
        nrOfMinesTextField.setText("" + nrOfMines + " ");
    }

    private void gameWon(Void nullValue) {
        JOptionPane.showMessageDialog(this.getContentPane(), "Congratulations! You found all the mines!");
    }

    private void updateTimer(int integer) {
        timerTextField.setText("" + integer + " ");
    }

    private JButton getBasicButton() {
        return new JButton();
    }
}
