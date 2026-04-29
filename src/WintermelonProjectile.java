import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;

class WintermelonProjectile extends Projectile {
    private static BufferedImage melonBulletImg;
    private static BufferedImage melonHitSheet;
    private static int hitFrameW, hitFrameH;
    private static final int HIT_FRAMES = 6;

    static {
        try {
            melonBulletImg = ImageIO.read(new File("Graphic/Wintermelon_projectile.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            melonHitSheet = ImageIO.read(new File("Graphic/Wintermelon_hit.png"));
            hitFrameW = melonHitSheet.getWidth() / HIT_FRAMES;
            hitFrameH = melonHitSheet.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private float startX, startY;
    private float targetX, targetY;
    private float progress = 0f;
    private static final float ARC_HEIGHT = 150f;
    private static final long FLIGHT_DURATION = 1000;
    private long flightStartTime;

    private float rotation = 0f;

    private boolean landed = false;
    private boolean splashApplied = false;
    private int hitFrame = 0;
    private long lastHitTime = 0;

    private int drawW = 38;
    private int drawH = 31;
    private int hitDrawW, hitDrawH;

    private int targetRow;

    public WintermelonProjectile(String path, int x, int y, int targetX, int targetY, int targetRow) {
        super(path, x, y);
        this.startX = x;
        this.startY = y;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetRow = targetRow;
        this.flightStartTime = System.currentTimeMillis();
        hitDrawW = (int) (hitFrameW * 0.7f);
        hitDrawH = (int) (hitFrameH * 0.7f);
    }

    public boolean isLanded() {
        return landed;
    }

    public boolean isSplashApplied() {
        return splashApplied;
    }

    public void markSplashApplied() {
        splashApplied = true;
    }

    public int getTargetRow() {
        return targetRow;
    }

    public int getLandX() {
        return (int) targetX;
    }

    public int getLandY() {
        return (int) targetY;
    }

    @Override
    public void update() {
        if (finished)
            return;

        if (landed) {
            if (System.currentTimeMillis() - lastHitTime > 80) {
                hitFrame++;
                lastHitTime = System.currentTimeMillis();
                if (hitFrame >= HIT_FRAMES) {
                    finished = true;
                }
            }
            return;
        }

        long elapsed = System.currentTimeMillis() - flightStartTime;
        progress = Math.min(1.0f, (float) elapsed / FLIGHT_DURATION);

        float currentX = startX + (targetX - startX) * progress;
        float currentY = startY + (targetY - startY) * progress - 4 * ARC_HEIGHT * progress * (1 - progress);

        this.x = (int) currentX;
        this.y = (int) currentY;
        this.realX = currentX;

        rotation = 135f * progress;

        if (progress >= 1.0f) {
            landed = true;
            lastHitTime = System.currentTimeMillis();
            hitFrame = 0;
        }
    }

    @Override
    public void draw(Graphics g) {
        if (finished)
            return;
        Graphics2D g2 = (Graphics2D) g;

        if (landed && melonHitSheet != null) {
            int srcX = hitFrame * hitFrameW;
            int hx = (int) targetX - hitDrawW / 2;
            int hy = (int) targetY - hitDrawH / 2;
            g2.drawImage(melonHitSheet, hx, hy, hx + hitDrawW, hy + hitDrawH,
                    srcX, 0, srcX + hitFrameW, hitFrameH, null);
        } else if (melonBulletImg != null) {
            AffineTransform old = g2.getTransform();
            int cx = x + drawW / 2;
            int cy = y + drawH / 2;
            g2.translate(cx, cy);
            g2.rotate(Math.toRadians(rotation));
            g2.translate(-drawW / 2, -drawH / 2);
            g2.drawImage(melonBulletImg, 0, 0, drawW, drawH, null);
            g2.setTransform(old);

            ShadowRenderer.draw(g2, cx, (int) startY + 50, drawW, 0.4f);
        }
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, drawW, drawH);
    }
}
