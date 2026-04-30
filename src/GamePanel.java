import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

class GamePanel extends JPanel implements ActionListener, GameEventListener {
    private ArrayList<NormalZombie> zombiesList = new ArrayList<>();
    private List<Projectile> projectileList = new ArrayList<>();
    private List<SoilEffect> soilEffects = new ArrayList<>();
    private List<Sun> sunList = new ArrayList<>();
    private List<LawnMower> lawnMowers = new ArrayList<>();
    private List<FallingCone> fallingCones = new ArrayList<>();
    private List<FallingBucket> fallingBuckets = new ArrayList<>();
    private long lastSunSpawn = System.currentTimeMillis();
    private static final long SUN_SPAWN_INTERVAL = 9000;
    private Timer timer;
    private long lastNano;
    private BufferedImage background;
    private BufferedImage sunCountImg;
    private int sunCount = 50;

    // === Directory Paths ===
    private final String GRAPHIC_PATH = "Graphic/";
    private final String SOUND_PATH = "SoundFX/";
    private final String BGM_PATH = "BGMusic/";

    // === Intro phase ===
    private enum GamePhase {
        MAIN_MENU, MORE_SCREEN, INTRO, PLAYING, GAME_OVER
    }

    private GamePhase gamePhase = GamePhase.MAIN_MENU;
    private long introStartTime;
    private static final long INITIAL_DELAY = 1000;
    private static final long READY_DURATION = 600;
    private static final long SET_DURATION = 700;
    private static final long PLANT_DURATION = 800;
    private boolean introSoundPlayed = false;
    private boolean zombiesSpawned = false;
    private long lastZombieWaveTime = -1;
    
    // === Wave Management ===
    private WaveManager waveManager = new WaveManager();
    private int currentWave = 1;


    // === Game Over ===
    private long gameOverStartTime = -1;
    private static final long FADE_DURATION = 1000;
    private static final long TEXT_FADE_DURATION = 800;
    private BufferedImage brainImg;
    private Rectangle retryBounds, exitBounds;
    private long retryClickTime = -1;

    // === Audio ===
    private Clip bgmClip;
    private Clip introClip; // IntroTheme music
    private long bgmStartTime;

    // === Main Menu ===
    private BufferedImage mainMenuImg;
    private BufferedImage startAdventureGlowImg;
    private BufferedImage moreGlowImg;
    private BufferedImage notMomJokeImg;
    private BufferedImage backBtnImg;
    private float menuFadeAlpha = 0f; // fade-in alpha for main menu (0→1)
    private long menuFadeInStart = -1; // timestamp when menu fade-in starts
    private int menuGlowBtn = -1; // 0=startAdventure, 1=more, -1=none
    private long menuGlowTime = -1;
    private boolean menuFadingOut = false; // fade to black after button click
    private long menuFadeOutStart = -1;
    private int menuFadeTarget = -1; // 0=start game, 1=more screen
    // More screen
    private float moreScreenAlpha = 0f;
    private long moreScreenFadeInStart = -1;
    private boolean moreFadingOut = false;
    private long moreFadeOutStart = -1;

    // Zombie announcement sounds
    private boolean zombieAnnouncePlayed = false;
    private long zombieAnnounceEndTime = -1; // khi nào sound "theZombiesAreComing" kết thúc
    private boolean firstGroanPlayed = false;
    private long nextGroanTime = -1;
    private final String[] groanFiles = {
            "Voices-groan.wav", "Voices-groan2.wav", "Voices-groan3.wav",
            "Voices-groan4.wav", "Voices-groan5.wav", "Voices-groan6.wav"
    };

    // === Shovel ===
    private BufferedImage shovelImg;
    private boolean isHoldingShovel = false;
    private Rectangle shovelBoxBounds;
    private long shovelPulseStart = -1;

    // === Menu & Settings ===
    private BufferedImage menuBtnImg;
    private BufferedImage settingTabImg;
    private BufferedImage tabRestartImg;
    private BufferedImage tabMainMenuImg;
    private BufferedImage tabBackToGameImg;
    private boolean settingsOpen = false;
    private long menuClickTime = -1;
    private long pauseStartTime = -1;
    private int settingsGlowBtn = -1;
    private long settingsGlowTime = -1;
    // Settings restart fade
    private boolean settingsRestartFading = false;
    private long settingsRestartFadeStart = -1;
    private int settingsFadeAction = -1;
    private float musicVolume = 1.0f;
    private float sfxVolume = 1.0f;
    private int draggingSlider = -1;

    static final int COLS = 9;
    static final int ROWS = 5;

    static final int TOP_BAR_H = 50;
    static final int CELL_W = 69;
    static final int CELL_H = 85;
    static final int GRID_LEFT = 80;
    static final int GRID_TOP = TOP_BAR_H + 10;

    private final List<PlantCard> cards = new ArrayList<>();
    private final Plant[][] grid = new Plant[ROWS][COLS];
    private Plant.Type heldType = null;
    private int hoverCol = -1;
    private int hoverRow = -1;
    private int mouseX, mouseY;

    // Preview sprite khi đang giữ cây (đổi theo loại cây)
    private java.util.Map<Plant.Type, BufferedImage> previewSheets = new java.util.HashMap<>();
    private java.util.Map<Plant.Type, int[]> previewDims = new java.util.HashMap<>();
    private int previewFrame = 0;
    private long lastPreviewTime = System.currentTimeMillis();

    public GamePanel() {
        this.setPreferredSize(new Dimension(800, 500));

        setFocusable(true);

        try {
            background = ImageIO.read(new File(GRAPHIC_PATH + "background.png"));
            sunCountImg = ImageIO.read(new File(GRAPHIC_PATH + "sunCount.png"));
            brainImg = ImageIO.read(new File(GRAPHIC_PATH + "brain.png"));
            shovelImg = ImageIO.read(new File(GRAPHIC_PATH + "shovel.png"));
            menuBtnImg = ImageIO.read(new File(GRAPHIC_PATH + "Menu.png"));
            settingTabImg = ImageIO.read(new File(GRAPHIC_PATH + "settingTab.png"));
            tabRestartImg = ImageIO.read(new File(GRAPHIC_PATH + "Tab_restartLevel.png"));
            tabMainMenuImg = ImageIO.read(new File(GRAPHIC_PATH + "Tab_mainMenu.png"));
            tabBackToGameImg = ImageIO.read(new File(GRAPHIC_PATH + "Tab_backToGame.png"));
            mainMenuImg = ImageIO.read(new File(GRAPHIC_PATH + "MainMenu.png"));
            startAdventureGlowImg = ImageIO.read(new File(GRAPHIC_PATH + "startAdventure.png"));
            moreGlowImg = ImageIO.read(new File(GRAPHIC_PATH + "more.png"));
            notMomJokeImg = ImageIO.read(new File(GRAPHIC_PATH + "notMomJoke.png"));
            backBtnImg = ImageIO.read(new File(GRAPHIC_PATH + "back.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            BufferedImage peaSheet = ImageIO.read(new File(GRAPHIC_PATH + "peashooter.png"));
            int peaFW = peaSheet.getWidth() / 8;
            int peaFH = peaSheet.getHeight();
            previewSheets.put(Plant.Type.PEASHOOTER, peaSheet);
            previewDims.put(Plant.Type.PEASHOOTER, new int[] { peaFW, peaFH, peaFW / 3, peaFH / 3, 8 });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            BufferedImage sfSheet = ImageIO.read(new File(GRAPHIC_PATH + "sunflower.png"));
            int sfFW = sfSheet.getWidth() / 6;
            int sfFH = sfSheet.getHeight();
            int sfDrawH = 62;
            int sfDrawW = (int) (sfFW * ((float) sfDrawH / sfFH));
            previewSheets.put(Plant.Type.SUNFLOWER, sfSheet);
            previewDims.put(Plant.Type.SUNFLOWER, new int[] { sfFW, sfFH, sfDrawW, sfDrawH, 6 });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            BufferedImage wnSheet = ImageIO.read(new File(GRAPHIC_PATH + "wallnut1.png"));
            int wnFW = wnSheet.getWidth() / 5;
            int wnFH = wnSheet.getHeight();
            int wnDrawH = 62;
            int wnDrawW = (int) (wnFW * ((float) wnDrawH / wnFH));
            previewSheets.put(Plant.Type.WALLNUT, wnSheet);
            previewDims.put(Plant.Type.WALLNUT, new int[] { wnFW, wnFH, wnDrawW, wnDrawH, 5 });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            BufferedImage cbSheet = ImageIO.read(new File(GRAPHIC_PATH + "cherryBomb.png"));
            int cbFW = cbSheet.getWidth() / 6;
            int cbFH = cbSheet.getHeight();
            int cbDrawH = 62;
            int cbDrawW = (int) (cbFW * ((float) cbDrawH / cbFH));
            previewSheets.put(Plant.Type.CHERRYBOMB, cbSheet);
            previewDims.put(Plant.Type.CHERRYBOMB, new int[] { cbFW, cbFH, cbDrawW, cbDrawH, 6 });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            BufferedImage tsSheet = ImageIO.read(new File(GRAPHIC_PATH + "2sunflower.png"));
            int tsFW = tsSheet.getWidth() / 10;
            int tsFH = tsSheet.getHeight();
            int tsDrawH = 74; // Tăng 20% so với Sunflower
            int tsDrawW = (int) (tsFW * ((float) tsDrawH / tsFH));
            previewSheets.put(Plant.Type.TWINSUNFLOWER, tsSheet);
            previewDims.put(Plant.Type.TWINSUNFLOWER, new int[] { tsFW, tsFH, tsDrawW, tsDrawH, 10 });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            BufferedImage tnSheet = ImageIO.read(new File(GRAPHIC_PATH + "tallnut1.png"));
            int tnFW = tnSheet.getWidth() / 9;
            int tnFH = tnSheet.getHeight();
            int tnDrawH = 93;
            int tnDrawW = (int) (tnFW * ((float) tnDrawH / tnFH));
            previewSheets.put(Plant.Type.TALLNUT, tnSheet);
            previewDims.put(Plant.Type.TALLNUT, new int[] { tnFW, tnFH, tnDrawW, tnDrawH, 9 });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            BufferedImage spSheet = ImageIO.read(new File(GRAPHIC_PATH + "Snowpea.png"));
            int spFW = spSheet.getWidth() / 8;
            int spFH = spSheet.getHeight();
            int spDrawH = 62;
            int spDrawW = (int) (spFW * ((float) spDrawH / spFH));
            previewSheets.put(Plant.Type.SNOWPEA, spSheet);
            previewDims.put(Plant.Type.SNOWPEA, new int[] { spFW, spFH, spDrawW, spDrawH, 8 });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            BufferedImage twSheet = ImageIO.read(new File(GRAPHIC_PATH + "Torchwoods.png"));
            int twFW = twSheet.getWidth() / 8;
            int twFH = twSheet.getHeight();
            int twDrawH = 62;
            int twDrawW = (int) (twFW * ((float) twDrawH / twFH));
            previewSheets.put(Plant.Type.TORCHWOOD, twSheet);
            previewDims.put(Plant.Type.TORCHWOOD, new int[] { twFW, twFH, twDrawW, twDrawH, 8 });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            BufferedImage gtSheet = ImageIO.read(new File(GRAPHIC_PATH + "Gatling.png"));
            int gtFW = gtSheet.getWidth() / 8; // 1370/8 = 171
            int gtFH = gtSheet.getHeight(); // 182
            int gtDrawH = gtFH / 3;
            int gtDrawW = gtFW / 3;
            previewSheets.put(Plant.Type.GATLING, gtSheet);
            previewDims.put(Plant.Type.GATLING, new int[] { gtFW, gtFH, gtDrawW, gtDrawH, 8 });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            BufferedImage wmSheet = ImageIO.read(new File(GRAPHIC_PATH + "Wintermelon.png"));
            int wmFW = wmSheet.getWidth() / 6;
            int wmFH = wmSheet.getHeight();
            int wmDrawH = 76;
            int wmDrawW = (int) (wmFW * ((float) wmDrawH / wmFH));
            previewSheets.put(Plant.Type.WINTERMELON, wmSheet);
            previewDims.put(Plant.Type.WINTERMELON, new int[] { wmFW, wmFH, wmDrawW, wmDrawH, 6 });
        } catch (Exception e) {
            e.printStackTrace();
        }

        buildCards();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleClick(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggingSlider = -1;
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMove(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                handleMove(e);
                if (settingsOpen && draggingSlider >= 0) {
                    handleSliderDrag(e.getX());
                }
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    cancelPlant();
            }
        });

        // Bắt đầu ở Main Menu
        menuFadeInStart = System.currentTimeMillis();
        playIntroMusic();

        // Khởi tạo 5 máy cắt cỏ
        initLawnMowers();

        // === OBSERVER PATTERN: Đăng ký GamePanel làm listener ===
        GameEventManager.getInstance().addListener(this);

        timer = new Timer(50, this);
        timer.start();
    }

    // === OBSERVER PATTERN: Các callback khi nhận sự kiện ===

    @Override
    public void onZombieDeath(NormalZombie zombie) {
        // Có thể mở rộng: thêm hiệu ứng, cộng điểm, drop item...
    }

    @Override
    public void onPlantDestroyed(Plant plant, int row, int col) {
        // Có thể mở rộng: play sound, hiệu ứng phá hủy...
    }

    @Override
    public void onWaveCleared(int waveNumber) {
        // Có thể mở rộng: hiển thị thông báo wave cleared, bonus sun...
    }

    @Override
    public void onWaveStarted(int waveNumber, int zombieCount) {
        // Có thể mở rộng: hiển thị "Wave X", cảnh báo đặc biệt cho huge wave...
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // === MAIN MENU SCREEN ===
        if (gamePhase == GamePhase.MAIN_MENU) {
            drawMainMenu(g2);
            return;
        }

        // === MORE SCREEN ===
        if (gamePhase == GamePhase.MORE_SCREEN) {
            drawMoreScreen(g2);
            return;
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Nền
        g.drawImage(background, 0, 0, getWidth(), getHeight(), null);

        // 2. Máy cắt cỏ (vẽ dưới cây)
        for (LawnMower mower : lawnMowers)
            mower.draw(g);

        // 2.5. Cây đã trồng trên lưới
        drawPlants(g2);

        // 2.7. Hiệu ứng đất
        for (SoilEffect fx : soilEffects)
            fx.draw(g2);

        // 3. Zombie + đạn
        Collections.sort(zombiesList);
        for (NormalZombie zombie : zombiesList)
            zombie.draw(g);
        for (FallingCone fc : fallingCones)
            fc.draw(g);
        for (FallingBucket fb : fallingBuckets)
            fb.draw(g);
        for (Projectile pr : projectileList)
            pr.draw(g);

        // 3.5. Mặt trời
        for (Sun sun : sunList)
            sun.draw(g2);

        // 4. Thanh chọn cây phía trên
        drawTopBar(g2);
        drawCards(g2);
        drawShovelBox(g2);

        // 4.5. Nút Menu (góc trên phải)
        if (gamePhase == GamePhase.PLAYING || settingsOpen) {
            drawMenuButton(g2);
        }

        // 5. Preview cây theo con trỏ khi đang giữ
        drawCursor(g2);

        // 6. Intro text
        if (gamePhase == GamePhase.INTRO) {
            drawIntroText(g2);
        }

        // 7. Game Over overlay
        if (gamePhase == GamePhase.GAME_OVER) {
            drawGameOver(g2);
        }

        // 8. Settings overlay (vẽ trên cùng)
        if (settingsOpen) {
            drawSettingsOverlay(g2);
        }

        // 9. Settings restart fade to black
        if (settingsRestartFading) {
            long elapsed = System.currentTimeMillis() - settingsRestartFadeStart;
            float fadeAlpha = Math.min(1.0f, (float) elapsed / 300f);
            g2.setColor(new Color(0, 0, 0, (int) (fadeAlpha * 255)));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // === Main Menu phase ===
        if (gamePhase == GamePhase.MAIN_MENU) {
            // Fade-in menu
            if (menuFadeInStart > 0) {
                long elapsed = System.currentTimeMillis() - menuFadeInStart;
                menuFadeAlpha = Math.min(1.0f, elapsed / 1000f);
            }

            if (menuGlowBtn >= 0 && menuGlowTime > 0) {
                if (System.currentTimeMillis() - menuGlowTime >= 150) {
                    int btn = menuGlowBtn;
                    menuGlowBtn = -1;
                    menuGlowTime = -1;

                    menuFadingOut = true;
                    menuFadeOutStart = System.currentTimeMillis();
                    menuFadeTarget = btn;
                }
            }

            if (menuFadingOut) {
                long elapsed = System.currentTimeMillis() - menuFadeOutStart;
                if (elapsed >= 500) {
                    menuFadingOut = false;
                    if (menuFadeTarget == 0) {
                        startGameFromMenu();
                    } else if (menuFadeTarget == 1) {
                        gamePhase = GamePhase.MORE_SCREEN;
                        moreScreenAlpha = 0f;
                        moreScreenFadeInStart = System.currentTimeMillis();
                    }
                }
            }
            repaint();
            return;
        }

        // === More Screen phase ===
        if (gamePhase == GamePhase.MORE_SCREEN) {
            // Fade-in
            if (moreScreenFadeInStart > 0) {
                long elapsed = System.currentTimeMillis() - moreScreenFadeInStart;
                moreScreenAlpha = Math.min(1.0f, elapsed / 500f); // 500ms fade in
            }
            // Fade-out (back to menu)
            if (moreFadingOut) {
                long elapsed = System.currentTimeMillis() - moreFadeOutStart;
                if (elapsed >= 500) {
                    moreFadingOut = false;
                    gamePhase = GamePhase.MAIN_MENU;
                    menuFadeAlpha = 1.0f;
                    menuFadeInStart = -1; // already fully visible
                    moreScreenAlpha = 0f;
                    moreScreenFadeInStart = -1;
                }
            }
            repaint();
            return;
        }

        // Intro phase: cập nhật card, mower entering, và repaint
        if (gamePhase == GamePhase.INTRO) {
            long elapsed = System.currentTimeMillis() - introStartTime;

            if (elapsed >= INITIAL_DELAY) {
                if (!introSoundPlayed) {
                    playSound("readySetPlant.wav");
                    introSoundPlayed = true;
                }

                // Cập nhật lawn mowers (entering animation)
                for (LawnMower mower : lawnMowers)
                    mower.update();

                long totalIntro = INITIAL_DELAY + READY_DURATION + SET_DURATION + PLANT_DURATION;
                if (elapsed >= totalIntro) {
                    gamePhase = GamePhase.PLAYING;
                    lastSunSpawn = System.currentTimeMillis();
                    lastNano = System.nanoTime();

                    for (PlantCard c : cards) {
                        c.setFrozen(false);
                    }

                    playBGM("04. Grasswalk.wav");
                    bgmStartTime = System.currentTimeMillis();
                }
            }
            repaint();
            return;
        }

        // Game Over phase: chỉ repaint
        if (gamePhase == GamePhase.GAME_OVER) {
            repaint();
            return;
        }

        // Settings open: game frozen, chỉ xử lý glow/fade
        if (settingsOpen) {
            handleSettingsTimers();
            repaint();
            return;
        }

        long now = System.nanoTime();
        float dt = (now - lastNano) / 1_000_000_000f;
        lastNano = now;
        dt = Math.min(dt, 0.05f);

        // Kiểm tra spawn zombie
        if (System.currentTimeMillis() - bgmStartTime >= 24000) {
            boolean shouldSpawn = false;
            if (!zombiesSpawned) {
                zombiesSpawned = true;
                shouldSpawn = true;
            } else if (System.currentTimeMillis() - lastZombieWaveTime >= 10000) {
                boolean allDead = true;
                for (NormalZombie z : zombiesList) {
                    if (z.health > 0) {
                        allDead = false;
                        break;
                    }
                }
                if (allDead) {
                    // === OBSERVER PATTERN: Thông báo wave đã được dọn sạch ===
                    GameEventManager.getInstance().fireWaveCleared(currentWave - 1);
                    shouldSpawn = true;
                }
            }

            if (shouldSpawn) {
                lastZombieWaveTime = System.currentTimeMillis();
                Random rand = new Random();
                
                List<WaveManager.ZombieType> typesToSpawn = waveManager.generateWave(currentWave);
                
                for (WaveManager.ZombieType type : typesToSpawn) {
                    int row = rand.nextInt(0, 5);
                    int xPos = 800 + rand.nextInt(0, 120);
                    
                    // === FACTORY PATTERN: Tạo zombie qua Factory thay vì switch-case ===
                    NormalZombie z = ZombieFactory.create(type, GRAPHIC_PATH, xPos, row * CELL_H);
                    if (z != null) {
                        float multiplier = waveManager.getHealthMultiplier();
                        if (multiplier > 1.0f) {
                            z.multiplyHealth(multiplier);
                        }
                        zombiesList.add(z);
                    }
                }
                
                // === OBSERVER PATTERN: Thông báo wave mới bắt đầu ===
                GameEventManager.getInstance().fireWaveStarted(currentWave, typesToSpawn.size());
                
                currentWave++; // Tăng số wave sau khi spawn
            }
        }

        // === Zombie announcement sounds ===
        long bgmElapsed = System.currentTimeMillis() - bgmStartTime;

        // Giây 19: play "theZombiesAreComing.wav"
        if (!zombieAnnouncePlayed && bgmElapsed >= 19000) {
            zombieAnnouncePlayed = true;
            Clip announceClip = playSoundAndGetClip("theZombiesAreComing.wav");
            if (announceClip != null) {
                // Tính thời điểm sound kết thúc
                long durationMs = announceClip.getMicrosecondLength() / 1000;
                zombieAnnounceEndTime = System.currentTimeMillis() + durationMs;
            } else {
                // Nếu không load được, fallback 2s
                zombieAnnounceEndTime = System.currentTimeMillis() + 2000;
            }
        }

        // 300ms sau khi "theZombiesAreComing" kết thúc: play groan đầu tiên
        if (zombieAnnounceEndTime > 0 && !firstGroanPlayed
                && System.currentTimeMillis() >= zombieAnnounceEndTime + 300) {
            firstGroanPlayed = true;
            playSound("Voices-groan.wav");
            // Bắt đầu lịch groan ngẫu nhiên
            nextGroanTime = System.currentTimeMillis() + 8000 + new Random().nextInt(7001);
        }

        // Groan ngẫu nhiên mỗi 8-15s
        if (firstGroanPlayed && nextGroanTime > 0 && System.currentTimeMillis() >= nextGroanTime) {
            String randomGroan = groanFiles[new Random().nextInt(groanFiles.length)];
            playSound(randomGroan);
            nextGroanTime = System.currentTimeMillis() + 8000 + new Random().nextInt(7001);
        }

        // Cập nhật card
        for (PlantCard c : cards)
            c.update(dt);

        // Cập nhật cây trên lưới + bắn đạn
        for (int r = 0; r < ROWS; r++) {
            for (int c2 = 0; c2 < COLS; c2++) {
                if (grid[r][c2] == null)
                    continue;

                if (grid[r][c2] instanceof Peashooter ps) {
                    boolean hasZombie = false;
                    for (NormalZombie z : zombiesList) {
                        // cùng hàng (zombie.y / CELL_H == r) VÀ đã vào lưới VÀ chưa đi qua peashooter
                        if (z.y / CELL_H == r && z.x <= GRID_LEFT + COLS * CELL_W && z.x >= ps.getX()) {
                            hasZombie = true;
                            break;
                        }
                    }
                    ps.setActive(hasZombie);
                }
                if (grid[r][c2] instanceof Snowpea sp) {
                    boolean hasZombie = false;
                    for (NormalZombie z : zombiesList) {
                        if (z.y / CELL_H == r && z.x <= GRID_LEFT + COLS * CELL_W && z.x >= sp.getX()) {
                            hasZombie = true;
                            break;
                        }
                    }
                    sp.setActive(hasZombie);
                }
                if (grid[r][c2] instanceof Gatling gt) {
                    boolean hasZombie = false;
                    for (NormalZombie z : zombiesList) {
                        if (z.y / CELL_H == r && z.x <= GRID_LEFT + COLS * CELL_W && z.x >= gt.getX()) {
                            hasZombie = true;
                            break;
                        }
                    }
                    gt.setActive(hasZombie);
                }
                if (grid[r][c2] instanceof Wintermelon wm) {
                    boolean hasZombie = false;
                    for (NormalZombie z : zombiesList) {
                        if (z.y / CELL_H == r && z.x <= GRID_LEFT + COLS * CELL_W && z.x >= wm.getX()) {
                            hasZombie = true;
                            break;
                        }
                    }
                    wm.setActive(hasZombie);
                }

                grid[r][c2].update();
                if (grid[r][c2] instanceof Peashooter ps && ps.canShoot()) {
                    projectileList.add(new Projectile(GRAPHIC_PATH + "bullet.png", ps.getX() + 50, ps.getY() + 5));
                }
                if (grid[r][c2] instanceof Snowpea sp && sp.canShoot()) {
                    projectileList.add(new SnowProjectile(GRAPHIC_PATH + "Snowpea_projectile.png", sp.getX() + 50,
                            sp.getY() - 20));
                }
                if (grid[r][c2] instanceof Gatling gt && gt.consumeShot()) {
                    projectileList.add(new Projectile(GRAPHIC_PATH + "bullet.png", gt.getX() + 50, gt.getY() + 5));
                }
                if (grid[r][c2] instanceof Wintermelon wm && wm.canShoot()) {
                    // Tìm zombie gần nhất cùng hàng (chỉ trong sân)
                    NormalZombie target = null;
                    for (NormalZombie z : zombiesList) {
                        if (z.y / CELL_H == r && z.health > 0 && z.x >= wm.getX() 
                                && z.x <= GRID_LEFT + COLS * CELL_W - 10) {
                            if (target == null || z.x < target.x)
                                target = z;
                        }
                    }
                    if (target != null && wm.consumeShot()) {
                        int tx = target.x + target.getBounds().width / 2;
                        int ty = target.y + target.getBounds().height / 2;
                        projectileList.add(new WintermelonProjectile(
                                GRAPHIC_PATH + "WIntermelon_projectile.png",
                                wm.getX() - 25, wm.getY() - 7, tx, ty, r));
                    }
                }
                if (grid[r][c2] instanceof Sunflower sf && sf.hasSunReady()) {
                    float sx = sf.getSunSpawnX();
                    float sy = sf.getSunSpawnY();
                    float landY = sf.getSunLandY();
                    sunList.add(new Sun(sx, sy, landY, 25));
                }
                if (grid[r][c2] instanceof TwinSunflower ts && ts.hasSunReady()) {
                    float sy = ts.getSunSpawnY();
                    float landY = ts.getSunLandY();
                    sunList.add(new Sun(ts.getSun1SpawnX(), sy, landY, 25));
                    sunList.add(new Sun(ts.getSun2SpawnX(), sy, landY, 25));
                }
                if (grid[r][c2] instanceof CherryBomb cb) {
                    if (cb.consumeExplosion()) {
                        playSound("explosion.wav");
                        // Gây 900 damage cho tất cả zombie trong phạm vi 3x3
                        int minRow = Math.max(0, r - 1);
                        int maxRow = Math.min(ROWS - 1, r + 1);
                        int minCol = Math.max(0, c2 - 1);
                        int maxCol = Math.min(COLS - 1, c2 + 1);
                        int xMin = GRID_LEFT + minCol * CELL_W;
                        int xMax = GRID_LEFT + (maxCol + 1) * CELL_W;
                        Rectangle blastArea = new Rectangle(xMin, GRID_TOP + minRow * CELL_H,
                                xMax - xMin, (maxRow - minRow + 1) * CELL_H);
                        for (NormalZombie z : zombiesList) {
                            int zRow = z.y / CELL_H;
                            if (z.health > 0 && zRow >= minRow && zRow <= maxRow
                                    && z.getBounds().intersects(blastArea)) {
                                z.takeDamage(900);
                                if (z.health <= 0) {
                                    z.dieFromExplosion();
                                } else {
                                    z.checkHalfTransition();
                                }
                            }
                        }
                    }
                    if (cb.isFullyDone()) {
                        for (NormalZombie z : zombiesList) {
                            if (z.isEating() && z.getTargetPlant() == cb) {
                                z.stopEating();
                            }
                        }
                        grid[r][c2] = null;
                    }
                }
            }
        }

        // Cập nhật zombie & đạn
        for (NormalZombie zombie : zombiesList) {
            zombie.update();
            if (zombie instanceof Conehead c) {
                if (!c.hasConeFell() && zombie.health < 100) {
                    c.markConeFell();
                    fallingCones.add(new FallingCone(zombie.x, zombie.y, zombie.getDrawW(), zombie.getDrawH()));
                }
            } else if (zombie instanceof Buckethead b) {
                if (!b.hasBucketFell() && zombie.health < 100) {
                    b.markBucketFell();
                    fallingBuckets.add(new FallingBucket(zombie.x, zombie.y, zombie.getDrawW(), zombie.getDrawH()));
                }
            } else if (zombie instanceof JackBox jb) {
                if (jb.checkExplodeDamage()) {
                    int centerCol = (jb.getX() + 50 - GRID_LEFT) / CELL_W;
                    if (centerCol < 0) centerCol = 0;
                    if (centerCol >= COLS) centerCol = COLS - 1;
                    int centerRow = jb.getY() / CELL_H;
                    for (int dr = -1; dr <= 1; dr++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            int r = centerRow + dr;
                            int c = centerCol + dc;
                            if (r >= 0 && r < ROWS && c >= 0 && c < COLS) {
                                if (grid[r][c] != null) {
                                    grid[r][c].takeDamage(9999);
                                }
                            }
                        }
                    }
                }
            }
            if (zombie.consumeBite()) {
                playSound("SFX chomp.wav");
            }
        }
        fallingCones.removeIf(FallingCone::isFinished);
        for (FallingCone fc : fallingCones) {
            fc.update();
        }
        fallingBuckets.removeIf(FallingBucket::isFinished);
        for (FallingBucket fb : fallingBuckets) {
            fb.update();
        }
        for (int i = 0; i < projectileList.size(); i++) {
            Projectile pr = projectileList.get(i);
            pr.update();
            if (!pr.isBreaking() && !pr.isFinished()) {
                int prRow = pr.y / CELL_H;
                if (prRow >= 0 && prRow < ROWS) {
                    for (int c = 0; c < COLS; c++) {
                        Plant p = grid[prRow][c];
                        if (p instanceof Torchwood && p != pr.getLastTorchwood() && !(pr instanceof WintermelonProjectile)) {
                            int prCenter = pr.x + pr.getBounds().width / 2;
                            if (prCenter >= p.getBounds().x && prCenter <= p.getBounds().x + p.getBounds().width) {
                                if (pr instanceof SnowProjectile) {
                                    Projectile newPr = new Projectile(GRAPHIC_PATH + "bullet.png", pr.x, pr.y + 22);
                                    newPr.realX = pr.realX;
                                    newPr.setLastTorchwood(p);
                                    projectileList.set(i, newPr);
                                    pr = newPr;
                                } else if (!(pr instanceof FireProjectile)) {
                                    FireProjectile newPr = new FireProjectile(GRAPHIC_PATH + "fire_projectile.png",
                                            pr.x, pr.y);
                                    newPr.realX = pr.realX;
                                    newPr.setLastTorchwood(p);
                                    projectileList.set(i, newPr);
                                    pr = newPr;
                                }
                            }
                        }
                    }
                }
            }
        }

        for (LawnMower mower : lawnMowers)
            mower.update();

        // Kiểm tra va chạm zombie → lawn mower (kích hoạt mower)
        checkZombieLawnMowerContact();

        // Kiểm tra va chạm lawn mower (đã kích hoạt) → zombie (giết zombie)
        checkLawnMowerKill();

        // === Kiểm tra thua cuộc: zombie chạm x=15 ===
        // Chỉ thua nếu hàng đó không còn mower idle (đã dùng mất hoặc đang chạy)
        for (NormalZombie zombie : zombiesList) {
            if (zombie.x <= 15) {
                int lane = zombie.getY() / CELL_H;
                boolean hasMower = false;
                for (LawnMower mower : lawnMowers) {
                    if (mower.getLane() == lane && (mower.isIdle() || mower.isActivated())) {
                        hasMower = true;
                        break;
                    }
                }
                
                if (!hasMower) {
                    gamePhase = GamePhase.GAME_OVER;
                    gameOverStartTime = System.currentTimeMillis();
                    // Dừng nhạc nền
                    if (bgmClip != null && bgmClip.isRunning()) {
                        bgmClip.stop();
                    }
                    playSound("losemusic.wav");
                    playSound("daveScream.wav");
                    repaint();
                    return;
                }
            }
        }

        checkZombiePlantContact();
        checkCollisions();

        // Cập nhật hiệu ứng đất
        for (SoilEffect fx : soilEffects)
            fx.update();
        soilEffects.removeIf(SoilEffect::isFinished);

        // Spawn mặt trời
        long nowMs = System.currentTimeMillis();
        if (nowMs - lastSunSpawn >= SUN_SPAWN_INTERVAL) {
            lastSunSpawn = nowMs;
            Random rand = new Random();
            int lane = rand.nextInt(ROWS);
            float sx = GRID_LEFT + rand.nextInt(COLS * CELL_W - Sun.DRAW_W);
            float landY = GRID_TOP + lane * CELL_H + (CELL_H - Sun.DRAW_H) / 2f;
            sunList.add(new Sun(sx, landY));
        }

        // Cập nhật mặt trời & thu thập
        for (Sun sun : sunList)
            sun.update();
        for (int i = sunList.size() - 1; i >= 0; i--) {
            if (sunList.get(i).isCollected()) {
                sunCount += sunList.get(i).getValue();
                sunList.remove(i);
            }
        }

        // Cập nhật animation preview cây
        if (heldType != null) {
            int[] dims = previewDims.get(heldType);
            if (dims != null && System.currentTimeMillis() - lastPreviewTime > 200) {
                previewFrame = (previewFrame + 1) % dims[4];
                lastPreviewTime = System.currentTimeMillis();
            }
        }

        repaint(); // chỉ gọi 1 lần / tick
    }

    private void buildCards() {
        int cardW = 36, cardH = 42, gap = 8;
        int sunSectionW = 55;
        int startX = sunSectionW + 8;
        int startY = (TOP_BAR_H - cardH) / 2;
        // Peashooter card: cost 100, cooldown 7500ms
        cards.add(new PlantCard(Plant.Type.PEASHOOTER, GRAPHIC_PATH + "PeaShooterCard.png",
                startX + 0 * (cardW + gap), startY, cardW, cardH, 100, 7500));
        // Sunflower card: cost 50, cooldown 7500ms
        cards.add(new PlantCard(Plant.Type.SUNFLOWER, GRAPHIC_PATH + "sunflowerCard.png",
                startX + 1 * (cardW + gap), startY, cardW, cardH, 50, 7500));
        // Wallnut card: cost 50, cooldown 30000ms
        cards.add(new PlantCard(Plant.Type.WALLNUT, GRAPHIC_PATH + "Card_wallNut.png",
                startX + 2 * (cardW + gap), startY, cardW, cardH, 50, 30000));
        // CherryBomb card: cost 150, cooldown 50000ms
        cards.add(new PlantCard(Plant.Type.CHERRYBOMB, GRAPHIC_PATH + "Card_cherryBomb.png",
                startX + 3 * (cardW + gap), startY, cardW, cardH, 150, 50000));
        // TwinSunflower card: cost 150, cooldown 50000ms
        cards.add(new PlantCard(Plant.Type.TWINSUNFLOWER, GRAPHIC_PATH + "Card_2sunflower.png",
                startX + 4 * (cardW + gap), startY, cardW, cardH, 300, 50000));
        // Tallnut card: cost 175, cooldown 30000ms
        cards.add(new PlantCard(Plant.Type.TALLNUT, GRAPHIC_PATH + "Card_tallNut.png",
                startX + 5 * (cardW + gap), startY, cardW, cardH, 500, 30000));
        // Snowpea card: cost 200, cooldown 7500ms
        cards.add(new PlantCard(Plant.Type.SNOWPEA, GRAPHIC_PATH + "Card_snowPea.png",
                startX + 6 * (cardW + gap), startY, cardW, cardH, 200, 7500));
        // Torchwood card: cost 200, cooldown 7500ms
        cards.add(new PlantCard(Plant.Type.TORCHWOOD, GRAPHIC_PATH + "Card_Torchwood.png",
                startX + 7 * (cardW + gap), startY, cardW, cardH, 200, 7500));
        // Gatling card: cost 300, cooldown 50000ms
        cards.add(new PlantCard(Plant.Type.GATLING, GRAPHIC_PATH + "Card_gatling.png",
                startX + 8 * (cardW + gap), startY, cardW, cardH, 500, 50000));
        // Wintermelon card: cost 500, cooldown 50000ms
        cards.add(new PlantCard(Plant.Type.WINTERMELON, GRAPHIC_PATH + "Card_winterMelon.png",
                startX + 9 * (cardW + gap), startY, cardW, cardH, 500, 50000));
    }

    private void drawPlants(Graphics2D g) {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] != null) {
                    // Ẩn sunflower khi đang rê TwinSunflower lên ô đó
                    if (heldType == Plant.Type.TWINSUNFLOWER
                            && grid[r][c] instanceof Sunflower
                            && r == hoverRow && c == hoverCol) {
                        continue; // không vẽ sunflower — ghost TwinSunflower sẽ thay thế
                    }
                    // Ẩn wallnut khi đang rê Tallnut lên ô đó
                    if (heldType == Plant.Type.TALLNUT
                            && grid[r][c] instanceof Wallnut
                            && r == hoverRow && c == hoverCol) {
                        continue; // không vẽ wallnut — ghost Tallnut sẽ thay thế
                    }
                    // Ẩn peashooter khi đang rê Gatling lên ô đó
                    if (heldType == Plant.Type.GATLING
                            && grid[r][c] instanceof Peashooter
                            && r == hoverRow && c == hoverCol) {
                        continue; // không vẽ peashooter — ghost Gatling sẽ thay thế
                    }
                    grid[r][c].draw(g);
                }
            }
        }
    }

    /* Thanh trên cùng — kiểu PVZ */
    private void drawTopBar(Graphics2D g) {
        int w = getWidth() / 2 + 200;
        int pad = 3; // độ dày viền sáng bên ngoài
        int sunSecW = 55;

        g.setColor(new Color(195, 100, 35));
        g.fillRoundRect(0, 0, w, TOP_BAR_H, 8, 8);
        g.setColor(new Color(30, 15, 0));
        g.setStroke(new BasicStroke(2.5f));
        g.drawRoundRect(0, 0, w, TOP_BAR_H, 8, 8);
        g.setStroke(new BasicStroke(1));

        GradientPaint barFill = new GradientPaint(
                0, pad, new Color(110, 60, 20),
                0, TOP_BAR_H - pad, new Color(85, 45, 15));
        g.setPaint(barFill);
        g.fillRoundRect(pad, pad, w - 2 * pad, TOP_BAR_H - 2 * pad, 6, 6);
        g.setColor(new Color(60, 30, 10));
        g.drawRoundRect(pad, pad, w - 2 * pad, TOP_BAR_H - 2 * pad, 6, 6);

        g.setColor(new Color(140, 100, 55));
        g.setStroke(new BasicStroke(2));
        g.drawLine(sunSecW, pad, sunSecW, TOP_BAR_H - pad);
        g.setStroke(new BasicStroke(1));

        if (sunCountImg != null) {
            int imgSize = 28;
            int imgX = (sunSecW - imgSize) / 2;
            int imgY = 2;
            g.drawImage(sunCountImg, imgX, imgY, imgX + imgSize, imgY + imgSize,
                    0, 0, sunCountImg.getWidth(), sunCountImg.getHeight(), null);
        }

        int labelW = 42, labelH = 16;
        int labelX = (sunSecW - labelW) / 2;
        int labelY = TOP_BAR_H - labelH - 4;
        g.setColor(new Color(245, 235, 210));
        g.fillRoundRect(labelX, labelY, labelW, labelH, 6, 6);
        g.setColor(new Color(130, 100, 50));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(labelX, labelY, labelW, labelH, 6, 6);
        g.setStroke(new BasicStroke(1));

        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.setColor(new Color(40, 30, 10));
        String sunText = String.valueOf(sunCount);
        FontMetrics fm = g.getFontMetrics();
        int textX = labelX + (labelW - fm.stringWidth(sunText)) / 2;
        int textY = labelY + (labelH + fm.getAscent() - fm.getDescent()) / 2;
        g.drawString(sunText, textX, textY);
    }

    /* Vẽ ô Shovel bên phải Top Bar */
    private void drawShovelBox(Graphics2D g) {
        int topBarW = getWidth() / 2 + 200;
        int boxSize = TOP_BAR_H;
        int gap = 5;
        int x = topBarW + gap;
        int y = 0;
        shovelBoxBounds = new Rectangle(x, y, boxSize, boxSize);

        int pad = 8;

        g.setColor(new Color(195, 100, 35));
        g.fillRoundRect(x, y, boxSize, boxSize, 8, 8);
        g.setColor(new Color(30, 15, 0));
        g.setStroke(new BasicStroke(2.5f));
        g.drawRoundRect(x, y, boxSize, boxSize, 8, 8);
        g.setStroke(new BasicStroke(1));

        GradientPaint barFill = new GradientPaint(
                x, pad, new Color(110, 60, 20),
                x, boxSize - pad, new Color(85, 45, 15));
        g.setPaint(barFill);
        g.fillRoundRect(x + pad, y + pad, boxSize - 2 * pad, boxSize - 2 * pad, 6, 6);
        g.setColor(new Color(60, 30, 10));
        g.drawRoundRect(x + pad, y + pad, boxSize - 2 * pad, boxSize - 2 * pad, 6, 6);

        if (shovelImg != null) {
            int maxDrawSize = (boxSize - 2 * pad - 12) * 3;
            int imgW = shovelImg.getWidth();
            int imgH = shovelImg.getHeight();
            float scale = Math.min((float) maxDrawSize / imgW, (float) maxDrawSize / imgH);
            int drawW = (int) (imgW * scale);
            int drawH = (int) (imgH * scale);
            int dx = x + (boxSize - drawW) / 2;
            int dy = y + (boxSize - drawH) / 2;

            Graphics2D g2d = (Graphics2D) g.create();
            if (isHoldingShovel) {
                long elapsed = System.currentTimeMillis() - shovelPulseStart;
                float cycle = (elapsed % 700) / 700f;
                float pulse = (float) (Math.sin(cycle * Math.PI * 2) * 0.5 + 0.5);
                float alpha = 0.4f + 0.6f * pulse;
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            }

            g2d.rotate(Math.toRadians(-45), x + boxSize / 2.0, y + boxSize / 2.0);
            g2d.drawImage(shovelImg, dx, dy, drawW, drawH, null);
            g2d.dispose();
        }
    }

    /* Cards */
    private void drawCards(Graphics2D g) {
        for (PlantCard card : cards)
            card.draw(g);
    }

    /* Con trỏ cây khi đang giữ */
    private void drawCursor(Graphics2D g) {
        if (isHoldingShovel) {
            if (hoverCol >= 0 && hoverRow >= 0 && isValidCell(hoverCol, hoverRow)) {
                Composite oldComp = g.getComposite();

                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
                g.setColor(Color.WHITE);
                int rowY = GRID_TOP + hoverRow * CELL_H;
                g.fillRect(GRID_LEFT, rowY, COLS * CELL_W, CELL_H);
                int colX = GRID_LEFT + hoverCol * CELL_W;
                g.fillRect(colX, GRID_TOP, CELL_W, ROWS * CELL_H);

                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
                g.fillRect(colX, rowY, CELL_W, CELL_H);

                g.setComposite(oldComp);
            }

            if (shovelImg != null) {
                int drawW = 81;
                int drawH = (int) (81.0 * shovelImg.getHeight() / shovelImg.getWidth());
                int curX = mouseX - drawW / 2;
                int curY = mouseY - drawH / 2;
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.rotate(Math.toRadians(-45), mouseX, mouseY);
                g2d.drawImage(shovelImg, curX, curY, drawW, drawH, null);
                g2d.dispose();
            }
            return;
        }

        if (heldType == null)
            return;
        BufferedImage sheet = previewSheets.get(heldType);
        int[] dims = previewDims.get(heldType);
        if (sheet == null || dims == null)
            return;

        int pFrameW = dims[0], pFrameH = dims[1], pDrawW = dims[2], pDrawH = dims[3];
        int srcX = previewFrame * pFrameW;

        if (hoverCol >= 0 && hoverRow >= 0 && isValidCell(hoverCol, hoverRow)) {
            boolean canShowGhost = false;
            if (heldType == Plant.Type.TWINSUNFLOWER) {
                canShowGhost = grid[hoverRow][hoverCol] instanceof Sunflower;
            } else if (heldType == Plant.Type.TALLNUT) {
                canShowGhost = grid[hoverRow][hoverCol] instanceof Wallnut;
            } else if (heldType == Plant.Type.GATLING) {
                canShowGhost = grid[hoverRow][hoverCol] instanceof Peashooter;
            } else {
                canShowGhost = grid[hoverRow][hoverCol] == null;
            }
            if (canShowGhost) {
                int cellX = GRID_LEFT + hoverCol * CELL_W;
                int cellY = GRID_TOP + hoverRow * CELL_H;
                if (heldType == Plant.Type.TWINSUNFLOWER)
                    cellY -= 10;
                if (heldType == Plant.Type.TALLNUT)
                    cellY -= (93 - 62 - 2); // căn chân tallnut (thấp xuống 2px)
                if (heldType == Plant.Type.WINTERMELON) {
                    cellY -= 2;
                    cellX -= 25;
                }
                Composite oldComp = g.getComposite();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.30f));
                g.drawImage(sheet,
                        cellX, cellY, cellX + pDrawW, cellY + pDrawH,
                        srcX, 0, srcX + pFrameW, pFrameH, null);
                g.setComposite(oldComp);
            }
        }

        int curX = mouseX - pDrawW / 2;
        int curY = mouseY - pDrawH / 2;
        if (heldType == Plant.Type.WINTERMELON) {
            curY -= 7;
            curX -= 20;
        }
        g.drawImage(sheet,
                curX, curY, curX + pDrawW, curY + pDrawH,
                srcX, 0, srcX + pFrameW, pFrameH, null);
    }

    private void handleMove(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();

        // hover cards
        for (PlantCard card : cards)
            card.setHovered(card.contains(mouseX, mouseY));

        // hover ô lưới
        hoverCol = (mouseX - GRID_LEFT) / CELL_W;
        hoverRow = (mouseY - GRID_TOP) / CELL_H;
        if (!isValidCell(hoverCol, hoverRow)) {
            hoverCol = -1;
            hoverRow = -1;
        }

        repaint(); // cập nhật cursor preview theo chuột
    }

    private void selectCard(Plant.Type type) {
        heldType = type;
        for (PlantCard c : cards)
            c.setSelected(c.getType() == type);
    }

    private void cancelPlant() {
        heldType = null;
        isHoldingShovel = false;
        for (PlantCard c : cards)
            c.setSelected(false);
    }

    private void placePlant(int col, int row) {
        int px = GRID_LEFT + col * CELL_W;
        int py = GRID_TOP + row * CELL_H;
        Plant plant = switch (heldType) {
            case PEASHOOTER -> new Peashooter(GRAPHIC_PATH + "peashooter.png", px, py);
            case SUNFLOWER -> new Sunflower(GRAPHIC_PATH + "sunflower.png", px, py);
            case WALLNUT -> new Wallnut(px, py);
            case CHERRYBOMB -> new CherryBomb(px, py);
            case TWINSUNFLOWER -> new TwinSunflower(px, py);
            case TALLNUT -> new Tallnut(px, py);
            case SNOWPEA -> new Snowpea(GRAPHIC_PATH + "Snowpea.png", px, py);
            case TORCHWOOD -> new Torchwood(px, py);
            case GATLING -> new Gatling(px, py);
            case WINTERMELON -> new Wintermelon(px, py);
        };
        grid[row][col] = plant;

        playSound("SFX plant.wav");

        // Trừ mặt trời & bắt đầu cooldown card
        for (PlantCard c : cards) {
            if (c.getType() == heldType) {
                sunCount -= c.getSunCost();
                c.startCooldown();
                break;
            }
        }

        int peaDrawH = (int) ((CELL_H / 2) * 1.5);
        int cx = px + CELL_W / 2;
        int cy = py + peaDrawH;
        soilEffects.add(new SoilEffect(cx, cy, peaDrawH));

        cancelPlant();
    }

    private boolean isValidCell(int col, int row) {
        return col >= 0 && col < COLS && row >= 0 && row < ROWS;
    }

    private void handleClick(MouseEvent e) {
        if (gamePhase == GamePhase.INTRO)
            return;

        int mx = e.getX(), my = e.getY();

        // === Main Menu clicks ===
        if (gamePhase == GamePhase.MAIN_MENU) {
            if (menuFadingOut || menuGlowBtn >= 0)
                return; // animating
            if (menuFadeAlpha < 1.0f)
                return; // still fading in

            // Scale factors: MainMenu.png 621×465 → 800×500
            float scaleX = 800f / 621f;
            float scaleY = 500f / 465f;

            // StartAdventure: hitbox centered at (689, 321), size 160×60
            {
                int bx = 689 - 80; // 609
                int by = 321 - 30; // 291
                int bw = 160;
                int bh = 60;
                if (mx >= bx && mx <= bx + bw && my >= by && my <= by + bh) {
                    menuGlowBtn = 0;
                    menuGlowTime = System.currentTimeMillis();
                    playSound("buttonclick.wav");
                    return;
                }
            }

            // More: rounded rectangle hitbox (shifted +50px right, +300px down)
            {
                float bx = 420f * scaleX + 80;
                float by = 130f * scaleY + 220;
                float bw = 100f * scaleX - 5;
                float bh = 40f * scaleY + 10;
                if (mx >= bx && mx <= bx + bw && my >= by && my <= by + bh) {
                    menuGlowBtn = 1;
                    menuGlowTime = System.currentTimeMillis();
                    playSound("buttonclick.wav");
                    return;
                }
            }
            return;
        }

        // === More Screen clicks ===
        if (gamePhase == GamePhase.MORE_SCREEN) {
            if (moreFadingOut)
                return;
            if (moreScreenAlpha < 1.0f)
                return;
            // Back button bounds (top-left area)
            Rectangle backBounds = getBackBtnBounds();
            if (backBounds.contains(mx, my)) {
                playSound("buttonclick.wav");
                moreFadingOut = true;
                moreFadeOutStart = System.currentTimeMillis();
                return;
            }
            return;
        }

        // === Settings overlay: xử lý click khi settings đang mở ===
        if (settingsOpen) {
            handleSettingsClick(mx, my);
            return;
        }

        // === Menu button click ===
        if (gamePhase == GamePhase.PLAYING) {
            Rectangle menuBounds = getMenuBtnBounds();
            if (menuBounds.contains(mx, my)) {
                menuClickTime = System.currentTimeMillis();
                playSound("buttonclick.wav");
                // Mở settings sau 50ms (chờ animation thu nhỏ xong)
                new javax.swing.Timer(50, ev -> {
                    ((javax.swing.Timer) ev.getSource()).stop();
                    openSettings();
                }).start();
                return;
            }
        }

        // mx, my đã khai báo ở trên

        // === Game Over: handle buttons ===
        if (gamePhase == GamePhase.GAME_OVER) {
            if (retryBounds != null && retryBounds.contains(mx, my)) {
                playSound("buttonclick.wav");
                retryClickTime = System.currentTimeMillis();
                // Delay restart 50ms for click animation
                new javax.swing.Timer(80, ev -> {
                    ((javax.swing.Timer) ev.getSource()).stop();
                    restartGame();
                }).start();
            }
            // EXIT TO MAP
            if (exitBounds != null && exitBounds.contains(mx, my)) {
                playSound("buttonclick.wav");
                goToMainMenu();
            }
            return;
        }

        for (Sun sun : sunList) {
            if (sun.isClickable() && sun.contains(mx, my)) {
                sun.collect(27, 16);
                playSound("sunCollect.wav");
                return;
            }
        }

        if (shovelBoxBounds != null && shovelBoxBounds.contains(mx, my)) {
            if (isHoldingShovel) {
                cancelPlant();
            } else {
                cancelPlant();
                isHoldingShovel = true;
                shovelPulseStart = System.currentTimeMillis();
                playSound("seedLift.wav");
            }
            return;
        }

        if (isHoldingShovel) {
            int col = (mx - GRID_LEFT) / CELL_W;
            int row = (my - GRID_TOP) / CELL_H;
            if (isValidCell(col, row)) {
                Plant p = grid[row][col];
                if (p != null) {
                    grid[row][col] = null;
                    playSound("shovel.wav");

                    for (NormalZombie z : zombiesList) {
                        if (z.isEating() && z.getTargetPlant() == p) {
                            z.stopEating();
                        }
                    }
                }
            }
            isHoldingShovel = false;
            repaint();
            return;
        }

        for (PlantCard card : cards) {
            if (card.contains(mx, my)) {
                if (card.isOnCooldown() || sunCount < card.getSunCost())
                    return;
                if (heldType == card.getType()) {
                    cancelPlant();
                } else {
                    selectCard(card.getType());
                    playSound("seedLift.wav");
                }
                return;
            }
        }

        int col = (mx - GRID_LEFT) / CELL_W;
        int row = (my - GRID_TOP) / CELL_H;
        if (heldType != null && isValidCell(col, row)) {
            if (heldType == Plant.Type.TWINSUNFLOWER) {
                if (grid[row][col] instanceof Sunflower) {
                    placePlant(col, row);
                } else if (grid[row][col] == null) {
                    cancelPlant();
                }
            } else if (heldType == Plant.Type.TALLNUT) {
                if (grid[row][col] instanceof Wallnut) {
                    placePlant(col, row);
                } else if (grid[row][col] == null) {
                    cancelPlant();
                }
            } else if (heldType == Plant.Type.GATLING) {
                if (grid[row][col] instanceof Peashooter) {
                    placePlant(col, row);
                } else if (grid[row][col] == null) {
                    cancelPlant();
                }
            } else if (heldType == Plant.Type.WINTERMELON) {
                if (grid[row][col] == null) {
                    placePlant(col, row);
                }
            } else if (grid[row][col] == null) {
                placePlant(col, row);
            }
        } else if (heldType != null) {
            if (!isValidCell(col, row))
                cancelPlant();
        }
    }

    private void checkZombiePlantContact() {
        for (NormalZombie zombie : zombiesList) {
            if (zombie.isEating()) {
                Plant target = zombie.getTargetPlant();
                boolean stillExists = false;
                if (target != null) {
                    outer: for (int r = 0; r < ROWS; r++) {
                        for (int c = 0; c < COLS; c++) {
                            if (grid[r][c] == target) {
                                stillExists = true;
                                break outer;
                            }
                        }
                    }
                }
                if (!stillExists) {
                    zombie.stopEating();
                }
                continue;
            }

            int zombieLane = zombie.getY() / CELL_H;
            if (zombieLane < 0 || zombieLane >= ROWS)
                continue;

            for (int c = 0; c < COLS; c++) {
                Plant plant = grid[zombieLane][c];
                if (plant == null)
                    continue;

                Rectangle zb = zombie.getEatingBounds();
                Rectangle pb = plant.getBounds();

                if (zb.intersects(pb)) {
                    if (zombie instanceof JackBox jb) {
                        jb.triggerExplosion();
                        playSound("Explosion.wav");
                    } else if (zombie instanceof Pole pole) {
                        if (pole.canJump()) {
                            pole.triggerJump(plant.getX());
                        } else if (!pole.isJumping()) {
                            zombie.startEating(plant);
                        }
                    } else {
                        zombie.startEating(plant);
                    }
                    break;
                }
            }
        }
    }

    private void checkCollisions() {
        projectileList.removeIf(pr -> {
            if (pr.isFinished())
                return true;

            if (pr instanceof WintermelonProjectile wmp) {
                if (wmp.isLanded() && !wmp.isSplashApplied()) {
                    wmp.markSplashApplied();
                    int landRow = wmp.getTargetRow();
                    int landCol = (wmp.getLandX() - GRID_LEFT) / CELL_W;
                    int minRow = Math.max(0, landRow - 1);
                    int maxRow = Math.min(ROWS - 1, landRow + 1);
                    int minCol = Math.max(0, landCol - 1);
                    int maxCol = Math.min(COLS - 1, landCol + 1);
                    int xMin = GRID_LEFT + minCol * CELL_W;
                    int xMax = GRID_LEFT + (maxCol + 1) * CELL_W;
                    Rectangle splashArea = new Rectangle(xMin, GRID_TOP + minRow * CELL_H,
                            xMax - xMin, (maxRow - minRow + 1) * CELL_H);
                    for (NormalZombie z : zombiesList) {
                        int zRow = z.y / CELL_H;
                        if (z.health > 0 && zRow >= minRow && zRow <= maxRow
                                && z.getBounds().intersects(splashArea)) {
                            z.takeDamage(40);
                            z.applySlow();
                            z.checkHalfTransition();
                        }
                    }
                    playSound("melonimpact.wav");
                }
                return false;
            }

            if (pr.isBreaking())
                return false;

            for (NormalZombie z : zombiesList) {
                if (z.health <= 0)
                    continue;

                int prRow = (pr instanceof SnowProjectile) ? (pr.y + 25) / CELL_H : pr.y / CELL_H;
                if (prRow == z.y / CELL_H
                        && pr.getBounds().intersects(z.getBounds())) {
                    if (pr instanceof FireProjectile) {
                        z.takeDamage(20);
                        z.removeSlow();
                        
                        int splashRadius = CELL_W / 2;
                        for (NormalZombie otherZ : zombiesList) {
                            if (otherZ != z && otherZ.health > 0 && otherZ.y / CELL_H == z.y / CELL_H) {
                                if (Math.abs(otherZ.x - z.x) <= splashRadius) {
                                    otherZ.takeDamage(10);
                                    otherZ.removeSlow();
                                    otherZ.checkHalfTransition();
                                }
                            }
                        }
                        playSound("fire.wav");
                    } else if (pr instanceof SnowProjectile) {
                        z.takeDamage(10);
                        z.applySlow();
                        playSound("hitFX.wav");
                    } else {
                        z.takeDamage(10);
                        playSound("hitFX.wav");
                    }
                    z.checkHalfTransition();
                    pr.hit();
                    return false;
                }
            }
            return pr.x > 800;
        });

        for (NormalZombie z : zombiesList) {
            if (z.isFinished()) {
                GameEventManager.getInstance().fireZombieDeath(z);
            }
        }
        zombiesList.removeIf(NormalZombie::isFinished);

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Plant plant = grid[r][c];
                if (plant != null && plant.isDead()) {
                    for (NormalZombie z : zombiesList) {
                        if (z.isEating() && z.getTargetPlant() == plant) {
                            z.stopEating();
                        }
                    }
                    GameEventManager.getInstance().firePlantDestroyed(plant, r, c);
                    grid[r][c] = null;
                }
            }
        }
    }

    private void drawIntroText(Graphics2D g) {
        long elapsed = System.currentTimeMillis() - introStartTime;
        if (elapsed < INITIAL_DELAY)
            return;

        long actualElapsed = elapsed - INITIAL_DELAY;
        String text;
        float baseSize;
        float scale;
        float progress;

        if (actualElapsed < READY_DURATION) {
            text = "Ready...";
            baseSize = 52f;
            progress = (float) actualElapsed / READY_DURATION;
            scale = 1.0f + 0.15f * progress;
        } else if (actualElapsed < READY_DURATION + SET_DURATION) {
            text = "Set...";
            baseSize = 52f;
            progress = (float) (actualElapsed - READY_DURATION) / SET_DURATION;
            scale = 1.0f + 0.15f * progress;
        } else if (actualElapsed < READY_DURATION + SET_DURATION + PLANT_DURATION) {
            text = "PLANT!";
            baseSize = 68f;
            scale = 1.0f;
        } else {
            return;
        }

        float fontSize = baseSize * scale;
        Font font = new Font("Impact", Font.BOLD, (int) fontSize);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        int textW = fm.stringWidth(text);
        int textH = fm.getAscent();
        int cx = getWidth() / 2 - textW / 2;
        int cy = getHeight() / 2 + textH / 4;

        g.setColor(Color.BLACK);
        int outline = 3;
        for (int dx = -outline; dx <= outline; dx++) {
            for (int dy = -outline; dy <= outline; dy++) {
                if (dx != 0 || dy != 0)
                    g.drawString(text, cx + dx, cy + dy);
            }
        }

        g.setColor(new Color(200, 25, 25));
        g.drawString(text, cx, cy);
    }

    // === Audio ===
    private void playSound(String filename) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(SOUND_PATH + filename));
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            applyVolume(clip, sfxVolume);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Play sound và trả về Clip để lấy duration */
    private Clip playSoundAndGetClip(String filename) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(SOUND_PATH + filename));
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            applyVolume(clip, sfxVolume);
            clip.start();
            return clip;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void playBGM(String filename) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(BGM_PATH + filename));
            bgmClip = AudioSystem.getClip();
            bgmClip.open(ais);
            applyVolume(bgmClip, musicVolume);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyVolume(Clip clip, float volume) {
        if (clip == null)
            return;
        try {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            if (volume <= 0.001f) {
                gain.setValue(gain.getMinimum());
            } else {
                float dB = 20f * (float) Math.log10(volume);
                dB = Math.max(dB, gain.getMinimum());
                dB = Math.min(dB, gain.getMaximum());
                gain.setValue(dB);
            }
        } catch (Exception e) {
        }
    }

    // === GAME OVER SCREEN ===
    private void drawGameOver(Graphics2D g) {
        int w = getWidth(), h = getHeight();
        long elapsed = System.currentTimeMillis() - gameOverStartTime;

        // 1. Fade to black (0 → 1 trong FADE_DURATION)
        float fadeAlpha = Math.min(1.0f, (float) elapsed / FADE_DURATION);
        g.setColor(new Color(0, 0, 0, (int) (fadeAlpha * 255)));
        g.fillRect(0, 0, w, h);

        if (elapsed < FADE_DURATION)
            return; // chưa đen hết thì chưa hiện gì

        // 2. Text fade in
        long textElapsed = elapsed - FADE_DURATION;
        float textAlpha = Math.min(1.0f, (float) textElapsed / TEXT_FADE_DURATION);
        Composite oldComp = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, textAlpha));

        // === Dòng chữ "THE ZOMBIES ATE YOUR BRAINS!" ===
        Font font = new Font("Impact", Font.BOLD, 36);
        g.setFont(font);
        String line1 = "THE ZOMBIES";
        String line2 = "ATE YOUR";
        String line3 = "BRAINS!";
        FontMetrics fm = g.getFontMetrics();

        int lineGap = 6;
        int totalTextH = fm.getAscent() * 3 + lineGap * 2;
        int textTop = h / 6;

        // Helper: vẽ chữ có viền trắng mỏng + fill xanh lá
        drawOutlinedText(g, line1, w / 2 - fm.stringWidth(line1) / 2, textTop + fm.getAscent());
        drawOutlinedText(g, line2, w / 2 - fm.stringWidth(line2) / 2, textTop + fm.getAscent() * 2 + lineGap);
        drawOutlinedText(g, line3, w / 2 - fm.stringWidth(line3) / 2, textTop + fm.getAscent() * 3 + lineGap * 2);

        // === Brain image ===
        if (brainImg != null) {
            int brainW = 280;
            int brainH = (int) (brainW * (522.0 / 1280.0));
            int bx = w / 2 - brainW / 2;
            int by = textTop + totalTextH + 30;
            g.drawImage(brainImg, bx, by, brainW, brainH, null);
        }

        // === Buttons ===
        int btnW = 140, btnH = 38;
        int btnY = h - 80;
        int gap = 40;
        int exitX = w / 2 - btnW - gap / 2;
        int retryX = w / 2 + gap / 2;
        exitBounds = new Rectangle(exitX, btnY, btnW, btnH);
        retryBounds = new Rectangle(retryX, btnY, btnW, btnH);

        // RETRY button click animation
        boolean retryClicked = retryClickTime > 0 && System.currentTimeMillis() - retryClickTime < 80;

        // EXIT TO MAP (gradient nâu đen)
        drawGradientButton(g, exitBounds, "EXIT TO MAP",
                new Color(90, 60, 30), new Color(50, 30, 10), false);

        // RETRY (gradient tím)
        drawGradientButton(g, retryBounds, "RETRY",
                new Color(110, 70, 160), new Color(65, 35, 110), retryClicked);

        g.setComposite(oldComp);
    }

    private void drawOutlinedText(Graphics2D g, String text, int x, int y) {
        // Viền trắng mỏng
        g.setColor(new Color(255, 255, 255, 120));
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (dx != 0 || dy != 0)
                    g.drawString(text, x + dx, y + dy);
            }
        }
        // Fill xanh lá
        g.setColor(new Color(60, 200, 40));
        g.drawString(text, x, y);
    }

    private void drawGradientButton(Graphics2D g, Rectangle r, String text,
            Color top, Color bottom, boolean clicked) {
        int bx = r.x, by = r.y, bw = r.width, bh = r.height;

        // Click animation: phóng to 8%
        if (clicked) {
            int extraW = (int) (bw * 0.08);
            int extraH = (int) (bh * 0.08);
            bx -= extraW / 2;
            by -= extraH / 2;
            bw += extraW;
            bh += extraH;
        }

        // Gradient fill
        GradientPaint gp = new GradientPaint(bx, by, top, bx, by + bh, bottom);
        g.setPaint(gp);
        g.fillRoundRect(bx, by, bw, bh, 10, 10);

        // Viền trắng
        g.setColor(new Color(220, 220, 220));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(bx, by, bw, bh, 10, 10);

        // Text
        g.setFont(new Font("Impact", Font.BOLD, 18));
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        int tx = bx + (bw - fm.stringWidth(text)) / 2;
        int ty = by + (bh + fm.getAscent() - fm.getDescent()) / 2;
        g.drawString(text, tx, ty);
    }

    // === MAIN MENU ===

    private void drawMainMenu(Graphics2D g) {
        int w = getWidth(), h = getHeight();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, w, h);
        if (mainMenuImg != null) {
            Composite oldComp = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, menuFadeAlpha));
            g.drawImage(mainMenuImg, 0, 0, w, h, null);
            if (menuGlowBtn == 0 && startAdventureGlowImg != null) {
                // Width stretched 5% to the left, keep height same as before
                int baseW = 160;
                int baseGh = (int) (baseW * 419.0 / 596.0); // ≈112
                int baseGw = (int) (baseW * 1.05); // 168 (+5%)
                int rightEdge = (689 - baseW / 2) + baseW;
                int gx = rightEdge - baseGw; // Shift left so only left side stretches
                int gy = 321 - baseGh / 2;

                // Shrink by 2px, keep top-left (gx, gy) the same
                int gw = baseGw - 2;
                int gh = (int) (gw * ((double) baseGh / baseGw));
                g.drawImage(startAdventureGlowImg, gx, gy, gx + gw, gy + gh,
                        0, 0, startAdventureGlowImg.getWidth(), startAdventureGlowImg.getHeight(), null);
            }
            if (menuGlowBtn == 1 && moreGlowImg != null) {
                // Hitbox center, 1.3x scale, height from aspect ratio (597×418)
                float sx = 800f / 621f;
                float sy = 500f / 465f;
                int baseX = (int) (420f * sx) + 80;
                int baseY = (int) (130f * sy) + 220;
                int baseW = (int) (100f * sx) - 5;
                int baseH = (int) (40f * sy) + 10;
                int baseGw = (int) (baseW * 1.3f);
                int baseGh = (int) (baseGw * 418.0 / 597.0); // aspect ratio
                int cx = baseX + baseW / 2;
                int cy = baseY + baseH / 2;
                int gx = cx - baseGw / 2;
                int gy = cy - baseGh / 2;

                // Shrink by 2px, keep top-left (gx, gy) the same
                int gw = baseGw - 2;
                int gh = (int) (gw * 418.0 / 597.0);
                g.drawImage(moreGlowImg, gx, gy, gx + gw, gy + gh,
                        0, 0, moreGlowImg.getWidth(), moreGlowImg.getHeight(), null);
            }
            g.setComposite(oldComp);
        }

        if (menuFadingOut) {
            long elapsed = System.currentTimeMillis() - menuFadeOutStart;
            float fadeOut = Math.min(1.0f, elapsed / 500f);
            g.setColor(new Color(0, 0, 0, (int) (fadeOut * 255)));
            g.fillRect(0, 0, w, h);
        }
    }

    private void drawMoreScreen(Graphics2D g) {
        int w = getWidth(), h = getHeight();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, w, h);
        if (notMomJokeImg != null) {
            Composite oldComp = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, moreScreenAlpha));
            g.drawImage(notMomJokeImg, 0, 0, w, h, null);
            if (backBtnImg != null && moreScreenAlpha >= 1.0f) {
                Rectangle bb = getBackBtnBounds();
                g.drawImage(backBtnImg, bb.x, bb.y, bb.x + bb.width, bb.y + bb.height,
                        0, 0, backBtnImg.getWidth(), backBtnImg.getHeight(), null);
            }
            g.setComposite(oldComp);
        }
        if (moreFadingOut) {
            long elapsed = System.currentTimeMillis() - moreFadeOutStart;
            float fadeOut = Math.min(1.0f, elapsed / 500f);
            g.setColor(new Color(0, 0, 0, (int) (fadeOut * 255)));
            g.fillRect(0, 0, w, h);
        }
    }

    private Rectangle getBackBtnBounds() {
        int btnH = 42;
        int btnW = (int) (847.0 * btnH / 295.0);
        return new Rectangle(8, 8, btnW, btnH);
    }

    private void startGameFromMenu() {
        stopIntroMusic();
        gamePhase = GamePhase.INTRO;
        introStartTime = System.currentTimeMillis();
        introSoundPlayed = false;
        for (PlantCard c : cards)
            c.setFrozen(true);
        initLawnMowers();
        menuFadingOut = false;
        menuFadeOutStart = -1;
        menuFadeTarget = -1;
        menuGlowBtn = -1;
        menuGlowTime = -1;
    }

    private void goToMainMenu() {
        if (bgmClip != null && bgmClip.isRunning())
            bgmClip.stop();
        zombiesList.clear();
        projectileList.clear();
        soilEffects.clear();
        sunList.clear();
        sunCount = 50;
        heldType = null;
        isHoldingShovel = false;
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                grid[r][c] = null;
        for (PlantCard c : cards) {
            c.setSelected(false);
            c.setFrozen(true);
        }
        zombiesSpawned = false;
        currentWave = 1;
        waveManager.reset();
        zombieAnnouncePlayed = false;
        zombieAnnounceEndTime = -1;
        firstGroanPlayed = false;
        nextGroanTime = -1;
        gameOverStartTime = -1;
        retryClickTime = -1;
        retryBounds = null;
        exitBounds = null;
        settingsOpen = false;
        settingsGlowBtn = -1;
        settingsGlowTime = -1;
        settingsRestartFading = false;
        settingsRestartFadeStart = -1;
        menuClickTime = -1;
        gamePhase = GamePhase.MAIN_MENU;
        menuFadeAlpha = 1.0f;
        menuFadeInStart = -1;
        menuFadingOut = false;
        menuFadeOutStart = -1;
        menuGlowBtn = -1;
        menuGlowTime = -1;
        playIntroMusic();
    }

    private void playIntroMusic() {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(BGM_PATH + "IntroTheme.wav"));
            introClip = AudioSystem.getClip();
            introClip.open(ais);
            applyVolume(introClip, musicVolume);
            introClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void stopIntroMusic() {
        if (introClip != null) {
            if (introClip.isRunning())
                introClip.stop();
            introClip.close();
            introClip = null;
        }
    }

    // === RESTART GAME ===
    private void restartGame() {
        // Dừng nhạc
        if (bgmClip != null && bgmClip.isRunning())
            bgmClip.stop();

        // Reset state
        zombiesList.clear();
        projectileList.clear();
        soilEffects.clear();
        sunList.clear();
        sunCount = 50;
        heldType = null;
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                grid[r][c] = null;

        // Reset cards
        for (PlantCard c : cards) {
            c.setSelected(false);
            c.setFrozen(true);
        }

        // Reset intro
        gamePhase = GamePhase.INTRO;
        introStartTime = System.currentTimeMillis();
        introSoundPlayed = false;

        // Reset lawn mowers
        initLawnMowers();

        // Reset game over
        gameOverStartTime = -1;
        retryClickTime = -1;
        retryBounds = null;
        exitBounds = null;

        // Reset zombie spawn & audio flags
        zombiesSpawned = false;
        currentWave = 1;
        waveManager.reset();
        zombieAnnouncePlayed = false;
        zombieAnnounceEndTime = -1;
        firstGroanPlayed = false;
        nextGroanTime = -1;

        lastNano = System.nanoTime();

        // Reset settings state
        settingsOpen = false;
        settingsGlowBtn = -1;
        settingsGlowTime = -1;
        settingsRestartFading = false;
        settingsRestartFadeStart = -1;
        settingsFadeAction = -1;
        menuClickTime = -1;
    }

    // === LAWN MOWER ===

    private void initLawnMowers() {
        lawnMowers.clear();
        long totalEnterTime = 700;
        long staggerDelay = totalEnterTime / ROWS;

        for (int lane = 0; lane < ROWS; lane++) {
            LawnMower mower = new LawnMower(lane, CELL_H, GRID_LEFT, GRID_TOP);
            lawnMowers.add(mower);
        }

        long baseTime = System.currentTimeMillis() + INITIAL_DELAY;
        for (int i = 0; i < ROWS; i++) {
            int laneIndex = ROWS - 1 - i;
            long startTime = baseTime + i * staggerDelay;
            long duration = staggerDelay;
            lawnMowers.get(laneIndex).startEntering(startTime, duration);
        }
    }

    /**
     * Kiểm tra zombie chạm vào máy cắt cỏ IDLE → kích hoạt mower.
     * Chỉ kích hoạt khi zombie đã vượt qua tất cả cây trên hàng.
     */
    private void checkZombieLawnMowerContact() {
        for (LawnMower mower : lawnMowers) {
            if (!mower.isIdle())
                continue;

            int lane = mower.getLane();
            for (NormalZombie zombie : zombiesList) {
                int zombieLane = zombie.getY() / CELL_H;
                if (zombieLane != lane)
                    continue;

                // Kiểm tra va chạm (Bỏ check passedAllPlants vì mower luôn bảo vệ cuối hàng)
                if (mower.getBounds().intersects(zombie.getBounds())) {
                    mower.activate();
                    playSound("lawnmower.wav");
                    break;
                }
            }
        }
    }

    /**
     * Kiểm tra lawn mower đã kích hoạt va chạm zombie cùng hàng → giết ngay.
     */
    private void checkLawnMowerKill() {
        for (LawnMower mower : lawnMowers) {
            if (!mower.isActivated())
                continue;

            int lane = mower.getLane();
            Rectangle mowerBounds = mower.getBounds();

            for (NormalZombie zombie : zombiesList) {
                int zombieLane = zombie.getY() / CELL_H;
                if (zombieLane != lane)
                    continue;

                if (zombie.health > 0 && mowerBounds.intersects(zombie.getBounds())) {
                    zombie.health = 0; // giết ngay
                }
            }
        }
        // Xoá zombie đã hoàn thành animation chết
        zombiesList.removeIf(NormalZombie::isFinished);
    }

    // === MENU BUTTON ===

    private Rectangle getMenuBtnBounds() {
        // Menu.png: 554×166, chiều cao = cardH = 42
        int cardH = 42;
        int menuH = cardH;
        int menuW = (int) (554.0 * menuH / 166.0);
        int menuX = getWidth() - menuW - 5;
        int menuY = (TOP_BAR_H - menuH) / 2;
        return new Rectangle(menuX, menuY, menuW, menuH);
    }

    private void drawMenuButton(Graphics2D g) {
        if (menuBtnImg == null)
            return;
        Rectangle r = getMenuBtnBounds();
        int bx = r.x, by = r.y, bw = r.width, bh = r.height;

        // Click animation: thu nhỏ 15% trong 50ms, giữ ở góc phải trên
        boolean clicking = menuClickTime > 0 && System.currentTimeMillis() - menuClickTime < 50;
        if (clicking) {
            int shrinkW = (int) (bw * 0.15);
            int shrinkH = (int) (bh * 0.15);
            bx += shrinkW; // anchor phải: dịch x sang phải
            bw -= shrinkW;
            bh -= shrinkH;
            // by giữ nguyên (anchor trên)
        }

        // Bóng bao phủ toàn bộ nút, mờ dần ra ngoài
        for (int i = 1; i <= 5; i++) {
            g.setColor(new Color(0, 0, 0, 40 / i));
            g.fillRect(bx - i, by - i, bw + 2 * i, bh + 2 * i);
        }

        // Vẽ nút Menu
        g.drawImage(menuBtnImg, bx, by, bx + bw, by + bh,
                0, 0, menuBtnImg.getWidth(), menuBtnImg.getHeight(), null);
    }

    // === SETTINGS OVERLAY ===

    private void openSettings() {
        settingsOpen = true;
        pauseStartTime = System.currentTimeMillis();
        // Tạm dừng nhạc nền
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
        }
    }

    private void closeSettings() {
        settingsOpen = false;
        long pauseDuration = System.currentTimeMillis() - pauseStartTime;

        // Offset tất cả timestamp toàn cục để game tiếp tục mượt
        bgmStartTime += pauseDuration;
        lastSunSpawn += pauseDuration;
        if (zombieAnnounceEndTime > 0)
            zombieAnnounceEndTime += pauseDuration;
        if (nextGroanTime > 0)
            nextGroanTime += pauseDuration;
        lastNano = System.nanoTime();

        // Tiếp tục nhạc nền nếu âm lượng > 0
        if (bgmClip != null && musicVolume > 0.001f) {
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
        }

        // Reset glow state
        settingsGlowBtn = -1;
        settingsGlowTime = -1;
    }

    /** Lấy vùng vẽ settingTab trên màn hình */
    private Rectangle getSettingsTabBounds() {
        int margin = 60;
        int tabH = getHeight() - 2 * margin; // 500 - 120 = 380
        int tabW = (int) (577.0 * tabH / 433.0);
        int tabX = (getWidth() - tabW) / 2;
        int tabY = margin;
        return new Rectangle(tabX, tabY, tabW, tabH);
    }

    /** Chuyển toạ độ gốc (trong settingTab.png 577×433) sang toạ độ màn hình */
    private Rectangle settingsBtnToScreen(int origX, int origY, int origW, int origH) {
        Rectangle tab = getSettingsTabBounds();
        float scale = (float) tab.height / 433.0f;
        int sx = tab.x + (int) (origX * scale);
        int sy = tab.y + (int) (origY * scale);
        int sw = (int) (origW * scale);
        int sh = (int) (origH * scale);
        return new Rectangle(sx, sy, sw, sh);
    }

    private void drawSettingsOverlay(Graphics2D g) {
        int w = getWidth(), h = getHeight();

        // Dim background
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(0, 0, w, h);

        // Vẽ setting tab
        if (settingTabImg != null) {
            Rectangle tab = getSettingsTabBounds();
            g.drawImage(settingTabImg, tab.x, tab.y, tab.x + tab.width, tab.y + tab.height,
                    0, 0, settingTabImg.getWidth(), settingTabImg.getHeight(), null);
        }

        // Vẽ glow button nếu đang glow
        if (settingsGlowBtn >= 0 && settingsGlowTime > 0) {
            BufferedImage glowImg = null;
            Rectangle btnRect = null;

            switch (settingsGlowBtn) {
                case 0 -> {
                    glowImg = tabRestartImg;
                    btnRect = settingsBtnToScreen(181, 238, 215, 35);
                }
                case 1 -> {
                    glowImg = tabMainMenuImg;
                    btnRect = settingsBtnToScreen(181, 280, 215, 35);
                }
                case 2 -> {
                    glowImg = tabBackToGameImg;
                    btnRect = settingsBtnToScreen(145, 349, 280, 50);
                }
            }

            if (glowImg != null && btnRect != null) {
                // Mặc định to hơn 10%, riêng Restart to hơn 14% (thêm 4%)
                double scaleFactor = (settingsGlowBtn == 0) ? 1.14 : 1.10;
                int nw = (int) (btnRect.width * scaleFactor);
                int nh = (int) (btnRect.height * scaleFactor);
                int nx = btnRect.x - (nw - btnRect.width) / 2;
                int ny = btnRect.y - (nh - btnRect.height) / 2;

                g.drawImage(glowImg, nx, ny, nx + nw, ny + nh,
                        0, 0, glowImg.getWidth(), glowImg.getHeight(), null);
            }
        }

        // Vẽ volume sliders
        drawVolumeSliders(g);
    }

    private void handleSettingsClick(int mx, int my) {
        // Nếu đang glow hoặc đang fade → không nhận click
        if (settingsGlowBtn >= 0 || settingsRestartFading)
            return;

        // Kiểm tra Restart Level
        Rectangle restartRect = settingsBtnToScreen(181, 238, 215, 35);
        if (restartRect.contains(mx, my)) {
            settingsGlowBtn = 0;
            settingsGlowTime = System.currentTimeMillis();
            playSound("buttonclick.wav");
            return;
        }

        // Kiểm tra Main Menu
        Rectangle mainMenuRect = settingsBtnToScreen(181, 280, 215, 35);
        if (mainMenuRect.contains(mx, my)) {
            settingsGlowBtn = 1;
            settingsGlowTime = System.currentTimeMillis();
            playSound("buttonclick.wav");
            return;
        }

        // Kiểm tra Back To Game
        Rectangle backRect = settingsBtnToScreen(145, 349, 280, 50);
        if (backRect.contains(mx, my)) {
            settingsGlowBtn = 2;
            settingsGlowTime = System.currentTimeMillis();
            playSound("buttonclick.wav");
            return;
        }

        // Kiểm tra click vào vùng slider Music
        Rectangle musicSliderArea = getSliderArea(0);
        if (musicSliderArea.contains(mx, my)) {
            draggingSlider = 0;
            handleSliderDrag(mx);
            return;
        }

        // Kiểm tra click vào vùng slider SFX
        Rectangle sfxSliderArea = getSliderArea(1);
        if (sfxSliderArea.contains(mx, my)) {
            draggingSlider = 1;
            handleSliderDrag(mx);
            return;
        }
    }

    /** Xử lý timer cho glow button và restart fade khi settings mở */
    private void handleSettingsTimers() {
        // Xử lý glow button hết hạn (150ms)
        if (settingsGlowBtn >= 0 && settingsGlowTime > 0) {
            long elapsed = System.currentTimeMillis() - settingsGlowTime;
            if (elapsed >= 150) {
                int btn = settingsGlowBtn;
                settingsGlowBtn = -1;
                settingsGlowTime = -1;

                switch (btn) {
                    case 0 -> {
                        // Restart Level: bắt đầu fade to black
                        settingsRestartFading = true;
                        settingsRestartFadeStart = System.currentTimeMillis();
                        settingsFadeAction = 0;
                    }
                    case 1 -> {
                        // Main Menu: fade to black then go to main menu
                        settingsRestartFading = true;
                        settingsRestartFadeStart = System.currentTimeMillis();
                        settingsFadeAction = 1;
                    }
                    case 2 -> {
                        // Back To Game: đóng settings, quay lại game
                        closeSettings();
                    }
                }
            }
        }

        // Xử lý restart/mainmenu fade (300ms)
        if (settingsRestartFading) {
            long elapsed = System.currentTimeMillis() - settingsRestartFadeStart;
            if (elapsed >= 300) {
                settingsRestartFading = false;
                settingsOpen = false;
                if (settingsFadeAction == 1) {
                    goToMainMenu();
                } else {
                    restartGame();
                }
                settingsFadeAction = -1;
            }
        }
    }

    // === VOLUME SLIDERS ===

    /** Lấy vùng thanh slider trên màn hình (dùng cho cả vẽ và click) */
    private Rectangle getSliderArea(int sliderIndex) {
        // Toạ độ gốc trong settingTab.png 577×433
        int origX = 322;
        int origY = (sliderIndex == 0) ? 122 : 157;
        int origW = 100;
        int origH = 16; // vùng nhấn click rộng hơn
        return settingsBtnToScreen(origX, origY, origW, origH);
    }

    private void drawVolumeSliders(Graphics2D g) {
        for (int i = 0; i < 2; i++) {
            Rectangle area = getSliderArea(i);
            float volume = (i == 0) ? musicVolume : sfxVolume;

            int trackY = area.y + area.height / 2;
            int trackLeft = area.x;
            int trackRight = area.x + area.width;

            // Vẽ thanh track (nền xám nhạt)
            g.setColor(new Color(80, 80, 80, 120));
            g.fillRoundRect(trackLeft, trackY - 2, area.width, 4, 3, 3);

            // Vẽ phần đã điền (màu xanh nhạt)
            int filledW = (int) (area.width * volume);
            g.setColor(new Color(120, 180, 80, 180));
            g.fillRoundRect(trackLeft, trackY - 2, filledW, 4, 3, 3);

            // Vẽ nút kéo (knob) màu xám
            int knobX = trackLeft + filledW;
            int knobR = 6;
            // Bóng nhỏ
            g.setColor(new Color(0, 0, 0, 50));
            g.fillOval(knobX - knobR + 1, trackY - knobR + 1, knobR * 2, knobR * 2);
            // Nút chính
            g.setColor(new Color(180, 180, 180));
            g.fillOval(knobX - knobR, trackY - knobR, knobR * 2, knobR * 2);
            // Viền
            g.setColor(new Color(100, 100, 100));
            g.drawOval(knobX - knobR, trackY - knobR, knobR * 2, knobR * 2);
            // Highlight sáng trên nút
            g.setColor(new Color(255, 255, 255, 80));
            g.fillOval(knobX - knobR + 2, trackY - knobR + 1, knobR - 2, knobR - 2);
        }
    }

    private void handleSliderDrag(int mx) {
        Rectangle area = getSliderArea(draggingSlider);
        float ratio = (float) (mx - area.x) / area.width;
        ratio = Math.max(0f, Math.min(1f, ratio));

        if (draggingSlider == 0) {
            musicVolume = ratio;
            // Cập nhật BGM volume ngay lập tức
            if (bgmClip != null) {
                applyVolume(bgmClip, musicVolume);
            }
        } else {
            sfxVolume = ratio;
        }
        repaint();
    }
}
