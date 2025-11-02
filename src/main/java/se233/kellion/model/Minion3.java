
package se233.kellion.model;

import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import se233.kellion.util.BulletManager;
import se233.kellion.util.Config;

import java.util.List;

public class Minion3 extends Minion {
    private final Image walkLeft;
    private final Image walkRight;
    private final Image bulletSprite;

    private double x, y;
    private final double patrolL, patrolR;
    private boolean facingRight = false;
    private double speed = 1.2;
    private int dir = 1;

    private long lastShotNanos = 0;
    private final double detectRange   = MinionKind.M3.cfg.detectRange;
    private final long   fireIntervalNs = MinionKind.M3.cfg.fireIntervalNs;

    public Minion3(double x, double y, double patrolL, double patrolR) {
        super(x, y, patrolL, patrolR, MinionKind.M3.skin(), MinionKind.M3.cfg, MinionKind.M3);
        this.x = x;
        this.y = y;
        this.patrolL  = patrolL;
        this.patrolR  = patrolR;

        walkLeft  = new Image(getClass().getResource("/se233/kellion/assets/M3_walk.gif").toExternalForm());
        walkRight = new Image(getClass().getResource("/se233/kellion/assets/M3_walk2.gif").toExternalForm());
        bulletSprite = new Image(getClass().getResource("/se233/kellion/assets/Bullet_M3.gif").toExternalForm());

        ImageView v = super.getView();
        v.setImage(walkLeft);
        v.setPreserveRatio(true);
        v.setFitHeight(MinionKind.M3.skin().fitHeight());
        v.setTranslateX(x);
        v.setTranslateY(y);
        v.setScaleX(1);
    }
    public Minion3(double x, double y, double patrolL, double patrolR, MinionKind kind) {
        this(x, y, patrolL, patrolR);
    }

    @Override
    public void update(Player player, List enemyBullets, Pane root, long now) {
        if (isDead()) return;

        patrolMove();
        applyWalkImageByDirection();

        ImageView v = super.getView();
        v.setTranslateX(x);
        v.setTranslateY(y);

        Bounds pb = player.getHitboxBounds();
        double px = pb.getMinX() + pb.getWidth()  * 0.5;
        double py = pb.getMinY() + pb.getHeight() * 0.5;

        Bounds mb = v.getBoundsInParent();
        double mx = mb.getMinX() + mb.getWidth()  * 0.5;
        double my = mb.getMinY() + mb.getHeight() * 0.5;

        double dx = px - mx;
        double dy = py - my;
        double dist = Math.hypot(dx, dy);

        boolean facingPlayer  = (dx >= 0 && facingRight) || (dx < 0 && !facingRight);
        boolean inDetectRange = dist <= detectRange;

        if (facingPlayer && inDetectRange && (now - lastShotNanos >= fireIntervalNs)) {
            lastShotNanos = now;

            double muzzleX = facingRight
                    ? (mb.getMinX() + mb.getWidth() * 0.80)
                    : (mb.getMinX() + mb.getWidth() * 0.70);
            double muzzleY = mb.getMinY() + mb.getHeight() * 0.45;

            double bulletSpeed = Config.BOSS_BULLET_SPEED;

            ImageView bulletIv = new ImageView(bulletSprite);
            BulletManager.fireBossBulletAimed(
                    enemyBullets, root, muzzleX, muzzleY,
                    px, py,
                    bulletSpeed, bulletIv
            );
            bulletIv.setScaleX(facingRight ? 1 : -1);
        }
    }

    private void patrolMove() {
        x += dir * speed;
        if (x <= patrolL)  { x = patrolL;  dir = 1;  }
        if (x >= patrolR)  { x = patrolR;  dir = -1; }
    }

    private void applyWalkImageByDirection() {
        ImageView v = super.getView();
        facingRight = (dir >= 0);

        if (facingRight) {
            if (v.getImage() != walkRight) v.setImage(walkRight);
            v.setScaleX(1);
        } else {
            if (v.getImage() != walkLeft)  v.setImage(walkLeft);
            v.setScaleX(1);
        }
    }

    @Override
    public void damage(int amount) {
        super.damage(amount);
    }

}
