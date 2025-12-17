import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BoardPanel extends JPanel {
    private final int ROWS = 8;
    private final int COLS = 8;
    private int[][] boardMatrix = new int[ROWS][COLS];
    private JPanel[] squarePanels = new JPanel[65];
    private int[] tilePoints = new int[65];
    private Map<Integer, Integer> laddersVisual;

    public BoardPanel() {
        setLayout(new GridLayout(ROWS, COLS));
        initMatrixData();
        initTilePoints();
        initVisualUI();
    }

    private void initTilePoints() {
        Random rand = new Random();
        tilePoints[1] = 0; // Start tidak ada poin
        for (int i = 2; i <= 64; i++) {
            // Poin acak antara 10, 20, 30, 40, atau 50
            tilePoints[i] = (rand.nextInt(5) + 1) * 10;
        }
    }

    // Method public untuk mereset dan mengacak ulang poin papan
    public void generateNewTilePoints() {
        initTilePoints();
    }

    public int getPointsAt(int pos) {
        if (pos >= 1 && pos <= 64) return tilePoints[pos];
        return 0;
    }

    public int claimPointsAt(int pos) {
        if (pos < 1 || pos > 64) return 0;
        int points = tilePoints[pos];
        if (points > 0) {
            tilePoints[pos] = 0;
            JPanel square = squarePanels[pos];
            if (square != null) {
                BorderLayout layout = (BorderLayout) square.getLayout();
                Component southComp = layout.getLayoutComponent(BorderLayout.SOUTH);
                if (southComp != null) {
                    square.remove(southComp);
                    square.revalidate();
                    square.repaint();
                }
            }
        }
        return points;
    }

    public void setLaddersMap(Map<Integer, Integer> ladders) {
        this.laddersVisual = ladders;
        repaint();
    }

    public void applyTheme() {
        ThemeManager.Theme theme = ThemeManager.getCurrentTheme();
        // Hapus semua komponen visual dan bangun ulang (agar warna & label poin terupdate)
        removeAll();
        initVisualUI();
        revalidate();
        repaint();
    }

    private void initMatrixData() {
        int counter = 1;
        for (int i = ROWS - 1; i >= 0; i--) {
            if ((ROWS - 1 - i) % 2 == 0) {
                for (int j = 0; j < COLS; j++) {
                    boardMatrix[i][j] = counter++;
                }
            } else {
                for (int j = COLS - 1; j >= 0; j--) {
                    boardMatrix[i][j] = counter++;
                }
            }
        }
    }

    private void initVisualUI() {
        ThemeManager.Theme theme = ThemeManager.getCurrentTheme();
        // Fallback jika theme null (pertahanan error)
        if (theme == null) theme = ThemeManager.Theme.CLASSIC;

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                int number = boardMatrix[i][j];
                JPanel square = createSquare(number, i, j, theme);
                squarePanels[number] = square;
                add(square);
            }
        }
    }

    private JPanel createSquare(int number, int row, int col, ThemeManager.Theme theme) {
        JPanel panel = new JPanel(new BorderLayout());

        if ((row + col) % 2 == 0) {
            panel.setBackground(theme.tileColor1);
        } else {
            panel.setBackground(theme.tileColor2);
        }

        Color borderColor = (theme == ThemeManager.Theme.DARK_MODE) ? Color.DARK_GRAY : Color.GRAY;
        panel.setBorder(new LineBorder(borderColor));

        JLabel numLabel = new JLabel(String.valueOf(number));
        numLabel.setFont(new Font("Arial", Font.BOLD, 14));

        if (theme == ThemeManager.Theme.DARK_MODE) {
            numLabel.setForeground(Color.WHITE);
        } else {
            numLabel.setForeground(Color.BLACK);
        }

        numLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 0));
        panel.add(numLabel, BorderLayout.NORTH);

        // Tambahkan label poin HANYA jika nilainya > 0
        if (tilePoints[number] > 0) {
            JLabel ptLabel = new JLabel("+" + tilePoints[number]);
            ptLabel.setFont(new Font("Arial", Font.PLAIN, 10));

            if (theme == ThemeManager.Theme.DARK_MODE) ptLabel.setForeground(Color.LIGHT_GRAY);
            else ptLabel.setForeground(Color.GRAY);

            ptLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(ptLabel, BorderLayout.SOUTH);
        }

        return panel;
    }

    public void updatePlayerPawns(Collection<Player> players) {
        for (int i = 1; i <= 64; i++) {
            JPanel sq = squarePanels[i];
            if (sq == null) continue;
            BorderLayout layout = (BorderLayout) sq.getLayout();
            Component centerComp = layout.getLayoutComponent(BorderLayout.CENTER);
            if (centerComp != null) sq.remove(centerComp);
            sq.revalidate();
            sq.repaint();
        }

        if (players == null || players.isEmpty()) return;

        Map<Integer, List<Player>> playersByPos = new HashMap<>();
        for (Player p : players) {
            int pos = Math.max(1, Math.min(p.getPosition(), 64));
            playersByPos.computeIfAbsent(pos, k -> new ArrayList<>()).add(p);
        }

        for (Map.Entry<Integer, List<Player>> entry : playersByPos.entrySet()) {
            int pos = entry.getKey();
            List<Player> playersAtPos = entry.getValue();
            JPanel targetSquare = squarePanels[pos];

            JPanel pawnsContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
            pawnsContainer.setOpaque(false);

            for (Player p : playersAtPos) {
                JPanel pawn = new JPanel();
                pawn.setBackground(p.getColor());
                int size = (playersAtPos.size() > 2) ? 15 : 20;
                pawn.setPreferredSize(new Dimension(size, size));
                pawn.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                pawn.setToolTipText(p.getName() + " | Score: " + p.getScore());
                pawnsContainer.add(pawn);
            }
            targetSquare.add(pawnsContainer, BorderLayout.CENTER);
            targetSquare.revalidate();
        }
        repaint();
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);

        if (laddersVisual == null || laddersVisual.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        ThemeManager.Theme theme = ThemeManager.getCurrentTheme();
        if (theme == null) theme = ThemeManager.Theme.CLASSIC;

        for (Map.Entry<Integer, Integer> entry : laddersVisual.entrySet()) {
            int startNode = entry.getKey();
            int endNode = entry.getValue();

            JPanel pStart = squarePanels[startNode];
            JPanel pEnd = squarePanels[endNode];

            if (pStart != null && pEnd != null) {
                // Konversi koordinat lokal panel ke koordinat board utama
                Point p1 = SwingUtilities.convertPoint(pStart, pStart.getWidth()/2, pStart.getHeight()/2, this);
                Point p2 = SwingUtilities.convertPoint(pEnd, pEnd.getWidth()/2, pEnd.getHeight()/2, this);

                // --- LOGIKA VISUAL BARU ---
                if (theme.ladderStyle == ThemeManager.LadderStyle.DOTTED) {
                    // Jika style dotted, kita buat seperti tali sederhana atau tetap garis putus
                    g2.setColor(theme.ladderColor);
                    Stroke dashed = new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
                    g2.setStroke(dashed);
                    g2.drawLine(p1.x, p1.y, p2.x, p2.y);

                    // Tambahkan dot di ujung
                    int dotSize = 12;
                    g2.setColor(theme.ladderColor.darker());
                    g2.fillOval(p1.x - dotSize/2, p1.y - dotSize/2, dotSize, dotSize);
                    g2.fillOval(p2.x - dotSize/2, p2.y - dotSize/2, dotSize, dotSize);
                }
                else {
                    // Untuk CLASSIC dan SOLID, gunakan visual tangga realistis (2 rel + anak tangga)
                    drawRealisticLadder(g2, p1, p2, theme.ladderColor);
                }
            }
        }
        g2.dispose();
    }

    /**
     * Menggambar tangga realistis dengan 2 rel samping dan anak tangga.
     */
    private void drawRealisticLadder(Graphics2D g2, Point p1, Point p2, Color color) {
        int ladderWidth = 22; // Lebar tangga

        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Hitung jumlah anak tangga berdasarkan panjang (tiap 20 pixel ada 1 anak tangga)
        int numSteps = (int) (distance / 20);

        // Vektor satuan
        double unitX = dx / distance;
        double unitY = dy / distance;

        // Vektor tegak lurus (normal) untuk membuat lebar ke kiri dan kanan
        // Normal vector (-y, x)
        double perpX = -unitY * (ladderWidth / 2.0);
        double perpY = unitX * (ladderWidth / 2.0);

        // Hitung 4 titik sudut rel tangga
        // Rel Kiri (Offset +perp)
        int x1Left = (int) (p1.x + perpX);
        int y1Left = (int) (p1.y + perpY);
        int x2Left = (int) (p2.x + perpX);
        int y2Left = (int) (p2.y + perpY);

        // Rel Kanan (Offset -perp)
        int x1Right = (int) (p1.x - perpX);
        int y1Right = (int) (p1.y - perpY);
        int x2Right = (int) (p2.x - perpX);
        int y2Right = (int) (p2.y - perpY);

        g2.setColor(color);
        // Gambar Rel Samping (Lebih tebal)
        g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x1Left, y1Left, x2Left, y2Left);
        g2.drawLine(x1Right, y1Right, x2Right, y2Right);

        // Efek Highlight (opsional, agar terlihat 3D sedikit)
        g2.setStroke(new BasicStroke(2));
        g2.setColor(new Color(255, 255, 255, 60)); // Putih transparan
        g2.drawLine(x1Left, y1Left, x2Left, y2Left);
        g2.drawLine(x1Right, y1Right, x2Right, y2Right);

        // Kembali ke warna asli untuk anak tangga
        g2.setColor(color);
        g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Gambar Anak Tangga (Rungs)
        for (int i = 1; i < numSteps; i++) {
            double fraction = (double) i / numSteps;

            // Interpolasi titik pada rel kiri
            int stepX1 = (int) (x1Left + (x2Left - x1Left) * fraction);
            int stepY1 = (int) (y1Left + (y2Left - y1Left) * fraction);

            // Interpolasi titik pada rel kanan
            int stepX2 = (int) (x1Right + (x2Right - x1Right) * fraction);
            int stepY2 = (int) (y1Right + (y2Right - y1Right) * fraction);

            g2.drawLine(stepX1, stepY1, stepX2, stepY2);
        }
    }
}