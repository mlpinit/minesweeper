package com.mlpinit.views;

import com.mlpinit.controllers.MainController;
import com.mlpinit.models.MouseButtonEvent;
import com.mlpinit.models.Cell;
import com.mlpinit.models.Coordinate;
import com.mlpinit.utils.Log;

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
    private static final String[] markSymbols = { "", "!" };
    private static final Color regularNotOpenedButtonColor = new Color(105,105,105);
    private static final Color[] markColors = { regularNotOpenedButtonColor, new Color(210,105,30) };
    private static final Color openCellColor = new Color(220,220,220);
    private static final Color mineColor = new Color(139,0,0);

    private Observable<Cell> openCellsObservable;
    private Observable<Cell> markCellsObservable;
    private Observable<Cell> incorrectMarkCellsObservable;
    private Observable<Cell> openMineCellObservable;
    private Observable<MouseEvent> restartGameObservable;
    public Observable<MouseButtonEvent> cellButtonBoardRequestObservable = null;
    public JButton[][] cellButtons;

    public BoardFrame(Observable<Cell> openCellsObservable, Observable<Cell> markCellsObservable,
                      Observable<Cell> incorrectMarkCellsObservable, Observable<Cell> openMineCellObservable)
    {
        super("Minesweeper");
        this.setResizable(false);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.cellButtons = new JButton[MainController.defaultHeight][MainController.defaultWidth];
        this.openCellsObservable = openCellsObservable;
        this.markCellsObservable = markCellsObservable;
        this.incorrectMarkCellsObservable = incorrectMarkCellsObservable;
        this.openMineCellObservable = openMineCellObservable;
        setupObservables();
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
    }

    private void addComponentsToPane(final Container pane) {
        final JPanel menuPanel = new JPanel();
        JButton restartButton = new JButton("Restart");
        menuPanel.add(restartButton);
        restartGameObservable = SwingObservable.fromMouseEvents(restartButton);
        pane.add(menuPanel, BorderLayout.NORTH);

        final JPanel cellsPanel = new JPanel();
        cellsPanel.setLayout(new GridLayout(0,30));
        for (int obsIndex = 0, i = 0; i < MainController.defaultHeight; i++) {
            for (int j = 0; j < MainController.defaultWidth; j++) {
                final Coordinate coordinate = new Coordinate(i, j);
                JButton button = new JButton();
                button.setUI((ButtonUI) BasicButtonUI.createUI(button));
                button.setBackground(regularNotOpenedButtonColor);
                button.setPreferredSize(new Dimension(25 ,25));
                button.setBorder(BorderFactory.createEtchedBorder());
                cellButtons[i][j] = button;

                Observable<MouseButtonEvent> observable = SwingObservable.fromMouseEvents(button)
                        .map(event -> new MouseButtonEvent(coordinate, event.getButton(), event.getID()));
                if (cellButtonBoardRequestObservable == null) {
                    cellButtonBoardRequestObservable = observable;
                } else {
                    cellButtonBoardRequestObservable = cellButtonBoardRequestObservable.mergeWith(observable);
                }
                cellsPanel.add(button);
            }
        }
        pane.add(cellsPanel, BorderLayout.CENTER);
    }

    private void openCell(Cell cell) {
        JButton button = cellButtons[cell.getX()][cell.getY()];
        button.setUI(getDefaultBasicButtonUI());
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

    private ButtonUI getDefaultBasicButtonUI() {
        return new JButton().getUI();
    }
}
