import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class Pole extends NormalZombie {
    private static BufferedImage r1, r2, j1, j2, w1, w2, e1, e2, dieSheet;
    private static int r1W, r1H, r2W, r2H, j1W, j1H, j2W, j2H;
    private static int w1W, w1H, w2W, w2H, e1W, e1H, e2W, e2H, dieW, dieH;

    private boolean hasJumped = false;
    private boolean isJumping = false;
    private long jumpStartTime = 0;
    private final long JUMP_DURATION = 1000;
    private float startJumpX;
    private float targetJumpX;

    static {
        try {
            r1 = ImageIO.read(new File("Graphic/PoleVaulting_1__run.png"));
            r1W = r1.getWidth() / 8; r1H = r1.getHeight();

            r2 = ImageIO.read(new File("Graphic/PoleVaulting_2__run.png"));
            r2W = r2.getWidth() / 8; r2H = r2.getHeight();

            j1 = ImageIO.read(new File("Graphic/PoleVaulting_1__jump.png"));
            j1W = j1.getWidth() / 6; j1H = j1.getHeight();

            j2 = ImageIO.read(new File("Graphic/PoleVaulting_2__jump.png"));
            j2W = j2.getWidth() / 6; j2H = j2.getHeight();

            w1 = ImageIO.read(new File("Graphic/PoleVaulting_1__walk.png"));
            w1W = w1.getWidth() / 7; w1H = w1.getHeight();

            w2 = ImageIO.read(new File("Graphic/PoleVaulting_2__walk.png"));
            w2W = w2.getWidth() / 7; w2H = w2.getHeight();

            e1 = ImageIO.read(new File("Graphic/PoleVaulting_1__eat.png"));
            e1W = e1.getWidth() / 7; e1H = e1.getHeight();

            e2 = ImageIO.read(new File("Graphic/PoleVaulting_2__eat.png"));
            e2W = e2.getWidth() / 7; e2H = e2.getHeight();

            dieSheet = ImageIO.read(new File("Graphic/PoleVaulting_die.png"));
            dieW = dieSheet.getWidth() / 4; dieH = dieSheet.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Pole(String path, int x, int y) {
        super(path, x, y);
        this.health = 200;
        this.speedMultiplier = 2.0f;
        this.shadowOffsetX = 50;
        this.boundsOffsetX = 50;
        this.shadowOffsetY = -8;
        this.dieScale = 0.7f;
    }

    public boolean canJump() {
        return !hasJumped && !isJumping && health > 0;
    }

    public void triggerJump(int plantX) {
        if (canJump()) {
            isJumping = true;
            jumpStartTime = System.currentTimeMillis();
            startJumpX = this.x;
            targetJumpX = plantX - 65;
            stopEating();
            this.speedMultiplier = 0.0f;
            this.currentFrame = 0;
        }
    }

    public boolean isJumping() {
        return isJumping;
    }

    @Override
    public void takeDamage(int amount) {
        if (isJumping) return;
        super.takeDamage(amount);
    }

    @Override
    public Rectangle getEatingBounds() {
        if (!hasJumped && !isJumping && health > 0) {
            return new Rectangle(x + 70, y + 20, 30 + 20, 2 * frameHeight / 3);
        }
        return super.getEatingBounds();
    }

    @Override
    public void update() {
        if (health <= 0) {
            overrideWalkSheet = null;
            overrideWalkFrames = -1;
            overrideEatSheet = null;
            overrideEatFrames = -1;

            overrideDieSheet = dieSheet;
            overrideDieFrameW = dieW;
            overrideDieFrameH = dieH;
            overrideDieFrames = 4;

            super.update();
            return;
        }

        if (isJumping) {
            this.liveScale = 1.0f;
            long elapsed = System.currentTimeMillis() - jumpStartTime;
            if (elapsed >= JUMP_DURATION) {
                isJumping = false;
                hasJumped = true;
                this.speedMultiplier = 1.0f;
                this.x = (int) targetJumpX;
                this.boundsOffsetX = 0;
                this.shadowOffsetX = 0;
                this.drawOffsetY = 3;
            } else {
                float progress = (float) elapsed / JUMP_DURATION;
                this.x = (int) (startJumpX + (targetJumpX - startJumpX) * progress);

                if (health >= 100) {
                    overrideWalkSheet = j1;
                    overrideWalkFrameW = j1W;
                    overrideWalkFrameH = j1H;
                } else {
                    overrideWalkSheet = j2;
                    overrideWalkFrameW = j2W;
                    overrideWalkFrameH = j2H;
                }
                overrideWalkFrames = 6;
                int f = (int) ((progress * 6));
                if (f >= 6) f = 5;
                this.currentFrame = f;
            }
        } else if (!hasJumped) {
            this.liveScale = 1.0f;
            this.speedMultiplier = 2.0f;
            if (health >= 100) {
                overrideWalkSheet = r1;
                overrideWalkFrameW = r1W;
                overrideWalkFrameH = r1H;
            } else {
                overrideWalkSheet = r2;
                overrideWalkFrameW = r2W;
                overrideWalkFrameH = r2H;
            }
            overrideWalkFrames = 8;
        } else {
            this.speedMultiplier = 1.0f;
            this.liveScale = 0.95f;
            if (health >= 100) {
                overrideWalkSheet = w1;
                overrideWalkFrameW = w1W;
                overrideWalkFrameH = w1H;
                overrideWalkFrames = 7;

                overrideEatSheet = e1;
                overrideEatFrameW = e1W;
                overrideEatFrameH = e1H;
                overrideEatFrames = 7;
            } else {
                overrideWalkSheet = w2;
                overrideWalkFrameW = w2W;
                overrideWalkFrameH = w2H;
                overrideWalkFrames = 7;

                overrideEatSheet = e2;
                overrideEatFrameW = e2W;
                overrideEatFrameH = e2H;
                overrideEatFrames = 7;
            }
        }

        super.update();
    }
}
