import java.awt.Color;
import java.util.Stack;

public class Player {
    private String name;
    private int position;
    private Color color;
    private int score; // Fitur Score
    private Stack<Integer> stepHistory;

    public Player(String name, Color color) {
        this.name = name;
        this.color = color;
        this.position = 1;
        this.score = 0; // Score awal 0
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

    // --- SCORE METHODS ---
    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        this.score += points;
    }
    // ---------------------

    public Stack<Integer> getHistory() {
        return stepHistory;
    }

    public void setPosition(int newPosition) {
        this.position = newPosition;
        this.stepHistory.push(newPosition);
    }

    public void setPositionRaw(int newPosition) {
        this.position = newPosition;
    }
}