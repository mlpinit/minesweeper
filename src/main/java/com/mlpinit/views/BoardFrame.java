package com.mlpinit.views;

import com.mlpinit.controllers.MainController;
import com.mlpinit.models.MouseButtonEvent;
import com.mlpinit.models.Cell;
import com.mlpinit.models.Coordinate;
import com.mlpinit.utils.Log;

import rx.Observable;
import rx.schedulers.SwingScheduler;
import rx.subscriptions.Subscriptions;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class BoardFrame extends JFrame {
    private static final String TAG = "[BoardFrame]";
    private static final String[] markSymbols = { "", "!" };
    private static final Color regularNotOpenedButtonColor = new Color(105,105,105);
    private static final Color[] markColors = { regularNotOpenedButtonColor, new Color(210,105,30) };

    private Observable<Cell> openCellsObservable;
    private Observable<Cell> markCellsObservable;
    private Observable<Boolean> restartGameObservable;
    public Observable<MouseButtonEvent>[][] cellButtonBoardRequestObservables;
    public JButton[][] cellButtons;

    public BoardFrame(Observable<Cell> openCellsObservable, Observable<Cell> markCellsObservable) {
        super("Minesweeper");
        this.setResizable(false);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.cellButtons = new JButton[MainController.defaultHeight][MainController.defaultWidth];
        this.openCellsObservable = openCellsObservable;
        this.markCellsObservable = markCellsObservable;
        this.cellButtonBoardRequestObservables = new Observable[MainController.defaultHeight][MainController.defaultWidth];
        setupObservables();
        addComponentsToPane(this.getContentPane());
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public Observable<Boolean> getRestartGameObservable() {
        return restartGameObservable;
    }

    public Observable<MouseButtonEvent>[][] getCellButtonBoardRequestObservables() {
        return cellButtonBoardRequestObservables;
    }

    private void setupObservables() {
        openCellsObservable.subscribe(this::openCell);
        markCellsObservable.subscribe(this::markCell);
    }

    private void addComponentsToPane(final Container pane) {
        final JPanel menuPanel = new JPanel();
        JButton restartButton = new JButton("Restart");
        menuPanel.add(restartButton);
        restartGameObservable = createRestartGameObservable(restartButton);
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
                cellButtonBoardRequestObservables[i][j] = createBoardRequestObservable(button, coordinate);
                cellsPanel.add(button);
            }
        }
        pane.add(cellsPanel, BorderLayout.CENTER);
    }

    private void openCell(Cell cell) {
        Color color = new Color(220,220,220);
        Color mineColor = new Color(139,0,0);
        JButton button = cellButtons[cell.getX()][cell.getY()];
        if (cell.isMine()) {
            button.setBackground(mineColor);
        } else {
            button.setUI(getDefaultBasicButtonUI());
            button.setBackground(color);
        }
        button.setText("" + cell.getDisplayValue());
        button.setFont(button.getFont().deriveFont(Font.BOLD));
        button.setBorder(new LineBorder(color));
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

    private ButtonUI getDefaultBasicButtonUI() {
        return new JButton().getUI();
    }

    private Observable<MouseButtonEvent> createBoardRequestObservable(final Component component, Coordinate coordinate) {
        return Observable.create((Observable.OnSubscribe<MouseButtonEvent>) subscriber -> {
            final MouseListener listener = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent event) {
                    subscriber.onNext(new MouseButtonEvent(coordinate, event.getButton(), event.getID()));
                }

                @Override
                public void mouseReleased(MouseEvent event) {
                    subscriber.onNext(new MouseButtonEvent(coordinate, event.getButton(), event.getID()));
                }

                @Override
                public void mouseEntered(MouseEvent event) {
                    subscriber.onNext(new MouseButtonEvent(coordinate, event.getButton(), event.getID()));
                }

                @Override
                public void mouseExited(MouseEvent event) {
                }
            };
            component.addMouseListener(listener);

            subscriber.add(Subscriptions.create(() -> component.removeMouseListener(listener)));
        })
        .subscribeOn(SwingScheduler.getInstance())
        .unsubscribeOn(SwingScheduler.getInstance());
    }

    private Observable<Boolean> createRestartGameObservable(final Component component) {
        return Observable.create((Observable.OnSubscribe<Boolean>) subscriber -> {
            final MouseListener listener = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    subscriber.onNext(true);
                }

                @Override
                public void mousePressed(MouseEvent event) {
                }

                @Override
                public void mouseReleased(MouseEvent event) {
                }

                @Override
                public void mouseEntered(MouseEvent event) {
                }

                @Override
                public void mouseExited(MouseEvent event) {
                }
            };
            component.addMouseListener(listener);

            subscriber.add(Subscriptions.create(() -> component.removeMouseListener(listener)));
        })
        .subscribeOn(SwingScheduler.getInstance())
        .unsubscribeOn(SwingScheduler.getInstance());
    }
}
