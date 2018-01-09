package com.mlpinit.views;

import com.mlpinit.controllers.MainController;
import com.mlpinit.models.MinesweeperTimer;
import com.mlpinit.models.MouseButtonEvent;
import com.mlpinit.models.Cell;
import com.mlpinit.models.Coordinate;
import com.mlpinit.utils.Log;

import rx.Observable;
import rx.observables.SwingObservable;

import javax.sound.sampled.Line;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.MouseEvent;

public class BoardFrame extends JFrame {
    private static final String TAG = "[BoardFrame]";
    private static final String[] markSymbols = { "", "!" };
    private static final Color baseColor = new Color(105,105,105);
    private static final Color[] markColors = { baseColor, new Color(210,105,30) };
    private static final Color openCellColor = new Color(220,220,220);
    private static final Color mineColor = new Color(139,0,0);

    private JTextField nrOfMinesTextField;
    private JTextField timerTextField;
    private int nrOfMines;

    private Observable<Cell> openCellsObservable;
    private Observable<Cell> markCellsObservable;
    private Observable<Cell> incorrectMarkCellsObservable;
    private Observable<Cell> openMineCellObservable;
    private Observable<Integer> elapsedTimeObservable;
    private Observable<MouseEvent> restartGameObservable;
    public Observable<MouseButtonEvent> cellButtonBoardRequestObservable;
    public JButton[][] cellButtons;

    public BoardFrame(Observable<Cell> openCellsObservable, Observable<Cell> markCellsObservable,
                      Observable<Cell> incorrectMarkCellsObservable, Observable<Cell> openMineCellObservable,
                      Observable<Integer> elapsedTimeObservable)
    {
        super("Minesweeper");
        this.setResizable(false);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.cellButtons = new JButton[MainController.defaultHeight][MainController.defaultWidth];
        this.openCellsObservable = openCellsObservable;
        this.markCellsObservable = markCellsObservable;
        this.cellButtonBoardRequestObservable = Observable.empty();
        this.incorrectMarkCellsObservable = incorrectMarkCellsObservable;
        this.openMineCellObservable = openMineCellObservable;
        this.elapsedTimeObservable = elapsedTimeObservable;
        setupObservables();
        this.nrOfMines = MainController.defaultNrOfMines;
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

    private void setupObservables() {
        openCellsObservable.subscribe(this::openCell);
        markCellsObservable.subscribe(this::markCell);
        incorrectMarkCellsObservable.subscribe(this::updateCellMarkedIncorrectly);
        openMineCellObservable.subscribe(this::openMine);
        elapsedTimeObservable.subscribe(this::updateTimer);
    }

    private void addComponentsToPane(final Container pane) {
        final JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BorderLayout());
        JButton restartButton = new JButton("Restart");
        restartButton.setBorder(new LineBorder(baseColor, 3));
        restartButton.setPreferredSize(new Dimension(200, 50));
        menuPanel.add(restartButton, BorderLayout.CENTER);
        nrOfMinesTextField = new JTextField(" " + nrOfMines + " ");
        nrOfMinesTextField.setFont(new Font("sans-serif", Font.PLAIN, 20));
        nrOfMinesTextField.setBorder(new LineBorder(baseColor, 3));
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
        timerTextField.setBorder(new LineBorder(baseColor, 3));
        timerTextField.setPreferredSize(new Dimension(60, 50));
        timerTextField.setHorizontalAlignment(SwingConstants.RIGHT);

        menuPanel.add(timerTextField, BorderLayout.EAST);
        restartGameObservable = SwingObservable.fromMouseEvents(restartButton);
        pane.add(menuPanel, BorderLayout.NORTH);

        final JPanel cellsPanel = new JPanel();
        cellsPanel.setLayout(new GridLayout(0,30));
        for (int i = 0; i < MainController.defaultHeight; i++) {
            for (int j = 0; j < MainController.defaultWidth; j++) {
                final Coordinate coordinate = new Coordinate(i, j);
                JButton button = new JButton();
                button.setUI((ButtonUI) BasicButtonUI.createUI(button));
                button.setBackground(baseColor);
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
        button.setBackground(openCellColor);
        button.setText("" + cell.getDisplayValue());
        button.setFont(button.getFont().deriveFont(Font.BOLD));
        button.setBorder(new LineBorder(openCellColor));
        button.setForeground(cell.getForegroundColor());
    }

    private void openMine(Cell cell) {
        JButton button = cellButtons[cell.getX()][cell.getY()];
        button.setBackground(mineColor);
        button.setText("" + cell.getDisplayValue());
        button.setFont(button.getFont().deriveFont(Font.BOLD));
        button.setBorder(new LineBorder(openCellColor));
        button.setForeground(cell.getForegroundColor());
    }

    private void markCell(Cell cell) {
        int markType = cell.isMarked() ? 1 : 0;
        nrOfMines += cell.isMarked() ? -1 : 1;
        nrOfMinesTextField.setText("" + nrOfMines + " ");
        JButton button = cellButtons[cell.getX()][cell.getY()];
        button.setBackground(markColors[markType]);
        button.setText(markSymbols[markType]);
        button.setFont(button.getFont().deriveFont(Font.BOLD));
        button.setForeground(Color.white);
    }

    private void updateCellMarkedIncorrectly(Cell cell) {
        JButton button = cellButtons[cell.getX()][cell.getY()];
        button.setText("!*");
        button.setForeground(Color.white);
        button.setBackground(new Color(255,140,0));
    }

    private void updateTimer(int integer) {
        timerTextField.setText("" + integer + " ");
    }

    private JButton getBasicButton() {
        return new JButton();
    }
}
