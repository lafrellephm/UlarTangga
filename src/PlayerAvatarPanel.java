import javax.swing.*;
import java.awt.*;

// --- CLASS PLAYER AVATAR PANEL ---
class PlayerAvatarPanel extends JPanel {
    private Color avatarColor = Color.GRAY;

    public PlayerAvatarPanel() {
        setPreferredSize(new Dimension(50, 50));
        setBackground(Color.WHITE);
    }

    public void setAvatarColor(Color c) {
        this.avatarColor = c;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = 40;
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        g2.setColor(avatarColor);
        g2.fillRoundRect(x, y, size, size, 10, 10);

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x, y, size, size, 10, 10);
    }
}