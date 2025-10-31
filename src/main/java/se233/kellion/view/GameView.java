package se233.kellion.view;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.image.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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

    // Tile and layout constants
    private static final int TILE_SIZE = 32;
    private static final int SKY_TILE_SIZE = 16;
    private static final int GRASS_HEIGHT = 16;
    private static final int SOIL_HEIGHT = 32;
    private static final int WATER_TILE_HEIGHT = 16;

    private int bossFireCounter = 0;
    private static final boolean DEBUG_MODE = false;
    private boolean playerDead = false;
    private static final boolean DEBUG_HITBOX = false;

    private boolean boss1Defeated = false;
    private Runnable onNextScene = () -> {};
    private boolean nextSceneTriggered = false;

    // Constructor: Sets up background, ground, player, boss, and assets
    public GameView() {
        root = new Pane();

        // Load tileset and cut tiles from the spritesheet
        Image tileset = new Image(getClass().getResource("/se233/kellion/assets/Stage_1.png").toExternalForm());
        WritableImage grassTile = new WritableImage(tileset.getPixelReader(), 0, 0, TILE_SIZE, GRASS_HEIGHT);
        WritableImage soilTile = new WritableImage(tileset.getPixelReader(), 256, 0, TILE_SIZE, SOIL_HEIGHT);

        // Water/wave tiles
        Image waveset = new Image(getClass().getResource("/se233/kellion/assets/Wave.png").toExternalForm());
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
        boss.getView().setScaleX(2);
        boss.getView().setScaleY(2);
        root.getChildren().add(boss.getView());

        // Load bullet sprites
        Image characterSheet = new Image(
                getClass().getResource("/se233/kellion/assets/Characters.png").toExternalForm());
        bulletSprite = new WritableImage(characterSheet.getPixelReader(), 287, 805, 8, 16);
        bossBulletSprite = new WritableImage(characterSheet.getPixelReader(), 368, 805, 8, 16);

        if (DEBUG_MODE)
            updateAllHitboxes();
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

        if (DEBUG_MODE)
            drawHitbox(iv, "bulletDebug_" + bullets.size(), Color.ORANGE);
    }

    public void fireBossBullet(double x, double y, boolean toLeft) {
        ImageView iv = new ImageView(bossBulletSprite);
        iv.setX(x);
        iv.setY(y);
        iv.setScaleX(toLeft ? -1 : 1);
        Bullet bullet = new Bullet(iv, Config.BOSS_BULLET_SPEED, true);
        bossBullets.add(bullet);
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
                    boss.damage(10);
                    toRemove.add(bullet);
                    root.getChildren().remove(bullet.getView());

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
            if (!playerDead && CollisionUtil.intersects(bullet.getView(), player.getView())) {
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
            double bulletX = boss.getView().getX();
            double bulletY = boss.getView().getY() + boss.getView().getBoundsInParent().getHeight() / 2;
            fireBossBullet(bulletX - 50, bulletY - 80, true);
            fireBossBullet(bulletX, bulletY - 80, true);
            bossFireCounter = 0;
        }

        if (DEBUG_MODE)
            updateAllHitboxes();
        checkLevelTransition();
    }

    // Change boss sprite to destroyed form
    private void showBossDestroyedSprite() {
        Image src = new Image(getClass().getResource("/se233/kellion/assets/Defense_Wall.png").toExternalForm());
        WritableImage destroyedSprite = new WritableImage(src.getPixelReader(), 113, 96, 110, 64);
        boss.getView().setImage(destroyedSprite);
        boss.getView().setScaleX(2.0);
        boss.getView().setScaleY(2.0);
        boss.getView().setY(boss.getView().getY() + 146);
        player.getView().toFront();

        boss1Defeated = true;
    }

    // Debug helpers: draw rectangles around game objects
    private void drawHitbox(Node node, String id, Color color) {
        if (!DEBUG_HITBOX || node == null) return;
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

        for (int i = 0; i < bullets.size(); i++) {
            drawHitbox(bullets.get(i).getView(), "bulletDebug_" + i, Color.ORANGE);
        }
    }

    private void updateAllBossBulletHitboxes() {
        if (!DEBUG_HITBOX) {
            root.getChildren().removeIf(n -> n instanceof Rectangle && n.getId() != null &&
                    n.getId().contains("bossBulletDebug"));
            return;
        }

        root.getChildren().removeIf(n -> n instanceof Rectangle && n.getId() != null &&
                n.getId().contains("bossBulletDebug"));
        for (int i = 0; i < bossBullets.size(); i++) {
            drawHitbox(bossBullets.get(i).getView(), "bossBulletDebug_" + i, Color.CYAN);
        }
    }

    private void checkLevelTransition() {
        if (!boss1Defeated || nextSceneTriggered) return;

        double playerRight = player.getView().getX() + player.getView().getFitWidth();
        if (playerRight >= Config.WINDOW_WIDTH - 4) {
            nextSceneTriggered = true;
            Runnable go = onNextScene;
            onNextScene = () -> {};
            go.run();
        }
    }


    // Player death
    public void killPlayer() {
        if (!playerDead) {
            playerDead = true;
            player.getView().setOpacity(0.4); // fade effect
            System.out.println("Player killed by boss bullet!");
        }
    }

    public void setOnNextScene(Runnable r) {
        this.onNextScene = (r != null) ? r : () -> {};
    }

    public boolean isBoss1Defeated() {
        return boss1Defeated;
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

    public Boss getBoss() {
        return boss;
    }

    public boolean isPlayerDead() {
        return playerDead;
    }
}
