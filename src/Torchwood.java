import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

class Torchwood extends Plant {
    private BufferedImage idleSheet;
    private int idleFrame = 0;
    private long lastIdleTime;
    private final int IDLE_FRAMES = 8;
    private int idleFrameW, idleFrameH;

    private int drawW, drawH;

    public Torchwood(int x, int y) {
        super(x, y, 100);
        try {
            idleSheet = ImageIO.read(new File("Graphic/Torchwoods.png"));
            idleFrameW = idleSheet.getWidth() / IDLE_FRAMES;
            idleFrameH = idleSheet.getHeight();

            drawH = 62;
            drawW = (int) (idleFrameW * ((float) drawH / idleFrameH));

            frameWidth = idleFrameW;
            frameHeight = idleFrameH;
        } catch (Exception e) {
            e.printStackTrace();
        }

        lastIdleTime = System.currentTimeMillis();
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Graphics g) {
        long now = System.currentTimeMillis();

        if (now - lastIdleTime > 100) {
            idleFrame = (idleFrame + 1) % IDLE_FRAMES;
            lastIdleTime = now;
        }

        ShadowRenderer.draw((Graphics2D) g, x + 3 + drawW / 2, y + drawH - 8, (int)(drawW * 1.2f), 1.0f);

        int srcX = idleFrame * idleFrameW;

        BufferedImage tmp = new BufferedImage(drawW, drawH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D tg = tmp.createGraphics();
        tg.drawImage(idleSheet, 0, 0, drawW, drawH,
                srcX, 0, srcX + idleFrameW, idleFrameH, null);

        if (isFlashing()) {
            tg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.65f));
            tg.setColor(Color.WHITE);
            tg.fillRect(0, 0, drawW, drawH);
        }
        tg.dispose();

        g.drawImage(tmp, x + 3, y, null);
    }

    public int getX() { return x; }
    public int getY() { return y; }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x + 15, y, Math.max(1, drawW - 15), drawH);
    }
}
