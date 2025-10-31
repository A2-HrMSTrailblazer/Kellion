package se233.kellion.view;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.image.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import se233.kellion.model.Boss;
import se233.kellion.model.Bullet;
import se233.kellion.model.Player;
import se233.kellion.util.CollisionUtil;
import se233.kellion.util.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView {
    private final Pane root;
    private final Player player;
    private final Boss boss;
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<Bullet> bossBullets = new ArrayList<>();
    private final WritableImage bulletSprite;
    private final WritableImage bossBulletSprite;
    private WritableImage[] explosionFrames;

    private static final int TILE_SIZE = 32;
    private static final int SKY_TILE_SIZE = 16;
    private static final int GRASS_HEIGHT = 16;
    private static final int SOIL_HEIGHT = 32;
    private static final int WATER_TILE_HEIGHT = 16;

    private int bossFireCounter = 0;
    private static final boolean DEBUG_MODE = false;
    private boolean playerDead = false;

    public GameView() {
        root = new Pane();

        // --- Background and environment draw ---
        drawEnvironment();

        // --- Create player ---
        int groundY = 368;
        int playerY = groundY + GRASS_HEIGHT - 64;
        player = new Player(100, playerY, groundY);
        root.getChildren().add(player.getView());

        // --- Create boss ---
        double bossX = Config.WINDOW_WIDTH - Boss.SPRITE_WIDTH - 55;
        double bossY = groundY + GRASS_HEIGHT - Boss.SPRITE_HEIGHT - 48;
        boss = new Boss(bossX, bossY, "/se233/kellion/assets/Defense_Wall.png");
        boss.getView().setScaleX(2);
        boss.getView().setScaleY(2);
        root.getChildren().add(boss.getView());

        // --- Load bullet/explosion sprites ---
        Image characterSheet = new Image(getClass().getResource("/se233/kellion/assets/Characters.png").toExternalForm());
        bulletSprite = new WritableImage(characterSheet.getPixelReader(), 287, 805, 8, 16);
        bossBulletSprite = new WritableImage(characterSheet.getPixelReader(), 368, 805, 8, 16);
        loadExplosionFrames(characterSheet);

        if (DEBUG_MODE) updateAllHitboxes();
    }

    // --- Environment/background drawing helper ---
    private void drawEnvironment() {
        Image tileset = new Image(getClass().getResource("/se233/kellion/assets/Stage_1.png").toExternalForm());
        WritableImage grassTile = new WritableImage(tileset.getPixelReader(), 0, 0, TILE_SIZE, GRASS_HEIGHT);
        WritableImage soilTile = new WritableImage(tileset.getPixelReader(), 256, 0, TILE_SIZE, SOIL_HEIGHT);

        Image waveset = new Image(getClass().getResource("/se233/kellion/assets/Wave.png").toExternalForm());
        WritableImage waterTile = new WritableImage(waveset.getPixelReader(), 0, 16, TILE_SIZE, TILE_SIZE);
        WritableImage[] waveTiles = {
            new WritableImage(waveset.getPixelReader(), 0, 0, TILE_SIZE, WATER_TILE_HEIGHT),
            new WritableImage(waveset.getPixelReader(), 32, 0, TILE_SIZE, WATER_TILE_HEIGHT)
        };

        WritableImage[] skyTiles = new WritableImage[5];
        for (int i = 0; i < 5; i++)
            skyTiles[i] = new WritableImage(tileset.getPixelReader(), 48 + SKY_TILE_SIZE * i, 48, SKY_TILE_SIZE, SKY_TILE_SIZE);

        int groundY = 368;
        int skyRows = groundY / SKY_TILE_SIZE;
        int skyCols = Config.WINDOW_WIDTH / SKY_TILE_SIZE;
        Random rand = new Random();

        for (int row = 0; row < skyRows; row++)
            for (int col = 0; col < skyCols; col++) {
                ImageView skyView = new ImageView(skyTiles[rand.nextInt(skyTiles.length)]);
                skyView.setX(col * SKY_TILE_SIZE);
                skyView.setY(row * SKY_TILE_SIZE);
                root.getChildren().add(skyView);
            }

        for (int col = 0; col < Config.WINDOW_WIDTH / TILE_SIZE; col++) {
            int x = col * TILE_SIZE;
            ImageView grassView = new ImageView(grassTile);
            grassView.setX(x); grassView.setY(groundY);
            root.getChildren().add(grassView);

            ImageView soilView = new ImageView(soilTile);
            soilView.setX(x); soilView.setY(groundY + GRASS_HEIGHT);
            root.getChildren().add(soilView);
        }

        int waterStartY = groundY + GRASS_HEIGHT + SOIL_HEIGHT;
        int cols = Config.WINDOW_WIDTH / TILE_SIZE;
        for (int c = 0; c < cols; c++) {
            int X = c * TILE_SIZE;
            int idx = c % 2;
            ImageView wave = new ImageView(waveTiles[idx]);
            wave.setX(X); wave.setY(waterStartY);
            root.getChildren().add(wave);
        }
        int Y = waterStartY + WATER_TILE_HEIGHT;
        while (Y < Config.WINDOW_HEIGHT) {
            for (int c = 0; c < cols; c++) {
                ImageView deep = new ImageView(waterTile);
                deep.setX(c * TILE_SIZE);
                deep.setY(Y);
                root.getChildren().add(deep);
            }
            Y += (WATER_TILE_HEIGHT - 1);
        }
    }

    // --- Bullet/fire logic ---
    public void fireBullet(double x, double y, boolean facingRight) {
        ImageView iv = new ImageView(bulletSprite);
        iv.setX(x); iv.setY(y); iv.setScaleX(facingRight ? 1 : -1);
        Bullet bullet = new Bullet(iv, Config.BULLET_SPEED, false);
        bullets.add(bullet); root.getChildren().add(iv);
        if (DEBUG_MODE) drawHitbox(iv, "bulletDebug_" + bullets.size(), Color.ORANGE);
    }

    public void fireBossBullet(double x, double y, boolean toLeft) {
        ImageView iv = new ImageView(bossBulletSprite);
        iv.setX(x); iv.setY(y);
        iv.setScaleX(toLeft ? -1 : 1);
        Bullet bullet = new Bullet(iv, Config.BOSS_BULLET_SPEED, true);
        bossBullets.add(bullet); root.getChildren().add(iv);
        if (DEBUG_MODE) drawHitbox(iv, "bossBulletDebug_" + bossBullets.size(), Color.CYAN);
    }

    public void updateBullets() {
        bullets.forEach(Bullet::update);
        bullets.removeIf(b -> {
            if (b.isOutOfBounds(Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT)) {
                root.getChildren().remove(b.getView());
                return true;
            }
            return false;
        });
        if (DEBUG_MODE) updateAllHitboxes();
    }

    public void updateBossBullets() {
        bossBullets.forEach(Bullet::update);
        bossBullets.removeIf(b -> {
            if (b.isOutOfBounds(Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT)) {
                root.getChildren().remove(b.getView());
                return true;
            }
            return false;
        });
        if (DEBUG_MODE) updateAllBossBulletHitboxes();
    }

    // --- Bullet collision/cleanup/explosion ---
    public void checkCollisions() {
        List<Bullet> toRemove = new ArrayList<>();
        // Bullet/boss
        if (boss != null && !boss.isDead()) {
            for (Bullet bullet : bullets) {
                if (CollisionUtil.intersects(bullet.getView(), boss.getView())) {
                    boss.damage(10);
                    showExplosionEffect(bullet.getView().getX(), bullet.getView().getY());
                    toRemove.add(bullet);
                    root.getChildren().remove(bullet.getView());
                    if (boss.isDead()) showBossDestroyedSprite();
                }
            }
        }
        // Player/boss
        if (boss != null && !boss.isDead()) {
            Bounds bossHitbox = boss.getCustomBounds(0, 10);
            Bounds playerHitbox = player.getView().getBoundsInParent();
            if (playerHitbox.intersects(bossHitbox)) {
                player.getView().setX(bossHitbox.getMinX() - playerHitbox.getWidth());
            }
            if (DEBUG_MODE) {
                Rectangle r = new Rectangle(bossHitbox.getMinX(), bossHitbox.getMinY(),
                        bossHitbox.getWidth(), bossHitbox.getHeight());
                r.setFill(Color.TRANSPARENT);
                r.setStroke(Color.BLUE);
                r.setId("debugBossHitbox");
                root.getChildren().removeIf(n -> n instanceof Rectangle && "debugBossHitbox".equals(n.getId()));
                root.getChildren().add(r);
            }
        }
        // Boss bullets/player
        List<Bullet> bossBulletsToRemove = new ArrayList<>();
        for (Bullet bullet : bossBullets) {
            if (!playerDead && CollisionUtil.intersects(bullet.getView(), player.getView())) {
                showExplosionEffect(bullet.getView().getX(), bullet.getView().getY());
                killPlayer();
                root.getChildren().remove(bullet.getView());
                bossBulletsToRemove.add(bullet);
                break; // Only 1 hit/frame
            }
        }
        bossBullets.removeAll(bossBulletsToRemove);
        bullets.removeAll(toRemove);
        if (DEBUG_MODE) updateAllHitboxes();
    }

    // --- Explosion/sprite animation for bullets ---
    private void loadExplosionFrames(Image characterSheet) {
        explosionFrames = new WritableImage[3];
        int startX = 215, startY = 905, frameSize = 32, space = 1;
        for (int i = 0; i < 3; i++) {
            explosionFrames[i] = new WritableImage(
                characterSheet.getPixelReader(),
                startX + i * (frameSize + space),
                startY,
                frameSize,
                frameSize
            );
        }
    }

    private void showExplosionEffect(double x, double y) {
        ImageView explosion = new ImageView(explosionFrames[0]);
        explosion.setFitWidth(32); explosion.setFitHeight(32);
        explosion.setX(x - 16); explosion.setY(y - 16);
        root.getChildren().add(explosion);
        Timeline timeline = new Timeline(
            new KeyFrame(javafx.util.Duration.millis(0), _ -> explosion.setImage(explosionFrames[0])),
            new KeyFrame(javafx.util.Duration.millis(80), _ -> explosion.setImage(explosionFrames[1])),
            new KeyFrame(javafx.util.Duration.millis(160), _ -> explosion.setImage(explosionFrames[2])),
            new KeyFrame(javafx.util.Duration.millis(240), _ -> root.getChildren().remove(explosion))
        );
        timeline.play();
    }

    public void updateBoss() {
        if (boss == null || boss.isDead()) return;
        boss.getView().setX(Config.WINDOW_WIDTH - Boss.SPRITE_WIDTH - 20);
        bossFireCounter++;
        if (bossFireCounter >= Config.BOSS_FIRE_INTERVAL && !playerDead) {
            double bulletX = boss.getView().getX();
            double bulletY = boss.getView().getY() + boss.getView().getBoundsInParent().getHeight() / 2;
            fireBossBullet(bulletX - 50, bulletY - 80, true);
            fireBossBullet(bulletX, bulletY - 80, true);
            bossFireCounter = 0;
        }
        if (DEBUG_MODE) updateAllHitboxes();
    }

    private void showBossDestroyedSprite() {
        Image src = new Image(getClass().getResource("/se233/kellion/assets/Defense_Wall.png").toExternalForm());
        WritableImage destroyedSprite = new WritableImage(src.getPixelReader(), 113, 96, 110, 64);
        boss.getView().setImage(destroyedSprite);
        boss.getView().setScaleX(2.0); boss.getView().setScaleY(2.0);
        boss.getView().setY(boss.getView().getY() + 146);
        player.getView().toFront();
    }

    // --- Debugging helpers ---
    private void drawHitbox(Node node, String id, Color color) {
        if (node == null) return;
        Bounds b = node.getBoundsInParent();
        Rectangle r = new Rectangle(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
        r.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.25));
        r.setStroke(color); r.setStrokeWidth(1.5); r.setId(id);
        root.getChildren().add(r);
    }
    private void updateAllHitboxes() {
        root.getChildren().removeIf(n -> n instanceof Rectangle && n.getId() != null &&
            (n.getId().startsWith("debug") || n.getId().contains("bulletDebug")));
        drawHitbox(player.getView(), "debugPlayer", Color.LIME);
        if (boss != null && !boss.isDead())
            drawHitbox(boss.getView(), "debugBoss", Color.RED);
        for (int i = 0; i < bullets.size(); i++)
            drawHitbox(bullets.get(i).getView(), "bulletDebug_" + i, Color.ORANGE);
    }
    private void updateAllBossBulletHitboxes() {
        root.getChildren().removeIf(n -> n instanceof Rectangle && n.getId() != null &&
            n.getId().contains("bossBulletDebug"));
        for (int i = 0; i < bossBullets.size(); i++)
            drawHitbox(bossBullets.get(i).getView(), "bossBulletDebug_" + i, Color.CYAN);
    }

    // --- Player death logic ---
    public void killPlayer() {
        if (!playerDead) {
            playerDead = true;
            player.playDeathAnimation();

            player.loseLife();
            System.out.println("Player killed by boss bullet! Lives left: " + player.getLives());

            if (player.getLives() > 0) {
                PauseTransition delay = new PauseTransition(Duration.millis(350));
                delay.setOnFinished(_ -> respawnPlayer());
                delay.play();
            }
            else {
                System.out.println("Game Over!");
            }
        }
    }

    private void respawnPlayer() {
        player.getView().setX(100);
        player.getView().setY(368 + GRASS_HEIGHT - 64);
        player.resetToIdle();
        playerDead = false;
    }

    // --- Public getters ---
    public Pane getRoot() { return root; }
    public Player getPlayer() { return player; }
    public List<Bullet> getBullets() { return bullets; }
    public Boss getBoss() { return boss; }
    public boolean isPlayerDead() { return playerDead; }
}
