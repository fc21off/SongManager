package de.st197974.songmanager.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class for managing and applying application themes.
 * This class provides methods to toggle between dark and light themes
 * and retrieve common custom colors defined for each theme.
 */
public final class AppTheme {

    private AppTheme() {
    }

    private static boolean dark = true;

    public static void applyDarkTheme() {
        try {
            FlatDarkLaf.setup();
            applyCommonColors(true);
            FlatLaf.updateUI();
            dark = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void applyLightTheme() {
        try {
            FlatLightLaf.setup();
            applyCommonColors(false);
            FlatLaf.updateUI();
            dark = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void toggleTheme() {
        if (dark) applyLightTheme();
        else applyDarkTheme();
    }

    public static boolean isDark() {
        return dark;
    }

    private static void applyCommonColors(boolean darkMode) {

        UIManager.put("App.accentColor", darkMode ? new Color(118, 117, 226) : new Color(113, 179, 223));

        UIManager.put("App.dangerColor", darkMode ? new Color(255, 110, 110) : new Color(200, 60, 60));

        UIManager.put("App.sidebarColor", darkMode ? new Color(43, 43, 43) : Color.WHITE);

        UIManager.put("App.selectionColor", darkMode ? new Color(146, 145, 251) : new Color(199, 221, 253));

        UIManager.put("App.dividerColor", darkMode ? new Color(81, 81, 81) : new Color(220, 220, 220));


        UIManager.put("TabbedPane.tabHeight", 40);
        UIManager.put("TabbedPane.tabInsets", new Insets(5, 15, 5, 15));
    }

    public static Color accent() {
        return UIManager.getColor("App.accentColor");
    }

    public static Color danger() {
        return UIManager.getColor("App.dangerColor");
    }

    public static Color sidebar() {
        return UIManager.getColor("App.sidebarColor");
    }

    public static Color selection() {
        return UIManager.getColor("App.selectionColor");
    }
}
