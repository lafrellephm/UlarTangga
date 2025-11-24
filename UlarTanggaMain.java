import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class UlarTanggaMain extends JFrame {

    // Components
    private BoardPanel boardPanel;
    private JTextArea logArea;
    private JButton rollButton;
    private JLabel turnLabel;

    // Game Logic Data
    private Queue<Player> playerQueue;
    private Random random;

    public UlarTanggaMain() {
        super("Ular Tangga Java (64 Kotak)");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        random = new Random();

        // 1. Setup Pemain (Menggunakan Queue)
        initPlayers();

        // 2. Setup Area Board (Visual Kiri)
        boardPanel = new BoardPanel();
        add(boardPanel, BorderLayout.CENTER);

        // 3. Setup Area Kontrol (Visual Kanan)
        initControlPanel();

        // 4. Render awal pion pemain
        boardPanel.updatePlayerPawns(playerQueue);
    }

    private void initPlayers() {
        playerQueue = new LinkedList<>();
        playerQueue.add(new Player("Player 1", Color.RED));
        playerQueue.add(new Player("Player 2", Color.BLUE));
    }

    private void initControlPanel() {
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setPreferredSize(new Dimension(320, 700));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Log Area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(new TitledBorder("Game Log"));

        // Button & Info
        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        turnLabel = new JLabel("Giliran: " + playerQueue.peek().getName(), SwingConstants.CENTER);
        turnLabel.setFont(new Font("Arial", Font.BOLD, 18));

        rollButton = new JButton("ROLL DADU");
        rollButton.setFont(new Font("Arial", Font.BOLD, 14));
        rollButton.setBackground(new Color(46, 139, 87));
        rollButton.setForeground(Color.WHITE);

        rollButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processTurn();
            }
        });

        btnPanel.add(turnLabel);
        btnPanel.add(rollButton);

        controlPanel.add(scroll, BorderLayout.CENTER);
        controlPanel.add(btnPanel, BorderLayout.SOUTH);

        add(controlPanel, BorderLayout.EAST);
    }

    // --- LOGIKA UTAMA PERMAINAN (Requirement 3) ---
    private void processTurn() {
        Player currentPlayer = playerQueue.poll();

        // 1. Random Angka Dadu (1-6)
        int diceVal = random.nextInt(6) + 1;

        // 2. Random Warna (Probabilitas)
        // 0.0 - 0.69 (70%) = Hijau (Maju)
        // 0.70 - 1.0 (30%) = Merah (Mundur)
        double chance = random.nextDouble();
        boolean isGreen = chance < 0.7;

        String logMsg = currentPlayer.getName() + " (Pos: " + currentPlayer.getPosition() + ")\n";
        logMsg += "Dadu: " + diceVal;

        int nextPos = currentPlayer.getPosition();

        if (isGreen) {
            logMsg += " [HIJAU - MAJU]\n";
            nextPos += diceVal;
        } else {
            logMsg += " [MERAH - MUNDUR]\n";
            nextPos -= diceVal;
        }

        // 3. Validasi Batas
        if (nextPos < 1) {
            logMsg += "-> Mentok bawah! Balik ke 1.\n";
            nextPos = 1;
        }

        if (nextPos >= 64) {
            nextPos = 64;
            logMsg += "-> FINISH!\n";
            JOptionPane.showMessageDialog(this, "SELAMAT!\n" + currentPlayer.getName() + " MENANG!");
            rollButton.setEnabled(false);
        }

        // 4. Simpan Posisi Baru
        currentPlayer.setPosition(nextPos);
        logMsg += "Posisi Akhir: " + nextPos + "\n--------------------\n";

        // 5. Update UI
        logArea.append(logMsg);
        logArea.setCaretPosition(logArea.getDocument().getLength());

        // Update gambar pion di papan
        // Kita perlu queue utuh untuk menggambar semua pion, jadi masukkan dulu pemain ini
        // (Kecuali dia menang, tapi logic visual tetap butuh objeknya)
        if (nextPos < 64) {
            playerQueue.add(currentPlayer);
            turnLabel.setText("Giliran: " + playerQueue.peek().getName());
        } else {
            // Jika menang, masukkan kembali agar tetap tergambar di board (opsional)
            playerQueue.add(currentPlayer);
        }

        boardPanel.updatePlayerPawns(playerQueue);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new UlarTanggaMain().setVisible(true);
        });
    }
}