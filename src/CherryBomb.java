import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

class CherryBomb extends Plant {
    // === Idle animation ===
    private BufferedImage idleSheet;
    private int idleFrame = 0;
    private long lastIdleTime;
    private final int IDLE_FRAMES = 6;
    private int idleFrameW, idleFrameH;

    // === Explosion animation ===
    private BufferedImage explodeSheet;
    private int explodeFrame = 0;
    private long lastExplodeTime;
    private final int EXPLODE_FRAMES = 8;
    private int explodeFrameW, explodeFrameH;

    private int drawW, drawH;

    private boolean idleDone = false;
    private boolean exploded = false;
    private boolean explosionConsumed = false;
    private boolean exploding = false;
    private boolean fullyDone = false;

    public CherryBomb(int x, int y) {
        super(x, y, 100);
        try {
            idleSheet = ImageIO.read(new File("Graphic/cherryBomb.png"));
            idleFrameW = idleSheet.getWidth() / IDLE_FRAMES;
            idleFrameH = idleSheet.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            explodeSheet = ImageIO.read(new File("Graphic/explode.png"));
            explodeFrameW = explodeSheet.getWidth() / EXPLODE_FRAMES;
            explodeFrameH = explodeSheet.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }

        drawH = 62;
        drawW = (int) (idleFrameW * ((float) drawH / idleFrameH));

        frameWidth = idleFrameW;
        frameHeight = idleFrameH;

        lastIdleTime = System.currentTimeMillis();
    }

    @Override
    public void update() {
        long now = System.currentTimeMillis();

        if (!idleDone) {
            if (now - lastIdleTime > 200) {
                idleFrame++;
                lastIdleTime = now;
                if (idleFrame >= IDLE_FRAMES) {
                    idleDone = true;
                    exploded = true;
                    exploding = true;
                    explodeFrame = 0;
                    lastExplodeTime = now;
                }
            }
        } else if (exploding) {
            if (now - lastExplodeTime > 80) {
                explodeFrame++;
                lastExplodeTime = now;
                if (explodeFrame >= EXPLODE_FRAMES) {
                    exploding = false;
                    fullyDone = true;
                }
            }
        }
    }

    public boolean consumeExplosion() {
        if (exploded && !explosionConsumed) {
            explosionConsumed = true;
            return true;
        }
        return false;
    }

    public boolean isFullyDone() {
        return fullyDone;
    }

    @Override
    public void draw(Graphics g) {
        if (fullyDone)
            return;

        if (!idleDone) {
            int idW = (int) (drawW * 1.3f);
            int idH = (int) (drawH * 1.3f);
            int offX = (idW - drawW) / 2;
            int offY = (idH - drawH) / 2;

            ShadowRenderer.draw((Graphics2D) g, x + drawW / 2, y + drawH - 8, idW, 1.0f);

            int srcX = idleFrame * idleFrameW;
            BufferedImage tmp = new BufferedImage(idW, idH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D tg = tmp.createGraphics();
            tg.drawImage(idleSheet, 0, 0, idW, idH,
                    srcX, 0, srcX + idleFrameW, idleFrameH, null);

            if (isFlashing()) {
                tg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.65f));
                tg.setColor(Color.WHITE);
                tg.fillRect(0, 0, idW, idH);
            }
            tg.dispose();
            g.drawImage(tmp, x - offX, y - offY, null);
        } else if (exploding && explodeSheet != null) {
            int srcX = explodeFrame * explodeFrameW;
            int exW = GamePanel.CELL_W * 3;
            int exH = GamePanel.CELL_H * 3;
            int centerX = x + drawW / 2;
            int centerY = y + drawH / 2;
            int ex = centerX - exW / 2;
            int ey = centerY - exH / 2;

            g.drawImage(explodeSheet, ex, ey, ex + exW, ey + exH,
                    srcX, 0, srcX + explodeFrameW, explodeFrameH, null);
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x + 15, y, Math.max(1, drawW - 15), drawH);
    }
}
