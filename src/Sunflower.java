import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

class Sunflower extends Plant {
    // === Idle animation ===
    private BufferedImage idleSheet;
    private int idleFrame = 0;
    private long lastIdleTime;
    private final int IDLE_FRAMES = 6;
    private int idleFrameW, idleFrameH;

    private int drawW, drawH;

    private long lastSunTime;
    private static final long SUN_INTERVAL = 26000;
    private boolean sunReady = false;

    public Sunflower(String path, int x, int y) {
        super(x, y, 100);
        try {
            idleSheet  = ImageIO.read(new File(path));
            idleFrameW = idleSheet.getWidth() / IDLE_FRAMES;
            idleFrameH = idleSheet.getHeight();

            drawH = 62;
            drawW = (int) (idleFrameW * ((float) drawH / idleFrameH));

            frameWidth  = idleFrameW;
            frameHeight = idleFrameH;
        } catch (Exception e) {
            e.printStackTrace();
        }
        lastIdleTime = System.currentTimeMillis();
        lastSunTime  = System.currentTimeMillis();
    }

    @Override
    public void update() {
        if (System.currentTimeMillis() - lastSunTime >= SUN_INTERVAL) {
            lastSunTime = System.currentTimeMillis();
            sunReady = true;
        }
    }

    public boolean hasSunReady() {
        if (sunReady) {
            sunReady = false;
            return true;
        }
        return false;
    }

    public float getSunSpawnX() {
        return x + drawW / 2f - Sun.DRAW_W / 2f;
    }

    public float getSunSpawnY() {
        return y;
    }

    public float getSunLandY() {
        return y + drawH - Sun.DRAW_H;
    }

    @Override
    public void draw(Graphics g) {
        long now = System.currentTimeMillis();

        if (now - lastIdleTime > 200) {
            idleFrame    = (idleFrame + 1) % IDLE_FRAMES;
            lastIdleTime = now;
        }

        ShadowRenderer.draw((Graphics2D) g, x + drawW / 2, y + drawH - 8, drawW, 1.0f);

        int srcX = idleFrame * idleFrameW;

        BufferedImage tmp = new BufferedImage(drawW, drawH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D tg = tmp.createGraphics();
        tg.drawImage(idleSheet, 0, 0, drawW, drawH,
                     srcX, 0, srcX + idleFrameW, idleFrameH, null);

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
