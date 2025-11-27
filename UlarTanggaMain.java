import java.awt.*;
import javax.swing.*;

public class UlarTanggaMain extends JFrame {

    private final int SIZE = 8;
    private final int TOTAL_SQUARES = 64;

    public UlarTanggaMain() {
        // Konfigurasi Frame Utama
        setTitle("Simulasi Board Ular Tangga 8x8");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Posisi di tengah layar

        // Membuat Panel Utama dengan Layout Grid 8x8
        JPanel boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(SIZE, SIZE));

        // 1. MEMPERSIAPKAN DATA (Logika Zig-Zag)
        int[][] boardData = generateZigZagBoard();

        // 2. MERENDER GUI (Menambahkan kotak ke dalam Grid)
        // GridLayout mengisi dari Kiri ke Kanan, Atas ke Bawah.
        // Maka kita loop array boardData dari indeks [0][0] (Pojok Kiri Atas)
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                int number = boardData[row][col];
                
                // Membuat komponen visual untuk kotak
                JPanel square = createSquare(number, row, col);
                boardPanel.add(square);
            }
        }

        add(boardPanel);
    }

    // Method Logika: Membuat array 2D dengan pola Ular Tangga
    private int[][] generateZigZagBoard() {
        int[][] board = new int[SIZE][SIZE];
        int currentNumber = 1;

        // Loop dari baris paling bawah (indeks 7) ke atas (indeks 0)
        for (int row = SIZE - 1; row >= 0; row--) {
            
            // Cek apakah baris ini berjalan dari Kiri-ke-Kanan atau Kanan-ke-Kiri
            // Baris paling bawah (row 7) adalah genap dari bawah (urutan ke-0 dari bawah), jadi Normal.
            // Baris atasnya (row 6) adalah ganjil dari bawah, jadi Reverse.
            
            int rowFromBottom = (SIZE - 1) - row;
            
            if (rowFromBottom % 2 == 0) {
                // Normal: Kiri ke Kanan
                for (int col = 0; col < SIZE; col++) {
                    board[row][col] = currentNumber++;
                }
            } else {
                // Reverse: Kanan ke Kiri
                for (int col = SIZE - 1; col >= 0; col--) {
                    board[row][col] = currentNumber++;
                }
            }
        }
        return board;
    }

    // Method Tampilan: Membuat desain per kotak
    private JPanel createSquare(int number, int row, int col) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        // Memberikan warna selang-seling agar mudah dilihat (seperti catur)
        if ((row + col) % 2 == 0) {
            panel.setBackground(new Color(200, 200, 200)); // Abu-abu terang
        } else {
            panel.setBackground(Color.WHITE);
        }

        // Menambahkan border hitam tipis
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // Membuat Label Angka
        JLabel label = new JLabel(String.valueOf(number), SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        
        // Warna khusus untuk START (1) dan FINISH (64)
        if (number == 1) {
            panel.setBackground(new Color(144, 238, 144)); // Hijau Muda
            label.setText("1");
            label.setFont(new Font("Arial", Font.BOLD, 20));
        } else if (number == TOTAL_SQUARES) {
            panel.setBackground(new Color(255, 182, 193)); // Merah Muda
            label.setText("64");
            label.setFont(new Font("Arial", Font.BOLD, 20));
        }

        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    public static void main(String[] args) {
        // Menjalankan GUI di Event Dispatch Thread (Best Practice Java Swing)
        SwingUtilities.invokeLater(() -> {
            new UlarTanggaMain().setVisible(true);
        });
    }
}