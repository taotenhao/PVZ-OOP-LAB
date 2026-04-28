import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

class Projectile {
    protected int x, y, frameWidth, frameHeight, health;
    private BufferedImage sheet;
    private int currentFrame = 0;
    private final int TOTAL_FRAMES = 1;

    // Break animation
    protected static BufferedImage breakSheet;
    protected static int breakFrameW, breakFrameH;
    protected int breakFrames = 3;

    static {
        try {
            breakSheet = ImageIO.read(new File("Graphic/projectile_peashooterBreak.png"));
            breakFrameW = breakSheet.getWidth() / 3; // 566 / 3
            breakFrameH = breakSheet.getHeight(); // 441
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected float realX;
    protected boolean breaking = false;
    protected boolean finished = false;
    protected int breakFrame = 0;
    protected long lastBreakTime = 0;
    private Plant lastTorchwood = null;

    public Projectile(String path, int x, int y) {
        this.x = x;
        this.realX = x;
        this.y = y;
        this.health = 1;
        try {
            File f = new File(path);
            sheet = ImageIO.read(f);
            this.frameWidth = sheet.getWidth() / TOTAL_FRAMES;
            this.frameHeight = sheet.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hit() {
        if (!breaking) {
            breaking = true;
            breakFrame = 0;
            lastBreakTime = System.currentTimeMillis();
        }
    }

    public boolean isBreaking() { return breaking; }
    public boolean isFinished() { return finished; }

    public void update() {
        if (breaking) {
            if (System.currentTimeMillis() - lastBreakTime > 80) { // 80ms per frame
                breakFrame++;
                lastBreakTime = System.currentTimeMillis();
                if (breakFrame >= breakFrames) {
                    finished = true;
                }
            }
        } else {
            realX += 7.5f;
            x = (int) realX;
        }
    }

    public void draw(Graphics g) {
        if (finished) return;

        if (breaking && breakSheet != null) {
            int drawW = (int) (breakFrameW / 5 * 0.8);
            int drawH = (int) (breakFrameH / 5 * 0.8);
            int srcX = breakFrame * breakFrameW;

            int cx = x + drawW / 2;
            int bottom = y + drawH + 23;
            ShadowRenderer.draw((Graphics2D) g, cx, bottom, drawW, 0.6f);

            int bulletW = frameWidth / 5;
            int bulletH = frameHeight / 5;
            int drawY = y - (drawH - bulletH) / 2;
            int drawX = x - (drawW - bulletW) / 2;

            g.drawImage(breakSheet, drawX, drawY, drawX + drawW, drawY + drawH, srcX, 0, srcX + breakFrameW, breakFrameH, null);
        } else {
            int drawW = frameWidth / 5;
            int drawH = frameHeight / 5;
            int srcX = currentFrame * frameWidth;

            int cx = x + drawW / 2;
            int bottom = y + drawH + 23;
            ShadowRenderer.draw((Graphics2D) g, cx, bottom, drawW, 0.6f);

            g.drawImage(sheet, x, y, x + drawW, y + drawH, srcX, 0, srcX + frameWidth, frameHeight, null);
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, frameWidth / 5, frameHeight / 5);
    }

    public Plant getLastTorchwood() {
        return lastTorchwood;
    }

    public void setLastTorchwood(Plant plant) {
        this.lastTorchwood = plant;
    }
}
