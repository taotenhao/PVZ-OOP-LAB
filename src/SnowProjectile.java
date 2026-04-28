import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

class SnowProjectile extends Projectile {
    private static BufferedImage snowBreakSheet;
    private static int snowBreakFrameW, snowBreakFrameH;
    private static BufferedImage bulletSheet;

    static {
        try {
            snowBreakSheet = ImageIO.read(new File("Graphic/Snowpea_hit.png"));
            snowBreakFrameW = snowBreakSheet.getWidth() / 4;
            snowBreakFrameH = snowBreakSheet.getHeight();

            bulletSheet = ImageIO.read(new File("Graphic/Snowpea_projectile.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SnowProjectile(String path, int x, int y) {
        super(path, x, y);
        this.breakFrames = 4;
    }

    @Override
    public void draw(Graphics g) {
        if (finished) return;

        if (breaking && snowBreakSheet != null) {
            float hitScale = 1.08f;
            int drawW = (int) (snowBreakFrameW / 5 * 0.8 * hitScale);
            int drawH = (int) (snowBreakFrameH / 5 * 0.8 * hitScale);
            int srcX = breakFrame * snowBreakFrameW;

            int cx = x + drawW / 2;
            int bottom = y + drawH + 23;
            ShadowRenderer.draw((Graphics2D) g, cx, bottom, drawW, 0.6f);

            int bulletW = (int) (frameWidth / 5 * 0.6f);
            int bulletH = (int) (frameHeight / 5 * 0.6f);
            int drawY = y - (drawH - bulletH) / 2;
            int drawX = x - (drawW - bulletW) / 2;

            g.drawImage(snowBreakSheet, drawX, drawY, drawX + drawW, drawY + drawH, srcX, 0, srcX + snowBreakFrameW, snowBreakFrameH, null);
        } else {
            float bulletScale = 0.6f;
            int drawW = (int) (frameWidth / 5 * bulletScale);
            int drawH = (int) (frameHeight / 5 * bulletScale);
            
            int cx = x + drawW / 2;
            int bottom = y + drawH + 23;
            ShadowRenderer.draw((Graphics2D) g, cx, bottom, drawW, 0.6f);

            g.drawImage(bulletSheet, x, y, x + drawW, y + drawH, 0, 0, frameWidth, frameHeight, null);
        }
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, (int)(frameWidth / 5 * 0.6f), (int)(frameHeight / 5 * 0.6f));
    }
}
