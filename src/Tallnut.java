import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

class Tallnut extends Plant {
    private BufferedImage idleSheet1;
    private BufferedImage idleSheet2;
    private BufferedImage idleSheet3;

    private final int IDLE_FRAMES = 9;
    private int[] srcFrameW = new int[3];
    private int[] srcFrameH = new int[3];
    private int[] stateDrawW = new int[3];

    private int drawH;

    private int idleFrame = 0;
    private long lastIdleTime;

    private static final int MAX_HEALTH = 1600;

    public Tallnut(int x, int y) {
        super(x, y, MAX_HEALTH);
        try {
            idleSheet1 = ImageIO.read(new File("Graphic/tallnut1.png"));
            srcFrameW[0] = idleSheet1.getWidth() / IDLE_FRAMES;
            srcFrameH[0] = idleSheet1.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            idleSheet2 = ImageIO.read(new File("Graphic/tallnut2.png"));
            srcFrameW[1] = idleSheet2.getWidth() / IDLE_FRAMES;
            srcFrameH[1] = idleSheet2.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            idleSheet3 = ImageIO.read(new File("Graphic/tallnut3.png"));
            srcFrameW[2] = idleSheet3.getWidth() / IDLE_FRAMES;
            srcFrameH[2] = idleSheet3.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }

        drawH = 93;
        for (int i = 0; i < 3; i++) {
            stateDrawW[i] = (int) (srcFrameW[i] * ((float) drawH / srcFrameH[i]));
        }

        frameWidth = srcFrameW[0];
        frameHeight = srcFrameH[0];

        lastIdleTime = System.currentTimeMillis();
    }

    private int getState() {
        float hpPercent = (float) health / MAX_HEALTH;
        if (hpPercent > 0.66f)
            return 0;
        if (hpPercent > 0.33f)
            return 1;
        return 2;
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Graphics g) {
        long now = System.currentTimeMillis();

        if (now - lastIdleTime > 200) {
            idleFrame = (idleFrame + 1) % IDLE_FRAMES;
            lastIdleTime = now;
        }

        int state = getState();
        BufferedImage activeSheet;
        int srcFW, srcFH, currentDrawW;

        switch (state) {
            case 1:
                activeSheet = idleSheet2;
                srcFW = srcFrameW[1];
                srcFH = srcFrameH[1];
                currentDrawW = stateDrawW[1];
                break;
            case 2:
                activeSheet = idleSheet3;
                srcFW = srcFrameW[2];
                srcFH = srcFrameH[2];
                currentDrawW = stateDrawW[2];
                break;
            default:
                activeSheet = idleSheet1;
                srcFW = srcFrameW[0];
                srcFH = srcFrameH[0];
                currentDrawW = stateDrawW[0];
                break;
        }

        int drawY = y + 62 - drawH + 2;

        ShadowRenderer.draw((Graphics2D) g, x + currentDrawW / 2, y + 62 - 8, currentDrawW, 1.0f);

        int srcX = idleFrame * srcFW;

        BufferedImage tmp = new BufferedImage(currentDrawW, drawH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D tg = tmp.createGraphics();
        tg.drawImage(activeSheet, 0, 0, currentDrawW, drawH,
                srcX, 0, srcX + srcFW, srcFH, null);

        if (isFlashing()) {
            tg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.65f));
            tg.setColor(Color.WHITE);
            tg.fillRect(0, 0, currentDrawW, drawH);
        }
        tg.dispose();

        g.drawImage(tmp, x, drawY, null);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x + 15, y + 62 - drawH, Math.max(1, stateDrawW[getState()] - 15), drawH);
    }
}
