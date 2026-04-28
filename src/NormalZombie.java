import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

class NormalZombie extends Zombie {
    // === Sprite sheet thường ===
    private BufferedImage normalSheet;
    private int normalFrameW, normalFrameH;
    private final int NORMAL_FRAMES = 7;

    // === Sprite sheet nửa thân (halfZombie.png: 1184×210, 7 frame) ===
    private static BufferedImage halfSheet;
    private static int halfFrameW, halfFrameH;
    private static final int HALF_FRAMES = 7;

    // === Sprite sheet ăn (zombieEating.png: 1223×204, 7 frame) ===
    private static BufferedImage eatingSheet;
    private static int eatingFrameW, eatingFrameH;
    private static final int EATING_FRAMES = 7;

    // === Sprite sheet ăn nửa thân ===
    private static BufferedImage halfEatingSheet;
    private static int halfEatingFrameW, halfEatingFrameH;
    private static final int HALF_EATING_FRAMES = 7;

    // === Sprite sheet chết (die_normalZombie.png: 1839×135, 9 frame) ===
    private static BufferedImage dieSheet;
    private static int dieFrameW, dieFrameH;
    private static final int DIE_FRAMES = 9;

    // === Sprite sheet chết nổ (explosiveDeath.png: 492 × 402, 2 frame) ===
    private static BufferedImage explosiveDieSheet;
    private static int explosiveDieFrameW, explosiveDieFrameH;
    private static final int EXPLOSIVE_DIE_FRAMES = 2;

    static {
        try {
            dieSheet = ImageIO.read(new File("Graphic/die_normalZombie.png"));
            dieFrameW = dieSheet.getWidth() / DIE_FRAMES;
            dieFrameH = dieSheet.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            explosiveDieSheet = ImageIO.read(new File("Graphic/explosiveDeath.png"));
            explosiveDieFrameW = explosiveDieSheet.getWidth() / EXPLOSIVE_DIE_FRAMES;
            explosiveDieFrameH = explosiveDieSheet.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            halfSheet = ImageIO.read(new File("Graphic/halfZombie.png"));
            halfFrameW = halfSheet.getWidth() / HALF_FRAMES; // 1184/7 ≈ 169
            halfFrameH = halfSheet.getHeight(); // 210
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            eatingSheet = ImageIO.read(new File("Graphic/zombieEating.png"));
            eatingFrameW = eatingSheet.getWidth() / EATING_FRAMES; // 1223/7 ≈ 174
            eatingFrameH = eatingSheet.getHeight(); // 204
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            halfEatingSheet = ImageIO.read(new File("Graphic/halfZombieEating.png"));
            halfEatingFrameW = halfEatingSheet.getWidth() / HALF_EATING_FRAMES;
            halfEatingFrameH = halfEatingSheet.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isHalf = false;
    private boolean isEating = false;
    protected boolean isDying = false;
    private boolean isExplosiveDying = false;
    private boolean isFinished = false;
    protected int currentFrame = 0;
    private long lastTime;
    private long deathStartTime; // thời điểm bắt đầu chết
    private float alpha = 1.0f; // độ trong suốt khi biến mất

    // Tham chiếu đến cây đang bị ăn
    private Plant targetPlant = null;
    // Đếm thời gian giữa các lần cắn
    private int biteTimer = 0;
    private static final int BITE_INTERVAL = 14; // giảm 30% thời gian cắn (từ 20 xuống 14)
    private boolean didBite = false; // flag cho GamePanel biết vừa cắn → play sound

    // Kích thước render — tính từ normal sheet, giữ nguyên luôn
    protected int drawW, drawH;

    public int getDrawW() { return drawW; }
    public int getDrawH() { return drawH; }

    // Override sheets cho các subclass như Conehead, Football
    protected BufferedImage overrideWalkSheet = null;
    protected int overrideWalkFrameW, overrideWalkFrameH;
    protected BufferedImage overrideEatSheet = null;
    protected int overrideEatFrameW, overrideEatFrameH;
    protected int overrideWalkFrames = -1;
    protected int overrideEatFrames = -1;
    protected float speedMultiplier = 1.0f;
    protected float moveAccumulator = 0f;

    protected BufferedImage overrideDieSheet = null;
    protected int overrideDieFrameW, overrideDieFrameH;
    protected int overrideDieFrames = -1;

    protected int deadOffsetX = 10;
    protected int deadOffsetY = 39;
    protected int drawOffsetY = 0;
    protected int drawOffsetX = 0;
    protected int shadowOffsetX = 0;

    protected float eatSpeedMultiplier = 1.0f;
    protected long dieFrameDuration = 150;
    protected long dieFadeDuration = 1000;
    protected float liveScale = 1.0f;
    protected float dieScale = 1.0f;

    // Slow effect
    protected boolean isSlowed = false;
    protected int shadowOffsetY = -5;
    protected long slowStartTime = 0;
    protected static final long SLOW_DURATION = 10000;
    protected int moveCounter = 0;

    public NormalZombie(String path, int x, int y) {
        super(x, y, 100);
        try {
            normalSheet = ImageIO.read(new File(path));
            normalFrameW = normalSheet.getWidth() / NORMAL_FRAMES;
            normalFrameH = normalSheet.getHeight();
            frameWidth = normalFrameW;
            frameHeight = normalFrameH;
        } catch (Exception e) {
            e.printStackTrace();
        }
        drawW = 2 * normalFrameW / 3;
        drawH = 2 * normalFrameH / 3;
        lastTime = System.currentTimeMillis();
    }

    /** GamePanel gọi để kiểm tra vừa cắn → play sound rồi reset */
    public boolean consumeBite() {
        if (didBite) {
            didBite = false;
            return true;
        }
        return false;
    }

    public void checkHalfTransition() {
        if (!isHalf && health <= 50 && !isExplosiveDying) {
            isHalf = true;
            if (isEating) {
                currentFrame = currentFrame % HALF_EATING_FRAMES;
            } else {
                currentFrame = currentFrame % HALF_FRAMES;
            }
        }
    }

    /** Chết do nổ bom */
    public void dieFromExplosion() {
        if (!isDying && !isExplosiveDying) {
            health = 0;
            isExplosiveDying = true;
            deathStartTime = System.currentTimeMillis();
            currentFrame = 0;
            lastTime = System.currentTimeMillis();
            stopEating();
        }
    }

    /** Bắt đầu ăn cây */
    public void startEating(Plant plant) {
        if (!isEating) {
            isEating = true;
            targetPlant = plant;
            biteTimer = 0;
            // Giữ nguyên currentFrame, chỉ mod theo eating frames
            if (isHalf) {
                currentFrame = currentFrame % HALF_EATING_FRAMES;
            } else {
                currentFrame = currentFrame % EATING_FRAMES;
            }
        }
    }

    /** Dừng ăn — quay về trạng thái đi bộ */
    public void stopEating() {
        isEating = false;
        targetPlant = null;
        biteTimer = 0;
        // Giữ nguyên currentFrame, mod theo walking frames
        if (isHalf) {
            currentFrame = currentFrame % HALF_FRAMES;
        } else {
            currentFrame = currentFrame % NORMAL_FRAMES;
        }
    }

    public boolean isEating() {
        return isEating;
    }

    public Plant getTargetPlant() {
        return targetPlant;
    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }

    public void applySlow() {
        isSlowed = true;
        slowStartTime = System.currentTimeMillis();
    }

    public void removeSlow() {
        isSlowed = false;
    }

    public void update() {
        if (isSlowed && (System.currentTimeMillis() - slowStartTime > SLOW_DURATION)) {
            isSlowed = false;
        }

        if (health <= 0 && !isDying && !isExplosiveDying) {
            isDying = true;
            deathStartTime = System.currentTimeMillis();
            currentFrame = 0;
            lastTime = System.currentTimeMillis();
            stopEating(); // ngừng ăn nếu đang ăn
        }

        if (isExplosiveDying) {
            long elapsed = System.currentTimeMillis() - deathStartTime;

            long frameDuration = 750; // 1500ms cho 2 frame
            long animDuration = EXPLOSIVE_DIE_FRAMES * frameDuration;

            if (currentFrame < EXPLOSIVE_DIE_FRAMES - 1) {
                if (System.currentTimeMillis() - lastTime > frameDuration) {
                    currentFrame++;
                    lastTime = System.currentTimeMillis();
                }
            } else {
                long holdDuration = 0;
                long fadeDuration = 1000; // fade away đi trong 1000ms

                if (elapsed > animDuration + holdDuration) {
                    float fadeProgress = (float) (elapsed - (animDuration + holdDuration)) / fadeDuration;
                    alpha = Math.max(0.0f, 1.0f - fadeProgress);
                    if (alpha <= 0) {
                        alpha = 0;
                        isFinished = true;
                    }
                }
            }
            return;
        }

        if (isDying) {
            long elapsed = System.currentTimeMillis() - deathStartTime;

            int totalDieFrames = overrideDieFrames != -1 ? overrideDieFrames : DIE_FRAMES;
            // Animation chạy frames (giả sử dieFrameDuration cho mượt)
            long animDuration = totalDieFrames * dieFrameDuration;

            if (currentFrame < totalDieFrames - 1) {
                if (System.currentTimeMillis() - lastTime > dieFrameDuration) {
                    currentFrame++;
                    lastTime = System.currentTimeMillis();
                }
            } else {
                long holdDuration = 0;

                if (elapsed > animDuration + holdDuration) {
                    float fadeProgress = (float) (elapsed - (animDuration + holdDuration)) / dieFadeDuration;
                    alpha = Math.max(0.0f, 1.0f - fadeProgress);
                    if (alpha <= 0) {
                        alpha = 0;
                        isFinished = true;
                    }
                }
            }
            return;
        }

        if (isEating) {
            int totalFrames = overrideEatFrames != -1 ? overrideEatFrames : (isHalf ? HALF_EATING_FRAMES : EATING_FRAMES);
            long frameTime = eatSpeedMultiplier > 0 ? (long) ((isSlowed ? 280 : 140) / eatSpeedMultiplier) : Long.MAX_VALUE;
            if (System.currentTimeMillis() - lastTime > frameTime) {
                currentFrame = (currentFrame + 1) % totalFrames;
                lastTime = System.currentTimeMillis();
            }

            // Cắn cây
            int biteInterval = eatSpeedMultiplier > 0 ? (int) ((isSlowed ? BITE_INTERVAL * 2 : BITE_INTERVAL) / eatSpeedMultiplier) : Integer.MAX_VALUE;
            biteTimer++;
            if (biteTimer >= biteInterval) {
                biteTimer = 0;
                if (targetPlant != null) {
                    targetPlant.takeDamage(10);
                    didBite = true;
                    if (targetPlant.isDead()) {
                        stopEating();
                    }
                }
            }
        } else {
            // Đi bộ bình thường
            float baseSpeed = 0.5f * speedMultiplier;
            float actualSpeed = isSlowed ? baseSpeed / 2f : baseSpeed;
            moveAccumulator += actualSpeed;
            if (moveAccumulator >= 1.0f) {
                int pixels = (int) moveAccumulator;
                x -= pixels;
                moveAccumulator -= pixels;
                if (x < -200) {
                    x = 800;
                }
            }
            int totalFrames = overrideWalkFrames != -1 ? overrideWalkFrames : (isHalf ? HALF_FRAMES : NORMAL_FRAMES);
            long walkFrameTime = speedMultiplier > 0 ? (long) ((isSlowed ? 400 : 200) / speedMultiplier) : Long.MAX_VALUE;
            if (System.currentTimeMillis() - lastTime > walkFrameTime) {
                currentFrame = (currentFrame + 1) % totalFrames;
                lastTime = System.currentTimeMillis();
            }
        }
    }

    public void draw(Graphics g) {
        // Chọn sheet và frame source phù hợp
        BufferedImage activeSheet;
        int srcFW, srcFH;

        if (isEating) {
            // Đang ăn
            if (overrideEatSheet != null) {
                activeSheet = overrideEatSheet;
                srcFW = overrideEatFrameW;
                srcFH = overrideEatFrameH;
            } else if (isHalf && halfEatingSheet != null) {
                activeSheet = halfEatingSheet;
                srcFW = halfEatingFrameW;
                srcFH = halfEatingFrameH;
            } else if (eatingSheet != null) {
                activeSheet = eatingSheet;
                srcFW = eatingFrameW;
                srcFH = eatingFrameH;
            } else {
                // fallback nếu không load được
                activeSheet = normalSheet;
                srcFW = normalFrameW;
                srcFH = normalFrameH;
            }
        } else {
            // Đi bộ
            if (overrideWalkSheet != null) {
                activeSheet = overrideWalkSheet;
                srcFW = overrideWalkFrameW;
                srcFH = overrideWalkFrameH;
            } else if (isHalf && halfSheet != null) {
                activeSheet = halfSheet;
                srcFW = halfFrameW;
                srcFH = halfFrameH;
            } else {
                activeSheet = normalSheet;
                srcFW = normalFrameW;
                srcFH = normalFrameH;
            }
        }

        int currentDrawW = drawW;
        int currentDrawH = drawH;

        // Nếu dùng override sheet, tính toán lại kích thước hiển thị
        if ((isEating && overrideEatSheet != null) || (!isEating && overrideWalkSheet != null)) {
            // Cao bằng normal zombie * liveScale, giữ nguyên tỉ lệ
            currentDrawH = (int) (drawH * liveScale);
            currentDrawW = (int) ((srcFW * currentDrawH) / srcFH);
        }

        // Nếu đang chết nổ
        if (isExplosiveDying && explosiveDieSheet != null) {
            activeSheet = explosiveDieSheet;
            srcFW = explosiveDieFrameW;
            srcFH = explosiveDieFrameH;
            currentDrawW = (int) (drawW * 0.7f);
            currentDrawH = (int) (drawH * 0.7f);
        } else if (isDying && (overrideDieSheet != null || dieSheet != null)) {
            if (overrideDieSheet != null) {
                activeSheet = overrideDieSheet;
                srcFW = overrideDieFrameW;
                srcFH = overrideDieFrameH;
            } else {
                activeSheet = dieSheet;
                srcFW = dieFrameW;
                srcFH = dieFrameH;
            }
            // Dùng tỉ lệ scale đồng nhất với zombie bình thường để giữ nguyên tỉ lệ của sheet mới
            float scale = ((float) drawH / normalFrameH) * dieScale;
            currentDrawW = (int) (srcFW * scale);
            currentDrawH = (int) (srcFH * scale);
        }

        int srcX = currentFrame * srcFW;

        // --- render zombie ra offscreen image (dùng kích thước hiện tại) ---
        BufferedImage tmp = new BufferedImage(Math.max(1, currentDrawW), Math.max(1, currentDrawH),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D tg = tmp.createGraphics();

        // Áp dụng alpha nếu đang dying
        if ((isDying || isExplosiveDying) && alpha < 1.0f) {
            tg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }

        tg.drawImage(activeSheet, 0, 0, currentDrawW, currentDrawH,
                srcX, 0, srcX + srcFW, srcFH, null);

        // Phủ overlay màu xanh navi nếu bị slow
        if (isSlowed && !isExplosiveDying && !isDying) {
            tg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.4f));
            tg.setColor(new Color(0, 0, 128)); // Navy
            tg.fillRect(0, 0, currentDrawW, currentDrawH);
        }

        // nháy sáng: phủ trắng lên đúng hình dáng sprite
        if (!isDying && !isExplosiveDying && isFlashing()) {
            tg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.65f));
            tg.setColor(Color.WHITE);
            tg.fillRect(0, 0, currentDrawW, currentDrawH);
        }
        tg.dispose();

        if (!isDying && !isExplosiveDying) {
            int cx = x + drawW / 2 + 3 + shadowOffsetX;
            int bottom = y + drawH + shadowOffsetY;
            ShadowRenderer.draw((Graphics2D) g, cx, bottom, drawW, 0.5f, 0.55f);
        }

        // Nếu đang chết, di chuyển xuống thấp hơn 35px
        int drawY = y + drawOffsetY;
        int drawX = x + drawOffsetX;
        if (isExplosiveDying) {
            drawY = y + drawH - currentDrawH + 10;
            drawX = x + (drawW - currentDrawW) / 2;
        } else if (isDying) {
            drawY = y + deadOffsetY;
            drawX = x + deadOffsetX;
        }
        g.drawImage(tmp, drawX, drawY, null);
    }
}
