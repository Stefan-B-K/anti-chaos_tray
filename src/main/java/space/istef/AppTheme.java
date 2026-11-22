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
            new Color(0x1E, 0x1E, 0x1E),       // surface (popup/card background)
            new Color(0x71, 0x97, 0xF9, 0x66),       // border
            new Color(0x1E, 0x1E, 0x1E),       // buttonBg
            new Color(0x56, 0x6E, 0xA3),       // buttonBorder
            new Color(0x86, 0xAB, 0xFF),       // buttonFg
            new Color(0x2B, 0x37, 0x51),       // buttonHoverBg
            new Color(0x2D, 0x2D, 0x2D),       // inputBg
            new Color(0x44, 0x44, 0x44),       // inputBorder
            new Color(0xFF, 0xA7, 0xA7)        // errorColor
    );

    public static final AppTheme LIGHT = new AppTheme(
            Mode.LIGHT,
            new Color(0xF4, 0xF6, 0xFF),       // surface (popup/card background)
            new Color(0x1, 0x25, 0x9A, 0x40),       // border
            new Color(0xF4, 0xF6, 0xFF),       // buttonBg
            new Color(0x88, 0xA9, 0xFD),       // buttonBorder
            new Color(0x30, 0x65, 0xED),       // buttonFg
            new Color(0xDD, 0xDF, 0xF1),       // buttonHoverBg
            new Color(0xF8, 0xF9, 0xFF),       // inputBg
            new Color(174, 186, 231, 140),     // inputBorder
            new Color(0xFF, 0x54, 0x54)        // errorColor
    );

    public final Mode mode;
    public final Color surface;
    public final Color border;
    public final Color buttonBg;
    public final Color buttonBorder;
    public final Color buttonFg;
    public final Color buttonHoverBg;
    public final Color inputBg;
    public final Color inputBorder;
    public final Color errorColor;

    private AppTheme(
            Mode mode,
            Color surface,
            Color border,
            Color buttonBg,
            Color buttonBorder,
            Color buttonFg,
            Color buttonHoverBg,
            Color inputBg,
            Color inputBorder,
            Color errorColor
    ) {
        this.mode = mode;
        this.surface = surface;
        this.border = border;
        this.buttonBg = buttonBg;
        this.buttonBorder = buttonBorder;
        this.buttonFg = buttonFg;
        this.buttonHoverBg = buttonHoverBg;
        this.inputBg = inputBg;
        this.inputBorder = inputBorder;
        this.errorColor = errorColor;
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