import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Collection;
import java.util.Map;
import java.util.Random;

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
        // Hapus pawn lama, tapi pertahankan label angka & poin
        for (int i = 1; i <= 64; i++) {
            JPanel sq = squarePanels[i];
            if (sq == null) continue;
            BorderLayout layout = (BorderLayout) sq.getLayout();
            Component centerComp = layout.getLayoutComponent(BorderLayout.CENTER);
            if (centerComp != null) sq.remove(centerComp);
        }

        if (players != null && !players.isEmpty()) {
            for (Player p : players) {
                int pos = Math.min(p.getPosition(), 64);
                pos = Math.max(pos, 1);

                JPanel targetSquare = squarePanels[pos];

                JPanel pawn = new JPanel();
                pawn.setBackground(p.getColor());
                pawn.setPreferredSize(new Dimension(25, 25));
                pawn.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                // Tooltip menampilkan Score juga
                pawn.setToolTipText(p.getName() + " | Score: " + p.getScore());

                JPanel wrapper = new JPanel(new GridBagLayout());
                wrapper.setOpaque(false);
                wrapper.add(pawn);

                targetSquare.add(wrapper, BorderLayout.CENTER);
                targetSquare.revalidate();
            }
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