import java.awt.Color;
import java.util.Stack;

public class Player {
    private String name;
    private int position;
    private Color color;
    private Stack<Integer> stepHistory; // Fitur Stack untuk riwayat

    public Player(String name, Color color) {
        this.name = name;
        this.color = color;
        this.position = 1; // Start di kotak 1
        this.stepHistory = new Stack<>();
        this.stepHistory.push(1);
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public Color getColor() {
        return color;
    }

    public Stack<Integer> getHistory() {
        return stepHistory;
    }

    // Update posisi dan simpan ke stack
    public void setPosition(int newPosition) {
        this.position = newPosition;
        this.stepHistory.push(newPosition);
    }

    public void reset() {
        this.position = 1;
        this.stepHistory.clear();
        this.stepHistory.push(1);
    }
}