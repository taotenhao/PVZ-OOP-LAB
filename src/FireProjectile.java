import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

class FireProjectile extends Projectile {
    private static BufferedImage fireBreakSheet;
    private static int fireBreakFrameW, fireBreakFrameH;
    private static BufferedImage fireBulletSheet;
    private static int fireBulletFrameW, fireBulletFrameH;
    private static final int FIRE_FRAMES = 3;

    private int bulletAnimFrame = 0;
    private long lastBulletAnimTime = 0;

    static {
        try {
            fireBreakSheet = ImageIO.read(new File("Graphic/fire_hit.png"));
            fireBreakFrameW = fireBreakSheet.getWidth() / 4;
            fireBreakFrameH = fireBreakSheet.getHeight();

            fireBulletSheet = ImageIO.read(new File("Graphic/fire_projectile.png"));
            fireBulletFrameW = fireBulletSheet.getWidth() / FIRE_FRAMES;
            fireBulletFrameH = fireBulletSheet.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FireProjectile(String dummyPath, int x, int y) {
        super(dummyPath, x, y);
        this.breakFrames = 4;
        lastBulletAnimTime = System.currentTimeMillis();
    }

    @Override
    public void draw(Graphics g) {
        if (finished) return;

        float bulletScale = 0.45f;
        float hitScale = 1.05f;
        int targetW = (int) (fireBulletFrameW / 5 * bulletScale);
        int targetH = (int) (fireBulletFrameH / 5 * bulletScale);

        if (breaking && fireBreakSheet != null) {
            int drawW = (int) (fireBreakFrameW / 5 * hitScale);
            int drawH = (int) (fireBreakFrameH / 5 * hitScale);
            int srcX = breakFrame * fireBreakFrameW;

            int cx = x + targetW / 2;
            int bottom = y + targetH + 23;
            ShadowRenderer.draw((Graphics2D) g, cx, bottom, drawW, 0.6f);

            int drawY = y - (drawH - targetH) / 2;
            int drawX = x - (drawW - targetW) / 2 + 44;

            g.drawImage(fireBreakSheet, drawX, drawY, drawX + drawW, drawY + drawH, srcX, 0, srcX + fireBreakFrameW, fireBreakFrameH, null);
        } else {
            if (System.currentTimeMillis() - lastBulletAnimTime > 80) {
                bulletAnimFrame = (bulletAnimFrame + 1) % FIRE_FRAMES;
                lastBulletAnimTime = System.currentTimeMillis();
            }

            int srcX = bulletAnimFrame * fireBulletFrameW;
            
            int cx = x + targetW / 2;
            int bottom = y + targetH + 23;
            ShadowRenderer.draw((Graphics2D) g, cx, bottom, targetW, 0.6f);

            g.drawImage(fireBulletSheet, x, y, x + targetW, y + targetH, srcX, 0, srcX + fireBulletFrameW, fireBulletFrameH, null);
        }
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x + 10, y, (int) (fireBulletFrameW / 5 * 0.45f), (int) (fireBulletFrameH / 5 * 0.45f));
    }
}
