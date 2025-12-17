import java.awt.*;

public class ThemeManager {

    // Enum untuk jenis gaya tangga (digunakan oleh BoardPanel)
    public enum LadderStyle {
        CLASSIC, // Garis simple / Tulang
        SOLID,   // Garis tebal solid / Batu / Rantai
        DOTTED   // Garis putus-putus / Akar / Tali
    }

    // Enum Tema yang disesuaikan dengan 4 Boss
    public enum Theme {
        // 1. TEMA HELL (Nuansa Merah Gelap & Lava)
        HELL("Hellfire Domain",
                new Color(40, 0, 0),       // Tile 1: Merah Kehitaman
                new Color(178, 34, 34),    // Tile 2: Firebrick Red
                new Color(255, 140, 0),    // Ladder: Oranye Terang (Lava)
                LadderStyle.SOLID,
                new Color(139, 0, 0),      // Button Bg: Dark Red
                Color.WHITE),              // Button Fg

        // 2. TEMA JUNGLE (Nuansa Hijau & Alam)
        JUNGLE("Ancient Jungle",
                new Color(34, 139, 34),    // Tile 1: Forest Green
                new Color(144, 238, 144),  // Tile 2: Light Green
                new Color(139, 69, 19),    // Ladder: Coklat (Kayu/Akar)
                LadderStyle.DOTTED,        // Style Dotted (mirip akar merambat)
                new Color(0, 100, 0),      // Button Bg: Dark Green
                Color.WHITE),

        // 3. TEMA GRAVEYARD (Nuansa Ungu Gelap & Kelabu)
        GRAVEYARD("Haunted Graveyard",
                new Color(47, 79, 79),     // Tile 1: Dark Slate Gray
                new Color(72, 61, 139),    // Tile 2: Dark Slate Blue/Purple
                new Color(220, 220, 220),  // Ladder: Putih Tulang
                LadderStyle.CLASSIC,       // Style Classic (mirip tulang)
                new Color(25, 25, 112),    // Button Bg: Midnight Blue
                Color.WHITE),

        // 4. TEMA MOUNTAIN (Nuansa Batu & Tanah)
        MOUNTAIN("Rocky Mountain",
                new Color(105, 105, 105),  // Tile 1: Dim Gray
                new Color(211, 211, 211),  // Tile 2: Light Gray
                new Color(101, 67, 33),    // Ladder: Coklat Tua (Rantai/Tali Tambang)
                LadderStyle.SOLID,
                new Color(47, 79, 79),     // Button Bg: Dark Slate
                Color.WHITE);

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

    private static Theme currentTheme = Theme.HELL; // Default start di Hell atau Jungle

    public static void setTheme(Theme theme) {
        currentTheme = theme;
    }

    public static Theme getCurrentTheme() {
        return currentTheme;
    }
}