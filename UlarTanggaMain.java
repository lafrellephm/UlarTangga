import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class UlarTanggaMain extends JFrame {

    // Main UI
    private BoardPanel boardPanel;
    private JPanel rightPanelContainer;
    private CardLayout cardLayout;

    // Setup Panel
    private JPanel setupPanel;
    private JComboBox<Integer> playerCountCombo;
    private JPanel namesInputPanel;
    private List<JTextField> nameFields;

    // Game Panel Components
    private JPanel gamePanel;
    private JTextArea logArea;
    private JTextArea scoreBoardArea;
    private JButton rollButton;
    private JLabel turnLabel;

    // Logic Data
    private Deque<Player> playerQueue; // Queue untuk giliran (Urutan berubah-ubah)
    private List<Player> fixedPlayerList; // List untuk Scoreboard (Urutan TETAP)

    private Random random;
    private Map<Integer, Integer> ladders;
    private final int FINISH_BONUS = 100;

    public UlarTanggaMain() {
        super("Ular Tangga (Fixed Realtime Scoreboard)");
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}

        random = new Random();
        initLadders();

        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        boardPanel = new BoardPanel();
        boardPanel.setLaddersMap(ladders);
        add(boardPanel, BorderLayout.CENTER);

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

    // --- SETUP PANEL ---
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
        btnStart.setForeground(Color.WHITE);
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
        fixedPlayerList = new ArrayList<>(); // Inisialisasi list tetap

        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE};

        int count = nameFields.size();
        for (int i = 0; i < count; i++) {
            String name = nameFields.get(i).getText().trim();
            if (name.isEmpty()) name = "Player " + (i + 1);

            Player newPlayer = new Player(name, colors[i]);

            // Masukkan ke kedua koleksi
            playerQueue.add(newPlayer);       // Untuk logika giliran (berputar)
            fixedPlayerList.add(newPlayer);   // Untuk display scoreboard (tetap)
        }

        boardPanel.updatePlayerPawns(playerQueue);
        turnLabel.setText("Giliran: " + playerQueue.peek().getName());
        logArea.setText("Game Dimulai!\n----------------\n");
        updateScoreBoard();

        cardLayout.show(rightPanelContainer, "GAME");
    }

    // --- GAME PANEL ---
    private void createGamePanel() {
        gamePanel = new JPanel(new BorderLayout());
        gamePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topInfoPanel = new JPanel(new BorderLayout());

        JTextArea infoTxt = new JTextArea(
                "Rules:\n" +
                        "1. Naik Tangga: Start = PRIMA.\n" +
                        "2. Merah: Mundur 1 langkah.\n" +
                        "3. Pemenang = POIN TERTINGGI."
        );
        infoTxt.setFont(new Font("Arial", Font.ITALIC, 11));
        infoTxt.setEditable(false);
        infoTxt.setBackground(new Color(240, 240, 240));
        infoTxt.setBorder(new EmptyBorder(0,0,10,0));

        scoreBoardArea = new JTextArea(5, 20);
        scoreBoardArea.setEditable(false);
        scoreBoardArea.setFont(new Font("Monospaced", Font.BOLD, 12));
        JScrollPane scoreScroll = new JScrollPane(scoreBoardArea);
        scoreScroll.setBorder(new TitledBorder("Live Scoreboard"));

        topInfoPanel.add(infoTxt, BorderLayout.NORTH);
        topInfoPanel.add(scoreScroll, BorderLayout.CENTER);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(new TitledBorder("Game Log"));

        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        turnLabel = new JLabel("Giliran: -", SwingConstants.CENTER);
        turnLabel.setFont(new Font("Arial", Font.BOLD, 18));

        rollButton = new JButton("ROLL DADU");
        rollButton.setFont(new Font("Arial", Font.BOLD, 16));
        rollButton.setBackground(new Color(46, 139, 87));
        rollButton.setForeground(Color.WHITE);
        rollButton.addActionListener(e -> processTurnWithAnimation());

        btnPanel.add(turnLabel);
        btnPanel.add(rollButton);

        gamePanel.add(topInfoPanel, BorderLayout.NORTH);
        gamePanel.add(logScroll, BorderLayout.CENTER);
        gamePanel.add(btnPanel, BorderLayout.SOUTH);
    }

    // --- LOGIKA UTAMA ---

    // PERBAIKAN: Gunakan fixedPlayerList agar urutan nama TIDAK berubah
    private void updateScoreBoard() {
        StringBuilder sb = new StringBuilder();
        // Loop berdasarkan list yang urutannya statis
        for (Player p : fixedPlayerList) {
            sb.append(String.format("%-10s : %d pts\n", p.getName(), p.getScore()));
        }
        scoreBoardArea.setText(sb.toString());
    }

    private boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) return false;
        }
        return true;
    }

    private void processTurnWithAnimation() {
        rollButton.setEnabled(false);
        Player currentPlayer = playerQueue.poll();

        int startPos = currentPlayer.getPosition();
        boolean startIsPrime = isPrime(startPos);

        int diceVal = random.nextInt(6) + 1;
        double chance = random.nextDouble();
        boolean isGreen = chance < 0.7;

        int finalDicePos = startPos;
        if (isGreen) finalDicePos += diceVal;
        else finalDicePos -= 1;

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
            }
        }
        finishTurn(player, currentPos);
    }

    private void finishTurn(Player player, int finalPos) {
        player.setPosition(finalPos);

        // Update Poin
        int gainedPoints = boardPanel.getPointsAt(finalPos);
        player.addScore(gainedPoints);
        logArea.append("Mendarat di " + finalPos + ". Dapat +" + gainedPoints + " poin.\n");

        boardPanel.updatePlayerPawns(getAllPlayersIncluding(player));

        // Update Scoreboard (Nama tetap, Poin berubah)
        updateScoreBoard();

        if (finalPos == 64) {
            player.addScore(FINISH_BONUS);
            logArea.append("FINISH! Bonus +" + FINISH_BONUS + " poin.\n");
            updateScoreBoard();

            playerQueue.addLast(player);
            showWinnerDialog();

            rollButton.setEnabled(false);
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

    private void showWinnerDialog() {
        PriorityQueue<Player> rankingQueue = new PriorityQueue<>(
                (p1, p2) -> Integer.compare(p2.getScore(), p1.getScore())
        );
        rankingQueue.addAll(fixedPlayerList); // Gunakan fixed list, isinya sama saja

        StringBuilder msg = new StringBuilder("PERMAINAN SELESAI!\n\nLeaderboard:\n");
        int rank = 1;
        String winnerName = "";

        while (!rankingQueue.isEmpty()) {
            Player p = rankingQueue.poll();
            if (rank == 1) winnerName = p.getName();

            msg.append(rank).append(". ").append(p.getName())
                    .append(" - Skor: ").append(p.getScore()).append("\n");
            rank++;
        }

        JOptionPane.showMessageDialog(this, msg.toString(), "Winner: " + winnerName, JOptionPane.INFORMATION_MESSAGE);
    }

    private Queue<Player> getAllPlayersIncluding(Player current) {
        Queue<Player> all = new LinkedList<>(playerQueue);
        if (!all.contains(current)) {
            all.add(current);
        }
        return all;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UlarTanggaMain());
    }
}