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

    // Simpan nilai poin per kotak
    private int[] tilePoints = new int[65];

    private Map<Integer, Integer> laddersVisual;

    public BoardPanel() {
        setLayout(new GridLayout(ROWS, COLS));
        initMatrixData();
        initTilePoints(); // Generate poin
        initVisualUI();
    }

    // Generate poin random (10 - 50) per kotak
    private void initTilePoints() {
        Random rand = new Random();
        tilePoints[1] = 0; // Start tidak ada poin
        for (int i = 2; i <= 64; i++) {
            tilePoints[i] = (rand.nextInt(5) + 1) * 10;
        }
    }

    // Method publik untuk mengambil poin
    public int getPointsAt(int pos) {
        if (pos >= 1 && pos <= 64) {
            return tilePoints[pos];
        }
        return 0;
    }

    public void setLaddersMap(Map<Integer, Integer> ladders) {
        this.laddersVisual = ladders;
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

        JLabel numLabel = new JLabel(String.valueOf(number));
        numLabel.setFont(new Font("Arial", Font.BOLD, 14));
        numLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 0));
        panel.add(numLabel, BorderLayout.NORTH);

        // Tampilkan Label Poin di Bawah Kotak
        if (number > 1) {
            JLabel ptLabel = new JLabel("+" + tilePoints[number]);
            ptLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            ptLabel.setForeground(Color.GRAY);
            ptLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(ptLabel, BorderLayout.SOUTH);
        }

        return panel;
    }

    public void updatePlayerPawns(Collection<Player> players) {
        // 1. Bersihkan area tengah (CENTER) dari semua kotak terlebih dahulu
        for (int i = 1; i <= 64; i++) {
            JPanel sq = squarePanels[i];
            if (sq == null) continue;

            // Ambil layout manager
            BorderLayout layout = (BorderLayout) sq.getLayout();
            // Cari komponen yang ada di CENTER (biasanya tempat pawn)
            Component centerComp = layout.getLayoutComponent(BorderLayout.CENTER);

            // Hapus jika ada
            if (centerComp != null) {
                sq.remove(centerComp);
            }
            // Validasi ulang tampilan kotak
            sq.revalidate();
            sq.repaint();
        }

        if (players == null || players.isEmpty()) return;

        // 2. Kelompokkan pemain berdasarkan posisi mereka
        // Key: Nomor Posisi (Integer), Value: List Player di posisi itu
        Map<Integer, List<Player>> playersByPos = new HashMap<>();

        for (Player p : players) {
            int pos = Math.min(p.getPosition(), 64);
            pos = Math.max(pos, 1);

            // Masukkan player ke list yang sesuai dengan posisinya
            playersByPos.computeIfAbsent(pos, k -> new ArrayList<>()).add(p);
        }

        // 3. Render pawn ke dalam kotak
        for (Map.Entry<Integer, List<Player>> entry : playersByPos.entrySet()) {
            int pos = entry.getKey();
            List<Player> playersAtPos = entry.getValue();

            JPanel targetSquare = squarePanels[pos];

            // PENTING: Buat SATU container untuk menampung banyak pawn sekaligus
            // FlowLayout akan menata pawn secara berjejer (horizontal)
            JPanel pawnsContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
            pawnsContainer.setOpaque(false); // Transparan agar warna kotak tetap terlihat

            for (Player p : playersAtPos) {
                JPanel pawn = new JPanel();
                pawn.setBackground(p.getColor());

                // Opsional: Perkecil ukuran sedikit jika ada banyak pemain di satu kotak
                int size = (playersAtPos.size() > 2) ? 15 : 20;
                pawn.setPreferredSize(new Dimension(size, size));

                pawn.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                pawn.setToolTipText(p.getName() + " | Score: " + p.getScore());

                pawnsContainer.add(pawn);
            }

            // Tambahkan container yang berisi kumpulan pawn ke kotak
            targetSquare.add(pawnsContainer, BorderLayout.CENTER);
            targetSquare.revalidate();
        }

        this.repaint();
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);

        if (laddersVisual == null || laddersVisual.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(3));

        for (Map.Entry<Integer, Integer> entry : laddersVisual.entrySet()) {
            int startNode = entry.getKey();
            int endNode = entry.getValue();

            JPanel pStart = squarePanels[startNode];
            JPanel pEnd = squarePanels[endNode];

            if (pStart != null && pEnd != null) {
                Point p1 = SwingUtilities.convertPoint(pStart, pStart.getWidth()/2, pStart.getHeight()/2, this);
                Point p2 = SwingUtilities.convertPoint(pEnd, pEnd.getWidth()/2, pEnd.getHeight()/2, this);

                g2.setColor(new Color(80, 80, 80, 180));
                g2.drawLine(p1.x, p1.y, p2.x, p2.y);

                int dotSize = 12;
                g2.setColor(new Color(220, 20, 60));
                g2.fillOval(p1.x - dotSize/2, p1.y - dotSize/2, dotSize, dotSize);

                g2.setColor(new Color(34, 139, 34));
                g2.fillOval(p2.x - dotSize/2, p2.y - dotSize/2, dotSize, dotSize);
            }
        }
        g2.dispose();
    }
}