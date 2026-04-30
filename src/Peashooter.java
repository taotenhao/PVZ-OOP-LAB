import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

class Peashooter extends Plant {
    private int shootTimer = 0;
    private boolean active = false;

    // === Idle animation ===
    private BufferedImage idleSheet;
    private int idleFrame = 0;
    private long lastIdleTime;
    private final int IDLE_FRAMES = 8;
    private int idleFrameW, idleFrameH;

    // === Shoot animation ===
    private BufferedImage shootSheet;
    private int shootAnimFrame = 0;
    private long lastShootAnimTime;
    private final int SHOOT_FRAMES = 3;
    private int shootFrameW, shootFrameH;
    private boolean shooting = false;

    private int drawW, drawH;

    public Peashooter(String path, int x, int y) {
        super(x, y, 60);
        try {
            idleSheet    = ImageIO.read(new File(path));
            idleFrameW   = idleSheet.getWidth()  / IDLE_FRAMES;
            idleFrameH   = idleSheet.getHeight();
            frameWidth   = idleFrameW;
            frameHeight  = idleFrameH;
            drawW        = idleFrameW / 3;
            drawH        = idleFrameH / 3;
        } catch (Exception e) { e.printStackTrace(); }

        try {
            shootSheet  = ImageIO.read(new File("Graphic/peaShooting.png"));
            shootFrameW = shootSheet.getWidth()  / SHOOT_FRAMES;
            shootFrameH = shootSheet.getHeight();
        } catch (Exception e) { e.printStackTrace(); }

        lastIdleTime      = System.currentTimeMillis();
        lastShootAnimTime = System.currentTimeMillis();
    }

    public void setActive(boolean active) {
        if (!this.active && active) shootTimer = 0;
        this.active = active;
    }

    @Override
    public void update() {
        if (active) shootTimer++;
    }

    public boolean canShoot() {
        if (active && shootTimer >= 32) {
            shootTimer = 0;
            shooting          = true;
            shootAnimFrame    = 0;
            lastShootAnimTime = System.currentTimeMillis();
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
            if (now - lastShootAnimTime > 80) {
                shootAnimFrame++;
                lastShootAnimTime = now;
                if (shootAnimFrame >= SHOOT_FRAMES) {
                    shooting       = false;
                    shootAnimFrame = 0;
                }
            }
            if (shooting) {
                activeSheet = shootSheet;
                srcFW = shootFrameW;
                srcFH = shootFrameH;
                frame = shootAnimFrame;
            } else {
                activeSheet = idleSheet;
                srcFW = idleFrameW;
                srcFH = idleFrameH;
                frame = idleFrame;
            }
        } else {
            if (now - lastIdleTime > 200) {
                idleFrame    = (idleFrame + 1) % IDLE_FRAMES;
                lastIdleTime = now;
            }
            activeSheet = idleSheet;
            srcFW = idleFrameW;
            srcFH = idleFrameH;
            frame = idleFrame;
        }

        int srcX = frame * srcFW;

        BufferedImage tmp = new BufferedImage(drawW, drawH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D tg = tmp.createGraphics();
        tg.drawImage(activeSheet, 0, 0, drawW, drawH,
                     srcX, 0, srcX + srcFW, srcFH, null);

        if (isFlashing()) {
            tg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.65f));
            tg.setColor(Color.WHITE);
            tg.fillRect(0, 0, drawW, drawH);
        }
        tg.dispose();

        g.drawImage(tmp, x, y, null);
    }

    public int getX() { return x; }
    public int getY() { return y; }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x + 15, y, Math.max(1, drawW - 15), drawH);
    }
}
