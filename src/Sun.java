import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

class Sun {
    enum State {
        FALLING, BOUNCING, LANDED, FLYING, COLLECTED
    }

    private static BufferedImage sheet;
    private static int frameW, frameH;
    private static final int TOTAL_FRAMES = 2;

    static {
        try {
            sheet = ImageIO.read(new File("Graphic/sun.png"));
            frameW = sheet.getWidth() / TOTAL_FRAMES;
            frameH = sheet.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private float x, y;
    private float targetX, targetY;
    private State state;
    private int currentFrame = 0;
    private long lastFrameTime;
    private float landY;
    private float glowPhase;

    private float velY = 0;
    private static final float GRAVITY = 0.15f;

    private int value;

    static final int DRAW_W = 42;
    static final int DRAW_H = 42;
    private static final float FALL_SPEED = 2;

    public Sun(float x, float landY) {
        this.x = x;
        this.y = -DRAW_H;
        this.landY = landY;
        this.state = State.FALLING;
        this.value = 25;
        this.lastFrameTime = System.currentTimeMillis();
        this.glowPhase = (float) (Math.random() * 2 * Math.PI);
    }

    public Sun(float sx, float sy, float landY, int value) {
        this.x = sx;
        this.y = sy;
        this.landY = landY;
        this.state = State.BOUNCING;
        this.value = value;
        this.velY = -3.0f;
        this.lastFrameTime = System.currentTimeMillis();
        this.glowPhase = (float) (Math.random() * 2 * Math.PI);
    }

    public int getValue() { return value; }

    public void update() {
        long now = System.currentTimeMillis();

        if (now - lastFrameTime > 250) {
            currentFrame = (currentFrame + 1) % TOTAL_FRAMES;
            lastFrameTime = now;
        }

        glowPhase += 0.07f;
        if (glowPhase > (float) (2 * Math.PI))
            glowPhase -= (float) (2 * Math.PI);

        switch (state) {
            case FALLING:
                y += FALL_SPEED;
                if (y >= landY) {
                    y = landY;
                    state = State.LANDED;
                }
                break;
            case BOUNCING:
                velY += GRAVITY;
                y += velY;
                if (y >= landY) {
                    y = landY;
                    state = State.LANDED;
                }
                break;
            case LANDED:
                break;
            case FLYING:
                float dx = targetX - x;
                float dy = targetY - y;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist < 10) {
                    state = State.COLLECTED;
                } else {
                    float flySpeed = Math.max(14f, dist * 0.1f);
                    x += dx / dist * flySpeed;
                    y += dy / dist * flySpeed;
                }
                break;
            case COLLECTED:
                break;
        }
    }

    public void draw(Graphics2D g) {
        if (state == State.COLLECTED || sheet == null)
            return;

        int drawX = (int) x;
        int drawY = (int) y;

        float glowAlpha = 0.15f + 0.12f * (float) Math.sin(glowPhase);

        int glow1 = 10;
        g.setColor(new Color(255, 230, 30, Math.min(255, (int) (glowAlpha * 220))));
        g.fillOval(drawX - glow1, drawY - glow1, DRAW_W + 2 * glow1, DRAW_H + 2 * glow1);

        int glow2 = 5;
        g.setColor(new Color(255, 255, 80, Math.min(255, (int) (glowAlpha * 350))));
        g.fillOval(drawX - glow2, drawY - glow2, DRAW_W + 2 * glow2, DRAW_H + 2 * glow2);

        int srcX = currentFrame * frameW;
        g.drawImage(sheet, drawX, drawY, drawX + DRAW_W, drawY + DRAW_H,
                srcX, 0, srcX + frameW, frameH, null);
    }

    public boolean contains(int mx, int my) {
        int pad = 10;
        return mx >= x - pad && mx <= x + DRAW_W + pad
                && my >= y - pad && my <= y + DRAW_H + pad;
    }

    public void collect(float targetX, float targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
        this.state = State.FLYING;
    }

    public boolean isCollected() {
        return state == State.COLLECTED;
    }

    public boolean isClickable() {
        return state == State.FALLING || state == State.BOUNCING || state == State.LANDED;
    }
}
