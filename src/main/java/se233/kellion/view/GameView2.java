package se233.kellion.view;

import javafx.animation.PauseTransition;
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

public class GameView2 extends GameView {
    private final Pane root;
    private final Player player;
    private Boss boss;
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<Bullet> bossBullets = new ArrayList<>();
    private final WritableImage bulletSprite;
    private final WritableImage bossBulletSprite;
    private WritableImage bossFrame1;
    private WritableImage bossFrame2;

    // Tile and layout constants
    private static final int TILE_SIZE = 32;
    private static final int SKY_TILE_SIZE = 16;
    private static final int GRASS_HEIGHT = 16;
    private static final int SOIL_HEIGHT = 32;
    private static final int WATER_TILE_HEIGHT = 16;
    private static final int Stalactite_HEIGHT = 160;

    private int bossFireCounter = 0;
    private static final boolean DEBUG_MODE = true;
    private boolean playerDead = false;
    private static final boolean DEBUG_HITBOX = false;

    private boolean boss1Defeated = false;
    private Runnable onNextScene = () -> {
    };
    private boolean nextSceneTriggered = false;

    private WritableImage[] explosionFrames;

    // (reuse these constants, or place them near your other constants)
    private static final int EXPLOSION_START_X = 215, EXPLOSION_START_Y = 905;
    private static final int EXPLOSION_FRAME_SIZE = 32, EXPLOSION_FRAME_SPACE = 1;

    private int score = 0;
    private int shotsFired = 0;
    private static final int SCORE_PER_HIT = 100;
    private static final int TIME_BONUS_FACTOR = 20; // Adjust as you like
    private static final int PAR_TIME_SECONDS = 45;
    private static final int LIFE_BONUS = 300;

    private long stageStartTimeMillis;
    private javafx.scene.text.Text scoreText;

    // Constructor: Sets up background, ground, player, boss, and assets
    public GameView2() {
        super();
        root = new Pane();

        // Load tileset and cut tiles from the spritesheet
        Image tileset = new Image(getClass().getResource("/se233/kellion/assets/Stage_2.png").toExternalForm());
        WritableImage grassTile = new WritableImage(tileset.getPixelReader(), 0, 0, TILE_SIZE, GRASS_HEIGHT);
        WritableImage soilTile = new WritableImage(tileset.getPixelReader(), 256, 0, TILE_SIZE, SOIL_HEIGHT);

        Image stalac = new Image(getClass().getResource("/se233/kellion/assets/STALACTITE.png").toExternalForm());
        WritableImage stalactiteTile = new WritableImage(stalac.getPixelReader(), 0, 0, 525, Stalactite_HEIGHT);

        // Water/wave tiles
        Image waveset = new Image(getClass().getResource("/se233/kellion/assets/Wave_2.png").toExternalForm());
        WritableImage waterTile = new WritableImage(waveset.getPixelReader(), 0, 16, TILE_SIZE, TILE_SIZE);
        WritableImage[] waveTiles = {
                new WritableImage(waveset.getPixelReader(), 0, 0, TILE_SIZE, WATER_TILE_HEIGHT),
                new WritableImage(waveset.getPixelReader(), 32, 0, TILE_SIZE, WATER_TILE_HEIGHT)
        };

        // Random sky tiles for variation
        WritableImage[] skyTiles = new WritableImage[5];
        for (int i = 0; i < 5; i++) {
            skyTiles[i] = new WritableImage(tileset.getPixelReader(), 48 + SKY_TILE_SIZE * i, 48, SKY_TILE_SIZE,
                    SKY_TILE_SIZE);
        }

        int groundY = 368;
        int skyRows = groundY / SKY_TILE_SIZE;
        int skyCols = Config.WINDOW_WIDTH / SKY_TILE_SIZE;
        Random rand = new Random();

        // Draw sky background
        for (int row = 0; row < skyRows; row++) {
            for (int col = 0; col < skyCols; col++) {
                ImageView skyView = new ImageView(skyTiles[rand.nextInt(skyTiles.length)]);
                skyView.setX(col * SKY_TILE_SIZE);
                skyView.setY(row * SKY_TILE_SIZE);
                root.getChildren().add(skyView);
            }
        }

        int cols1 = (int) Math.ceil((double) Config.WINDOW_WIDTH / TILE_SIZE);

        for (int c = 0; c < cols1; c++) {
            ImageView iv = new ImageView(stalactiteTile);
            iv.setX(c * 525);
            iv.setY(0); // ชิดขอบบนของหน้าต่าง (y = 0)
            root.getChildren().add(iv);
            iv.toFront();
        }

        // Draw grass and soil tiles (ground)
        for (int col = 0; col < Config.WINDOW_WIDTH / TILE_SIZE; col++) {
            int x = col * TILE_SIZE;

            ImageView grassView = new ImageView(grassTile);
            grassView.setX(x);
            grassView.setY(groundY);
            root.getChildren().add(grassView);

            ImageView soilView = new ImageView(soilTile);
            soilView.setX(x);
            soilView.setY(groundY + GRASS_HEIGHT);
            root.getChildren().add(soilView);
        }

        // Draw water and waves below ground
        int waterStartY = groundY + GRASS_HEIGHT + SOIL_HEIGHT;
        int cols = Config.WINDOW_WIDTH / TILE_SIZE;

        for (int c = 0; c < cols; c++) {
            int X = c * TILE_SIZE;
            int idx = c % 2;
            ImageView wave = new ImageView(waveTiles[idx]);
            wave.setX(X);
            wave.setY(waterStartY);
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

        // Create and place the player
        int playerY = groundY + GRASS_HEIGHT - 64;
        player = new Player(100, playerY, "/se233/kellion/assets/Player.png", groundY);
        root.getChildren().add(player.getView());

        // Create and place the boss
        double bossX = Config.WINDOW_WIDTH - Boss.SPRITE_WIDTH - 55;
        double bossY = groundY + GRASS_HEIGHT - Boss.SPRITE_HEIGHT - 48;
        boss = new Boss(bossX, bossY, "/se233/kellion/assets/Defense_Wall.png");

        Image javaSheet = new Image(getClass().getResource("/se233/kellion/assets/Java.png").toExternalForm());
        int sheetW = (int) javaSheet.getWidth(); // 306
        int sheetH = (int) javaSheet.getHeight(); // 113

        int frames = 3;
        int FRAME_W = sheetW / frames; // 102
        int FRAME_H = sheetH; // 113

        bossFrame1 = new WritableImage(javaSheet.getPixelReader(), 0, 0, FRAME_W, FRAME_H - 17);
        bossFrame2 = new WritableImage(javaSheet.getPixelReader(), 112, 0, FRAME_W, 113);

        int TRIM_BOTTOM = 17;
        int CROP_H = Math.max(1, FRAME_H - TRIM_BOTTOM);

        WritableImage bossIdle = new WritableImage(
                javaSheet.getPixelReader(),
                0, // frame 0
                0,
                Math.min(FRAME_W, sheetW),
                Math.min(CROP_H, sheetH));

        boss.getView().setImage(bossFrame1);
        boss.getView().setScaleX(2);
        boss.getView().setScaleY(2);
        boss.getView().setY(boss.getView().getY() - 30);
        root.getChildren().add(boss.getView());

        // Load bullet sprites
        Image characterSheet = new Image(
                getClass().getResource("/se233/kellion/assets/Characters.png").toExternalForm());
        bulletSprite = new WritableImage(characterSheet.getPixelReader(), 287, 805, 8, 16);
        bossBulletSprite = new WritableImage(characterSheet.getPixelReader(), 368, 805, 8, 16);

        if (DEBUG_MODE)
            updateAllHitboxes();

        loadExplosionFrames();

        // Track stage start time
        stageStartTimeMillis = System.currentTimeMillis();

        // Score UI
        scoreText = new javafx.scene.text.Text(12, 30, "Score: 0");
        scoreText.setStyle("-fx-font-size:20; -fx-fill:white; -fx-font-weight:bold;");
        scoreText.setStroke(Color.BLACK); // makes white text readable
        scoreText.setStrokeWidth(2);
        root.getChildren().add(scoreText);

        updateScoreText();

    }

    // Bullet creation
    public void fireBullet(double x, double y, boolean facingRight) {
        ImageView iv = new ImageView(bulletSprite);
        iv.setX(x);
        iv.setY(y);
        iv.setScaleX(facingRight ? 1 : -1);
        Bullet bullet = new Bullet(iv, Config.BULLET_SPEED, false);
        bullets.add(bullet);
        root.getChildren().add(iv);
        shotsFired++;

        if (DEBUG_MODE)
            drawHitbox(iv, "bulletDebug_" + bullets.size(), Color.ORANGE);
    }

    private void loadExplosionFrames() {
        Image characterSheet = new Image(
                getClass().getResource("/se233/kellion/assets/Characters.png").toExternalForm());
        explosionFrames = new WritableImage[3];
        for (int i = 0; i < 3; i++) {
            explosionFrames[i] = new WritableImage(
                    characterSheet.getPixelReader(),
                    EXPLOSION_START_X + i * (EXPLOSION_FRAME_SIZE + EXPLOSION_FRAME_SPACE),
                    EXPLOSION_START_Y,
                    EXPLOSION_FRAME_SIZE,
                    EXPLOSION_FRAME_SIZE);
        }
    }

    private void showExplosionEffect(double x, double y) {
        ImageView explosion = new ImageView(explosionFrames[0]);
        explosion.setFitWidth(32);
        explosion.setFitHeight(32);
        explosion.setX(x - 16);
        explosion.setY(y - 16);
        root.getChildren().add(explosion);

        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(0),
                        e -> explosion.setImage(explosionFrames[0])),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(80),
                        e -> explosion.setImage(explosionFrames[1])),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(160),
                        e -> explosion.setImage(explosionFrames[2])),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(240),
                        e -> root.getChildren().remove(explosion)));
        timeline.play();
    }

    public void fireBossBulletAimed(double x, double y, double targetX, double targetY, double speed) {
        ImageView iv = new ImageView(bossBulletSprite);
        iv.setX(x);
        iv.setY(y);

        double dx = targetX - x, dy = targetY - y;
        double len = Math.hypot(dx, dy);
        if (len == 0)
            len = 1;
        double vx = (dx / len) * speed;
        double vy = (dy / len) * speed;

        iv.setScaleX(vx >= 0 ? 1 : -1);
        Bullet b = new Bullet(iv, vx, vy, true);
        bossBullets.add(b);
        root.getChildren().add(iv);

        if (DEBUG_MODE)
            drawHitbox(iv, "bossBulletDebug_" + bossBullets.size(), Color.CYAN);
    }

    // Bullet updates and cleanup
    public void updateBullets() {
        bullets.forEach(Bullet::update);
        bullets.removeIf(b -> {
            if (b.isOutOfBounds(Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT)) {
                root.getChildren().remove(b.getView());
                return true;
            }
            return false;
        });
        if (DEBUG_MODE)
            updateAllHitboxes();
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
        if (DEBUG_MODE)
            updateAllBossBulletHitboxes();
    }

    // Collision detection (Player–Boss–Bullet)
    public void checkCollisions() {
        List<Bullet> toRemove = new ArrayList<>();

        // Player bullets hitting the boss
        if (boss != null && !boss.isDead()) {
            for (Bullet bullet : bullets) {
                if (CollisionUtil.intersects(bullet.getView(), boss.getView())) {
                    showExplosionEffect(bullet.getView().getX(), bullet.getView().getY());
                    boss.damage(10);
                    toRemove.add(bullet);
                    root.getChildren().remove(bullet.getView());
                    score += SCORE_PER_HIT;
                    updateScoreText();

                    if (boss.isDead())
                        showBossDestroyedSprite();
                }
            }
        }

        // Player colliding with boss body
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

        // Boss bullets hitting player
        List<Bullet> bossBulletsToRemove = new ArrayList<>();
        for (Bullet bullet : bossBullets) {
            if (!playerDead && bullet.getView().getBoundsInParent().intersects(player.getHitboxBounds())) {
                showExplosionEffect(bullet.getView().getX(), bullet.getView().getY());
                killPlayer();
                root.getChildren().remove(bullet.getView());
                bossBulletsToRemove.add(bullet);
                break; // one hit per frame
            }
        }

        bossBullets.removeAll(bossBulletsToRemove);
        bullets.removeAll(toRemove);
        if (DEBUG_MODE)
            updateAllHitboxes();
    }

    // Boss behavior and attack cycle
    public void updateBoss() {
        if (boss == null || boss.isDead()) {
            checkLevelTransition();
            return;
        }
        boss.getView().setX(Config.WINDOW_WIDTH - Boss.SPRITE_WIDTH - 20);

        bossFireCounter++;
        if (bossFireCounter >= Config.BOSS_FIRE_INTERVAL && !playerDead) {

            double bulletX = boss.getView().getX() + boss.getView().getBoundsInParent().getWidth() * 0.10;
            double bulletY = boss.getView().getY() + boss.getView().getBoundsInParent().getHeight() * 0.30;

            Bounds p = player.getHitboxBounds();
            double tx = p.getMinX() + p.getWidth() / 2.0;
            double ty = p.getMinY() + p.getHeight();

            double s = Config.BOSS_BULLET_SPEED + 1;
            boss.getView().setImage(bossFrame2);
            fireBossBulletAimed(bulletX - 20, bulletY - 20, tx, ty, s);
            fireBossBulletAimed(bulletX - 10, bulletY - 20, tx, ty, s);
            fireBossBulletAimed(bulletX + 0, bulletY - 20, tx, ty, s);
            fireBossBulletAimed(bulletX + 10, bulletY - 20, tx, ty, s);

            javafx.animation.Timeline revert = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(300),
                            e -> boss.getView().setImage(bossFrame1)));
            revert.play();
            bossFireCounter = 0;
        }

        if (DEBUG_MODE)
            updateAllHitboxes();
        checkLevelTransition();
    }

    // Change boss sprite to destroyed form
    private void showBossDestroyedSprite() {
        Image src = new Image(getClass().getResource("/se233/kellion/assets/Java.png").toExternalForm());
        WritableImage destroyedSprite = new WritableImage(src.getPixelReader(), 226, 0, 80, 40);
        boss.getView().setImage(destroyedSprite);
        boss.getView().setScaleX(2.0);
        boss.getView().setScaleY(2.0);
        boss.getView().setX(boss.getView().getX() + 15);
        boss.getView().setY(boss.getView().getY() - 25);
        player.getView().toFront();

        boss1Defeated = true;

        long elapsedMillis = System.currentTimeMillis() - stageStartTimeMillis;
        int elapsedSeconds = (int) (elapsedMillis / 1000);

        // Time bonus: max(0, (45 - time) * 20)
        int timeBonus = Math.max(0, (PAR_TIME_SECONDS - elapsedSeconds) * TIME_BONUS_FACTOR);

        // Lives bonus: 300 per remaining life
        int livesBonus = Math.max(0, player.getLives() * LIFE_BONUS);

        score += timeBonus + livesBonus;

        updateScoreText();

    }

    private void checkLevelTransition() {
        if (!boss1Defeated || nextSceneTriggered)
            return;

        double playerRight = player.getView().getX() + player.getView().getFitWidth();
        if (playerRight >= Config.WINDOW_WIDTH - 4) {
            nextSceneTriggered = true;
            Runnable go = onNextScene;
            onNextScene = () -> {
            };
            go.run();
        }
    }

    // Debug helpers: draw rectangles around game objects
    private void drawHitbox(Node node, String id, Color color) {
        if (!DEBUG_HITBOX || node == null)
            return;
        Bounds b = node.getBoundsInParent();

        Rectangle r = new Rectangle(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
        r.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.25));
        r.setStroke(color);
        r.setStrokeWidth(1.5);
        r.setId(id);

        root.getChildren().add(r);
    }

    private void updateAllHitboxes() {
        if (!DEBUG_HITBOX) {
            root.getChildren().removeIf(n -> n instanceof Rectangle && n.getId() != null &&
                    (n.getId().startsWith("debug") || n.getId().contains("bulletDebug")));
            return;
        }

        root.getChildren().removeIf(n -> n instanceof Rectangle && n.getId() != null &&
                (n.getId().startsWith("debug") || n.getId().contains("bulletDebug")));

        drawHitbox(player.getView(), "debugPlayer", Color.LIME);
        if (boss != null && !boss.isDead())
            drawHitbox(boss.getView(), "debugBoss", Color.RED);

        for (int i = 0; i < bullets.size(); i++)
            drawHitbox(bullets.get(i).getView(), "bulletDebug_" + i, Color.ORANGE);
    }

    private void updateAllBossBulletHitboxes() {
        if (!DEBUG_HITBOX) {
            root.getChildren().removeIf(n -> n instanceof Rectangle && n.getId() != null &&
                    n.getId().contains("bossBulletDebug"));
            return;
        }

        root.getChildren().removeIf(n -> n instanceof Rectangle && n.getId() != null &&
                n.getId().contains("bossBulletDebug"));

        for (int i = 0; i < bossBullets.size(); i++)
            drawHitbox(bossBullets.get(i).getView(), "bossBulletDebug_" + i, Color.CYAN);
    }

    private void respawnPlayer() {
        player.getView().setX(100);
        player.getView().setY(320);
        player.resetToIdle();
        player.getView().setOpacity(1.0);
        playerDead = false;
    }

    // Player death
    public void killPlayer() {
        if (!playerDead) {
            playerDead = true;
            player.loseLife(); // decrement player lives

            player.playDeathAnimation();
            System.out.println("Player killed by boss bullet! Lives left: " + player.getLives());

            if (player.getLives() > 0) {
                PauseTransition delay = new PauseTransition(
                        Duration.millis(420));
                delay.setOnFinished(_ -> respawnPlayer());
                delay.play();
            } else {
                System.out.println("Game Over!");
                // Optionally show Game Over screen/UI
            }
        }
    }

    // Getters
    public Pane getRoot() {
        return root;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Bullet> getBullets() {
        return bullets;
    }

    private void setBoss(Boss b) {
        this.boss = b;
    }

    public Boss getBoss() {
        return boss;
    }

    public boolean isPlayerDead() {
        return playerDead;
    }

    public void setOnNextScene(Runnable r) {
        this.onNextScene = (r != null) ? r : () -> {
        };
    }

    public boolean isBoss1Defeated() {
        return boss1Defeated;
    }

    private void updateScoreText() {
        scoreText.setText("Score: " + score);
    }

}
