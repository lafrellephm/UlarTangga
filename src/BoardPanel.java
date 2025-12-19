import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class BoardPanel extends JPanel {

    // --- Data Logika ---
    private final int TOTAL_TILES = 64;
    private final int TILE_SIZE = 55;

    // Koordinat visual untuk setiap Tile (1-64)
    private Point[] tileCoords = new Point[TOTAL_TILES + 1];

    // Nilai Poin di setiap tile
    private int[] tilePoints = new int[TOTAL_TILES + 1];

    // Data Visual Tangga & Bos
    private Map<Integer, Integer> laddersVisual;
    private Set<Integer> bossTiles = new HashSet<>();

    // --- Komponen & Aset ---
    private JPanel[] squarePanels = new JPanel[TOTAL_TILES + 1];
    private Map<Integer, BufferedImage> bossImages = new HashMap<>();
    private Map<ThemeManager.Theme, Image> themeBackgrounds = new HashMap<>();
    private Random visualRandom = new Random();

    public BoardPanel() {
        setLayout(null); // Absolute positioning agar tile bisa diatur koordinatnya

        initTilePoints();
        loadBossImages();
        loadThemeBackgrounds(); // Cache gambar background

        initPathCoords();
        initVisualUI();
    }

    // --- Inisialisasi Aset ---

    private void loadThemeBackgrounds() {
        for (ThemeManager.Theme theme : ThemeManager.Theme.values()) {
            try {
                java.net.URL imgUrl = getClass().getResource(theme.bgImagePath);
                if (imgUrl != null) {
                    BufferedImage bg = ImageIO.read(imgUrl);
                    themeBackgrounds.put(theme, bg);
                } else {
                    System.err.println("Background tidak ditemukan: " + theme.bgImagePath);
                }
            } catch (IOException e) {
                System.err.println("Gagal memuat background " + theme.name + ": " + e.getMessage());
            }
        }
    }

    private void loadBossImages() {
        try {
            for (int i = 1; i <= 4; i++) {
                java.net.URL imgUrl = getClass().getResource("/images/bos" + i + ".png");
                if (imgUrl != null) bossImages.put(i, ImageIO.read(imgUrl));
            }
        } catch (IOException e) {
            System.err.println("Gagal memuat gambar bos: " + e.getMessage());
        }
    }

    // --- Logika Game pada Board ---

    // Generate poin acak untuk tile
    private void initTilePoints() {
        Random rand = new Random();
        tilePoints[1] = 0; // Start tidak ada poin
        for (int i = 2; i <= 64; i++) tilePoints[i] = (rand.nextInt(5) + 1) * 10;
    }

    public void generateNewTilePoints() { initTilePoints(); }

    public int claimPointsAt(int pos) {
        if (pos < 1 || pos > 64) return 0;
        int points = tilePoints[pos];
        if (points > 0) {
            tilePoints[pos] = 0; // Poin diambil
            // Hapus label poin dari visual tile
            JPanel square = squarePanels[pos];
            if (square != null) {
                BorderLayout layout = (BorderLayout) square.getLayout();
                Component southComp = layout.getLayoutComponent(BorderLayout.SOUTH);
                if (southComp != null) square.remove(southComp);
                square.revalidate();
                square.repaint();
            }
        }
        return points;
    }

    // Generate posisi bos secara acak
    public void generateBossTiles(int count) {
        bossTiles.clear();
        Random rand = new Random();
        int attempts = 0;
        while (bossTiles.size() < count && attempts < 1000) {
            attempts++;
            int pos = rand.nextInt(62) + 2; // Hindari tile 1 dan 64
            if (!bossTiles.contains(pos)) bossTiles.add(pos);
        }
        repaint();
    }

    public boolean isBossTile(int pos) { return bossTiles.contains(pos); }

    public void setLaddersMap(Map<Integer, Integer> ladders) {
        this.laddersVisual = ladders;
        repaint();
    }

    // --- Logika Visual (Rendering) ---

    // Override paintComponent untuk menggambar Background Image
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        ThemeManager.Theme currentTheme = ThemeManager.getCurrentTheme();
        if (currentTheme == null) currentTheme = ThemeManager.Theme.HELL;

        Image bgImage = themeBackgrounds.get(currentTheme);

        if (bgImage != null) {
            Graphics2D g2d = (Graphics2D) g;
            // Stretch background memenuhi panel
            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            // Overlay gelap transparan agar tile lebih kontras
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        } else {
            g.setColor(new Color(20, 20, 20));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    // Override paintChildren untuk menggambar Tangga & Bos di atas background tapi di bawah/atas komponen
    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        if (laddersVisual == null && bossTiles.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        ThemeManager.Theme theme = ThemeManager.getCurrentTheme();
        if (theme == null) theme = ThemeManager.Theme.HELL;

        // 1. Gambar Tangga
        if (laddersVisual != null) {
            for (Map.Entry<Integer, Integer> entry : laddersVisual.entrySet()) {
                int startNode = entry.getKey();
                int endNode = entry.getValue();
                Point p1Global = tileCoords[startNode];
                Point p2Global = tileCoords[endNode];

                if (p1Global != null && p2Global != null) {
                    // Hitung pusat tile
                    Point p1 = new Point(p1Global.x + TILE_SIZE / 2, p1Global.y + TILE_SIZE / 2);
                    Point p2 = new Point(p2Global.x + TILE_SIZE / 2, p2Global.y + TILE_SIZE / 2);

                    if (theme.ladderStyle == ThemeManager.LadderStyle.DOTTED) {
                        drawDottedLadder(g2, p1, p2, theme.ladderColor);
                    } else {
                        drawRealisticLadder(g2, p1, p2, theme.ladderColor);
                    }
                }
            }
        }

        // 2. Gambar Bos
        int bossIndex = 0;
        for (Integer pos : bossTiles) {
            Point pGlobal = tileCoords[pos];
            if (pGlobal != null) {
                int imgId = (bossIndex % 4) + 1;
                BufferedImage img = bossImages.get(imgId);
                int x = pGlobal.x + (TILE_SIZE - 40) / 2;
                int y = pGlobal.y + (TILE_SIZE - 40) / 2;

                if (img != null) {
                    g2.drawImage(img, x, y, 40, 40, null);
                } else {
                    g2.setColor(Color.RED);
                    g2.fillOval(x, y, 30, 30);
                }
                bossIndex++;
            }
        }
        g2.dispose();
    }

    // Mengupdate posisi pion pemain di GUI
    public void updatePlayerPawns(Collection<Player> players) {
        // Bersihkan semua tile dari pion lama
        for (int i = 1; i <= TOTAL_TILES; i++) {
            JPanel sq = squarePanels[i];
            if (sq != null) {
                BorderLayout layout = (BorderLayout) sq.getLayout();
                Component centerComp = layout.getLayoutComponent(BorderLayout.CENTER);
                if (centerComp != null) sq.remove(centerComp);
                sq.revalidate();
                sq.repaint();
            }
        }

        if (players == null || players.isEmpty()) return;

        // Kelompokkan pemain berdasarkan posisi
        Map<Integer, List<Player>> playersByPos = new HashMap<>();
        for (Player p : players) {
            int pos = Math.max(1, Math.min(p.getPosition(), TOTAL_TILES));
            playersByPos.computeIfAbsent(pos, k -> new ArrayList<>()).add(p);
        }

        // Gambar pion baru
        for (Map.Entry<Integer, List<Player>> entry : playersByPos.entrySet()) {
            int pos = entry.getKey();
            List<Player> playersAtPos = entry.getValue();
            JPanel targetSquare = squarePanels[pos];

            if (targetSquare != null) {
                JPanel pawnsContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
                pawnsContainer.setOpaque(false);

                for (Player p : playersAtPos) {
                    JPanel pawn = new JPanel();
                    pawn.setBackground(p.getColor());
                    int size = (playersAtPos.size() > 2) ? 10 : 15; // Kecilkan jika ramai
                    pawn.setPreferredSize(new Dimension(size, size));
                    pawn.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    pawn.setToolTipText(p.getName());
                    pawnsContainer.add(pawn);
                }
                targetSquare.add(pawnsContainer, BorderLayout.CENTER);
                targetSquare.revalidate();
            }
        }
        repaint();
    }

    // --- Helper Drawing ---

    private void drawDottedLadder(Graphics2D g2, Point p1, Point p2, Color color) {
        g2.setColor(color);
        Stroke dashed = new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
        g2.setStroke(dashed);
        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        int dotSize = 10;
        g2.setColor(color.darker());
        g2.fillOval(p1.x - dotSize/2, p1.y - dotSize/2, dotSize, dotSize);
        g2.fillOval(p2.x - dotSize/2, p2.y - dotSize/2, dotSize, dotSize);
    }

    private void drawRealisticLadder(Graphics2D g2, Point p1, Point p2, Color color) {
        int ladderWidth = 18;
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        int numSteps = (int) (distance / 15);

        // Vektor tegak lurus
        double unitX = dx / distance;
        double unitY = dy / distance;
        double perpX = -unitY * (ladderWidth / 2.0);
        double perpY = unitX * (ladderWidth / 2.0);

        // Koordinat Tiang Kiri & Kanan
        int x1Left = (int) (p1.x + perpX); int y1Left = (int) (p1.y + perpY);
        int x2Left = (int) (p2.x + perpX); int y2Left = (int) (p2.y + perpY);
        int x1Right = (int) (p1.x - perpX); int y1Right = (int) (p1.y - perpY);
        int x2Right = (int) (p2.x - perpX); int y2Right = (int) (p2.y - perpY);

        // Gambar Tiang
        g2.setColor(color);
        g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x1Left, y1Left, x2Left, y2Left);
        g2.drawLine(x1Right, y1Right, x2Right, y2Right);

        // Highlight Efek
        g2.setStroke(new BasicStroke(2));
        g2.setColor(new Color(255, 255, 255, 60));
        g2.drawLine(x1Left, y1Left, x2Left, y2Left);
        g2.drawLine(x1Right, y1Right, x2Right, y2Right);

        // Gambar Anak Tangga
        g2.setColor(color);
        g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 1; i < numSteps; i++) {
            double fraction = (double) i / numSteps;
            int stepX1 = (int) (x1Left + (x2Left - x1Left) * fraction);
            int stepY1 = (int) (y1Left + (y2Left - y1Left) * fraction);
            int stepX2 = (int) (x1Right + (x2Right - x1Right) * fraction);
            int stepY2 = (int) (y1Right + (y2Right - y1Right) * fraction);
            g2.drawLine(stepX1, stepY1, stepX2, stepY2);
        }
    }

    // --- Inisialisasi Visual Tile (RoundedPanel) ---

    public void applyTheme() {
        removeAll();
        initPathCoords();
        initVisualUI();
        revalidate();
        repaint();
    }

    private void initVisualUI() {
        ThemeManager.Theme theme = ThemeManager.getCurrentTheme();
        if (theme == null) theme = ThemeManager.Theme.HELL;

        for (int i = 1; i <= TOTAL_TILES; i++) {
            JPanel square = createSquare(i, theme);
            squarePanels[i] = square;
            add(square);

            Point p = tileCoords[i];
            if (p != null) {
                square.setBounds(p.x, p.y, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    private JPanel createSquare(int number, ThemeManager.Theme theme) {
        // Buat panel rounded dengan radius acak (efek organik)
        int randomRadius = 10 + visualRandom.nextInt(16);
        RoundedPanel panel = new RoundedPanel(randomRadius);
        panel.setLayout(new BorderLayout());

        // Warna Selang-seling
        boolean isEven = (number % 2 == 0);
        Color bgColor = isEven ? theme.tileColor1 : theme.tileColor2;
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Teks Kontras
        double brightness = (0.299 * bgColor.getRed() + 0.587 * bgColor.getGreen() + 0.114 * bgColor.getBlue()) / 255;
        Color textColor = (brightness < 0.5) ? Color.WHITE : Color.BLACK;
        Color pointColor = (brightness < 0.5) ? Color.LIGHT_GRAY : Color.GRAY;

        JLabel numLabel = new JLabel(String.valueOf(number));
        numLabel.setFont(new Font("Arial", Font.BOLD, 10));
        numLabel.setForeground(textColor);
        numLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 0));
        panel.add(numLabel, BorderLayout.NORTH);

        // Label Poin (jika ada)
        if (tilePoints[number] > 0) {
            JLabel ptLabel = new JLabel("+" + tilePoints[number]);
            ptLabel.setFont(new Font("Arial", Font.PLAIN, 9));
            ptLabel.setForeground(pointColor);
            ptLabel.setHorizontalAlignment(SwingConstants.CENTER);
            ptLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
            panel.add(ptLabel, BorderLayout.SOUTH);
        }

        return panel;
    }

    // Inner Class: Panel dengan sudut melengkung & Outline Putih
    private class RoundedPanel extends JPanel {
        private int radius;

        public RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, w, h, radius, radius);

            // Outline Putih Tebal
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawRoundRect(1, 1, w - 2, h - 2, radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // --- KOORDINAT TILE (Hardcoded untuk setiap tema) ---

    public void initPathCoords() {
        ThemeManager.Theme theme = ThemeManager.getCurrentTheme();
        if (theme == null) theme = ThemeManager.Theme.HELL;
        for(int i=0; i<tileCoords.length; i++) tileCoords[i] = new Point(0,0);

        switch (theme) {
            case HELL:      initHellCoords(); break;
            case JUNGLE:    initJungleCoords(); break;
            case GRAVEYARD: initGraveyardCoords(); break;
            case MOUNTAIN:  initMountainCoords(); break;
            default:        initHellCoords(); break;
        }
    }

    private void initHellCoords() {
        tileCoords[1] = new Point(86, 690); tileCoords[2] = new Point(142, 681);
        tileCoords[3] = new Point(197, 669); tileCoords[4] = new Point(252, 655);
        tileCoords[5] = new Point(306, 638); tileCoords[6] = new Point(335, 585);
        tileCoords[7] = new Point(321, 530); tileCoords[8] = new Point(301, 476);
        tileCoords[9] = new Point(247, 456); tileCoords[10] = new Point(192, 431);
        tileCoords[11] = new Point(137, 408); tileCoords[12] = new Point(115, 354);
        tileCoords[13] = new Point(96, 299); tileCoords[14] = new Point(104, 244);
        tileCoords[15] = new Point(124, 189); tileCoords[16] = new Point(179, 161);
        tileCoords[17] = new Point(233, 170); tileCoords[18] = new Point(288, 188);
        tileCoords[19] = new Point(313, 244); tileCoords[20] = new Point(331, 297);
        tileCoords[21] = new Point(356, 350); tileCoords[22] = new Point(379, 405);
        tileCoords[23] = new Point(402, 458); tileCoords[24] = new Point(421, 511);
        tileCoords[25] = new Point(438, 564); tileCoords[26] = new Point(464, 617);
        tileCoords[27] = new Point(519, 635); tileCoords[28] = new Point(573, 619);
        tileCoords[29] = new Point(590, 564); tileCoords[30] = new Point(610, 508);
        tileCoords[31] = new Point(597, 454); tileCoords[32] = new Point(579, 398);
        tileCoords[33] = new Point(562, 342); tileCoords[34] = new Point(538, 288);
        tileCoords[35] = new Point(509, 234); tileCoords[36] = new Point(480, 180);
        tileCoords[37] = new Point(426, 160); tileCoords[38] = new Point(373, 138);
        tileCoords[39] = new Point(357, 84); tileCoords[40] = new Point(412, 50);
        tileCoords[41] = new Point(467, 48); tileCoords[42] = new Point(521, 48);
        tileCoords[43] = new Point(575, 63); tileCoords[44] = new Point(600, 116);
        tileCoords[45] = new Point(620, 171); tileCoords[46] = new Point(639, 225);
        tileCoords[47] = new Point(655, 281); tileCoords[48] = new Point(673, 336);
        tileCoords[49] = new Point(692, 392); tileCoords[50] = new Point(715, 444);
        tileCoords[51] = new Point(741, 498); tileCoords[52] = new Point(797, 521);
        tileCoords[53] = new Point(852, 500); tileCoords[54] = new Point(875, 446);
        tileCoords[55] = new Point(888, 391); tileCoords[56] = new Point(872, 334);
        tileCoords[57] = new Point(849, 279); tileCoords[58] = new Point(821, 223);
        tileCoords[59] = new Point(766, 187); tileCoords[60] = new Point(740, 131);
        tileCoords[61] = new Point(758, 77); tileCoords[62] = new Point(810, 59);
        tileCoords[63] = new Point(865, 77); tileCoords[64] = new Point(920, 102);
    }

    private void initJungleCoords() {
        tileCoords[1] = new Point(80, 151); tileCoords[2] = new Point(134, 120);
        tileCoords[3] = new Point(190, 154); tileCoords[4] = new Point(204, 208);
        tileCoords[5] = new Point(189, 262); tileCoords[6] = new Point(210, 317);
        tileCoords[7] = new Point(232, 370); tileCoords[8] = new Point(251, 424);
        tileCoords[9] = new Point(260, 478); tileCoords[10] = new Point(250, 533);
        tileCoords[11] = new Point(262, 587); tileCoords[12] = new Point(287, 641);
        tileCoords[13] = new Point(342, 661); tileCoords[14] = new Point(396, 672);
        tileCoords[15] = new Point(451, 685); tileCoords[16] = new Point(505, 659);
        tileCoords[17] = new Point(521, 604); tileCoords[18] = new Point(504, 550);
        tileCoords[19] = new Point(449, 517); tileCoords[20] = new Point(421, 464);
        tileCoords[21] = new Point(407, 410); tileCoords[22] = new Point(390, 356);
        tileCoords[23] = new Point(378, 302); tileCoords[24] = new Point(369, 246);
        tileCoords[25] = new Point(383, 191); tileCoords[26] = new Point(406, 134);
        tileCoords[27] = new Point(461, 112); tileCoords[28] = new Point(515, 92);
        tileCoords[29] = new Point(570, 105); tileCoords[30] = new Point(604, 158);
        tileCoords[31] = new Point(628, 214); tileCoords[32] = new Point(643, 270);
        tileCoords[33] = new Point(659, 324); tileCoords[34] = new Point(673, 379);
        tileCoords[35] = new Point(656, 434); tileCoords[36] = new Point(641, 489);
        tileCoords[37] = new Point(658, 544); tileCoords[38] = new Point(675, 599);
        tileCoords[39] = new Point(730, 617); tileCoords[40] = new Point(785, 636);
        tileCoords[41] = new Point(840, 654); tileCoords[42] = new Point(894, 665);
        tileCoords[43] = new Point(948, 659); tileCoords[44] = new Point(1003, 648);
        tileCoords[45] = new Point(1028, 593); tileCoords[46] = new Point(1045, 537);
        tileCoords[47] = new Point(1058, 482); tileCoords[48] = new Point(1052, 428);
        tileCoords[49] = new Point(1028, 374); tileCoords[50] = new Point(973, 363);
        tileCoords[51] = new Point(918, 375); tileCoords[52] = new Point(863, 355);
        tileCoords[53] = new Point(808, 332); tileCoords[54] = new Point(753, 306);
        tileCoords[55] = new Point(728, 251); tileCoords[56] = new Point(712, 196);
        tileCoords[57] = new Point(698, 141); tileCoords[58] = new Point(718, 85);
        tileCoords[59] = new Point(773, 67); tileCoords[60] = new Point(827, 45);
        tileCoords[61] = new Point(881, 28); tileCoords[62] = new Point(936, 43);
        tileCoords[63] = new Point(991, 62); tileCoords[64] = new Point(1046, 99);
    }

    private void initGraveyardCoords() {
        tileCoords[1] = new Point(50, 68); tileCoords[2] = new Point(106, 86);
        tileCoords[3] = new Point(161, 64); tileCoords[4] = new Point(217, 89);
        tileCoords[5] = new Point(272, 69); tileCoords[6] = new Point(394, 65);
        tileCoords[7] = new Point(450, 83); tileCoords[8] = new Point(506, 62);
        tileCoords[9] = new Point(562, 82); tileCoords[10] = new Point(672, 55);
        tileCoords[11] = new Point(728, 74); tileCoords[12] = new Point(783, 55);
        tileCoords[13] = new Point(874, 50); tileCoords[14] = new Point(930, 63);
        tileCoords[15] = new Point(983, 82); tileCoords[16] = new Point(1012, 138);
        tileCoords[17] = new Point(1028, 193); tileCoords[18] = new Point(1044, 249);
        tileCoords[19] = new Point(1028, 304); tileCoords[20] = new Point(974, 329);
        tileCoords[21] = new Point(917, 322); tileCoords[22] = new Point(861, 298);
        tileCoords[23] = new Point(834, 241); tileCoords[24] = new Point(779, 221);
        tileCoords[25] = new Point(725, 209); tileCoords[26] = new Point(670, 197);
        tileCoords[27] = new Point(615, 205); tileCoords[28] = new Point(561, 222);
        tileCoords[29] = new Point(476, 243); tileCoords[30] = new Point(422, 224);
        tileCoords[31] = new Point(367, 236); tileCoords[32] = new Point(268, 236);
        tileCoords[33] = new Point(213, 221); tileCoords[34] = new Point(158, 236);
        tileCoords[35] = new Point(104, 258); tileCoords[36] = new Point(53, 346);
        tileCoords[37] = new Point(66, 401); tileCoords[38] = new Point(86, 457);
        tileCoords[39] = new Point(142, 483); tileCoords[40] = new Point(198, 466);
        tileCoords[41] = new Point(257, 443); tileCoords[42] = new Point(312, 416);
        tileCoords[43] = new Point(366, 390); tileCoords[44] = new Point(422, 413);
        tileCoords[45] = new Point(477, 389); tileCoords[46] = new Point(531, 413);
        tileCoords[47] = new Point(585, 389); tileCoords[48] = new Point(639, 410);
        tileCoords[49] = new Point(749, 371); tileCoords[50] = new Point(804, 391);
        tileCoords[51] = new Point(859, 409); tileCoords[52] = new Point(877, 464);
        tileCoords[53] = new Point(858, 518); tileCoords[54] = new Point(804, 538);
        tileCoords[55] = new Point(749, 530); tileCoords[56] = new Point(696, 541);
        tileCoords[57] = new Point(517, 546); tileCoords[58] = new Point(490, 603);
        tileCoords[59] = new Point(510, 658); tileCoords[60] = new Point(565, 670);
        tileCoords[61] = new Point(621, 657); tileCoords[62] = new Point(775, 646);
        tileCoords[63] = new Point(830, 671); tileCoords[64] = new Point(884, 653);
    }

    private void initMountainCoords() {
        tileCoords[1] = new Point(1109, 630); tileCoords[2] = new Point(1085, 576);
        tileCoords[3] = new Point(1030, 556); tileCoords[4] = new Point(910, 602);
        tileCoords[5] = new Point(856, 615); tileCoords[6] = new Point(803, 636);
        tileCoords[7] = new Point(749, 650); tileCoords[8] = new Point(654, 661);
        tileCoords[9] = new Point(599, 647); tileCoords[10] = new Point(544, 665);
        tileCoords[11] = new Point(489, 645); tileCoords[12] = new Point(384, 624);
        tileCoords[13] = new Point(330, 609); tileCoords[14] = new Point(275, 627);
        tileCoords[15] = new Point(220, 608); tileCoords[16] = new Point(164, 627);
        tileCoords[17] = new Point(70, 585); tileCoords[18] = new Point(53, 529);
        tileCoords[19] = new Point(107, 506); tileCoords[20] = new Point(161, 494);
        tileCoords[21] = new Point(216, 486); tileCoords[22] = new Point(271, 499);
        tileCoords[23] = new Point(393, 516); tileCoords[24] = new Point(449, 506);
        tileCoords[25] = new Point(504, 491); tileCoords[26] = new Point(558, 510);
        tileCoords[27] = new Point(611, 530); tileCoords[28] = new Point(733, 525);
        tileCoords[29] = new Point(788, 507); tileCoords[30] = new Point(843, 485);
        tileCoords[31] = new Point(916, 422); tileCoords[32] = new Point(975, 399);
        tileCoords[33] = new Point(1031, 376); tileCoords[34] = new Point(1087, 357);
        tileCoords[35] = new Point(1076, 264); tileCoords[36] = new Point(1021, 238);
        tileCoords[37] = new Point(968, 219); tileCoords[38] = new Point(912, 245);
        tileCoords[39] = new Point(856, 274); tileCoords[40] = new Point(821, 330);
        tileCoords[41] = new Point(790, 386); tileCoords[42] = new Point(702, 418);
        tileCoords[43] = new Point(647, 389); tileCoords[44] = new Point(591, 365);
        tileCoords[45] = new Point(536, 349); tileCoords[46] = new Point(450, 386);
        tileCoords[47] = new Point(397, 397); tileCoords[48] = new Point(343, 382);
        tileCoords[49] = new Point(289, 358); tileCoords[50] = new Point(235, 344);
        tileCoords[51] = new Point(131, 394); tileCoords[52] = new Point(76, 373);
        tileCoords[53] = new Point(58, 319); tileCoords[54] = new Point(84, 264);
        tileCoords[55] = new Point(138, 243); tileCoords[56] = new Point(264, 207);
        tileCoords[57] = new Point(320, 193); tileCoords[58] = new Point(371, 209);
        tileCoords[59] = new Point(427, 228); tileCoords[60] = new Point(482, 246);
        tileCoords[61] = new Point(536, 215); tileCoords[62] = new Point(591, 178);
        tileCoords[63] = new Point(644, 143); tileCoords[64] = new Point(698, 105);
    }
}