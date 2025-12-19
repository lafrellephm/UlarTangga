import javax.swing.*;
import java.awt.*;

public class DicePanel extends JPanel {
    private int number = 1;
    private Color diceColor = Color.WHITE;

    public DicePanel() {
        setPreferredSize(new Dimension(80, 80));
        setBackground(new Color(230, 230, 250)); // Background panel luar (bukan dadu)
    }

    // --- Setters untuk Update Visual ---
    public void setNumber(int number) {
        this.number = number;
        repaint();
    }

    public void setDiceColor(Color color) {
        this.diceColor = color;
        repaint();
    }

    // --- Rendering Dadu ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int diceSize = 50;
        int x = (w - diceSize) / 2;
        int y = (h - diceSize) / 2;

        // 1. Gambar Kotak Dadu (Rounded)
        g2.setColor(diceColor);
        g2.fillRoundRect(x, y, diceSize, diceSize, 15, 15);

        // 2. Gambar Border Dadu
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x, y, diceSize, diceSize, 15, 15);

        // 3. Gambar Titik Dadu (Dots) sesuai Angka
        g2.setColor(Color.BLACK);
        int dotSize = 8;
        int center = diceSize / 2;
        int q1 = diceSize / 4;
        int q3 = diceSize * 3 / 4;

        if (number % 2 != 0) drawDot(g2, x + center, y + center, dotSize); // Pusat (1, 3, 5)
        if (number > 1) { drawDot(g2, x + q1, y + q1, dotSize); drawDot(g2, x + q3, y + q3, dotSize); } // Diagonal 1
        if (number > 3) { drawDot(g2, x + q3, y + q1, dotSize); drawDot(g2, x + q1, y + q3, dotSize); } // Diagonal 2
        if (number == 6) { drawDot(g2, x + q1, y + center, dotSize); drawDot(g2, x + q3, y + center, dotSize); } // Tengah Samping
    }

    private void drawDot(Graphics2D g2, int x, int y, int size) {
        g2.fillOval(x - size / 2, y - size / 2, size, size);
    }
}