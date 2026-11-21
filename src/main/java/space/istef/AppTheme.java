package space.istef;

import java.awt.Color;
import java.util.prefs.Preferences;

public final class AppTheme {

    public enum Mode {
        DARK, LIGHT
    }

    private static final String PREF_KEY_THEME = "theme.mode";
    private static final Preferences PREFS = Preferences.userNodeForPackage(TrayApp.class);

    public static final AppTheme DARK = new AppTheme(
            Mode.DARK,
            new Color(0x1E, 0x1E, 0x1E), // popup/window background
            new Color(0x44, 0x44, 0x44), // outer border
            new Color(0x1E, 0x1E, 0x1E), // button background
            new Color(0x1E, 0x1E, 0x1E), // button border
            new Color(0x86, 0xAB, 0xFF), // button text
            new Color(0x2B, 0x37, 0x51)  // buttonHoverBg
    );

    public static final AppTheme LIGHT = new AppTheme(
            Mode.LIGHT,
            new Color(0xF4, 0xF6, 0xFF), // popup/window background
            new Color(0xC7, 0xD3, 0xF1), // outer border
            new Color(0xF4, 0xF6, 0xFF), // button background
            new Color(0xF4, 0xF6, 0xFF), // button border
            new Color(0x30, 0x65, 0xED), // button text
            new Color(0xDD, 0xDF, 0xF1)  // buttonHoverBg
    );

    public final Mode mode;
    public final Color surface;
    public final Color border;
    public final Color buttonBg;
    public final Color buttonBorder;
    public final Color buttonFg;
    public final Color buttonHoverBg;

    private AppTheme(Mode mode, Color surface, Color border, Color buttonBg, Color buttonBorder, Color buttonFg, Color buttonHoverBg) {
        this.mode = mode;
        this.surface = surface;
        this.border = border;
        this.buttonBg = buttonBg;
        this.buttonBorder = buttonBorder;
        this.buttonFg = buttonFg;
        this.buttonHoverBg = buttonHoverBg;
    }

    public static Mode loadSavedMode() {
        String raw = PREFS.get(PREF_KEY_THEME, Mode.DARK.name());
        try {
            return Mode.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return Mode.DARK;
        }
    }

    public static void saveMode(Mode mode) {
        PREFS.put(PREF_KEY_THEME, mode.name());
    }

    public static AppTheme of(Mode mode) {
        return mode == Mode.LIGHT ? LIGHT : DARK;
    }
}