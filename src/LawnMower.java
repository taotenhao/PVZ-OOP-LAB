import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

class LawnMower {
    private float realX, realY;
    int x, y;

    private int lane;

    private static final int IMG_W = 100;
    private static final int IMG_H = 85;

    private int drawW, drawH;

    private static BufferedImage mowerImg;
    static {
        try {
            mowerImg = ImageIO.read(new File("Graphic/lawnMower.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // === Trạng thái ===
    private enum State { ENTERING, IDLE, ACTIVATED, GONE }
    private State state = State.ENTERING;

    private float targetX;
    private long enterStartTime;
    private long enterDuration;

    private float startX;

    private static final float ACTIVATED_SPEED = 10.5f;

    public LawnMower(int lane, int cellH, int gridLeft, int gridTop) {
        this.lane = lane;

        drawH = cellH / 2;
        drawW = (int) (IMG_W * ((float) drawH / IMG_H));

        float centerY = gridTop + lane * cellH + (cellH - drawH) / 2f;
        this.realY = centerY;
        this.y = (int) realY;

        this.targetX = gridLeft - drawW;

        this.startX = -drawW - 10;
        this.realX = startX;
        this.x = (int) realX;
    }

    public void startEntering(long startTime, long duration) {
        this.enterStartTime = startTime;
        this.enterDuration = duration;
        this.state = State.ENTERING;
    }

    public void activate() {
        if (state == State.IDLE) {
            state = State.ACTIVATED;
        }
    }

    public boolean isIdle() { return state == State.IDLE; }
    public boolean isActivated() { return state == State.ACTIVATED; }
    public boolean isGone() { return state == State.GONE; }
    public int getLane() { return lane; }

    public void update() {
        switch (state) {
            case ENTERING -> {
                long now = System.currentTimeMillis();
                float progress = (float) (now - enterStartTime) / enterDuration;
                if (progress >= 1.0f) {
                    realX = targetX;
                    state = State.IDLE;
                } else {
                    float eased = 1.0f - (1.0f - progress) * (1.0f - progress);
                    realX = startX + (targetX - startX) * eased;
                }
                x = (int) realX;
            }
            case IDLE -> {}
            case ACTIVATED -> {
                realX += ACTIVATED_SPEED;
                x = (int) realX;
                if (x > 850) {
                    state = State.GONE;
                }
            }
            case GONE -> {}
        }
    }

    public void draw(Graphics g) {
        if (state == State.GONE) return;
        if (mowerImg == null) return;

        Graphics2D g2 = (Graphics2D) g;

        int cx = x + drawW / 2;
        int bottom = y + drawH;
        ShadowRenderer.draw(g2, cx + 3, bottom, drawW, 1.5f, 0.55f);

        g2.drawImage(mowerImg, x, y, x + drawW, y + drawH,
                0, 0, mowerImg.getWidth(), mowerImg.getHeight(), null);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, drawW, drawH);
    }

    public int getDrawW() { return drawW; }
    public int getDrawH() { return drawH; }
}
