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

    // [BARU] Setter manual untuk memuat skor dari sesi sebelumnya
    public void setScore(int score) {
        this.score = score;
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

    // Mengintip posisi sebelumnya tanpa mengubah Stack (Untuk Logic Penentuan Target)
    public int getPreviousPosition() {
        // Size > 1 artinya minimal ada [PosisiAwal, PosisiSekarang]
        if (stepHistory.size() > 1) {
            // Ambil elemen kedua dari atas (size - 2)
            return stepHistory.get(stepHistory.size() - 2);
        }
        // Jika baru mulai (cuma ada 1 data), tetap di posisi sekarang
        return position;
    }

    // Eksekusi Mundur: Hapus posisi terakhir dari Stack (Untuk Logic Finish Turn)
    public void revertToPreviousStep() {
        if (stepHistory.size() > 1) {
            stepHistory.pop(); // Hapus posisi saat ini (Undo)
            this.position = stepHistory.peek(); // Set posisi ke data history sebelumnya
        }
    }

    // --- [BARU] FITUR RESET SESSION ---
    // Mengembalikan player ke posisi awal tapi TIDAK menghapus skor (Akumulasi)
    public void resetForNewSession() {
        this.position = 1;
        this.stepHistory.clear();
        this.stepHistory.push(1);
    }
}