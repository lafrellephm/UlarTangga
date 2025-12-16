import java.awt.*;

public class ThemeManager {

    // Enum untuk jenis gaya tangga
    public enum LadderStyle {
        CLASSIC, // Garis simple
        SOLID,   // Garis tebal solid
        DOTTED   // Garis putus-putus (seperti tali)
    }

    // Enum Tema yang tersedia
    public enum Theme {
        CLASSIC("Classic",
                new Color(245, 245, 220), new Color(176, 224, 230), // Tiles
                new Color(34, 139, 34), LadderStyle.CLASSIC,        // Ladder
                new Color(70, 130, 180), Color.WHITE),              // Button

        DARK_MODE("Dark Neon",
                new Color(60, 60, 60), new Color(80, 80, 80),
                new Color(0, 255, 127), LadderStyle.SOLID,
                new Color(255, 69, 0), Color.BLACK),

        OCEAN("Ocean Breeze",
                new Color(224, 255, 255), new Color(135, 206, 250),
                new Color(255, 140, 0), LadderStyle.DOTTED,
                new Color(0, 105, 148), Color.WHITE),

        WOODEN("Retro Wood",
                new Color(222, 184, 135), new Color(139, 69, 19),
                new Color(101, 67, 33), LadderStyle.SOLID,
                new Color(160, 82, 45), Color.WHITE);

        public final String name;
        public final Color tileColor1;
        public final Color tileColor2;
        public final Color ladderColor;
        public final LadderStyle ladderStyle;
        public final Color btnBg;
        public final Color btnFg;

        Theme(String name, Color t1, Color t2, Color lC, LadderStyle lS, Color bBg, Color bFg) {
            this.name = name;
            this.tileColor1 = t1;
            this.tileColor2 = t2;
            this.ladderColor = lC;
            this.ladderStyle = lS;
            this.btnBg = bBg;
            this.btnFg = bFg;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static Theme currentTheme = Theme.CLASSIC;

    public static void setTheme(Theme theme) {
        currentTheme = theme;
    }

    public static Theme getCurrentTheme() {
        return currentTheme;
    }
}