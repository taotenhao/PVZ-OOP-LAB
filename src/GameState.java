import java.awt.*;
import java.awt.event.MouseEvent;

public interface GameState {

    void update(GamePanel panel);

    void draw(Graphics2D g, GamePanel panel);

    void handleClick(int mx, int my, GamePanel panel);

    default void handleMouseMove(int mx, int my, GamePanel panel) {}

    default void onEnter(GamePanel panel) {}

    default void onExit(GamePanel panel) {}
}
