package com.mlpinit.controllers;

import com.mlpinit.views.OptionsFrame;

import java.awt.event.WindowEvent;

public class OptionsController {

    private OptionsFrame optionsFrame;

    public OptionsController() {
        this.optionsFrame = new OptionsFrame();
        optionsFrame.gameModeObservable.subscribe(data -> {
            new BoardController(data[0], data[1], data[2]);
            closeFrame();
        });
    }

    private void closeFrame() {
        optionsFrame.dispatchEvent(new WindowEvent(optionsFrame, WindowEvent.WINDOW_CLOSING));
    }

    public static void main(String[] args) {
        new OptionsController();
    }
}
