import java.awt.*;

public class ThemeManager {

    public enum LadderStyle {
        CLASSIC, SOLID, DOTTED
    }

    public enum Theme {
        // Tambahkan parameter path gambar di akhir constructor
        HELL("Hellfire Domain",
                new Color(40, 0, 0), new Color(178, 34, 34), new Color(255, 140, 0),
                LadderStyle.SOLID, new Color(139, 0, 0), Color.WHITE,
                "/images/bg_hell.png"), // <-- Path Gambar

        JUNGLE("Ancient Jungle",
                new Color(34, 139, 34), new Color(144, 238, 144), new Color(139, 69, 19),
                LadderStyle.DOTTED, new Color(0, 100, 0), Color.WHITE,
                "/images/bg_jungle.png"), // <-- Path Gambar

        GRAVEYARD("Haunted Graveyard",
                new Color(47, 79, 79), new Color(72, 61, 139), new Color(220, 220, 220),
                LadderStyle.CLASSIC, new Color(25, 25, 112), Color.WHITE,
                "/images/bg_graveyard.png"), // <-- Path Gambar

        MOUNTAIN("Rocky Mountain",
                new Color(105, 105, 105), new Color(211, 211, 211), new Color(101, 67, 33),
                LadderStyle.SOLID, new Color(47, 79, 79), Color.WHITE,
                "/images/bg_mountain.png"); // <-- Path Gambar

        public final String name;
        public final Color tileColor1;
        public final Color tileColor2;
        public final Color ladderColor;
        public final LadderStyle ladderStyle;
        public final Color btnBg;
        public final Color btnFg;
        public final String bgImagePath; // [BARU] Field untuk path gambar

        // Update Constructor
        Theme(String name, Color t1, Color t2, Color lC, LadderStyle lS, Color bBg, Color bFg, String bgPath) {
            this.name = name;
            this.tileColor1 = t1;
            this.tileColor2 = t2;
            this.ladderColor = lC;
            this.ladderStyle = lS;
            this.btnBg = bBg;
            this.btnFg = bFg;
            this.bgImagePath = bgPath; // [BARU]
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static Theme currentTheme = Theme.HELL;

    public static void setTheme(Theme theme) {
        currentTheme = theme;
    }

    public static Theme getCurrentTheme() {
        return currentTheme;
    }
}