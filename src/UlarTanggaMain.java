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
    
    // Algoritma
    private DijkstraPathFinder pathFinder;

    public UlarTanggaMain() {
        super("Ladder Board Game - Dijkstra Edition");
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}

        random = new Random();
        
        // 1. Inisialisasi Tangga Random
        initRandomLadders();
        
        // 2. Inisialisasi Dijkstra
        pathFinder = new DijkstraPathFinder(); 

        setSize(1200, 750);
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

    private void initRandomLadders() {
        ladders = new HashMap<>();
        int targetLadders = 6; 
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
                System.out.println("Ladder Generated: " + start + " -> " + end);
            }
        }
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
        fixedPlayerList = new ArrayList<>();

        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE};

        int count = nameFields.size();
        for (int i = 0; i < count; i++) {
            String name = nameFields.get(i).getText().trim();
            if (name.isEmpty()) name = "Player " + (i + 1);

            Player newPlayer = new Player(name, colors[i]);
            playerQueue.add(newPlayer);
            fixedPlayerList.add(newPlayer);
        }

        boardPanel.updatePlayerPawns(playerQueue);
        updateScoreBoard();
        updateTurnInfo(); 
        logArea.setText("Game Dimulai!\nRandom Map Generated.\n----------------\n");

        cardLayout.show(rightPanelContainer, "GAME");
    }

    private void createGamePanel() {
        gamePanel = new JPanel(new BorderLayout(0, 10));
        gamePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel upperSection = new JPanel(new BorderLayout(10, 0));

        // Panel KIRI
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

        // Panel KANAN
        JPanel rightInfoPanel = new JPanel(new BorderLayout());
        
        JPanel topInfoPanel = new JPanel(new BorderLayout());
        JTextArea infoTxt = new JTextArea(
                "Rules:\n1. Hijau = Maju & Dapat Poin.\n2. Merah = Mundur & 0 Poin.\n3. Dijkstra Pathfinding."
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
            sb.append(String.format("%-8s: %d\n", 
                p.getName().length() > 8 ? p.getName().substring(0,8) : p.getName(), 
                p.getScore()));
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
        try { SoundUtility.playSound("dice_roll.wav"); } catch (Exception ex) {}

        Player currentPlayer = playerQueue.peek(); 

        int realDiceVal = random.nextInt(6) + 1;
        double chance = random.nextDouble();
        boolean isGreen = chance < 0.7; 

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

    // --- UPDATE: LOGIKA GERAK DAN PENERUSAN STATUS WARNA DADU ---
    private void executeMovementLogic(int diceVal, boolean isGreen) {
        Player currentPlayer = playerQueue.poll();
        
        int startPos = currentPlayer.getPosition();
        boolean startIsPrime = isPrime(startPos);
        String direction = isGreen ? "[MAJU]" : "[MUNDUR]";

        logArea.append(String.format(">> %s: Dadu %d %s\n", currentPlayer.getName(), diceVal, direction));

        // 1. Tentukan Titik Akhir Berdasarkan Dadu (Target Sementara)
        int targetPos = startPos;
        if (isGreen) {
            targetPos += diceVal;
            if (targetPos > 64) targetPos = 64;
        } else {
            targetPos -= 1; // Mundur 1 langkah
            if (targetPos < 1) targetPos = 1;
        }

        // 2. Dijkstra Calculation
        List<Integer> movementPath = pathFinder.findShortestPath(startPos, targetPos);
        
        final int finalDestinationCalc = targetPos; 

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (int pos : movementPath) {
                    if (pos == startPos) continue;
                    try { SoundUtility.playSound("step.wav"); } catch(Exception ex){}
                    Thread.sleep(400); 
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
                int actualFinalPos = finalDestinationCalc;
                
                // Cek Tangga (Hanya jika Hijau & Start Prima)
                if (isGreen && startIsPrime && ladders.containsKey(actualFinalPos)) {
                    int ladderDest = ladders.get(actualFinalPos);
                    logArea.append("   (Dapat prima, rute menuju Tangga ditemukan! " + actualFinalPos + "->" + ladderDest + ")\n");
                    actualFinalPos = ladderDest;
                }
                
                // KIRIM STATUS isGreen KE finishTurn
                finishTurn(currentPlayer, actualFinalPos, isGreen);
            }
        };
        worker.execute();
    }

    // --- UPDATE: LOGIKA FINISH TURN (CEK POIN) ---
    private void finishTurn(Player player, int finalPos, boolean isGreen) {
        player.setPosition(finalPos);

        // LOGIKA BARU: Hanya dapat poin jika isGreen = true
        if (isGreen) {
            int gainedPoints = boardPanel.getPointsAt(finalPos);
            player.addScore(gainedPoints);
            logArea.append("   Posisi Akhir: " + finalPos + " (+ " + gainedPoints + " pts)\n");
        } else {
            // Jika merah (mundur), tidak dapat poin
            logArea.append("   Posisi Akhir: " + finalPos + " (Mundur -> 0 Poin)\n");
        }

        boardPanel.updatePlayerPawns(getAllPlayersIncluding(player));
        updateScoreBoard();

        if (finalPos == 64) {
            player.addScore(FINISH_BONUS);
            logArea.append("FINISH! Bonus +" + FINISH_BONUS + "\n");
            updateScoreBoard();
            playerQueue.addLast(player);
            showWinnerDialog();
            rollButton.setEnabled(false);
            return;
        }

        if (finalPos % 5 == 0) {
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

    // --- INNER CLASS: DIJKSTRA PATH FINDER ---
    class DijkstraPathFinder {
        public List<Integer> findShortestPath(int startNode, int endNode) {
            if (startNode == endNode) return Collections.singletonList(startNode);

            PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.distance));
            Map<Integer, Integer> distances = new HashMap<>();
            Map<Integer, Integer> previous = new HashMap<>();
            Set<Integer> visited = new HashSet<>();

            for (int i = 1; i <= 64; i++) {
                distances.put(i, Integer.MAX_VALUE);
            }
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
            if (!previous.containsKey(crawl) && startNode != endNode) {
                return path; 
            }
            
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
            int node;
            int distance;
            public NodeDistance(int node, int distance) {
                this.node = node;
                this.distance = distance;
            }
        }
    }
}

// --- CLASS VISUAL DADU ---
class DicePanel extends JPanel {
    private int number = 1;
    private Color diceColor = Color.WHITE;

    public DicePanel() {
        setPreferredSize(new Dimension(80, 80)); 
        setBackground(new Color(230, 230, 250));
    }

    public void setNumber(int number) {
        this.number = number;
        repaint();
    }

    public void setDiceColor(Color color) {
        this.diceColor = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int diceSize = 50; 
        int x = (w - diceSize) / 2;
        int y = (h - diceSize) / 2;

        g2.setColor(diceColor);
        g2.fillRoundRect(x, y, diceSize, diceSize, 15, 15);
        
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x, y, diceSize, diceSize, 15, 15);

        g2.setColor(Color.BLACK);
        int dotSize = 8;
        int center = diceSize / 2;
        int q1 = diceSize / 4;
        int q3 = diceSize * 3 / 4;

        if (number % 2 != 0) drawDot(g2, x + center, y + center, dotSize);
        if (number > 1) { drawDot(g2, x + q1, y + q1, dotSize); drawDot(g2, x + q3, y + q3, dotSize); }
        if (number > 3) { drawDot(g2, x + q3, y + q1, dotSize); drawDot(g2, x + q1, y + q3, dotSize); }
        if (number == 6) { drawDot(g2, x + q1, y + center, dotSize); drawDot(g2, x + q3, y + center, dotSize); }
    }

    private void drawDot(Graphics2D g2, int x, int y, int size) {
        g2.fillOval(x - size / 2, y - size / 2, size, size);
    }
}

// --- CLASS PLAYER AVATAR PANEL ---
class PlayerAvatarPanel extends JPanel {
    private Color avatarColor = Color.GRAY;

    public PlayerAvatarPanel() {
        setPreferredSize(new Dimension(50, 50));
        setBackground(Color.WHITE); 
    }

    public void setAvatarColor(Color c) {
        this.avatarColor = c;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = 40;
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        g2.setColor(avatarColor);
        g2.fillRoundRect(x, y, size, size, 10, 10);

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x, y, size, size, 10, 10);
    }
}