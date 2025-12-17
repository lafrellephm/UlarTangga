import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class UlarTanggaMain extends JFrame {

    // --- MAIN UI ---
    private BoardPanel boardPanel;
    private JPanel rightPanelContainer;
    private CardLayout cardLayout;

    // --- UI SETUP PANEL COMPONENTS ---
    private JPanel setupPanel;
    private JPanel formPanel;
    private JLabel titleLabel;
    private JButton btnStart;

    private JComboBox<Integer> playerCountCombo;
    private JComboBox<ThemeManager.Theme> themeCombo;
    private JComboBox<Integer> ladderCountCombo;
    private JComboBox<Integer> bossCountCombo;

    private JPanel namesInputPanel;
    private List<JTextField> nameFields;

    // --- UI GAME PANEL COMPONENTS ---
    private JPanel gamePanel;
    private JPanel leftControlPanel;
    private JPanel rightInfoPanel;
    private JPanel turnInfoContainer;
    private JLabel diceVisualLabel;

    private DicePanel dicePanel;
    private PlayerAvatarPanel currentAvatarPanel;
    private JLabel currentPlayerNameLabel;

    private JTextArea infoTxt;
    private JTextArea logArea;
    private JTextArea scoreBoardArea;
    private JButton rollButton;
    private JButton exitGameButton; // [BARU] Tombol Exit Adventure

    // --- LOGIC DATA ---
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

        // --- WINDOW SETUP ---
        setUndecorated(false);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(false);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // CENTER: Board Panel
        boardPanel = new BoardPanel();
        add(boardPanel, BorderLayout.CENTER);

        // EAST: Right Panel
        initRightPanel();
        add(rightPanelContainer, BorderLayout.EAST);

        setVisible(true);
    }

    // --- INITIALIZATION ---

    private void initRightPanel() {
        cardLayout = new CardLayout();
        rightPanelContainer = new JPanel(cardLayout);
        rightPanelContainer.setPreferredSize(new Dimension(350, 0));

        createSetupPanel();
        createGamePanel();

        rightPanelContainer.add(setupPanel, "SETUP");
        rightPanelContainer.add(gamePanel, "GAME");
        cardLayout.show(rightPanelContainer, "SETUP");
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
            if (rowStart != rowEnd) ladders.put(start, end);
        }
        boardPanel.setLaddersMap(ladders);
    }

    // --- SETUP PANEL ---

    private void createSetupPanel() {
        setupPanel = new JPanel(new BorderLayout());
        setupPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        titleLabel = new JLabel("<html><div style='text-align: center;'>GAME<br>CONFIGURATION</div></html>", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Verdana", Font.BOLD, 24));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        setupPanel.add(titleLabel, BorderLayout.NORTH);

        formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);

        Integer[] pOptions = {2, 3, 4};
        playerCountCombo = new JComboBox<>(pOptions);
        addConfigRow(formPanel, "Jumlah Pemain:", playerCountCombo);

        Integer[] lOptions = {4, 6, 8, 10, 12};
        ladderCountCombo = new JComboBox<>(lOptions);
        ladderCountCombo.setSelectedIndex(1);
        addConfigRow(formPanel, "Jumlah Tangga:", ladderCountCombo);

        Integer[] bOptions = {0, 1, 2, 3, 4};
        bossCountCombo = new JComboBox<>(bOptions);
        bossCountCombo.setSelectedIndex(0);
        addConfigRow(formPanel, "Jumlah Bos:", bossCountCombo);

        themeCombo = new JComboBox<>(ThemeManager.Theme.values());
        addConfigRow(formPanel, "Tema Board:", themeCombo);

        formPanel.add(Box.createVerticalStrut(20));

        namesInputPanel = new JPanel();
        namesInputPanel.setLayout(new BoxLayout(namesInputPanel, BoxLayout.Y_AXIS));
        namesInputPanel.setOpaque(false);
        nameFields = new ArrayList<>();
        updateNameFields(2);

        formPanel.add(namesInputPanel);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        setupPanel.add(scrollPane, BorderLayout.CENTER);

        btnStart = new JButton("START ADVENTURE");
        btnStart.setFont(new Font("Arial", Font.BOLD, 18));
        btnStart.setFocusPainted(false);
        btnStart.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnStart.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnStart.setPreferredSize(new Dimension(240, 60));

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnWrapper.setOpaque(false);
        btnWrapper.setBorder(new EmptyBorder(10, 0, 0, 0));
        btnWrapper.add(btnStart);

        setupPanel.add(btnWrapper, BorderLayout.SOUTH);

        playerCountCombo.addActionListener(e -> {
            int count = (int) playerCountCombo.getSelectedItem();
            updateNameFields(count);
            updateSetupTheme(ThemeManager.getCurrentTheme());
            formPanel.revalidate();
            formPanel.repaint();
        });

        themeCombo.addActionListener(e -> {
            ThemeManager.Theme selected = (ThemeManager.Theme) themeCombo.getSelectedItem();
            ThemeManager.setTheme(selected);
            boardPanel.applyTheme();
            updateSetupTheme(selected);
        });

        btnStart.addActionListener(e -> startGame());

        updateSetupTheme(ThemeManager.getCurrentTheme());
    }

    // --- GAME PANEL (DASHBOARD) ---

    private void createGamePanel() {
        gamePanel = new JPanel(new BorderLayout(0, 10));
        gamePanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel upperSection = new JPanel(new BorderLayout(10, 0));
        upperSection.setOpaque(false);

        // 1. LEFT CONTROL PANEL (Turn & Dice)
        leftControlPanel = new JPanel();
        leftControlPanel.setLayout(new BoxLayout(leftControlPanel, BoxLayout.Y_AXIS));
        leftControlPanel.setPreferredSize(new Dimension(140, 0));
        leftControlPanel.setOpaque(false);

        // Turn Info Box
        turnInfoContainer = new JPanel(new BorderLayout());
        turnInfoContainer.setMaximumSize(new Dimension(140, 110));
        // Style border & color diatur di updateGameTheme nantinya

        currentPlayerNameLabel = new JLabel("Player 1", SwingConstants.CENTER);
        currentPlayerNameLabel.setFont(new Font("Arial", Font.BOLD, 13));
        currentPlayerNameLabel.setBorder(new EmptyBorder(5, 0, 5, 0));

        currentAvatarPanel = new PlayerAvatarPanel();

        turnInfoContainer.add(currentAvatarPanel, BorderLayout.CENTER);
        turnInfoContainer.add(currentPlayerNameLabel, BorderLayout.SOUTH);

        // Dice Visual
        dicePanel = new DicePanel();
        JPanel diceContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        diceContainer.setOpaque(false);
        diceContainer.add(dicePanel);

        diceVisualLabel = new JLabel("Dice Visual:");
        diceVisualLabel.setFont(new Font("Arial", Font.BOLD, 12));
        diceVisualLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftControlPanel.add(turnInfoContainer);
        leftControlPanel.add(Box.createVerticalStrut(20));
        leftControlPanel.add(diceVisualLabel);
        leftControlPanel.add(diceContainer);
        leftControlPanel.add(Box.createVerticalGlue());

        // 2. RIGHT INFO PANEL (Rules, Score, Log)
        rightInfoPanel = new JPanel(new BorderLayout(0, 10));
        rightInfoPanel.setOpaque(false);

        JPanel topInfoPanel = new JPanel(new BorderLayout(0, 5));
        topInfoPanel.setOpaque(false);

        // Rules Box
        infoTxt = new JTextArea(
                "Rules:\n1. Hijau = Maju, Merah = Mundur.\n2. Tangga HANYA JIKA Start = Prima.\n3. BOS: Tertahan sampai dapat dadu 6."
        );
        infoTxt.setFont(new Font("SansSerif", Font.PLAIN, 11));
        infoTxt.setEditable(false);
        infoTxt.setLineWrap(true);
        infoTxt.setWrapStyleWord(true);
        // Border diatur di updateGameTheme

        // Scoreboard
        scoreBoardArea = new JTextArea(6, 20);
        scoreBoardArea.setEditable(false);
        scoreBoardArea.setFont(new Font("Monospaced", Font.BOLD, 12));
        JScrollPane scoreScroll = new JScrollPane(scoreBoardArea);
        // Border ScrollPane diatur di updateGameTheme

        topInfoPanel.add(infoTxt, BorderLayout.NORTH);
        topInfoPanel.add(scoreScroll, BorderLayout.CENTER);

        // Log Area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);

        rightInfoPanel.add(topInfoPanel, BorderLayout.NORTH);
        rightInfoPanel.add(logScroll, BorderLayout.CENTER);

        upperSection.add(leftControlPanel, BorderLayout.WEST);
        upperSection.add(rightInfoPanel, BorderLayout.CENTER);

        // --- BUTTONS AREA (ROLL & EXIT) ---
        // Menggunakan GridLayout(2, 1) agar Vertikal (Atas-Bawah) dan lebar penuh
        JPanel bottomButtonPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        bottomButtonPanel.setOpaque(false);
        bottomButtonPanel.setPreferredSize(new Dimension(0, 120)); // Tinggi cukup untuk 2 tombol

        // A. Tombol Roll (Posisi ATAS)
        rollButton = new JButton("ROLL DADU (SPACE)");
        rollButton.setFont(new Font("Arial", Font.BOLD, 16));
        rollButton.setFocusPainted(false);
        rollButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rollButton.setUI(new javax.swing.plaf.basic.BasicButtonUI()); // Wajib untuk warna background
        rollButton.addActionListener(e -> processTurnWithAnimation());

        // B. Tombol Exit (Posisi BAWAH)
        exitGameButton = new JButton("EXIT ADVENTURE");
        exitGameButton.setFont(new Font("Arial", Font.BOLD, 14));
        exitGameButton.setFocusPainted(false);
        exitGameButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exitGameButton.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        exitGameButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Keluar ke menu utama?\nProgress game saat ini akan hilang.",
                    "Exit Adventure", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                cardLayout.show(rightPanelContainer, "SETUP");
            }
        });

        bottomButtonPanel.add(rollButton);
        bottomButtonPanel.add(exitGameButton);

        gamePanel.add(upperSection, BorderLayout.CENTER);
        gamePanel.add(bottomButtonPanel, BorderLayout.SOUTH);

        // --- KEY BINDING (SPACEBAR UNTUK ROLL) ---
        InputMap inputMap = gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = gamePanel.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("SPACE"), "rollTheDice");
        actionMap.put("rollTheDice", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (rollButton.isEnabled()) {
                    rollButton.doClick(); // Simulasikan klik tombol
                }
            }
        });
    }

    // --- STYLING HELPERS ---

    private void updateSetupTheme(ThemeManager.Theme theme) {
        if (theme == null) return;

        setupPanel.setBackground(theme.tileColor1.darker());
        titleLabel.setForeground(Color.WHITE);

        Color btnBg = theme.btnBg;
        Color textCol = getContrastColor(btnBg);

        btnStart.setBackground(btnBg);
        btnStart.setForeground(textCol);
        btnStart.setOpaque(true);
        btnStart.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(theme.ladderColor, 3, true),
                new EmptyBorder(10, 20, 10, 20)
        ));

        for (JTextField tf : nameFields) {
            tf.setBackground(Color.WHITE);
            tf.setForeground(Color.BLACK);
            tf.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(theme.btnBg, 2),
                    new EmptyBorder(5, 5, 5, 5)
            ));
        }

        updateLabelsRecursive(formPanel, Color.WHITE);
        setupPanel.repaint();
    }

    // [PERBAIKAN] Update tampilan Dashboard Game dengan Background SOLID
    private void updateGameTheme(ThemeManager.Theme theme) {
        if (theme == null) return;

        // 1. Background Utama Panel Game
        gamePanel.setBackground(theme.tileColor1.darker());

        Color accentColor = theme.ladderColor;
        Color btnColor = theme.btnBg;
        Color textColor = Color.WHITE;

        // 2. Styling Turn Info
        TitledBorder turnBorder = BorderFactory.createTitledBorder(
                new LineBorder(accentColor, 2), "Current Turn");
        turnBorder.setTitleColor(accentColor);
        turnBorder.setTitleFont(new Font("Arial", Font.BOLD, 12));

        turnInfoContainer.setBorder(turnBorder);
        turnInfoContainer.setBackground(Color.WHITE); // Solid White

        // 3. Styling Label
        diceVisualLabel.setForeground(textColor);

        // 4. Styling Text Areas (Rules, Score, Log)
        Color borderColor = theme.btnBg;

        // Warna background solid untuk kotak teks (Putih Gading/Krem sangat muda)
        // Jangan gunakan Alpha (transparansi) agar tidak glitch
        Color boxBgColor = new Color(250, 250, 250);

        // --- Rules Box ---
        infoTxt.setBackground(boxBgColor);
        infoTxt.setForeground(Color.BLACK);
        infoTxt.setOpaque(true); // Wajib True agar warna solid tergambar
        infoTxt.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 2), new EmptyBorder(5, 5, 5, 5)));

        // --- Scoreboard ---
        JScrollPane scoreScroll = (JScrollPane) scoreBoardArea.getParent().getParent();
        if (scoreScroll != null) {
            TitledBorder sbBorder = BorderFactory.createTitledBorder(
                    new LineBorder(borderColor, 2), "Live Scoreboard");
            sbBorder.setTitleColor(textColor);
            sbBorder.setTitleFont(new Font("Arial", Font.BOLD, 12));
            scoreScroll.setBorder(sbBorder);
            // ScrollPane biarkan transparan, tapi kontennya (TextArea) dibuat solid
            scoreScroll.setOpaque(false);
            scoreScroll.getViewport().setOpaque(false);
        }
        scoreBoardArea.setBackground(boxBgColor); // Solid
        scoreBoardArea.setOpaque(true);           // Solid (Mengatasi glitch background)

        // --- Log Area ---
        JScrollPane logScroll = (JScrollPane) logArea.getParent().getParent();
        if (logScroll != null) {
            TitledBorder logBorder = BorderFactory.createTitledBorder(
                    new LineBorder(borderColor, 2), "Game Log");
            logBorder.setTitleColor(textColor);
            logBorder.setTitleFont(new Font("Arial", Font.BOLD, 12));
            logScroll.setBorder(logBorder);
            logScroll.setOpaque(false);
            logScroll.getViewport().setOpaque(false);
        }
        logArea.setBackground(boxBgColor); // Solid
        logArea.setOpaque(true);           // Solid (Mengatasi glitch background)

        // 5. Styling Roll Button
        Color rollTextCol = getContrastColor(btnColor);
        rollButton.setBackground(btnColor);
        rollButton.setForeground(rollTextCol);
        rollButton.setOpaque(true);
        rollButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(accentColor, 3, true), new EmptyBorder(5, 0, 5, 0)
        ));

        // 6. Styling Tombol Exit
        exitGameButton.setBackground(btnColor.darker());
        exitGameButton.setForeground(Color.WHITE);
        exitGameButton.setOpaque(true);
        exitGameButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.WHITE, 2, true), new EmptyBorder(5, 0, 5, 0)
        ));

        // Paksa repaint ulang agar artefak visual hilang
        gamePanel.revalidate();
        gamePanel.repaint();
    }

    private void addConfigRow(JPanel parent, String labelText, JComboBox<?> combo) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        row.setBorder(new EmptyBorder(5, 0, 5, 0));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Arial", Font.BOLD, 14));
        lbl.setForeground(Color.WHITE);

        combo.setFont(new Font("Arial", Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
        ((JComponent) combo.getRenderer()).setBorder(new EmptyBorder(5,5,5,5));

        row.add(lbl, BorderLayout.WEST);
        row.add(combo, BorderLayout.EAST);
        combo.setPreferredSize(new Dimension(150, 30));
        parent.add(row);
    }

    private void updateNameFields(int count) {
        namesInputPanel.removeAll();
        nameFields.clear();
        for (int i = 0; i < count; i++) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            p.setPreferredSize(new Dimension(300, 60));
            p.setMinimumSize(new Dimension(200, 60));
            p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
            p.setBorder(new EmptyBorder(5, 0, 5, 0));

            JLabel lbl = new JLabel("Hero " + (i + 1));
            lbl.setFont(new Font("Arial", Font.PLAIN, 12));
            lbl.setForeground(Color.WHITE);

            JTextField tf = new JTextField("Player " + (i + 1));
            tf.setFont(new Font("Arial", Font.PLAIN, 14));

            nameFields.add(tf);
            p.add(lbl, BorderLayout.NORTH);
            p.add(tf, BorderLayout.CENTER);
            namesInputPanel.add(p);
            namesInputPanel.add(Box.createVerticalStrut(10));
        }
        namesInputPanel.revalidate();
        namesInputPanel.repaint();
    }

    private void updateLabelsRecursive(Container container, Color color) {
        for (Component c : container.getComponents()) {
            if (c instanceof JLabel) {
                ((JLabel) c).setForeground(color);
            } else if (c instanceof Container) {
                updateLabelsRecursive((Container) c, color);
            }
        }
    }

    private Color getContrastColor(Color background) {
        double luminance = (0.299 * background.getRed() +
                0.587 * background.getGreen() +
                0.114 * background.getBlue()) / 255;
        return luminance < 0.5 ? Color.WHITE : Color.BLACK;
    }

    // --- GAME LOGIC ---

    private void startGame() {
        ThemeManager.Theme selectedTheme = (ThemeManager.Theme) themeCombo.getSelectedItem();
        ThemeManager.setTheme(selectedTheme);

        updateGameTheme(selectedTheme);

        int selectedLadderCount = (int) ladderCountCombo.getSelectedItem();
        initRandomLadders(selectedLadderCount);

        int selectedBossCount = (int) bossCountCombo.getSelectedItem();
        boardPanel.generateBossTiles(selectedBossCount);

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
            // Restore score if playing again
            if (globalScoreMap.containsKey(name)) {
                newPlayer.setScore(globalScoreMap.get(name));
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

    private void processTurnWithAnimation() {
        rollButton.setEnabled(false);
        exitGameButton.setEnabled(false); // Disable exit saat roll
        try { SoundUtility.playSound("dice_roll.wav"); } catch (Exception ex) {}

        Player currentPlayer = playerQueue.peek();
        int realDiceVal = random.nextInt(6) + 1;
        double chance = random.nextDouble();
        boolean isGreen = chance < 0.7;

        if (currentPlayer.isTrapped()) {
            logArea.append(">> " + currentPlayer.getName() + " TERTAHAN BOS! Butuh angka 6.\n");
        }

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

    private boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n == 2 || n == 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) return false;
        }
        return true;
    }

    private void executeMovementLogic(int diceVal, boolean isGreen) {
        Player currentPlayer = playerQueue.poll();
        int startPos = currentPlayer.getPosition();

        if (currentPlayer.isTrapped()) {
            logArea.append(String.format("   Status: Trapped di Tile %d. Dadu: %d\n", startPos, diceVal));
            if (diceVal == 6) {
                logArea.append("   [BOSS FIGHT] Dadu 6! BERHASIL lolos!\n");
                performBossReward(currentPlayer);
                currentPlayer.setTrapped(false);
                finishTurn(currentPlayer, startPos, true);
            } else {
                logArea.append("   [BOSS FIGHT] Gagal. Masih tertahan.\n");
                finishTurn(currentPlayer, startPos, true);
            }
            return;
        }

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
                movementPath.add(ladderTop);

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

    private void performBossReward(Player winner) {
        int totalStolen = 0;
        for (Player victim : fixedPlayerList) {
            if (victim != winner && victim.getScore() > 0) {
                int stealAmount = victim.getScore() / 2;
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
            exitGameButton.setEnabled(true);
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
        exitGameButton.setEnabled(true);
    }

    private void showWinnerDialog() {
        // Simpan skor ke Global Map sebelum dialog agar "Pilih Map Baru" membawa skor update
        for (Player p : fixedPlayerList) {
            globalScoreMap.put(p.getName(), p.getScore());
        }

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

        Object[] options = {"Main Lagi", "Pilih Map Baru", "Keluar"};
        int choice = JOptionPane.showOptionDialog(this,
                msg.toString() + "\nApakah Anda ingin lanjut?",
                "Winner: " + winnerName,
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 0) { // Main Lagi (Reset board dengan setting sama)
            startGame();
        } else if (choice == 1) { // Pilih Map Baru (Kembali ke Setup dengan skor tersimpan)
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