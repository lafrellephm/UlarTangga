import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Collection;
import java.util.Map;

public class BoardPanel extends JPanel {
    private final int ROWS = 8;
    private final int COLS = 8;
    private int[][] boardMatrix = new int[ROWS][COLS];
    private JPanel[] squarePanels = new JPanel[65];

    // Data Tangga untuk Visualisasi
    private Map<Integer, Integer> laddersVisual;

    public BoardPanel() {
        setLayout(new GridLayout(ROWS, COLS));
        initMatrixData();
        initVisualUI();
    }

    public void setLaddersMap(Map<Integer, Integer> ladders) {
        this.laddersVisual = ladders;
        repaint(); // Gambar garis saat data diterima
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
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                int number = boardMatrix[i][j];
                JPanel square = createSquare(number, i, j);
                squarePanels[number] = square;
                add(square);
            }
        }
    }

    private JPanel createSquare(int number, int row, int col) {
        JPanel panel = new JPanel(new BorderLayout());
        if ((row + col) % 2 == 0) {
            panel.setBackground(new Color(245, 245, 220));
        } else {
            panel.setBackground(new Color(176, 224, 230));
        }
        panel.setBorder(new LineBorder(Color.GRAY));

        JLabel label = new JLabel(String.valueOf(number));
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 0));
        panel.add(label, BorderLayout.NORTH);

        return panel;
    }

    public void updatePlayerPawns(Collection<Player> players) {
        // 1. Bersihkan semua pawn dari papan
        for (int i = 1; i <= 64; i++) {
            JPanel sq = squarePanels[i];
            if (sq == null) continue;
            Component centerComp = ((BorderLayout)sq.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (centerComp != null) sq.remove(centerComp);

            // Kita tidak perlu repaint per kotak di sini, karena kita akan repaint full di bawah
        }

        // 2. Jika players belum ada, tetap lakukan repaint untuk gambar garis
        if (players != null && !players.isEmpty()) {
            // Gambar Ulang Pawn Baru
            for (Player p : players) {
                int pos = Math.min(p.getPosition(), 64);
                pos = Math.max(pos, 1);

                JPanel targetSquare = squarePanels[pos];

                JPanel pawn = new JPanel();
                pawn.setBackground(p.getColor());
                pawn.setPreferredSize(new Dimension(25, 25));
                pawn.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                pawn.setToolTipText(p.getName());

                JPanel wrapper = new JPanel(new GridBagLayout());
                wrapper.setOpaque(false);
                wrapper.add(pawn);

                targetSquare.add(wrapper, BorderLayout.CENTER);
                targetSquare.revalidate(); // Revalidate layout kotak
            }
        }

        // --- KUNCI PERBAIKAN ---
        // Panggil repaint() pada BoardPanel (this).
        // Ini memaksa Java menggambar ulang Panel UTAMA beserta garis-garisnya
        // SETELAH pion-pion ditempatkan.
        this.repaint();
    }

    // Menggambar Garis Tangga
    // Gunakan paintChildren agar digambar SETELAH kotak-kotak (background) dirender
    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g); // Gambar dulu anak-anaknya (Kotak papan & Pion)

        // Sekarang gambar garis overlay di atasnya
        if (laddersVisual == null || laddersVisual.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Set Stroke dan Warna Garis
        g2.setStroke(new BasicStroke(3));

        for (Map.Entry<Integer, Integer> entry : laddersVisual.entrySet()) {
            int startNode = entry.getKey();
            int endNode = entry.getValue();

            JPanel pStart = squarePanels[startNode];
            JPanel pEnd = squarePanels[endNode];

            if (pStart != null && pEnd != null) {
                // Ambil koordinat tengah panel relatif terhadap BoardPanel
                Point p1 = SwingUtilities.convertPoint(pStart, pStart.getWidth()/2, pStart.getHeight()/2, this);
                Point p2 = SwingUtilities.convertPoint(pEnd, pEnd.getWidth()/2, pEnd.getHeight()/2, this);

                // Gambar Garis Transparan
                g2.setColor(new Color(80, 80, 80, 180));
                g2.drawLine(p1.x, p1.y, p2.x, p2.y);

                // Gambar Titik Start (Merah)
                int dotSize = 12;
                g2.setColor(new Color(220, 20, 60));
                g2.fillOval(p1.x - dotSize/2, p1.y - dotSize/2, dotSize, dotSize);

                // Gambar Titik End (Hijau)
                g2.setColor(new Color(34, 139, 34));
                g2.fillOval(p2.x - dotSize/2, p2.y - dotSize/2, dotSize, dotSize);
            }
        }
        g2.dispose();
    }
}