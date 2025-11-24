import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class BoardPanel extends JPanel {
    private final int ROWS = 8;
    private final int COLS = 8;
    private int[][] boardMatrix = new int[ROWS][COLS];

    // Array untuk menyimpan referensi ke Panel setiap kotak (Index 1-64)
    // Agar kita bisa menaruh pion pemain di kotak yang tepat nanti.
    private JPanel[] squarePanels = new JPanel[65];

    public BoardPanel() {
        setLayout(new GridLayout(ROWS, COLS)); // Layout Grid 8x8
        initMatrixData();  // 1. Siapkan logika angka zig-zag
        initVisualUI();    // 2. Gambar kotak berdasarkan logika tsb
    }

    // Logika Matrix 2 Dimensi (Zig-Zag dari Bawah ke Atas)
    private void initMatrixData() {
        int counter = 1;
        // Loop dari baris paling bawah (7) ke atas (0)
        for (int i = ROWS - 1; i >= 0; i--) {
            // Cek arah: (ROWS-1-i) -> 0, 1, 2... dari bawah
            // Jika genap: Kiri ke Kanan. Jika ganjil: Kanan ke Kiri.
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

    // Menggambar Kotak ke Layar
    private void initVisualUI() {
        // GridLayout mengisi dari Kiri-Atas (Row 0) ke Kanan-Bawah.
        // Kita baca isi boardMatrix yang sudah diisi zig-zag tadi.
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                int number = boardMatrix[i][j];
                JPanel square = createSquare(number, i, j);

                // Simpan panel ini ke array mapping agar bisa dipanggil ID-nya
                squarePanels[number] = square;

                add(square);
            }
        }
    }

    private JPanel createSquare(int number, int row, int col) {
        JPanel panel = new JPanel(new BorderLayout());

        // Warna Papan Catur (Checkerboard pattern)
        if ((row + col) % 2 == 0) {
            panel.setBackground(new Color(245, 245, 220)); // Beige
        } else {
            panel.setBackground(new Color(176, 224, 230)); // Powder Blue
        }

        panel.setBorder(new LineBorder(Color.GRAY));

        // Label Angka (Ditaruh di atas/North)
        JLabel label = new JLabel(String.valueOf(number));
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 0));
        panel.add(label, BorderLayout.NORTH);

        return panel;
    }

    // Method untuk menggambar ulang posisi semua pemain
    public void updatePlayerPawns(java.util.Collection<Player> players) {
        // 1. Bersihkan semua kotak dari pion lama
        for (int i = 1; i <= 64; i++) {
            JPanel sq = squarePanels[i];
            // Hapus komponen di Center (lokasi pion)
            Component centerComp = ((BorderLayout)sq.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (centerComp != null) {
                sq.remove(centerComp);
            }
            sq.repaint();
            sq.revalidate();
        }

        // 2. Gambar ulang pion pemain di posisi baru
        for (Player p : players) {
            JPanel targetSquare = squarePanels[p.getPosition()];

            // Buat visual pion sederhana (Kotak kecil berwarna)
            JPanel pawn = new JPanel();
            pawn.setBackground(p.getColor());
            pawn.setPreferredSize(new Dimension(25, 25));
            pawn.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            pawn.setToolTipText(p.getName());

            // Panel transparan pembungkus agar pion ada di tengah
            JPanel wrapper = new JPanel(new GridBagLayout());
            wrapper.setOpaque(false);
            wrapper.add(pawn);

            targetSquare.add(wrapper, BorderLayout.CENTER);
            targetSquare.revalidate();
        }
    }
}