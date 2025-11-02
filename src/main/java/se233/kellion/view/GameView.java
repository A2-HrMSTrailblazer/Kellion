package se233.kellion.view;

import javafx.animation.*;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
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
    private Image bulletSprite;
    protected Image bossBulletSprite;
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

    // -- Player --
    private long lastPlayerShotNs = 0L;
    private static final long PLAYER_FIRE_COOLDOWN_NS = 250_000_000L;

    // --- Explosion Animation (Single Source) ---
    private static final int EXPLOSION_START_X = 215, EXPLOSION_START_Y = 905,
            EXPLOSION_FRAME_SIZE = 32, EXPLOSION_FRAME_SPACE = 1;

    // --- Score Management (All via ScoreManager) ---
    protected ScoreManager scoreManager;
    private javafx.scene.text.Text scoreText; // Only for UI; all score logic is delegated.

    // -- Minion --
    protected final List<se233.kellion.model.Minion> minions = new java.util.ArrayList<>();
    protected boolean bossSpawned = false;

    // -- Power-up --
    private static class PowerUp {
        final ImageView view;
        final Runnable apply; //
        PowerUp(ImageView v, Runnable a) { view = v; apply = a; }
    }
    private final List<PowerUp> powerUps = new ArrayList<>();
    private boolean powerUpSpawned = false;
    private boolean powerUpCollected = false;
    private double lastMinionDeathX = Config.WINDOW_WIDTH * 0.5;
    private double lastMinionDeathY = 320;
    private Image powerUpImage;

    // === Fire mode ===
    private enum FireMode { NORMAL, SPREAD3, RAPID, CHARGE }
    private FireMode fireMode = FireMode.NORMAL;
    private static final long PLAYER_FIRE_COOLDOWN_NS_NORMAL = 250_000_000L;
    private static final long PLAYER_FIRE_COOLDOWN_NS_RAPID  =  90_000_000L;

    // --- Charge State ---
    private boolean charging = false;
    private long chargeStartNs = 0L;

    // --- Charge Preview Visual ---
    private ImageView chargePreview;
    private static final double CHARGE_PREVIEW_MIN_SCALE = 0.8; // เริ่มโต
    private static final double CHARGE_PREVIEW_MAX_SCALE = 3.0;

    private static final long CHARGE_MIN_NS = 300_000_000L;   // 0.3s เริ่มมีผล
    private static final long CHARGE_MAX_NS = 1_600_000_000L;

    private WritableImage bulletSpriteNormal;
    private WritableImage bulletSpriteUpgraded;

    // --- Health bars ---
    private Rectangle bossHpBg, bossHpFg;
    private final java.util.Map<Minion, Rectangle[]> minionHpBars = new java.util.HashMap<>();

    private static final double HP_HEIGHT = 6.0;
    private static final double HP_PAD    = 1.0;

    // Lives HUD
    private Image heartImage;
    private final java.util.List<ImageView> lifeIcons = new java.util.ArrayList<>();
    private static final double HEART_W = 16, HEART_H = 16;
    private static final double HEART_GAP = 4;
    private static final double HEART_OFFSET_Y = -18;

    private final List<Animation> pausable = new ArrayList<>();
    private boolean paused = false;


    public GameView() {
        root = new Pane();
        drawEnvironment();

        int groundY = 368;
        int playerY = groundY + GRASS_HEIGHT - 64;
        player = new Player(100, playerY, "/se233/kellion/assets/Player.png", groundY);
        root.getChildren().add(player.getView());

        heartImage = new Image(getClass().getResource("/se233/kellion/assets/Life.png").toExternalForm());
        initLivesHUD();
        layoutLivesHUD();

        spawnMinionsForThisStage();

        Image characterSheet = new Image(getClass().getResource("/se233/kellion/assets/Characters.png").toExternalForm());
        bulletSpriteNormal = new WritableImage(characterSheet.getPixelReader(), 287, 805, 8, 16);
        bulletSprite = bulletSpriteNormal;

        Image upgraded = new Image(getClass().getResource("/se233/kellion/assets/Bullet_M1.png").toExternalForm());
        bulletSpriteUpgraded = new WritableImage(upgraded.getPixelReader(), 0, 0, 8, 8);

        bossBulletSprite = new Image(getClass().getResource("/se233/kellion/assets/BulletBoss_1.gif").toExternalForm());
        explosionFrames = ExplosionUtil.loadExplosionFrames(
                characterSheet, EXPLOSION_START_X, EXPLOSION_START_Y, EXPLOSION_FRAME_SIZE, EXPLOSION_FRAME_SPACE);

        // --- Score UI/Manager
        scoreText = new javafx.scene.text.Text(12, 30, "Score: 0");
        scoreText.setStyle("-fx-font-size:20; -fx-fill:white; -fx-font-weight:bold;");
        scoreText.setStroke(Color.BLACK);
        scoreText.setStrokeWidth(2);
        root.getChildren().add(scoreText);
        scoreManager = new ScoreManager(scoreText, 100, 20, 45, 300);

        try {
            var url = getClass().getResource("/se233/kellion/assets/MassDestructionFalcon.gif");
            if (url == null) {
                System.out.println("[PowerUp] Resource not found: /se233/kellion/assets/MassDestructionFalcon.gif");
                powerUpImage = null;
            } else {
                powerUpImage = new Image(url.toExternalForm());
                System.out.println("[PowerUp] Image loaded OK");
            }
        } catch (Exception ex) {
            System.out.println("[PowerUp] Failed to load image");
            ex.printStackTrace();
            powerUpImage = null;
        }

        if (DEBUG_MODE)
            updateAllHitboxes();
    }

    public <A extends Animation> A register(A anim) {
        if (anim != null) pausable.add(anim);
        return anim;
    }

    public void setPaused(boolean value) {
        if (this.paused == value) return;
        this.paused = value;
        if (value) {
            for (Animation a : pausable) { try { a.pause(); } catch (Exception ignored) {} }
        } else {
            for (Animation a : pausable) { try { a.play(); } catch (Exception ignored) {} }
        }
        // แจ้ง Minion ด้วย (เพื่อรองรับอนาคตถ้ามีแอนิเมชันภายใน Minion)
        for (var m : minions) {
            m.setPaused(value);
        }
        if (boss != null) boss.setPaused(value);
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

    public boolean tryFirePlayerBullet(long now) {
        if (playerDead || paused) return false;
        if (playerDead) return false;
        if (fireMode == FireMode.CHARGE) return false;
        long cd = (fireMode == FireMode.RAPID) ? PLAYER_FIRE_COOLDOWN_NS_RAPID : PLAYER_FIRE_COOLDOWN_NS_NORMAL;
        if (now - lastPlayerShotNs < cd) return false;

        fireBullet(player.getGunX(), player.getGunY(), player.isFacingRight());
        lastPlayerShotNs = now;
        return true;
    }

    public void onFirePress(long now) {
        if (playerDead || paused) return;
        if (playerDead) return;
        if (fireMode == FireMode.CHARGE) {
            charging = true;
            chargeStartNs = now;
            showChargePreview();
        }
    }

    public void onFireRelease(long now) {
        if (playerDead || paused) return;
        if (playerDead) return;
        if (fireMode == FireMode.CHARGE && charging) {
            charging = false;
            hideChargePreview();
            long dur = Math.max(CHARGE_MIN_NS, Math.min(CHARGE_MAX_NS, now - chargeStartNs));
            fireChargeBullet(dur);
        }
    }

    private void showChargePreview() {
        if (chargePreview == null) {
            chargePreview = new ImageView(getChargeSprite());
            chargePreview.setOpacity(0.9);
            chargePreview.setEffect(new DropShadow(10, Color.ORANGE));
            root.getChildren().add(chargePreview);
        }
        updateChargePreview(System.nanoTime());
    }

    private void hideChargePreview() {
        if (chargePreview != null) {
            root.getChildren().remove(chargePreview);
            chargePreview = null;
        }
    }

    private void updateChargePreview(long now) {
        if (chargePreview == null) return;

        long elapsed = now - chargeStartNs;
        double t = (elapsed - CHARGE_MIN_NS) / (double)(CHARGE_MAX_NS - CHARGE_MIN_NS);
        t = Math.max(0, Math.min(1, t));

        double s = CHARGE_PREVIEW_MIN_SCALE + (CHARGE_PREVIEW_MAX_SCALE - CHARGE_PREVIEW_MIN_SCALE) * t;

        double gx = player.getGunX();
        double gy = player.getGunY();

        boolean right = player.isFacingRight();
        chargePreview.setScaleX(right ?  s : -s);
        chargePreview.setScaleY(Math.abs(s));
        chargePreview.setX(gx);
        chargePreview.setY(gy);
        chargePreview.toFront();
    }

    public void updateChargingVisual(long now) {
        if (paused) return;
        if (fireMode == FireMode.CHARGE && charging) {
            updateChargePreview(now);
        }
    }

    private void fireChargeBullet(long durNs) {
        double t = (durNs - CHARGE_MIN_NS) / (double)(CHARGE_MAX_NS - CHARGE_MIN_NS);
        t = Math.max(0, Math.min(1, t));

        double scale = 1.3 + (3.0 - 1.3) * t;

        double speed = Config.BULLET_SPEED * (1.0 + 0.35 * t);

        int damage = 9999;

        boolean facingRight = player.isFacingRight();
        double x = player.getGunX();
        double y = player.getGunY();

        ImageView iv = new ImageView(getChargeSprite());
        iv.setScaleX(scale);
        iv.setScaleY(scale);
        iv.setX(x); iv.setY(y);

        int dir = facingRight ? 1 : -1;
        Bullet b = new Bullet(iv, dir * speed, 0, false);

        bullets.add(b);
        root.getChildren().add(iv);
        scoreManager.addShot();

        if (DEBUG_MODE) drawHitbox(iv, "bulletDebug_" + bullets.size(), Color.ORANGE);
        iv.getProperties().put("chargeDamage", damage);
    }

    /** Spawn a player bullet (uses BulletManager) */
    public void fireBullet(double x, double y, boolean facingRight) {
        if (playerDead) return;

        if (fireMode == FireMode.NORMAL || fireMode == FireMode.RAPID) {
            ImageView iv = new ImageView(bulletSprite);
            BulletManager.firePlayerBullet(bullets, root, x, y, iv, Config.BULLET_SPEED, facingRight);
            scoreManager.addShot();
            if (DEBUG_MODE) drawHitbox(iv, "bulletDebug_" + bullets.size(), Color.ORANGE);
            return;
        }
        // === SPREAD3 MODE:
        final double speed = Config.BULLET_SPEED;
        final int dir = facingRight ? 1 : -1;
        final double baseAngle = 0.0;
        final double spreadDeg = 12.0;
        double[] degs = new double[] { baseAngle, +spreadDeg, -spreadDeg };

        for (double d : degs) {
            double rad = Math.toRadians(d);
            double vx = dir * speed * Math.cos(rad);
            double vy = speed * Math.sin(rad);
            ImageView iv = new ImageView(bulletSprite);
            iv.setX(x);
            iv.setY(y);

            Bullet b = new Bullet(iv, vx, vy, false);
            bullets.add(b);
            root.getChildren().add(iv);

            if (DEBUG_MODE) drawHitbox(iv, "bulletDebug_" + bullets.size(), Color.ORANGE);
        }
        scoreManager.addShot();
    }

    private boolean spawnPowerUp(double x, double y) {
        try {
            ImageView iv;
            if (powerUpImage != null) {
                iv = new ImageView(powerUpImage);
                iv.setFitWidth(24);
                iv.setFitHeight(24);
                iv.setPreserveRatio(false);
            } else {
                WritableImage solid = new WritableImage(24, 24);
                PixelWriter pw = solid.getPixelWriter();
                for (int yy = 0; yy < 24; yy++)
                    for (int xx = 0; xx < 24; xx++)
                        pw.setColor(xx, yy, Color.GOLD);
                iv = new ImageView(solid);
            }

            iv.setX(x - 12);
            iv.setY(y - 12);

            Runnable apply = createPowerUpEffect();

            PowerUp p = new PowerUp(iv, apply);
            powerUps.add(p);
            root.getChildren().add(iv);
            iv.toFront();
            System.out.println("[PowerUp] Spawned at (" + iv.getX() + "," + iv.getY() + ")");
            return true;
        } catch (Exception ex) {
            System.out.println("[PowerUp] Spawn failed");
            ex.printStackTrace();
            return false;
        }
    }
    protected Runnable createPowerUpEffect() {
        return () -> {
            applySpread3Mode();
            setBulletSpriteImage(bulletSpriteUpgraded);
            markPowerUpCollected();
            showPickupToast("Spread Shot!");
        };
    }

    private void updatePowerUpsPickup() {
        if (powerUps.isEmpty()) return;

        Bounds pb = player.getHitboxBounds();
        List<PowerUp> toRemove = new ArrayList<>();
        for (PowerUp p: powerUps) {
            if (p.view.getBoundsInParent().intersects(pb)) {
                p.apply.run();
                root.getChildren().remove(p.view);
                toRemove.add(p);
            }
        }
        powerUps.removeAll(toRemove);
    }

    void showPickupToast(String message) {
        Bounds pb = player.getView().getBoundsInParent();
        double cx = pb.getMinX() + pb.getWidth() / 2.0;
        double topY = pb.getMinY() - 8; // เหนือหัวนิดหน่อย

        javafx.scene.text.Text toast = new javafx.scene.text.Text(message);
        toast.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-fill: #ffd54a;");
        toast.setStroke(Color.BLACK);
        toast.setStrokeWidth(1.5);
        toast.setX(cx);
        toast.setY(topY);
        toast.setOpacity(0);

        toast.setTranslateX(-toast.getLayoutBounds().getWidth() / 2.0);

        root.getChildren().add(toast);
        toast.toFront();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(120), toast);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        TranslateTransition up = new TranslateTransition(Duration.millis(650), toast);
        up.setByY(-24);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(350), toast);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setDelay(Duration.millis(450));

        ParallelTransition p = register(new ParallelTransition(fadeIn, up, fadeOut));
        p.setOnFinished(_ -> root.getChildren().remove(toast));
        p.play();
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
        if (paused) return;
        BulletManager.updateBullets(bullets, root, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
        if (DEBUG_MODE)
            updateAllHitboxes();
    }

    /** Update boss bullets (movement and out-of-bounds removal) */
    public void updateBossBullets() {
        if (paused) return;
        BulletManager.updateBullets(bossBullets, root, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
        if (DEBUG_MODE)
            updateAllBossBulletHitboxes();
    }

    /** Handle all collisions (player/boss/bullets) and score updates */
    public void checkCollisions() {
        if (paused) return;
        List<Bullet> toRemove = new ArrayList<>();
        // Player bullets hitting the boss
        if (boss != null && !boss.isDead()) {
            for (Bullet bullet : bullets) {
                if (CollisionUtil.intersects(bullet.getView(), boss.getView())) {
                    int dmg = 10;
                    Object cd = bullet.getView().getProperties().get("chargeDamage");
                    if (cd instanceof Integer) dmg = (Integer) cd;

                    boss.damage(dmg);
                    updateBossHpBar();
                    showExplosionEffect(bullet.getView().getX(), bullet.getView().getY());
                    toRemove.add(bullet);
                    root.getChildren().remove(bullet.getView());
                    scoreManager.addHit();
                    if (boss.isDead()) {
                        removeBossHpBar();
                        showBossDestroyedSprite();
                    }
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
        updatePowerUpsPickup();
        if (DEBUG_MODE) updateAllHitboxes();
    }

    /** Boss logic and bullet firing pattern. */
    public void updateBoss() {
        if (paused) {
            updateBossHpBar();
            return;
        }
        if (!bossSpawned || boss == null) {
            checkLevelTransition();
            updateBossHpBar();
            return;
        }
        if (boss.isDead()) {
            checkLevelTransition();
            updateBossHpBar();
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

        removeBossHpBar();
        boss1Defeated = true;
        scoreManager.applyStageClearBonuses(player);

        se233.kellion.util.TotalScore.addStageScore(scoreManager.getScore());
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
            updateLivesHUD();
            player.playDeathAnimation();
            System.out.println("Player killed by boss bullet! Lives left: " + player.getLives());
            if (player.getLives() > 0) {
                PauseTransition delay = register(new PauseTransition(Duration.millis(420)));
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

    void createBossHpBar() {
        bossHpBg = new Rectangle(120, HP_HEIGHT + 2*HP_PAD);
        bossHpBg.setArcWidth(6); bossHpBg.setArcHeight(6);
        bossHpBg.setFill(Color.color(0,0,0,0.5));

        bossHpFg = new Rectangle(120, HP_HEIGHT);
        bossHpFg.setArcWidth(6); bossHpFg.setArcHeight(6);
        bossHpFg.setFill(Color.LIMEGREEN);

        root.getChildren().addAll(bossHpBg, bossHpFg);
        bossHpBg.toFront(); bossHpFg.toFront();
        boss.getView().toFront();
    }

    void updateBossHpBar() {
        if (boss == null || bossHpFg == null) return;

        double ratio = Math.max(0, Math.min(1, boss.getHp() / (double) boss.getMaxHp()));
        double W = 120.0;

        bossHpFg.setWidth(W * ratio);

        Bounds bb = boss.getView().getBoundsInParent();
        double x = bb.getMinX() + (bb.getWidth() - W) / 2.0;
        double y = bb.getMinY() - 10.0;

        bossHpBg.setX(x);                bossHpBg.setY(y);
        bossHpFg.setX(x + HP_PAD);       bossHpFg.setY(y + HP_PAD);

        bossHpBg.toFront(); bossHpFg.toFront();
    }
    void removeBossHpBar() {
        if (bossHpBg != null) root.getChildren().remove(bossHpBg);
        if (bossHpFg != null) root.getChildren().remove(bossHpFg);
        bossHpBg = null; bossHpFg = null;
    }

    protected void spawnBoss() {
        int groundY = 368;
        double bossX = Config.WINDOW_WIDTH - Boss.SPRITE_WIDTH - 55;
        double bossY = groundY + GRASS_HEIGHT - Boss.SPRITE_HEIGHT - 48;

        boss = new Boss(bossX, bossY, "/se233/kellion/assets/Defense_Wall.png",500);
        boss.getView().setScaleX(2);
        boss.getView().setScaleY(2);
        root.getChildren().add(boss.getView());
        createBossHpBar();
        updateBossHpBar();
    }

    private void createMinionHpBar(Minion m) {
        Rectangle bg = new Rectangle(28, HP_HEIGHT + 2*HP_PAD);
        bg.setArcWidth(4); bg.setArcHeight(4);
        bg.setFill(Color.color(0,0,0,0.55));

        Rectangle fg = new Rectangle(28, HP_HEIGHT);
        fg.setArcWidth(4); fg.setArcHeight(4);
        fg.setFill(Color.ORANGERED);

        root.getChildren().addAll(bg, fg);
        minionHpBars.put(m, new Rectangle[]{bg, fg});
        updateMinionHpBar(m);
    }

    private void updateMinionHpBar(Minion m) {
        Rectangle[] bars = minionHpBars.get(m);
        if (bars == null) return;
        Rectangle bg = bars[0], fg = bars[1];

        double W = 28.0;
        double ratio = Math.max(0, Math.min(1, m.getHp() / (double) m.getMaxHp()));
        fg.setWidth(W * ratio);

        Bounds mb = m.getView().getBoundsInParent();
        double x = mb.getMinX() + (mb.getWidth() - W) / 2.0;
        double y = mb.getMinY() - 6.0;   // สูงกว่าหัวนิด

        bg.setX(x);            bg.setY(y);
        fg.setX(x + HP_PAD);   fg.setY(y + HP_PAD);

        bg.toFront(); fg.toFront(); m.getView().toFront();
    }

    private void removeMinionHpBar(Minion m) {
        Rectangle[] bars = minionHpBars.remove(m);
        if (bars != null) {
            root.getChildren().removeAll(bars[0], bars[1]);
        }
    }

    protected void spawnMinionsForThisStage() {
        addMinion(MinionKind.M1, 550, 340, 400, 550);
        addMinion(MinionKind.M1, 620, 340, 500, 600);
        addMinion(MinionKind.M1, 700, 340, 450, 570);
    }

    protected void addMinion(MinionKind kind, double x, double y, double patrolL, double patrolR) {
        Minion m = se233.kellion.model.MinionFactory.create(kind, x, y, patrolL, patrolR);
        minions.add(m);
        root.getChildren().add(m.getView());
        createMinionHpBar(m);
    }

    public void updateMinions(long now) {
        if (paused) return;
        for (Minion m : minions) {
            m.update(player, bossBullets, root, now);
            updateMinionHpBar(m);
        }

        List<Bullet> removePlayerBullets = new ArrayList<>();

        for (Bullet b : bullets) {
            Bounds bb = b.getView().getBoundsInParent();
            for (se233.kellion.model.Minion m : minions) {
                if (!m.isDead() && m.getBounds().intersects(bb)) {
                    Bounds mb = m.getBounds();
                    double cx = mb.getMinX() + mb.getWidth() / 2.0;
                    double cy = mb.getMinY() + mb.getHeight() / 2.0;

                    m.damage(1);
                    updateMinionHpBar(m);
                    showExplosionEffect(b.getView().getX(), b.getView().getY());

                    root.getChildren().remove(b.getView());
                    removePlayerBullets.add(b);

                    if (m.isDead()) {
                        removeMinionHpBar(m);
                        cx = Math.max(12, Math.min(cx, Config.WINDOW_WIDTH  - 12));
                        cy = Math.max(12, Math.min(cy,  Config.WINDOW_HEIGHT - 12));
                        lastMinionDeathX = cx;
                        lastMinionDeathY = cy;
                        root.getChildren().remove(m.getView());
                        scoreManager.addMinionKill(m.getKind());
                    }
                    break;
                }
            }
        }
        bullets.removeAll(removePlayerBullets);

        if (!minions.isEmpty()) {
            minions.removeIf(se233.kellion.model.Minion::isDead);
        }

        java.util.List<Minion> dead = new java.util.ArrayList<>();
        for (var entry : minionHpBars.keySet()) {
            if (entry.isDead()) dead.add(entry);
        }
        for (Minion d : dead) removeMinionHpBar(d);

        if (minions.isEmpty()) {
            if (!powerUpSpawned) {
                double sx = lastMinionDeathX;
                double sy = Math.min(lastMinionDeathY, 360);
                boolean ok = spawnPowerUp(sx, sy);
                if (ok) powerUpSpawned = true;
            }

            if (!bossSpawned && powerUpCollected) {
                bossSpawned = true;
                System.out.println("[Boss] Spawning after power-up collected");
                spawnBoss();
            }
        }
    }

    private void initLivesHUD() {
        for (var iv : lifeIcons) root.getChildren().remove(iv);
        lifeIcons.clear();

        int lives = Math.max(0, player.getLives());
        for (int i = 0; i < lives; i++) {
            ImageView iv = new ImageView(heartImage);
            iv.setFitWidth(HEART_W);
            iv.setFitHeight(HEART_H);
            iv.setPreserveRatio(false);
            iv.setMouseTransparent(true);
            lifeIcons.add(iv);
            root.getChildren().add(iv);
        }
    }

    private void layoutLivesHUD() {
        if (lifeIcons.isEmpty()) return;

        Bounds pb = player.getView().getBoundsInParent();
        double totalW = HEART_W * lifeIcons.size() + HEART_GAP * (lifeIcons.size() - 1);
        double startX = pb.getMinX() + (pb.getWidth() - totalW) / 2.0;
        double y = pb.getMinY() + HEART_OFFSET_Y;

        for (int i = 0; i < lifeIcons.size(); i++) {
            ImageView iv = lifeIcons.get(i);
            iv.setX(startX + i * (HEART_W + HEART_GAP));
            iv.setY(y);
            iv.toFront();
        }
        player.getView().toFront();
    }

    private void syncLivesHUD() {
        int lives = Math.max(0, player.getLives());
        if (lives != lifeIcons.size()) {
            initLivesHUD();
        }
        layoutLivesHUD();
    }

    /** เรียกทุกเฟรมจาก controller */
    public void updateLivesHUD() {
        syncLivesHUD();
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
    protected void applySpread3Mode() { fireMode = FireMode.SPREAD3; }
    protected void applyRapidMode()   { fireMode = FireMode.RAPID; }
    protected void setBulletSpriteImage(Image img) { this.bulletSprite = img; }
    protected void markPowerUpCollected() { this.powerUpCollected = true; }
    protected void applyChargeMode()  { fireMode = FireMode.CHARGE; }
    protected Image getChargeSprite() {return bulletSprite; }
    public boolean isPaused() { return paused; }
}