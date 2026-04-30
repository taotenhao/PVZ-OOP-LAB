import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

class Gatling extends Plant {
    private boolean active = false;

    // === Idle animation ===
    private BufferedImage idleSheet;
    private int idleFrame = 0;
    private long lastIdleTime;
    private final int IDLE_FRAMES = 8;
    private int idleFrameW, idleFrameH;

    // === Shooting animation ===
    private BufferedImage shootSheet;
    private int shootAnimFrame = 0;
    private long shootAnimStartTime;
    private static final int SHOOT_FRAMES = 8;
    private int shootFrameW, shootFrameH;

    private int drawW, drawH;

    // === Shooting logic ===
    private static final int BURST_COUNT = 4;
    private static final long BURST_INTERVAL = 200;
    private static final long SHOOT_COOLDOWN = 1700;

    private boolean shooting = false;
    private int shotsFired = 0;
    private long lastShotTime = 0;
    private long burstEndTime = 0;
    private long lastBurstEndTime = 0;
    private boolean pendingShot = false;

    private static final long TOTAL_SHOOT_ANIM = (BURST_COUNT - 1) * BURST_INTERVAL;
    private static final long SHOOT_FRAME_DURATION = TOTAL_SHOOT_ANIM / SHOOT_FRAMES;

    public Gatling(int x, int y) {
        super(x, y, 100);
        try {
            idleSheet = ImageIO.read(new File("Graphic/Gatling.png"));
            idleFrameW = idleSheet.getWidth() / IDLE_FRAMES;
            idleFrameH = idleSheet.getHeight();

            drawH = idleFrameH / 3;
            drawW = idleFrameW / 3;

            frameWidth = idleFrameW;
            frameHeight = idleFrameH;
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            shootSheet = ImageIO.read(new File("Graphic/gatling_shoot.png"));
            shootFrameW = shootSheet.getWidth() / SHOOT_FRAMES;
            shootFrameH = shootSheet.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }

        lastIdleTime = System.currentTimeMillis();
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void update() {
        if (!active) return;
        long now = System.currentTimeMillis();

        if (shooting) {
            if (shotsFired < BURST_COUNT) {
                if (now - lastShotTime >= BURST_INTERVAL) {
                    pendingShot = true;
                    shotsFired++;
                    lastShotTime = now;
                    if (shotsFired >= BURST_COUNT) {
                        lastBurstEndTime = now;
                    }
                }
            } else {
                if (now - shootAnimStartTime >= TOTAL_SHOOT_ANIM + 100) {
                    shooting = false;
                }
            }
        } else {
            if (lastBurstEndTime == 0 || now - lastBurstEndTime >= SHOOT_COOLDOWN) {
                shooting = true;
                shotsFired = 1;
                pendingShot = true;
                lastShotTime = now;
                shootAnimStartTime = now;
            }
        }
    }

    public boolean consumeShot() {
        if (pendingShot) {
            pendingShot = false;
            return true;
        }
        return false;
    }

    @Override
    public void draw(Graphics g) {
        long now = System.currentTimeMillis();

        ShadowRenderer.draw((Graphics2D) g, x + drawW / 2, y + drawH - 8, drawW, 1.0f);

        BufferedImage activeSheet;
        int srcFW, srcFH;
        int frame;

        if (shooting && shootSheet != null) {
            long elapsed = now - shootAnimStartTime;
            int calcFrame = (int) (elapsed / Math.max(1, SHOOT_FRAME_DURATION));
            if (calcFrame >= SHOOT_FRAMES) {
                calcFrame = SHOOT_FRAMES - 1;
            }
            shootAnimFrame = calcFrame;
            activeSheet = shootSheet;
            srcFW = shootFrameW;
            srcFH = shootFrameH;
            frame = shootAnimFrame;
        } else {
            if (now - lastIdleTime > 200) {
                idleFrame = (idleFrame + 1) % IDLE_FRAMES;
                lastIdleTime = now;
            }
            activeSheet = idleSheet;
            srcFW = idleFrameW;
            srcFH = idleFrameH;
            frame = idleFrame;
        }

        int srcX = frame * srcFW;
        
        int currentDrawW = drawW;
        int currentDrawH = drawH;
        if (activeSheet == shootSheet) {
            currentDrawW = (int)(drawW * 1.1);
            currentDrawH = (int)(drawH * 1.1);
        }

        BufferedImage tmp = new BufferedImage(currentDrawW, currentDrawH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D tg = tmp.createGraphics();
        tg.drawImage(activeSheet, 0, 0, currentDrawW, currentDrawH,
                srcX, 0, srcX + srcFW, srcFH, null);

        if (isFlashing()) {
            tg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.65f));
            tg.setColor(Color.WHITE);
            tg.fillRect(0, 0, currentDrawW, currentDrawH);
        }
        tg.dispose();

        int offsetX = (currentDrawW - drawW) / 2;
        int offsetY = (currentDrawH - drawH);
        g.drawImage(tmp, x - offsetX, y - offsetY, null);
    }

    public int getX() { return x; }
    public int getY() { return y; }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x + 15, y, Math.max(1, drawW - 15), drawH);
    }
}
