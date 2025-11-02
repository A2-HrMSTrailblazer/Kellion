// se233.kellion.model.Minion.java
package se233.kellion.model;

import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import se233.kellion.model.MinionConfig;
import se233.kellion.model.MinionSkin;
import se233.kellion.util.BulletManager;
import se233.kellion.util.Config;

import java.util.List;

public class Minion {
    public enum State { WALK, ATTACK, DEAD }

    private final MinionSkin skin;
    private final MinionConfig cfg;

    private final MinionKind kind;
    private State state = State.WALK;

    private double x, y;
    private double patrolLeft, patrolRight;
    private int dir = 1; // 1=ขวา, -1=ซ้าย

    private int hp;
    private int maxHp;
    private long lastShotNs = 0;
    private boolean paused = false;

    private static Image ENEMY_BULLET_SPRITE;

    private static Image enemyBulletSprite() {
        if (ENEMY_BULLET_SPRITE == null) {
            var url = Minion.class.getResource("/se233/kellion/assets/Bullet_M1.gif");
            if (url == null) throw new IllegalStateException("Bullet_M1.gif not found");
            ENEMY_BULLET_SPRITE = new Image(url.toExternalForm());
        }
        return ENEMY_BULLET_SPRITE;
    }

    private final ImageView view = new ImageView();


    public Minion(double x, double y, double patrolLeft, double patrolRight, MinionSkin skin, MinionConfig cfg,  MinionKind kind) {
        this.x = x; this.y = y;
        this.patrolLeft = patrolLeft;
        this.patrolRight = patrolRight;
        this.skin = skin;
        this.cfg = cfg;
        this.kind = kind;
        this.maxHp = Math.max(1, cfg.hp);
        this.hp = this.maxHp;

        view.setImage(skin.walkLeft());
        view.setPreserveRatio(true);
        view.setFitHeight(skin.fitHeight());
        view.setTranslateX(x);
        view.setTranslateY(y);
        view.setScaleX(1);
    }

    public ImageView getView() { return view; }
    public boolean isDead() { return state == State.DEAD; }
    public Bounds getBounds() { return view.getBoundsInParent(); }
    public int getHp()     { return hp; }
    public int getMaxHp()  { return maxHp; }

    public void damage(int d) {
        if (state == State.DEAD) return;
        hp -= d;
        if (hp <= 0) {
            state = State.DEAD;
            view.setVisible(false);
        }
    }

    public void update(Player player, List enemyBullets, Pane layerRoot, long nowNs) {
        if (state == State.DEAD) return;
        if (paused) return;

        Bounds pb = player.getView().getBoundsInParent();
        double px = pb.getMinX() + pb.getWidth() * 0.5;
        double py = pb.getMinY() + pb.getHeight() * 0.5;

        double dx = px - x;
        double dy = py - y;
        double dist = Math.hypot(dx, dy);

        if (dist <= cfg.detectRange) {
            if (state != State.ATTACK) {
                state = State.ATTACK;
                view.setImage(skin.attack());
            }
            if (dx != 0) view.setScaleX(dx >= 0 ? 1 : -1);
            tryShootToward(px, py, enemyBullets, layerRoot, nowNs);
        } else {
            if (state != State.WALK) {
                state = State.WALK;
                applyWalkImageByDirection();
            }
            patrolMove();
            applyWalkImageByDirection();
        }

        view.setTranslateX(x);
        view.setTranslateY(y);
    }

    private void patrolMove() {
        x += dir * cfg.speed;
        if (x <= patrolLeft) { x = patrolLeft; dir = 1; }
        if (x >= patrolRight){ x = patrolRight; dir = -1; }
    }

    private void applyWalkImageByDirection() {
        if (dir >= 0) {
            if (view.getImage() != skin.walkRight()) view.setImage(skin.walkRight());
            view.setScaleX(1);
        } else {
            if (view.getImage() != skin.walkLeft()) view.setImage(skin.walkLeft());
            view.setScaleX(1);
        }
    }

    private void tryShootToward(double tx, double ty, List<Bullet> enemyBullets, Pane layerRoot, long nowNs) {
        if (nowNs - lastShotNs < cfg.fireIntervalNs) return;
        lastShotNs = nowNs;

        Bounds b = view.getBoundsInParent();
        double spawnX = b.getMinX() + b.getWidth() * 0.55;
        double spawnY = b.getMinY() + b.getHeight() * 0.35;

        ImageView bulletView = new ImageView(enemyBulletSprite());
        bulletView.setFitWidth(8);
        bulletView.setFitHeight(8);

        double speed = Config.BOSS_BULLET_SPEED -0.2;
        BulletManager.fireBossBulletAimed(enemyBullets, layerRoot, spawnX, spawnY, tx, ty, speed, bulletView);
        bulletView.setScaleX((tx - spawnX) >= 0 ? 1 : -1);
    }
    public MinionKind getKind() { return kind; }
    public void setPaused(boolean value) {
        this.paused = value;
    }
}

