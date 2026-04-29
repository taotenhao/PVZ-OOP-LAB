import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

class SoilEffect {
    private static BufferedImage sheet;
    private static final int TOTAL_FRAMES = 4;
    private static int srcFrameW, srcFrameH;

    private int x, y;
    private int drawW, drawH;
    private int currentFrame = 0;
    private long lastFrameTime;
    private boolean finished = false;

    static {
        try {
            sheet = ImageIO.read(new File("Graphic/soilPlant.png"));
            srcFrameW = sheet.getWidth() / TOTAL_FRAMES;
            srcFrameH = sheet.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SoilEffect(int cx, int cy, int peaDrawH) {
        this.drawH = (int) ((peaDrawH / 2) * 1.3);
        this.drawW = drawH * srcFrameW / srcFrameH;

        this.x = cx - drawW / 2;
        this.y = cy - drawH;

        lastFrameTime = System.currentTimeMillis();
    }

    public void update() {
        if (finished)
            return;
        if (System.currentTimeMillis() - lastFrameTime > 60) {
            currentFrame++;
            lastFrameTime = System.currentTimeMillis();
            if (currentFrame >= TOTAL_FRAMES) {
                finished = true;
            }
        }
    }

    public void draw(Graphics2D g) {
        if (finished || sheet == null)
            return;
        int srcX = currentFrame * srcFrameW;
        g.drawImage(sheet, x, y, x + drawW, y + drawH,
                srcX, 0, srcX + srcFrameW, srcFrameH, null);
    }

    public boolean isFinished() {
        return finished;
    }
}
