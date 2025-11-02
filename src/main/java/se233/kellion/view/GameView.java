package se233.kellion.view;

import javafx.animation.PauseTransition;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.image.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import se233.kellion.model.*;
import se233.kellion.util.BulletManager;
import se233.kellion.util.CollisionUtil;
import se233.kellion.util.Config;
import se233.kellion.util.DebugUtil;
import se233.kellion.util.ExplosionUtil;
import se233.kellion.util.ScoreManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView {
    // --- Core Game Entities & Assets ---
    protected final Pane root;
    protected final Player player;
    public Boss boss;
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<Bullet> bossBullets = new ArrayList<>();
    private final WritableImage bulletSprite;
    protected WritableImage bossBulletSprite;
    protected WritableImage[] explosionFrames;


    // --- Environment/Level Constants ---
    private static final int TILE_SIZE = 32;
    private static final int SKY_TILE_SIZE = 16;
    private static final int GRASS_HEIGHT = 16;
    private static final int SOIL_HEIGHT = 32;
    private static final int WATER_TILE_HEIGHT = 16;

    protected int bossFireCounter = 0;
    protected javafx.scene.image.Image bossBulletImage;

    // --- Game State Flags ---
    protected static final boolean DEBUG_MODE = false;
    private static final boolean DEBUG_HITBOX = false;
    protected boolean playerDead = false;
    protected boolean boss1Defeated = false;
    private Runnable onNextScene = () -> {};
    private Runnable onGameOver = () -> {};
    protected Runnable onGameWin = () -> {};
    private boolean nextSceneTriggered = false;


    // --- Explosion Animation (Single Source) ---
    private static final int EXPLOSION_START_X = 215, EXPLOSION_START_Y = 905,
            EXPLOSION_FRAME_SIZE = 32, EXPLOSION_FRAME_SPACE = 1;

    // --- Score Management (All via ScoreManager) ---
    protected ScoreManager scoreManager;
    private javafx.scene.text.Text scoreText; // Only for UI; all score logic is delegated.

    // -- Minion --
    protected final List<se233.kellion.model.Minion> minions = new java.util.ArrayList<>();
    protected boolean bossSpawned = false;

    public GameView() {
        root = new Pane();
        drawEnvironment();

        int groundY = 368;
        int playerY = groundY + GRASS_HEIGHT - 64;
        player = new Player(100, playerY, "/se233/kellion/assets/Player.png", groundY);
        root.getChildren().add(player.getView());

        spawnMinionsForThisStage();

        Image characterSheet = new Image(getClass().getResource("/se233/kellion/assets/Characters.png").toExternalForm());
        bulletSprite = new WritableImage(characterSheet.getPixelReader(), 287, 805, 8, 16);
        Image BossBullet1 = new Image(getClass().getResource("/se233/kellion/assets/Bullet_M1.png").toExternalForm());
        bossBulletSprite = new WritableImage(BossBullet1.getPixelReader(), 0, 0, 8, 8);
        explosionFrames = ExplosionUtil.loadExplosionFrames(
                characterSheet, EXPLOSION_START_X, EXPLOSION_START_Y, EXPLOSION_FRAME_SIZE, EXPLOSION_FRAME_SPACE);

        // --- Score UI/Manager
        scoreText = new javafx.scene.text.Text(12, 30, "Score: 0");
        scoreText.setStyle("-fx-font-size:20; -fx-fill:white; -fx-font-weight:bold;");
        scoreText.setStroke(Color.BLACK);
        scoreText.setStrokeWidth(2);
        root.getChildren().add(scoreText);
        scoreManager = new ScoreManager(scoreText, 100, 20, 45, 300);

        if (DEBUG_MODE)
            updateAllHitboxes();
    }

    /** Draws the environment background. Subclasses should override for new stages. */
    protected void drawEnvironment() {
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

        int groundY = 368, skyRows = groundY / SKY_TILE_SIZE, skyCols = Config.WINDOW_WIDTH / SKY_TILE_SIZE;
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
            int X = c * TILE_SIZE, idx = c % 2;
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

    /** Add an explosion effect at (x, y) */
    private void showExplosionEffect(double x, double y) {
        ExplosionUtil.playExplosion(root, explosionFrames, x, y);
    }

    /** Spawn a player bullet (uses BulletManager) */
    public void fireBullet(double x, double y, boolean facingRight) {
        ImageView iv = new ImageView(bulletSprite);
        BulletManager.firePlayerBullet(bullets, root, x, y, iv, Config.BULLET_SPEED, facingRight);
        scoreManager.addShot();
        if (DEBUG_MODE)
            drawHitbox(iv, "bulletDebug_" + bullets.size(), Color.ORANGE);
    }

    /** Spawn a boss bullet aimed at a target (uses BulletManager) */
    public void fireBossBulletAimed(double x, double y, double targetX, double targetY, double speed) {
        ImageView iv = new ImageView(bossBulletSprite);
        BulletManager.fireBossBulletAimed(bossBullets, root, x, y, targetX, targetY, speed, iv);
        if (DEBUG_MODE)
            drawHitbox(iv, "bossBulletDebug_" + bossBullets.size(), Color.CYAN);
    }

    /** Update player bullets (movement and out-of-bounds removal) */
    public void updateBullets() {
        BulletManager.updateBullets(bullets, root, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
        if (DEBUG_MODE)
            updateAllHitboxes();
    }

    /** Update boss bullets (movement and out-of-bounds removal) */
    public void updateBossBullets() {
        BulletManager.updateBullets(bossBullets, root, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
        if (DEBUG_MODE)
            updateAllBossBulletHitboxes();
    }

    /** Handle all collisions (player/boss/bullets) and score updates */
    public void checkCollisions() {
        List<Bullet> toRemove = new ArrayList<>();
        // Player bullets hitting the boss
        if (boss != null && !boss.isDead()) {
            for (Bullet bullet : bullets) {
                if (CollisionUtil.intersects(bullet.getView(), boss.getView())) {
                    boss.damage(10);
                    showExplosionEffect(bullet.getView().getX(), bullet.getView().getY());
                    toRemove.add(bullet);
                    root.getChildren().remove(bullet.getView());
                    scoreManager.addHit();
                    if (boss.isDead())
                        showBossDestroyedSprite();
                }
            }
        }
        // Player colliding with boss (blocked)
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
        if (DEBUG_MODE) updateAllHitboxes();
    }

    /** Boss logic and bullet firing pattern. */
    public void updateBoss() {
        if (!bossSpawned || boss == null) {
            checkLevelTransition();
            return;
        }
        if (boss.isDead()) {
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
            fireBossBulletAimed(bulletX - 20, bulletY - 20, tx, ty, s);
            fireBossBulletAimed(bulletX + 30, bulletY - 15, tx, ty, s);
            bossFireCounter = 0;
        }
        if (DEBUG_MODE)
            updateAllHitboxes();
        checkLevelTransition();
    }

    /** Called when boss is defeated: update art and apply end-of-stage bonuses. */
    protected void showBossDestroyedSprite() {
        Image src = new Image(getClass().getResource("/se233/kellion/assets/Defense_Wall.png").toExternalForm());
        WritableImage destroyedSprite = new WritableImage(src.getPixelReader(), 113, 96, 110, 64);
        boss.getView().setImage(destroyedSprite);
        boss.getView().setScaleX(2.0);
        boss.getView().setScaleY(2.0);
        boss.getView().setY(boss.getView().getY() + 146);
        player.getView().toFront();

        boss1Defeated = true;
        scoreManager.applyStageClearBonuses(player);
    }

    /** Checks for player end-of-level/right edge condition. */
    protected void checkLevelTransition() {
        if (!boss1Defeated || nextSceneTriggered)
            return;
        Bounds pb = player.getView().getBoundsInParent();
        double playerRight = pb.getMaxX();
        if (playerRight >= Config.WINDOW_WIDTH - 4) {
            nextSceneTriggered = true;
            Runnable go = onNextScene;
            onNextScene = () -> {};
            javafx.application.Platform.runLater(go);
        }
    }

    /** Handle player being hit/killed. */
    public void killPlayer() {
        if (!playerDead) {
            playerDead = true;
            player.loseLife();
            player.playDeathAnimation();
            System.out.println("Player killed by boss bullet! Lives left: " + player.getLives());
            if (player.getLives() > 0) {
                PauseTransition delay = new PauseTransition(Duration.millis(420));
                delay.setOnFinished(_ -> respawnPlayer());
                delay.play();
            } else {
                System.out.println("Game Over!");
                onGameOver.run();
            }
        }
    }

    private void respawnPlayer() {
        player.getView().setX(100);
        player.getView().setY(320);
        player.resetToIdle();
        player.getView().setOpacity(1.0);
        playerDead = false;
    }

    // --- Debug Utilities ---
    private void drawHitbox(Node node, String id, Color color) {
        DebugUtil.drawHitbox(root, node, id, color);
    }

    protected void updateAllHitboxes() {
        DebugUtil.updateAllHitboxes(root, player.getView(),
                boss != null && !boss.isDead() ? boss.getView() : null, true,
                bullets.stream().map(Bullet::getView).toList(), DEBUG_HITBOX);
    }

    private void updateAllBossBulletHitboxes() {
        if (!DEBUG_HITBOX) {
            root.getChildren().removeIf(n -> n instanceof Rectangle && n.getId() != null && n.getId().contains("bossBulletDebug"));
            return;
        }
        root.getChildren().removeIf(n -> n instanceof Rectangle && n.getId() != null &&
                n.getId().contains("bossBulletDebug"));
        for (int i = 0; i < bossBullets.size(); i++)
            drawHitbox(bossBullets.get(i).getView(), "bossBulletDebug_" + i, Color.CYAN);
    }

    protected void spawnBoss() {
        int groundY = 368;
        double bossX = Config.WINDOW_WIDTH - Boss.SPRITE_WIDTH - 55;
        double bossY = groundY + GRASS_HEIGHT - Boss.SPRITE_HEIGHT - 48;

        boss = new Boss(bossX, bossY, "/se233/kellion/assets/Defense_Wall.png");
        boss.getView().setScaleX(2);
        boss.getView().setScaleY(2);
        root.getChildren().add(boss.getView());
    }

    protected void spawnMinionsForThisStage() {
        addMinion(MinionKind.M1, 550, 340, 400, 550);
        addMinion(MinionKind.M1, 620, 340, 500, 600);
        addMinion(MinionKind.M1, 700, 340, 620, 760);
    }

    protected void addMinion(MinionKind kind, double x, double y, double patrolL, double patrolR) {
        Minion m = se233.kellion.model.MinionFactory.create(kind, x, y, patrolL, patrolR);
        minions.add(m);
        root.getChildren().add(m.getView());
    }

    public void updateMinions(long now) {

        for (Minion m : minions) {
            m.update(player, bossBullets, root, now);
        }
        List<Bullet> removePlayerBullets = new ArrayList<>();
        for (Bullet b : bullets) {
            Bounds bb = b.getView().getBoundsInParent();
            for (se233.kellion.model.Minion m : minions) {
                if (!m.isDead() && m.getBounds().intersects(bb)) {
                    m.damage(1);
                    showExplosionEffect(b.getView().getX(), b.getView().getY());
                    root.getChildren().remove(b.getView());
                    removePlayerBullets.add(b);

                    if (m.isDead()) {
                        scoreManager.addMinionKill(m.getKind());
                    }
                    break;
                }
            }
        }

        bullets.removeAll(removePlayerBullets);

        minions.removeIf(se233.kellion.model.Minion::isDead);

        if (!bossSpawned && minions.isEmpty()) {
            bossSpawned = true;
            spawnBoss();
        }
    }

    // --- Public API ---
    protected void setBossBulletImage(javafx.scene.image.Image img) {this.bossBulletImage = img;}
    public void setOnGameWin(Runnable r) { this.onGameWin = (r != null) ? r : () -> {}; }
    public Pane getRoot() { return root; }
    public Player getPlayer() { return player; }
    public List<Bullet> getBullets() { return bullets; }
    public Boss getBoss() { return boss; }
    public boolean isPlayerDead() { return playerDead; }
    public void setOnNextScene(Runnable r) { this.onNextScene = r != null ? r : () -> {}; }
    public boolean isBoss1Defeated() { return boss1Defeated; }
    public void setOnGameOver(Runnable r) { this.onGameOver = (r != null) ? r : () -> {}; }
}