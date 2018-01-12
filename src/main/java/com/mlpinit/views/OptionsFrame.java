package com.mlpinit.views;

import rx.Observable;
import rx.observables.SwingObservable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class OptionsFrame extends JFrame {

    private final static Integer[] beginnerMode = {8, 8, 10};
    private final static Integer[] intermediateMode = {16, 16, 40};
    private final static Integer[] expertMode = {16, 30, 99};

    public Observable<Integer[]> gameModeObservable;

    public OptionsFrame() {
        super("Minesweeper");
        this.setPreferredSize(new Dimension(450, 800));
        this.setResizable(false);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addComponentsToPane(this.getContentPane());
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public void addComponentsToPane(final Container pane) {
        pane.setLayout(new GridLayout(3,1));
        JButton beginnerButton = new JButton("Beginner");
        styleOptionButton(beginnerButton);
        pane.add(beginnerButton, BorderLayout.NORTH);
        gameModeObservable = SwingObservable.fromMouseEvents(beginnerButton)
                .filter(mouseEvent -> mouseEvent.getID() == MouseEvent.MOUSE_CLICKED)
                .map(event -> beginnerMode);
        JButton intermediateButton = new JButton("Intermediate");
        styleOptionButton(intermediateButton);
        pane.add(intermediateButton, BorderLayout.CENTER);
        gameModeObservable = gameModeObservable.mergeWith(SwingObservable.fromMouseEvents(intermediateButton)
                .filter(mouseEvent -> mouseEvent.getID() == MouseEvent.MOUSE_CLICKED)
                .map(event -> intermediateMode));
        JButton expertButton = new JButton("Expert");
        styleOptionButton(expertButton);
        pane.add(expertButton, BorderLayout.SOUTH);
        gameModeObservable = gameModeObservable.mergeWith(SwingObservable.fromMouseEvents(expertButton)
                .filter(mouseEvent -> mouseEvent.getID() == MouseEvent.MOUSE_CLICKED)
                .map(event -> expertMode));
    }

    private void styleOptionButton(JButton button) {
        button.setPreferredSize(new Dimension(250, 100));
        button.setFont(button.getFont().deriveFont(Font.BOLD, 20));
    }
}
