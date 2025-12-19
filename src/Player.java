import java.awt.Color;
import java.util.Stack;

public class Player {
    private String name;
    private int position;
    private Color color;
    private int score;

    // Stack untuk menyimpan riwayat posisi (untuk fitur mundur)
    private Stack<Integer> stepHistory;

    // Status khusus jika pemain tertangkap oleh Boss
    private boolean isTrappedByBoss = false;

    public Player(String name, Color color) {
        this.name = name;
        this.color = color;
        this.position = 1; // Mulai dari tile 1
        this.score = 0;
        this.stepHistory = new Stack<>();
        this.stepHistory.push(1);
    }

    // --- Boss Trap Logic ---
    public boolean isTrapped() {
        return isTrappedByBoss;
    }

    public void setTrapped(boolean trapped) {
        this.isTrappedByBoss = trapped;
    }

    // --- Basic Getters ---
    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    // --- Score Management ---
    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public void setScore(int score) {
        this.score = score;
    }

    // --- Movement & History Logic ---
    public int getPosition() {
        return position;
    }

    // Update posisi dan simpan ke history
    public void setPosition(int newPosition) {
        this.position = newPosition;
        this.stepHistory.push(newPosition);
    }

    // Set posisi tanpa update history (jarang dipakai, untuk force set)
    public void setPositionRaw(int newPosition) {
        this.position = newPosition;
    }

    // Mengintip posisi sebelum langkah terakhir (tanpa menghapus data)
    public int getPreviousPosition() {
        if (stepHistory.size() > 1) {
            return stepHistory.get(stepHistory.size() - 2);
        }
        return position;
    }

    // Mundur: Menghapus langkah terakhir (Undo)
    public void revertToPreviousStep() {
        if (stepHistory.size() > 1) {
            stepHistory.pop(); // Buang posisi sekarang
            this.position = stepHistory.peek(); // Kembali ke posisi sebelumnya
        }
    }

    // Reset untuk sesi baru (tetap simpan nama/warna, reset posisi)
    public void resetForNewSession() {
        this.position = 1;
        this.stepHistory.clear();
        this.stepHistory.push(1);
    }
}