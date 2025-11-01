package se233.kellion.util;

import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import se233.kellion.model.Bullet;

import java.util.Iterator;
import java.util.List;

/**
 * Utility class for handling all bullet list-related game logic.
 */
public class BulletManager {
    /**
     * Update all bullets: move, and remove those out of bounds.
     */
    public static void updateBullets(List<Bullet> bullets, Pane root, int width, int height) {
        Iterator<Bullet> iterator = bullets.iterator();
        while (iterator.hasNext()) {
            Bullet b = iterator.next();
            b.update();
            if (b.isOutOfBounds(width, height)) {
                root.getChildren().remove(b.getView());
                iterator.remove();
            }
        }
    }

    /**
     * Spawn a player bullet (horizontal).
     * @param bullets List to insert into
     * @param root Game Pane
     * @param x X position
     * @param y Y position
     * @param bulletSprite Sprite for bullet
     * @param speed Bullet speed (positive = right, negative = left)
     * @param facingRight Direction
     */
    public static void firePlayerBullet(List<Bullet> bullets, Pane root,
                                        double x, double y, ImageView bulletSprite, int speed, boolean facingRight) {
        bulletSprite.setX(x);
        bulletSprite.setY(y);
        bulletSprite.setScaleX(facingRight ? 1 : -1);
        Bullet bullet = new Bullet(bulletSprite, speed, false);
        bullets.add(bullet);
        root.getChildren().add(bulletSprite);
    }

    /**
     * Spawn a boss bullet, possibly aimed (uses vx/vy internally on the Bullet).
     * @param bossBullets List to insert into
     * @param root Game Pane
     * @param x Source X
     * @param y Source Y
     * @param targetX Target X (usually player)
     * @param targetY Target Y (usually player)
     * @param speed Bullet speed
     * @param bulletSprite Sprite for bullet
     */
    public static void fireBossBulletAimed(List<Bullet> bossBullets, Pane root,
                                           double x, double y, double targetX, double targetY, double speed, ImageView bulletSprite) {
        bulletSprite.setX(x);
        bulletSprite.setY(y);

        double dx = targetX - x;
        double dy = targetY - y;
        double len = Math.hypot(dx, dy);
        if (len == 0) len = 1;
        double vx = (dx / len) * speed;
        double vy = (dy / len) * speed;

        bulletSprite.setScaleX(vx >= 0 ? 1 : -1);
        Bullet b = new Bullet(bulletSprite, vx, vy, true);
        bossBullets.add(b);
        root.getChildren().add(bulletSprite);
    }
}
