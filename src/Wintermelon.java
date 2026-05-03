import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

class Wintermelon extends Plant {
    private boolean active = false;
    private long lastShootTime = 0;
    private static final long SHOOT_COOLDOWN = 3000;

    // === Idle animation ===
    private BufferedImage idleSheet;
    private int idleFrame = 0;
    private long lastIdleTime;
    private final int IDLE_FRAMES = 6;
    private int idleFrameW, idleFrameH;

    // === Shoot animation ===
    private BufferedImage shootSheet;
    private int shootAnimFrame = 0;
    private long lastShootAnimTime;
    private final int SHOOT_FRAMES = 6;
    private int shootFrameW, shootFrameH;
    private boolean shooting = false;

    private int drawW, drawH;
    private boolean pendingShot = false;

    public Wintermelon(int x, int y) {
        super(x, y, 100);
        try {
            idleSheet = ImageIO.read(new File("Graphic/Wintermelon.png"));
            idleFrameW = idleSheet.getWidth() / IDLE_FRAMES;
            idleFrameH = idleSheet.getHeight();

            drawH = 76;
            drawW = (int) (idleFrameW * ((float) drawH / idleFrameH));

            frameWidth = idleFrameW;
            frameHeight = idleFrameH;
        } catch (Exception e) { e.printStackTrace(); }

        try {
            shootSheet = ImageIO.read(new File("Graphic/Wintermelon_shoot.png"));
            shootFrameW = shootSheet.getWidth() / SHOOT_FRAMES;
            shootFrameH = shootSheet.getHeight();
        } catch (Exception e) { e.printStackTrace(); }

        lastIdleTime = System.currentTimeMillis();
        lastShootAnimTime = System.currentTimeMillis();
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void update() {
    }

    public boolean canShoot() {
        if (!active) return false;
        long now = System.currentTimeMillis();
        if (lastShootTime == 0 || now - lastShootTime >= SHOOT_COOLDOWN) {
            lastShootTime = now;
            shooting = true;
            shootAnimFrame = 0;
            lastShootAnimTime = now;
            pendingShot = true;
            return true;
        }
        return false;
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

        ShadowRenderer.draw((Graphics2D) g, x + drawW / 2 - 16, y + drawH + 12 - 32, drawW, 0.7f);

        BufferedImage activeSheet;
        int srcFW, srcFH;
        int frame;
        int currentDrawW = drawW;

        if (shooting && shootSheet != null) {
            if (now - lastShootAnimTime > 80) {
                shootAnimFrame++;
                lastShootAnimTime = now;
                if (shootAnimFrame >= SHOOT_FRAMES) {
                    shooting = false;
                    shootAnimFrame = 0;
                }
            }
            if (shooting) {
                activeSheet = shootSheet;
                srcFW = shootFrameW;
                srcFH = shootFrameH;
                frame = shootAnimFrame;
                currentDrawW = (int) (shootFrameW * ((float) drawH / shootFrameH));
            } else {
                activeSheet = idleSheet;
                srcFW = idleFrameW;
                srcFH = idleFrameH;
                frame = idleFrame;
            }
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
        int currentDrawH = drawH;
        if (shooting) {
            currentDrawH = (int)(drawH * 1.5f);
            currentDrawW = (int) (srcFW * ((float) currentDrawH / srcFH));
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

        int drawY = y - 7;
        if (shooting) {
            drawY -= (currentDrawH - drawH);
        }
        g.drawImage(tmp, x - 25, drawY, null);
    }

    public int getX() { return x; }
    public int getY() { return y; }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x + 15, y, Math.max(1, drawW - 15), drawH);
    }
}
