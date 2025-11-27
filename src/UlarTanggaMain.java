import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class UlarTanggaMain extends JFrame {

    // Main UI
    private BoardPanel boardPanel;
    private JPanel rightPanelContainer;
    private CardLayout cardLayout;
    private DicePanel dicePanel;

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
    private Deque<Player> playerQueue; // Queue untuk giliran
    private List<Player> fixedPlayerList; // List untuk Scoreboard

    private Random random;
    private Map<Integer, Integer> ladders;
    private final int FINISH_BONUS = 100;

    public UlarTanggaMain() {
        super("Ular Tangga (Fixed Realtime Scoreboard)");
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}

        random = new Random();
        initLadders();

        setSize(1200, 750); // Ukuran pas untuk layout baru
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Init Board
        boardPanel = new BoardPanel();
        boardPanel.setLaddersMap(ladders);
        add(boardPanel, BorderLayout.CENTER);

        // 2. Init Panel Kanan
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
        rightPanelContainer.setPreferredSize(new Dimension(400, 700));

        // PENTING: Inisialisasi KEDUA panel di awal agar komponen siap
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

    // --- START GAME LOGIC ---
    private void startGame() {
        try {
            // Safety Check
            if (turnLabel == null || logArea == null) {
                // Harusnya tidak terjadi karena createGamePanel sudah dipanggil di initRightPanel
                JOptionPane.showMessageDialog(this, "Error inisialisasi UI. Silakan restart aplikasi.");
                return;
            }

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

            // Update Board
            boardPanel.updatePlayerPawns(playerQueue);
            
            // Update Label Giliran
            if (!playerQueue.isEmpty()) {
                turnLabel.setText(playerQueue.peek().getName());
            }

            logArea.setText("Game Dimulai!\n----------------\n");
            updateScoreBoard();

            // Pindah Layar
            cardLayout.show(rightPanelContainer, "GAME");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memulai game: " + ex.getMessage());
        }
    }

    // --- GAME PANEL (LAYOUT VISUAL BARU) ---
    private void createGamePanel() {
        gamePanel = new JPanel(new BorderLayout());
        gamePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. LOG & SCORE (KANAN - CENTER)
        JPanel logScoreContainer = new JPanel(new BorderLayout());
        
        JPanel topInfoPanel = new JPanel(new BorderLayout());
        JTextArea infoTxt = new JTextArea(
                "Rules:\n1. Tangga: Start PRIMA.\n2. Merah: Mundur.\n3. Win: Skor Tertinggi."
        );
        infoTxt.setFont(new Font("Arial", Font.ITALIC, 11));
        infoTxt.setEditable(false);
        infoTxt.setBackground(new Color(240, 240, 240));
        infoTxt.setBorder(new EmptyBorder(0, 0, 10, 0));

        scoreBoardArea = new JTextArea(5, 15);
        scoreBoardArea.setEditable(false);
        scoreBoardArea.setFont(new Font("Monospaced", Font.BOLD, 12));
        JScrollPane scoreScroll = new JScrollPane(scoreBoardArea);
        scoreScroll.setBorder(new TitledBorder("Live Score"));

        topInfoPanel.add(infoTxt, BorderLayout.NORTH);
        topInfoPanel.add(scoreScroll, BorderLayout.CENTER);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(new TitledBorder("Game Log"));

        logScoreContainer.add(topInfoPanel, BorderLayout.NORTH);
        logScoreContainer.add(logScroll, BorderLayout.CENTER);

        // 2. DADU AREA (KIRI - WEST) - Visual Dadu Tetap Ada
        JPanel diceContainer = new JPanel();
        diceContainer.setLayout(new BoxLayout(diceContainer, BoxLayout.Y_AXIS));
        diceContainer.setBorder(new EmptyBorder(0, 5, 0, 10));
        diceContainer.setPreferredSize(new Dimension(140, 0));

        turnLabel = new JLabel("Player 1", SwingConstants.CENTER);
        turnLabel.setFont(new Font("Arial", Font.BOLD, 16));
        turnLabel.setForeground(new Color(0, 102, 204));
        turnLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel labelStatic = new JLabel("Giliran Saat Ini:", SwingConstants.CENTER);
        labelStatic.setAlignmentX(Component.CENTER_ALIGNMENT);

        dicePanel = new DicePanel(); 
        dicePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        diceContainer.add(Box.createVerticalStrut(20));
        diceContainer.add(labelStatic);
        diceContainer.add(Box.createVerticalStrut(5));
        diceContainer.add(turnLabel);
        diceContainer.add(Box.createVerticalStrut(10));
        diceContainer.add(dicePanel);
        diceContainer.add(Box.createVerticalStrut(20));

        // 3. BUTTON
        rollButton = new JButton("ROLL DADU");
        rollButton.setFont(new Font("Arial", Font.BOLD, 16));
        rollButton.setBackground(new Color(46, 139, 87));
        rollButton.setForeground(Color.BLUE);
        rollButton.addActionListener(e -> processTurnWithAnimation());

        gamePanel.add(diceContainer, BorderLayout.WEST);
        gamePanel.add(logScoreContainer, BorderLayout.CENTER); 
        gamePanel.add(rollButton, BorderLayout.SOUTH);
    }

    // --- GAME LOGIC ---

    private void updateScoreBoard() {
        if (fixedPlayerList == null) return;
        StringBuilder sb = new StringBuilder();
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
        SoundUtility.playSound("dice_roll.wav");

        int realDiceVal = random.nextInt(6) + 1;
        double chance = random.nextDouble();
        boolean isGreen = chance < 0.7; // 70% Hijau

        final int totalAnimationSteps = 15;
        javax.swing.Timer diceTimer = new javax.swing.Timer(80, null);
        diceTimer.addActionListener(new ActionListener() {
            int step = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                step++;
                dicePanel.setDiceColor(Color.WHITE);
                dicePanel.setNumber(random.nextInt(6) + 1);

                if (step >= totalAnimationSteps) {
                    diceTimer.stop();
                    dicePanel.setNumber(realDiceVal);
                    if (isGreen) {
                        dicePanel.setDiceColor(new Color(144, 238, 144));
                        SoundUtility.playSound("move_green.wav");
                    } else {
                        dicePanel.setDiceColor(new Color(255, 99, 71));
                        SoundUtility.playSound("move_red.wav");
                    }
                    executeMovementLogic(realDiceVal, isGreen);
                }
            }
        });
        diceTimer.start();
    }

    private void executeMovementLogic(int diceVal, boolean isGreen) {
        Player currentPlayer = playerQueue.poll();
        if (currentPlayer == null) return;

        int startPos = currentPlayer.getPosition();
        boolean startIsPrime = isPrime(startPos);
        String direction = isGreen ? "[MAJU]" : "[MUNDUR -1]";

        logArea.append(String.format("%s (Pos: %d) Start Prima: %b\n", currentPlayer.getName(), startPos, startIsPrime));
        logArea.append(String.format("Dadu: %d %s\n", diceVal, direction));

        List<Integer> movementPath = new ArrayList<>();
        int currentSimPos = startPos;
        int stepsRemaining = diceVal;

        if (isGreen) {
            while (stepsRemaining > 0) {
                currentSimPos++;
                if (currentSimPos > 64) {
                    currentSimPos = 64;
                    movementPath.add(currentSimPos);
                    break;
                }
                if (startIsPrime && ladders.containsKey(currentSimPos)) {
                    int ladderDest = ladders.get(currentSimPos);
                    movementPath.add(currentSimPos); 
                    SoundUtility.playSound("ladder.wav");
                    currentSimPos = ladderDest; 
                    logArea.append(">> LINKED NODE! Start Prima. Naik tangga -> " + ladderDest + "\n");
                }
                movementPath.add(currentSimPos);
                stepsRemaining--;
            }
        } else {
            currentSimPos = (currentSimPos - 1 < 1) ? 1 : currentSimPos - 1;
            movementPath.add(currentSimPos);
        }

        final int finalDestination = currentSimPos;

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (int pos : movementPath) {
                    SoundUtility.playSound("step.wav");
                    Thread.sleep(300); 
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
                finishTurn(currentPlayer, finalDestination);
            }
        };
        worker.execute();
    }

    private void finishTurn(Player player, int finalPos) {
        player.setPosition(finalPos);
        int gainedPoints = boardPanel.getPointsAt(finalPos);
        player.addScore(gainedPoints);
        logArea.append("Mendarat di " + finalPos + ". Dapat +" + gainedPoints + " poin.\n");

        boardPanel.updatePlayerPawns(getAllPlayersIncluding(player));
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
            turnLabel.setText(player.getName() + " (LAGI)");
        } else {
            playerQueue.addLast(player);
            turnLabel.setText(playerQueue.peek().getName());
        }

        logArea.append("--------------------\n");
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
            msg.append(rank).append(". ").append(p.getName()).append(" - Skor: ").append(p.getScore()).append("\n");
            rank++;
        }
        JOptionPane.showMessageDialog(this, msg.toString(), "Winner: " + winnerName, JOptionPane.INFORMATION_MESSAGE);
    }

    private Queue<Player> getAllPlayersIncluding(Player current) {
        Queue<Player> all = new LinkedList<>(playerQueue);
        if (!all.contains(current)) all.add(current);
        return all;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UlarTanggaMain());
    }
}

// --- HELPER CLASSES ---

class Player {
    private String name;
    private int position;
    private int score;
    private Color color;

    public Player(String name, Color color) {
        this.name = name;
        this.color = color;
        this.position = 1; 
        this.score = 0;
    }
    public void setPositionRaw(int pos) { this.position = pos; }
    public void setPosition(int pos) { this.position = pos; }
    public int getPosition() { return position; }
    public String getName() { return name; }
    public int getScore() { return score; }
    public void addScore(int s) { this.score += s; }
    public Color getColor() { return color; }
}

class SoundUtility {
    public static void playSound(String filename) {
        // Implementasi suara jika ada
    }
}

// --- BOARD PANEL (Visual Tangga Realistis) ---
class BoardPanel extends JPanel {
    private Map<Integer, Integer> ladders;
    private Queue<Player> players; 

    public BoardPanel() { this.setBackground(Color.WHITE); }
    public void setLaddersMap(Map<Integer, Integer> ladders) { this.ladders = ladders; repaint(); }
    public void updatePlayerPawns(Queue<Player> players) { this.players = new LinkedList<>(players); repaint(); }
    public int getPointsAt(int pos) { return pos * 10; }

    private Point getCoordForPos(int pos) {
        if (pos < 1) pos = 1;
        if (pos > 64) pos = 64;
        int row = (pos - 1) / 8;
        int col = (pos - 1) % 8;
        if (row % 2 == 1) col = 7 - col;
        int drawRow = 7 - row;
        int w = getWidth();
        int h = getHeight();
        int cw = w / 8; 
        int ch = h / 8;
        return new Point(col * cw + cw/2, drawRow * ch + ch/2);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(); int h = getHeight();
        int cw = w / 8; int ch = h / 8;

        // Grid
        g2.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i <= 8; i++) {
            g2.drawLine(0, i * ch, w, i * ch);
            g2.drawLine(i * cw, 0, i * cw, h);
        }

        // Numbers
        g2.setColor(Color.GRAY);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        for (int i = 1; i <= 64; i++) {
            Point p = getCoordForPos(i);
            g2.drawString(String.valueOf(i), p.x - cw/2 + 5, p.y + ch/2 - 5);
        }

        // Real Ladder
        if (ladders != null) {
            for (Map.Entry<Integer, Integer> entry : ladders.entrySet()) {
                Point pStart = getCoordForPos(entry.getKey());
                Point pEnd = getCoordForPos(entry.getValue());
                drawRealLadder(g2, pStart.x, pStart.y, pEnd.x, pEnd.y);
            }
        }

        // Players
        if (players != null) {
            int offset = 0;
            for (Player p : players) {
                Point pt = getCoordForPos(p.getPosition());
                int drawX = pt.x - 15 + offset; 
                int drawY = pt.y - 15 + offset;
                g2.setColor(p.getColor());
                g2.fillOval(drawX, drawY, 30, 30);
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2));
                g2.drawOval(drawX, drawY, 30, 30);
                g2.setColor(Color.WHITE);
                String initial = (p.getName().length() > 0) ? p.getName().substring(0, 1).toUpperCase() : "?";
                g2.drawString(initial, drawX + 10, drawY + 20);
                offset += 5; 
            }
        }
    }

    private void drawRealLadder(Graphics2D g2, int x1, int y1, int x2, int y2) {
        g2.setColor(new Color(139, 69, 19)); // Warna Kayu
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int width = 20;
        double offsetX = Math.sin(angle) * (width / 2.0);
        double offsetY = Math.cos(angle) * (width / 2.0);
        int r1x1 = (int) (x1 - offsetX), r1y1 = (int) (y1 + offsetY);
        int r1x2 = (int) (x2 - offsetX), r1y2 = (int) (y2 + offsetY);
        int r2x1 = (int) (x1 + offsetX), r2y1 = (int) (y1 - offsetY);
        int r2x2 = (int) (x2 + offsetX), r2y2 = (int) (y2 - offsetY);
        g2.setStroke(new BasicStroke(3));
        g2.drawLine(r1x1, r1y1, r1x2, r1y2);
        g2.drawLine(r2x1, r2y1, r2x2, r2y2);
        g2.setStroke(new BasicStroke(2));
        double distance = Math.hypot(x2 - x1, y2 - y1);
        int steps = (int) (distance / 20);
        for (int i = 0; i <= steps; i++) {
            double ratio = (double) i / steps;
            int sx1 = (int) (r1x1 + (r1x2 - r1x1) * ratio);
            int sy1 = (int) (r1y1 + (r1y2 - r1y1) * ratio);
            int sx2 = (int) (r2x1 + (r2x2 - r2x1) * ratio);
            int sy2 = (int) (r2y1 + (r2y2 - r2y1) * ratio);
            g2.drawLine(sx1, sy1, sx2, sy2);
        }
    }
}

// --- DICE PANEL (Visual Dadu 2D) ---
class DicePanel extends JPanel {
    private int number = 1;
    private Color diceColor = Color.WHITE;

    public DicePanel() {
        setPreferredSize(new Dimension(80, 80));
        setBackground(new Color(230, 230, 250));
        setBorder(new EmptyBorder(5, 5, 5, 5));
    }
    public void setNumber(int number) { this.number = number; repaint(); }
    public void setDiceColor(Color color) { this.diceColor = color; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(); int h = getHeight();
        int diceSize = Math.min(w, h) - 10;
        int x = (w - diceSize) / 2; int y = (h - diceSize) / 2;
        g2.setColor(diceColor);
        g2.fillRoundRect(x, y, diceSize, diceSize, 15, 15);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x, y, diceSize, diceSize, 15, 15);
        g2.setColor(Color.BLACK);
        int dotSize = diceSize / 6;
        int center = diceSize / 2;
        int q1 = diceSize / 4; int q3 = diceSize * 3 / 4;
        if (number % 2 != 0) drawDot(g2, x + center, y + center, dotSize);
        if (number > 1) { drawDot(g2, x + q1, y + q1, dotSize); drawDot(g2, x + q3, y + q3, dotSize); }
        if (number > 3) { drawDot(g2, x + q3, y + q1, dotSize); drawDot(g2, x + q1, y + q3, dotSize); }
        if (number == 6) { drawDot(g2, x + q1, y + center, dotSize); drawDot(g2, x + q3, y + center, dotSize); }
    }
    private void drawDot(Graphics2D g2, int x, int y, int size) { g2.fillOval(x - size / 2, y - size / 2, size, size); }
}