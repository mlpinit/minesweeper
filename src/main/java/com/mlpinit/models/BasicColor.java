package com.mlpinit.models;

import javax.swing.plaf.ButtonUI;
import java.awt.*;

public class BasicColor {
    private static final String TAG = "[BasicColor]";

    public static final Color baseColor = new Color(105,105,105);
    public static final Color openCellColor = new Color(220,220,220);
    public static final Color mineColor = new Color(139,0,0);
    public static final Color markedCellBackgroundColor = new Color(210,105,30);
    public static final Color incorrectCellMarkBackgroundColor = new Color(255,140,0);
    public final static Color[] colors = {
            Color.white,                           // not used
            new Color(11,36,251),        // => 1
            new Color(14,122,17),        // => 2
            new Color(252,13,27),        // => 3
            new Color(2,11,121)  ,       // => 4
            new Color(163, 141, 28),     // => 5
            new Color(169,169,169),      // => 6
            new Color(255,140,0),        // => 7
            new Color(0,0,0)             // => 8
    };

    public static Color fromValue(int value) {
        if (value == -1) {
            return mineColor;
        } else {
            return colors[value];
        }
    }
}
