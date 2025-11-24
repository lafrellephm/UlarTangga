import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class UlarTanggaMain extends JFrame {

    // Main UI Components
    private BoardPanel boardPanel;
    private JPanel rightPanelContainer;
    private CardLayout cardLayout;

    // --- SETUP PANEL COMPONENTS ---
    private JPanel setupPanel;
    private JComboBox<Integer> playerCountCombo;
    private JPanel namesInputPanel;
    private List<JTextField> nameFields;

    // --- GAME PANEL COMPONENTS ---
    private JPanel gamePanel;
    private JTextArea logArea;
    private JButton rollButton;
    private JLabel turnLabel;

    // Logic Data
    private Deque<Player> playerQueue;
    private Random random;
    private Map<Integer, Integer> ladders;

    public UlarTanggaMain() {
        super("Ular Tangga (Integrated Setup)");
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}

        // 1. Init Data
        random = new Random();
        initLadders();

        // 2. Setup Frame Utama
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 3. Board Panel (Langsung dipasang agar garis terlihat)
        boardPanel = new BoardPanel();
        boardPanel.setLaddersMap(ladders);
        add(boardPanel, BorderLayout.CENTER);

        // 4. Init Right Panel (CardLayout: Setup vs Game)
        initRightPanel();
        add(rightPanelContainer, BorderLayout.EAST);

        setVisible(true);
    }

    private void initLadders() {
        ladders = new HashMap<>();
        ladders.put(4, 14);
        ladders.put(12, 28);
        ladders.put(24, 36);
        ladders.put(42, 58);
        ladders.put(50, 60);
    }

    private void initRightPanel() {
        cardLayout = new CardLayout();
        rightPanelContainer = new JPanel(cardLayout);
        rightPanelContainer.setPreferredSize(new Dimension(350, 700));

        createSetupPanel();
        createGamePanel();

        rightPanelContainer.add(setupPanel, "SETUP");
        rightPanelContainer.add(gamePanel, "GAME");

        cardLayout.show(rightPanelContainer, "SETUP");
    }

    // --- HALAMAN 1: SETUP ---
    private void createSetupPanel() {
        setupPanel = new JPanel(new BorderLayout());
        setupPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("GAME CONFIGURATION", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        setupPanel.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        JPanel comboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        comboPanel.add(new JLabel("Jumlah Pemain:"));
        Integer[] options = {2, 3, 4};
        playerCountCombo = new JComboBox<>(options);
        comboPanel.add(playerCountCombo);
        formPanel.add(comboPanel);

        formPanel.add(Box.createVerticalStrut(15));

        namesInputPanel = new JPanel();
        namesInputPanel.setLayout(new BoxLayout(namesInputPanel, BoxLayout.Y_AXIS));
        nameFields = new ArrayList<>();

        updateNameFields(2);

        playerCountCombo.addActionListener(e -> {
            int count = (int) playerCountCombo.getSelectedItem();
            updateNameFields(count);
        });

        formPanel.add(namesInputPanel);
        setupPanel.add(formPanel, BorderLayout.CENTER);

        JButton btnStart = new JButton("START GAME");
        btnStart.setFont(new Font("Arial", Font.BOLD, 16));
        btnStart.setBackground(new Color(70, 130, 180));
        btnStart.setForeground(Color.GREEN);
        btnStart.setPreferredSize(new Dimension(100, 50));

        btnStart.addActionListener(e -> startGame());

        setupPanel.add(btnStart, BorderLayout.SOUTH);
    }

    private void updateNameFields(int count) {
        namesInputPanel.removeAll();
        nameFields.clear();

        for (int i = 0; i < count; i++) {
            JPanel p = new JPanel(new BorderLayout());
            p.setBorder(new EmptyBorder(5, 0, 5, 0));
            p.add(new JLabel("Nama Player " + (i + 1) + ":"), BorderLayout.NORTH);

            JTextField tf = new JTextField("Player " + (i + 1));
            nameFields.add(tf);
            p.add(tf, BorderLayout.CENTER);

            namesInputPanel.add(p);
        }
        namesInputPanel.revalidate();
        namesInputPanel.repaint();
    }

    private void startGame() {
        playerQueue = new LinkedList<>();
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE};

        int count = nameFields.size();
        for (int i = 0; i < count; i++) {
            String name = nameFields.get(i).getText().trim();
            if (name.isEmpty()) name = "Player " + (i + 1);
            playerQueue.add(new Player(name, colors[i]));
        }

        boardPanel.updatePlayerPawns(playerQueue);

        turnLabel.setText("Giliran: " + playerQueue.peek().getName());
        logArea.setText("Game Dimulai!\n----------------\n");

        cardLayout.show(rightPanelContainer, "GAME");
    }

    // --- HALAMAN 2: GAME CONTROL ---
    private void createGamePanel() {
        gamePanel = new JPanel(new BorderLayout());
        gamePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextArea infoTxt = new JTextArea(
                "Info Aturan:\n" +
                        "1. Naik Tangga: Start = PRIMA.\n" +
                        "2. Extra Turn: Di KELIPATAN 5.\n" +
                        "3. Merah: Mundur 1 langkah.\n" + // Update Text Info
                        "Linked Nodes:\n4->14, 12->28, 24->36, 42->58, 50->60\n"
        );
        infoTxt.setFont(new Font("Arial", Font.ITALIC, 11));
        infoTxt.setEditable(false);
        infoTxt.setBackground(new Color(240, 240, 240));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(new TitledBorder("Game Log"));

        JPanel btnPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        turnLabel = new JLabel("Giliran: -", SwingConstants.CENTER);
        turnLabel.setFont(new Font("Arial", Font.BOLD, 18));

        rollButton = new JButton("ROLL DADU");
        rollButton.setFont(new Font("Arial", Font.BOLD, 16));
        rollButton.setBackground(new Color(46, 139, 87));
        rollButton.setForeground(Color.BLUE);
        rollButton.addActionListener(e -> processTurnWithAnimation());

        btnPanel.add(infoTxt);
        btnPanel.add(turnLabel);
        btnPanel.add(rollButton);

        gamePanel.add(scroll, BorderLayout.CENTER);
        gamePanel.add(btnPanel, BorderLayout.SOUTH);
    }

    private boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) return false;
        }
        return true;
    }

    // --- LOGIKA UTAMA (MODIFIED) ---
    private void processTurnWithAnimation() {
        rollButton.setEnabled(false);
        Player currentPlayer = playerQueue.poll();

        int startPos = currentPlayer.getPosition();
        boolean startIsPrime = isPrime(startPos);

        int diceVal = random.nextInt(6) + 1;
        double chance = random.nextDouble();
        boolean isGreen = chance < 0.7; // 70% Peluang Maju

        // --- BAGIAN YANG DIUBAH ---
        int finalDicePos = startPos;
        if (isGreen) {
            finalDicePos += diceVal; // Maju sesuai dadu
        } else {
            finalDicePos -= 1;       // Mundur HANYA 1 langkah (penalti tetap)
        }
        // ---------------------------

        // Validasi Batas
        if (finalDicePos < 1) finalDicePos = 1;
        if (finalDicePos > 64) finalDicePos = 64;

        String direction = isGreen ? "[MAJU]" : "[MUNDUR -1]";
        logArea.append(String.format("%s (Pos: %d) Start Prima: %b\n", currentPlayer.getName(), startPos, startIsPrime));
        logArea.append(String.format("Dadu: %d %s -> Target: %d\n", diceVal, direction, finalDicePos));

        final int targetPos = finalDicePos;

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                int current = startPos;
                int step = (targetPos > startPos) ? 1 : -1;

                // Loop animasi
                while (current != targetPos) {
                    Thread.sleep(1000);
                    current += step;
                    publish(current);
                }
                return null;
            }
            @Override
            protected void process(List<Integer> chunks) {
                currentPlayer.setPositionRaw(chunks.get(chunks.size() - 1));
                boardPanel.updatePlayerPawns(getAllPlayersIncluding(currentPlayer));
            }
            @Override
            protected void done() {
                handleLadderLogic(currentPlayer, targetPos, startIsPrime);
            }
        };
        worker.execute();
    }

    private void handleLadderLogic(Player player, int currentPos, boolean startWasPrime) {
        if (ladders.containsKey(currentPos)) {
            int ladderDest = ladders.get(currentPos);
            if (startWasPrime) {
                logArea.append(">> LINKED NODE! Start Prima. Naik ke " + ladderDest + "\n");
                javax.swing.Timer ladderTimer = new javax.swing.Timer(1000, e -> {
                    player.setPosition(ladderDest);
                    finishTurn(player, ladderDest);
                    ((javax.swing.Timer)e.getSource()).stop();
                });
                ladderTimer.setInitialDelay(1000);
                ladderTimer.start();
                return;
            } else {
                logArea.append(">> Kaki Tangga di " + currentPos + ", tapi Start bukan Prima.\n");
            }
        }
        finishTurn(player, currentPos);
    }

    private void finishTurn(Player player, int finalPos) {
        player.setPosition(finalPos);
        boardPanel.updatePlayerPawns(getAllPlayersIncluding(player));
        logArea.append("Posisi Akhir: " + finalPos + "\n");

        if (finalPos == 64) {
            JOptionPane.showMessageDialog(this, "SELAMAT!\n" + player.getName() + " MENANG!");
            rollButton.setEnabled(false);
            playerQueue.addLast(player);
            return;
        }

        if (finalPos % 5 == 0) {
            logArea.append(">> EXTRA TURN! Kelipatan 5.\n");
            JOptionPane.showMessageDialog(this, player.getName() + " dapat EXTRA TURN!");
            playerQueue.addFirst(player);
            turnLabel.setText("Giliran (LAGI): " + player.getName());
        } else {
            playerQueue.addLast(player);
            turnLabel.setText("Giliran: " + playerQueue.peek().getName());
        }

        logArea.append("--------------------\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
        rollButton.setEnabled(true);
    }

    private Queue<Player> getAllPlayersIncluding(Player current) {
        Queue<Player> all = new LinkedList<>(playerQueue);
        all.add(current);
        return all;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UlarTanggaMain());
    }
}