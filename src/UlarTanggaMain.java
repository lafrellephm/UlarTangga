import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class UlarTanggaMain extends JFrame {

    // Main UI
    private BoardPanel boardPanel;
    private JPanel rightPanelContainer;
    private CardLayout cardLayout;

    // UI Komponen Game
    private DicePanel dicePanel;
    private PlayerAvatarPanel currentAvatarPanel;
    private JLabel currentPlayerNameLabel;

    // Setup Panel
    private JPanel setupPanel;
    private JComboBox<Integer> playerCountCombo;
    private JComboBox<ThemeManager.Theme> themeCombo;
    private JComboBox<Integer> ladderCountCombo;
    private JComboBox<Integer> bossCountCombo; // [BARU] Config Bos

    private JPanel namesInputPanel;
    private List<JTextField> nameFields;

    // Game Panel Components
    private JPanel gamePanel;
    private JTextArea logArea;
    private JTextArea scoreBoardArea;
    private JButton rollButton;

    // Logic Data
    private Deque<Player> playerQueue;
    private List<Player> fixedPlayerList;

    private Random random;
    private Map<Integer, Integer> ladders;
    private final int FINISH_BONUS = 100;

    private Map<String, Integer> globalScoreMap = new HashMap<>();
    private DijkstraPathFinder pathFinder;

    public UlarTanggaMain() {
        super("Ladder Board Game - Boss Edition");
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}

        random = new Random();
        ladders = new HashMap<>();
        pathFinder = new DijkstraPathFinder();

        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        boardPanel = new BoardPanel();
        add(boardPanel, BorderLayout.CENTER);

        initRightPanel();
        add(rightPanelContainer, BorderLayout.EAST);

        setVisible(true);
    }

    private void initRandomLadders(int targetLadders) {
        ladders = new HashMap<>();
        int attempts = 0;

        while (ladders.size() < targetLadders && attempts < 1000) {
            attempts++;
            int start = random.nextInt(60) + 2;
            int end = start + random.nextInt(63 - start) + 1;
            if (end > 63) end = 63;

            if (ladders.containsKey(start) || ladders.containsValue(start) ||
                    ladders.containsKey(end) || ladders.containsValue(end)) {
                continue;
            }

            int rowStart = (start - 1) / 8;
            int rowEnd = (end - 1) / 8;
            if (rowStart != rowEnd) {
                ladders.put(start, end);
            }
        }
        boardPanel.setLaddersMap(ladders);
    }

    private void initRightPanel() {
        cardLayout = new CardLayout();
        rightPanelContainer = new JPanel(cardLayout);
        rightPanelContainer.setPreferredSize(new Dimension(400, 700));

        createSetupPanel();
        createGamePanel();

        rightPanelContainer.add(setupPanel, "SETUP");
        rightPanelContainer.add(gamePanel, "GAME");
        cardLayout.show(rightPanelContainer, "SETUP");
    }

    private void createSetupPanel() {
        setupPanel = new JPanel(new BorderLayout());
        setupPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("GAME CONFIGURATION", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        setupPanel.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        JPanel playerComboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        playerComboPanel.add(new JLabel("Jumlah Pemain:"));
        Integer[] options = {2, 3, 4};
        playerCountCombo = new JComboBox<>(options);
        playerComboPanel.add(playerCountCombo);
        formPanel.add(playerComboPanel);

        // Config Tangga
        JPanel ladderComboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ladderComboPanel.add(new JLabel("Jumlah Tangga:"));
        Integer[] ladderOptions = {4, 6, 8, 10, 12};
        ladderCountCombo = new JComboBox<>(ladderOptions);
        ladderCountCombo.setSelectedIndex(1);
        ladderComboPanel.add(ladderCountCombo);
        formPanel.add(ladderComboPanel);

        // [BARU] Config Bos
        JPanel bossComboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bossComboPanel.add(new JLabel("Jumlah Bos:"));
        Integer[] bossOptions = {0, 1, 2, 3, 4};
        bossCountCombo = new JComboBox<>(bossOptions);
        bossCountCombo.setSelectedIndex(0);
        bossComboPanel.add(bossCountCombo);
        formPanel.add(bossComboPanel);

        // Config Tema
        JPanel themeComboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        themeComboPanel.add(new JLabel("Tema Board:"));
        themeCombo = new JComboBox<>(ThemeManager.Theme.values());
        themeComboPanel.add(themeCombo);
        formPanel.add(themeComboPanel);

        formPanel.add(Box.createVerticalStrut(15));

        namesInputPanel = new JPanel();
        namesInputPanel.setLayout(new BoxLayout(namesInputPanel, BoxLayout.Y_AXIS));
        nameFields = new ArrayList<>();
        updateNameFields(2);

        playerCountCombo.addActionListener(e -> {
            int count = (int) playerCountCombo.getSelectedItem();
            updateNameFields(count);
        });

        themeCombo.addActionListener(e -> {
            ThemeManager.Theme selected = (ThemeManager.Theme) themeCombo.getSelectedItem();
            ThemeManager.setTheme(selected);
            boardPanel.applyTheme();
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
        ThemeManager.Theme selectedTheme = (ThemeManager.Theme) themeCombo.getSelectedItem();
        ThemeManager.setTheme(selectedTheme);

        rollButton.setBackground(selectedTheme.btnBg);
        rollButton.setForeground(selectedTheme.btnFg);

        // 1. Generate Tangga
        int selectedLadderCount = (int) ladderCountCombo.getSelectedItem();
        initRandomLadders(selectedLadderCount);

        // 2. [BARU] Generate Bos
        int selectedBossCount = (int) bossCountCombo.getSelectedItem();
        boardPanel.generateBossTiles(selectedBossCount);

        // 3. Reset Poin & UI
        boardPanel.generateNewTilePoints();
        boardPanel.applyTheme();

        playerQueue = new LinkedList<>();
        fixedPlayerList = new ArrayList<>();

        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE};

        int count = nameFields.size();
        for (int i = 0; i < count; i++) {
            String name = nameFields.get(i).getText().trim();
            if (name.isEmpty()) name = "Player " + (i + 1);

            Player newPlayer = new Player(name, colors[i]);

            if (globalScoreMap.containsKey(name)) {
                int savedScore = globalScoreMap.get(name);
                newPlayer.setScore(savedScore);
            }

            playerQueue.add(newPlayer);
            fixedPlayerList.add(newPlayer);
        }

        boardPanel.updatePlayerPawns(playerQueue);
        updateScoreBoard();
        updateTurnInfo();

        logArea.setText("Game Dimulai!\nTheme: " + selectedTheme.name + "\n");
        logArea.append("Tangga: " + selectedLadderCount + ", Bos: " + selectedBossCount + "\n");
        if (!globalScoreMap.isEmpty()) {
            logArea.append("Scoreboard dimuat (Akumulasi).\n");
        }
        logArea.append("----------------\n");

        cardLayout.show(rightPanelContainer, "GAME");
        rollButton.setEnabled(true);
    }

    private void createGamePanel() {
        gamePanel = new JPanel(new BorderLayout(0, 10));
        gamePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel upperSection = new JPanel(new BorderLayout(10, 0));

        JPanel leftControlPanel = new JPanel();
        leftControlPanel.setLayout(new BoxLayout(leftControlPanel, BoxLayout.Y_AXIS));
        leftControlPanel.setPreferredSize(new Dimension(130, 0));

        JPanel turnInfoContainer = new JPanel(new BorderLayout());
        turnInfoContainer.setBorder(new TitledBorder(new LineBorder(Color.GRAY), "Turn"));
        turnInfoContainer.setMaximumSize(new Dimension(130, 90));
        turnInfoContainer.setBackground(Color.WHITE);

        currentPlayerNameLabel = new JLabel("Player 1", SwingConstants.CENTER);
        currentPlayerNameLabel.setFont(new Font("Arial", Font.BOLD, 12));

        currentAvatarPanel = new PlayerAvatarPanel();

        turnInfoContainer.add(currentAvatarPanel, BorderLayout.CENTER);
        turnInfoContainer.add(currentPlayerNameLabel, BorderLayout.SOUTH);

        dicePanel = new DicePanel();
        JPanel diceContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        diceContainer.add(dicePanel);

        leftControlPanel.add(turnInfoContainer);
        leftControlPanel.add(Box.createVerticalStrut(30));
        leftControlPanel.add(new JLabel("Dice Visual:"));
        leftControlPanel.add(diceContainer);
        leftControlPanel.add(Box.createVerticalGlue());

        JPanel rightInfoPanel = new JPanel(new BorderLayout());

        JPanel topInfoPanel = new JPanel(new BorderLayout());
        JTextArea infoTxt = new JTextArea(
                "Rules:\n1. Hijau = Maju, Merah = Mundur.\n2. Tangga HANYA JIKA Start = Prima.\n3. BOS: Tertahan sampai dapat dadu 6."
        );
        infoTxt.setFont(new Font("Arial", Font.ITALIC, 11));
        infoTxt.setEditable(false);
        infoTxt.setBackground(new Color(245, 245, 245));
        infoTxt.setBorder(new EmptyBorder(0, 0, 5, 0));

        scoreBoardArea = new JTextArea(6, 20);
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

        rightInfoPanel.add(topInfoPanel, BorderLayout.NORTH);
        rightInfoPanel.add(logScroll, BorderLayout.CENTER);

        upperSection.add(leftControlPanel, BorderLayout.WEST);
        upperSection.add(rightInfoPanel, BorderLayout.CENTER);

        rollButton = new JButton("ROLL DADU");
        rollButton.setFont(new Font("Arial", Font.BOLD, 18));
        rollButton.setBackground(Color.BLUE);
        rollButton.setForeground(Color.WHITE);
        rollButton.setFocusPainted(false);
        rollButton.setPreferredSize(new Dimension(0, 50));
        rollButton.addActionListener(e -> processTurnWithAnimation());

        gamePanel.add(upperSection, BorderLayout.CENTER);
        gamePanel.add(rollButton, BorderLayout.SOUTH);
    }

    private void updateTurnInfo() {
        if (playerQueue != null && !playerQueue.isEmpty()) {
            Player current = playerQueue.peek();
            currentPlayerNameLabel.setText(current.getName());
            currentAvatarPanel.setAvatarColor(current.getColor());
        }
    }

    private void updateScoreBoard() {
        StringBuilder sb = new StringBuilder();
        for (Player p : fixedPlayerList) {
            String trapped = p.isTrapped() ? " [BOSS!]" : "";
            sb.append(String.format("%-8s: %d%s\n",
                    p.getName().length() > 8 ? p.getName().substring(0,8) : p.getName(),
                    p.getScore(), trapped));
        }
        scoreBoardArea.setText(sb.toString());
    }

    private boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n == 2 || n == 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) return false;
        }
        return true;
    }

    private void processTurnWithAnimation() {
        rollButton.setEnabled(false);
        try { SoundUtility.playSound("dice_roll.wav"); } catch (Exception ex) {}

        Player currentPlayer = playerQueue.peek();

        int realDiceVal = random.nextInt(6) + 1;
        double chance = random.nextDouble();
        boolean isGreen = chance < 0.7;

        // --- CEK STATUS BOS (TRAPPED) ---
        if (currentPlayer.isTrapped()) {
            logArea.append(">> " + currentPlayer.getName() + " TERTAHAN BOS! Butuh angka 6.\n");
        }
        // --------------------------------

        final int totalAnimationSteps = 15;
        javax.swing.Timer diceTimer = new javax.swing.Timer(80, null);
        diceTimer.addActionListener(new java.awt.event.ActionListener() {
            int step = 0;
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                step++;
                dicePanel.setDiceColor(Color.WHITE);
                dicePanel.setNumber(random.nextInt(6) + 1);

                if (step >= totalAnimationSteps) {
                    diceTimer.stop();
                    dicePanel.setNumber(realDiceVal);
                    if (isGreen) {
                        dicePanel.setDiceColor(new Color(144, 238, 144));
                        try { SoundUtility.playSound("move_green.wav"); } catch(Exception ex){}
                    } else {
                        dicePanel.setDiceColor(new Color(255, 99, 71));
                        try { SoundUtility.playSound("move_red.wav"); } catch(Exception ex){}
                    }
                    executeMovementLogic(realDiceVal, isGreen);
                }
            }
        });
        diceTimer.start();
    }

    private void executeMovementLogic(int diceVal, boolean isGreen) {
        Player currentPlayer = playerQueue.poll();
        int startPos = currentPlayer.getPosition();

        // --- LOGIKA 1: PEMAIN TERTAHAN BOS ---
        if (currentPlayer.isTrapped()) {
            logArea.append(String.format("   Status: Trapped di Tile %d. Dadu: %d\n", startPos, diceVal));

            if (diceVal == 6) {
                logArea.append("   [BOSS FIGHT] Dadu 6! BERHASIL lolos!\n");
                // Curi Poin
                performBossReward(currentPlayer);
                // Bebas, tapi giliran habis (tidak jalan)
                currentPlayer.setTrapped(false);
                finishTurn(currentPlayer, startPos, true);
            } else {
                logArea.append("   [BOSS FIGHT] Gagal. Masih tertahan.\n");
                finishTurn(currentPlayer, startPos, true);
            }
            return;
        }

        // --- LOGIKA 2: GERAKAN NORMAL (TERMASUK PRIMA & TANGGA) ---
        boolean startIsPrime = isPrime(startPos);
        String direction = isGreen ? "[MAJU]" : "[MUNDUR - REVERSE]";
        logArea.append(String.format(">> %s (Start: %d): Dadu %d %s\n",
                currentPlayer.getName(), startPos, diceVal, direction));

        int targetPos;
        List<Integer> movementPath = new ArrayList<>();

        if (isGreen) {
            int ladderIntersection = -1;
            int stepsToIntersection = 0;

            if (startIsPrime) {
                for (int i = 1; i <= diceVal; i++) {
                    int checkPos = startPos + i;
                    if (checkPos > 64) break;
                    if (ladders.containsKey(checkPos)) {
                        ladderIntersection = checkPos;
                        stepsToIntersection = i;
                        break;
                    }
                }
            }

            if (ladderIntersection != -1) {
                int ladderTop = ladders.get(ladderIntersection);
                int remainingSteps = diceVal - stepsToIntersection;

                logArea.append(String.format("   [PRIME EFFECT] Start Prima (%d) -> Injak Tangga di %d -> AUTO NAIK ke %d\n",
                        startPos, ladderIntersection, ladderTop));

                movementPath.addAll(pathFinder.findShortestPath(startPos, ladderIntersection));
                movementPath.add(ladderTop); // Visual naik

                if (remainingSteps > 0) {
                    logArea.append(String.format("   [PRIME EFFECT] Lanjut sisa %d langkah dari %d\n", remainingSteps, ladderTop));
                    int finalTarget = ladderTop + remainingSteps;
                    if (finalTarget > 64) finalTarget = 64;

                    List<Integer> pathRest = pathFinder.findShortestPath(ladderTop, finalTarget);
                    if (!pathRest.isEmpty()) pathRest.remove(0);
                    movementPath.addAll(pathRest);
                    targetPos = finalTarget;
                } else {
                    targetPos = ladderTop;
                }

            } else {
                targetPos = startPos + diceVal;
                if (targetPos > 64) targetPos = 64;
                movementPath = pathFinder.findShortestPath(startPos, targetPos);

                if (ladders.containsKey(targetPos)) {
                    logArea.append("   (Berhenti di dasar tangga " + targetPos + ", tapi Start BUKAN Prima -> Tidak naik)\n");
                }
            }
        } else {
            targetPos = currentPlayer.getPreviousPosition();
            if (targetPos == startPos) {
                logArea.append("   (Masih di Start, tidak bisa mundur)\n");
                movementPath.add(startPos);
            } else {
                movementPath.add(startPos);
                movementPath.add(targetPos);
            }
        }

        final int finalDestinationCalc = targetPos;
        final List<Integer> animPath = movementPath;

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (int pos : animPath) {
                    if (pos != startPos) {
                        Thread.sleep(300);
                        try { SoundUtility.playSound("step.wav"); } catch(Exception ex){}
                    }
                    publish(pos);
                }
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                int latestPos = chunks.get(chunks.size() - 1);
                currentPlayer.setPositionRaw(latestPos);
                boardPanel.updatePlayerPawns(getAllPlayersIncluding(currentPlayer));
            }

            @Override
            protected void done() {
                // [BARU] CEK APAKAH MENDARAT DI BOS
                if (boardPanel.isBossTile(finalDestinationCalc)) {
                    logArea.append("   [DANGER] Mendarat di Tile BOS (" + finalDestinationCalc + ")!\n");
                    logArea.append("   Anda TERTAHAN sampai mendapat dadu 6.\n");
                    currentPlayer.setTrapped(true);
                }

                finishTurn(currentPlayer, finalDestinationCalc, isGreen);
            }
        };
        worker.execute();
    }

    // [BARU] Logika Mencuri Poin (Reward Bos)
    private void performBossReward(Player winner) {
        int totalStolen = 0;
        for (Player victim : fixedPlayerList) {
            if (victim != winner && victim.getScore() > 0) {
                int stealAmount = victim.getScore() / 2; // 50%
                victim.addScore(-stealAmount);
                winner.addScore(stealAmount);
                totalStolen += stealAmount;
                logArea.append("   - Mencuri " + stealAmount + " pts dari " + victim.getName() + "\n");
            }
        }
        if (totalStolen > 0) {
            logArea.append("   Total curian: +" + totalStolen + " pts!\n");
        } else {
            logArea.append("   (Lawan tidak punya poin untuk dicuri)\n");
        }
    }

    private void finishTurn(Player player, int finalPos, boolean isGreen) {
        if (isGreen) {
            player.setPosition(finalPos);
            int gainedPoints = boardPanel.claimPointsAt(finalPos);
            player.addScore(gainedPoints);
            if (gainedPoints > 0) logArea.append("   Posisi Akhir: " + finalPos + " (Dapat +" + gainedPoints + " pts)\n");
            else logArea.append("   Posisi Akhir: " + finalPos + "\n");
        } else {
            player.revertToPreviousStep();
            logArea.append("   Posisi Akhir: " + player.getPosition() + " (Kembali ke masa lalu)\n");
        }

        boardPanel.updatePlayerPawns(getAllPlayersIncluding(player));
        updateScoreBoard();

        if (player.getPosition() == 64) {
            player.addScore(FINISH_BONUS);
            logArea.append("FINISH! Bonus +" + FINISH_BONUS + "\n");
            updateScoreBoard();
            playerQueue.addLast(player);
            showWinnerDialog();
            rollButton.setEnabled(false);
            return;
        }

        if (player.getPosition() % 5 == 0 && !player.isTrapped()) {
            logArea.append("   [EXTRA TURN] Kelipatan 5!\n");
            JOptionPane.showMessageDialog(this, player.getName() + " dapat EXTRA TURN!");
            playerQueue.addFirst(player);
        } else {
            playerQueue.addLast(player);
        }

        updateTurnInfo();
        logArea.append("\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
        rollButton.setEnabled(true);
    }

    private void showWinnerDialog() {
        PriorityQueue<Player> rankingQueue = new PriorityQueue<>(
                (p1, p2) -> Integer.compare(p2.getScore(), p1.getScore())
        );
        rankingQueue.addAll(fixedPlayerList);
        for (Player p : fixedPlayerList) {
            globalScoreMap.put(p.getName(), p.getScore());
        }

        StringBuilder msg = new StringBuilder("PERMAINAN SELESAI!\n\nLeaderboard:\n");
        int rank = 1;
        String winnerName = "";
        while (!rankingQueue.isEmpty()) {
            Player p = rankingQueue.poll();
            if (rank == 1) winnerName = p.getName();
            msg.append(rank).append(". ").append(p.getName()).append(" - Skor: ").append(p.getScore()).append("\n");
            rank++;
        }

        Object[] options = {"Main Lagi", "Keluar"};
        int choice = JOptionPane.showOptionDialog(this,
                msg.toString() + "\nApakah Anda ingin main lagi? (Skor akan diakumulasi)",
                "Winner: " + winnerName,
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == JOptionPane.YES_OPTION) {
            cardLayout.show(rightPanelContainer, "SETUP");
            logArea.setText("");
        } else {
            System.exit(0);
        }
    }

    private Queue<Player> getAllPlayersIncluding(Player current) {
        Queue<Player> all = new LinkedList<>(playerQueue);
        if (!all.contains(current)) all.add(current);
        return all;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UlarTanggaMain());
    }

    class DijkstraPathFinder {
        public List<Integer> findShortestPath(int startNode, int endNode) {
            if (startNode == endNode) return Collections.singletonList(startNode);
            PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.distance));
            Map<Integer, Integer> distances = new HashMap<>();
            Map<Integer, Integer> previous = new HashMap<>();
            Set<Integer> visited = new HashSet<>();
            for (int i = 1; i <= 64; i++) distances.put(i, Integer.MAX_VALUE);
            distances.put(startNode, 0);
            pq.add(new NodeDistance(startNode, 0));

            while (!pq.isEmpty()) {
                NodeDistance current = pq.poll();
                int u = current.node;
                if (visited.contains(u)) continue;
                visited.add(u);
                if (u == endNode) break;
                List<Integer> neighbors = getNeighbors(u);
                for (int v : neighbors) {
                    if (!visited.contains(v)) {
                        int newDist = distances.get(u) + 1;
                        if (newDist < distances.get(v)) {
                            distances.put(v, newDist);
                            previous.put(v, u);
                            pq.add(new NodeDistance(v, newDist));
                        }
                    }
                }
            }
            List<Integer> path = new ArrayList<>();
            Integer crawl = endNode;
            if (!previous.containsKey(crawl) && startNode != endNode) return path;
            path.add(crawl);
            while (previous.containsKey(crawl)) {
                crawl = previous.get(crawl);
                path.add(crawl);
            }
            Collections.reverse(path);
            return path;
        }

        private List<Integer> getNeighbors(int node) {
            List<Integer> neighbors = new ArrayList<>();
            if (node + 1 <= 64) neighbors.add(node + 1);
            if (node - 1 >= 1) neighbors.add(node - 1);
            return neighbors;
        }

        class NodeDistance {
            int node, distance;
            public NodeDistance(int node, int distance) { this.node = node; this.distance = distance; }
        }
    }
}