package se233.kellion.view;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.image.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import se233.kellion.model.Boss;
import se233.kellion.model.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView {
    private final Pane root;
    private final Player player;
    private final Boss boss;
    private final List<ImageView> bullets = new ArrayList<>();
    private final List<ImageView> bossBullets = new ArrayList<>();
    private final WritableImage bulletSprite;
    private final WritableImage bossBulletSprite;

    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 525;
    private static final int TILE_SIZE = 32;
    private static final int SKY_TILE_SIZE = 16;
    private static final int GRASS_HEIGHT = 16;
    private static final int SOIL_HEIGHT = 32;
    private static final int WATER_TILE_HEIGHT = 16;
    private static final int BULLET_SPEED = 3;

    private int bossFireCounter = 0;
    private static final int BOSS_FIRE_INTERVAL = 210;

    private static final boolean DEBUG_MODE = false; // toggle for hitbox display
    private boolean playerDead = false;

    public GameView() {
        root = new Pane();
        Image tileset = new Image(getClass().getResource("/se233/kellion/assets/Stage_1.png").toExternalForm());
        WritableImage grassTile = new WritableImage(tileset.getPixelReader(), 0, 0, TILE_SIZE, GRASS_HEIGHT);
        WritableImage soilTile = new WritableImage(tileset.getPixelReader(), 256, 0, TILE_SIZE, SOIL_HEIGHT);
        Image waveset = new Image(getClass().getResource("/se233/kellion/assets/Wave.png").toExternalForm());
        WritableImage waterTile = new WritableImage(waveset.getPixelReader(), 0, 16, TILE_SIZE, TILE_SIZE);
        WritableImage[] waveTiles = {
                new WritableImage(waveset.getPixelReader(), 0, 0, TILE_SIZE, WATER_TILE_HEIGHT),
                new WritableImage(waveset.getPixelReader(), 32, 0, TILE_SIZE, WATER_TILE_HEIGHT)
        };

        // Sky background
        WritableImage[] skyTiles = new WritableImage[5];
        for (int i = 0; i < 5; i++)
            skyTiles[i] = new WritableImage(tileset.getPixelReader(), 48 + SKY_TILE_SIZE * i, 48,
                    SKY_TILE_SIZE, SKY_TILE_SIZE);

        int groundY = 368;
        int skyRows = groundY / SKY_TILE_SIZE;
        int skyCols = WINDOW_WIDTH / SKY_TILE_SIZE;
        Random rand = new Random();

        for (int row = 0; row < skyRows; row++) {
            for (int col = 0; col < skyCols; col++) {
                ImageView skyView = new ImageView(skyTiles[rand.nextInt(skyTiles.length)]);
                skyView.setX(col * SKY_TILE_SIZE);
                skyView.setY(row * SKY_TILE_SIZE);
                root.getChildren().add(skyView);
            }
        }

        // Grass + soil
        for (int col = 0; col < WINDOW_WIDTH / TILE_SIZE; col++) {
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

        // Wave and water
        int waterStartY = groundY + GRASS_HEIGHT + SOIL_HEIGHT;
        int cols = WINDOW_WIDTH / TILE_SIZE;

        for (int c = 0; c < cols; c++) {
            int X = c * TILE_SIZE;
            int idx = c % 2;
            ImageView wave = new ImageView(waveTiles[idx]);
            wave.setX(X);
            wave.setY(waterStartY);
            wave.setSmooth(false);
            root.getChildren().add(wave);
        }

        int Y = waterStartY + WATER_TILE_HEIGHT;
        while (Y < WINDOW_HEIGHT) {
            for (int c = 0; c < cols; c++) {
                int X = c * TILE_SIZE;
                ImageView deep = new ImageView(waterTile);
                deep.setX(X);
                deep.setY(Y);
                deep.setSmooth(false);
                root.getChildren().add(deep);
            }
            Y += (WATER_TILE_HEIGHT - 1);
        }

        // Player
        int playerY = groundY + GRASS_HEIGHT - 64;
        player = new Player(100, playerY, "/se233/kellion/assets/Player.png", groundY);
        root.getChildren().add(player.getView());

        // Boss
        double bossX = WINDOW_WIDTH - Boss.SPRITE_WIDTH - 55;
        double bossY = groundY + GRASS_HEIGHT - Boss.SPRITE_HEIGHT - 48;
        boss = new Boss(bossX, bossY, "/se233/kellion/assets/Defense_Wall.png");
        boss.getView().setScaleX(2);
        boss.getView().setScaleY(2);
        root.getChildren().add(boss.getView());

        // Bullet sprite
        Image characterSheet = new Image(
                getClass().getResource("/se233/kellion/assets/Characters.png").toExternalForm());
        bulletSprite = new WritableImage(characterSheet.getPixelReader(), 287, 805, 8, 16);
        bossBulletSprite = new WritableImage(characterSheet.getPixelReader(), 368, 805, 8, 16);

        if (DEBUG_MODE)
            updateAllHitboxes();
    }

    // --- Fire bullets ---
    public void fireBullet(double x, double y, boolean facingRight) {
        ImageView bullet = new ImageView(bulletSprite);
        bullet.setX(x);
        bullet.setY(y);
        bullet.setScaleX(facingRight ? 1 : -1);
        root.getChildren().add(bullet);
        bullets.add(bullet);

        if (DEBUG_MODE)
            drawHitbox(bullet, "bulletDebug_" + bullets.size(), Color.ORANGE);
    }

    public void fireBossBullet(double x, double y, boolean toLeft) {
        ImageView bullet = new ImageView(bossBulletSprite);
        bullet.setX(x);
        bullet.setY(y);
        bullet.setScaleX(toLeft ? -1 : 1);
        root.getChildren().add(bullet);
        bossBullets.add(bullet);

        if (DEBUG_MODE)
            drawHitbox(bullet, "bossBulletDebug_" + bossBullets.size(), Color.CYAN);
    }

    // --- Bullet update ---
    public void updateBullets() {
        List<ImageView> toRemove = new ArrayList<>();
        for (ImageView bullet : bullets) {
            bullet.setX(bullet.getX() + (bullet.getScaleX() > 0 ? BULLET_SPEED : -BULLET_SPEED));
            if (bullet.getX() < -20 || bullet.getX() > WINDOW_WIDTH + 20) {
                toRemove.add(bullet);
                root.getChildren().remove(bullet);
                root.getChildren()
                        .removeIf(n -> n.getId() != null && n.getId().equals("bulletDebug_" + bullet.hashCode()));
            }
        }
        bullets.removeAll(toRemove);
        if (DEBUG_MODE)
            updateAllHitboxes();
    }

    public void updateBossBullets() {
        int speed = 2; // Boss bullet speed
        List<ImageView> toRemove = new ArrayList<>();
        for (ImageView bullet : bossBullets) {
            bullet.setX(bullet.getX() + (bullet.getScaleX() > 0 ? speed : -speed));
            if (bullet.getX() < -40 || bullet.getX() > WINDOW_WIDTH + 40) {
                toRemove.add(bullet);
                root.getChildren().remove(bullet);
            }
        }
        bossBullets.removeAll(toRemove);
        if (DEBUG_MODE)
            updateAllBossBulletHitboxes();
    }

    // --- Collision detection ---
    public void checkCollisions() {
        List<ImageView> toRemove = new ArrayList<>();

        // Bullet vs Boss
        if (boss != null && !boss.isDead() && root.getChildren().contains(boss.getView())) {
            for (ImageView bullet : bullets) {
                if (bullet.getBoundsInParent().intersects(boss.getBounds())) {
                    boss.damage(10);
                    toRemove.add(bullet);
                    root.getChildren().remove(bullet);

                    if (boss.isDead()) {
                        showBossDestroyedSprite();
                        System.out.println("Boss defeated!");
                    }
                }
            }
        }

        // Player vs Boss collision: block player from moving past boss when alive
        if (boss != null && !boss.isDead() && root.getChildren().contains(boss.getView())) {
            // Use the expanded boss hitbox for stricter boundaries
            double expandX = 0; // Tune as needed
            double expandY = 10;
            Bounds bossHitbox = boss.getCustomBounds(expandX, expandY);
            Bounds playerHitbox = player.getView().getBoundsInParent();

            if (playerHitbox.intersects(bossHitbox)) {
                // We'll assume player approaches from the left; block their right edge at
                // boss's left
                player.getView().setX(bossHitbox.getMinX() - playerHitbox.getWidth());
            }

            // For debugging, draw the boss's custom hitbox
            if (DEBUG_MODE) {
                Rectangle r = new Rectangle(
                        bossHitbox.getMinX(), bossHitbox.getMinY(),
                        bossHitbox.getWidth(), bossHitbox.getHeight());
                r.setFill(Color.TRANSPARENT);
                r.setStroke(Color.BLUE);
                r.setStrokeWidth(2.0);
                r.setId("debugBossHitbox");
                // Remove previous if any (optional)
                root.getChildren()
                        .removeIf(node -> node instanceof Rectangle && "debugBossHitbox".equals(node.getId()));
                root.getChildren().add(r);
            }
        }

        // BossBullets vs Player
        List<ImageView> bossBulletsToRemove = new ArrayList<>();
        for (ImageView bullet : bossBullets) {
            if (!playerDead && bullet.getBoundsInParent().intersects(player.getView().getBoundsInParent())) {
                killPlayer();
                root.getChildren().remove(bullet);
                bossBulletsToRemove.add(bullet);
                // (Optional) break if you want only one hit per frame
                break;
            }
        }
        bossBullets.removeAll(bossBulletsToRemove);

        bullets.removeAll(toRemove);
        if (DEBUG_MODE)
            updateAllHitboxes();
    }

    // --- Boss behavior ---
    public void updateBoss() {
        if (boss == null || boss.isDead())
            return;

        boss.getView().setX(WINDOW_WIDTH - Boss.SPRITE_WIDTH - 20);

        // Boss fires every interval
        bossFireCounter++;
        if (bossFireCounter >= BOSS_FIRE_INTERVAL && !playerDead) {
            // Fire bullet from boss toward left
            double bulletX = boss.getView().getX();
            double bulletY = boss.getView().getY() + boss.getView().getBoundsInParent().getHeight() / 2;
            double offset = 20;
            fireBossBullet(bulletX - 50, bulletY - 80, true);
            fireBossBullet(bulletX, bulletY - 80, true);
            bossFireCounter = 0; // reset
        }

        if (DEBUG_MODE)
            updateAllHitboxes();
    }

    private void showBossDestroyedSprite() {
        Image src = new Image(getClass().getResource("/se233/kellion/assets/Defense_Wall.png").toExternalForm());
        WritableImage destroyedSprite = new WritableImage(src.getPixelReader(), 113, 96, 110, 64);
        boss.getView().setImage(destroyedSprite);
        boss.getView().setScaleX(2.0);
        boss.getView().setScaleY(2.0);
        boss.getView().setY(boss.getView().getY() + 146);
        player.getView().toFront();
    }

    // --- Debug methods ---
    private void drawHitbox(Node node, String id, Color color) {
        if (node == null)
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
        root.getChildren().removeIf(n -> n instanceof Rectangle && n.getId() != null &&
                (n.getId().startsWith("debug") || n.getId().contains("bulletDebug")));

        drawHitbox(player.getView(), "debugPlayer", Color.LIME);
        if (boss != null && !boss.isDead())
            drawHitbox(boss.getView(), "debugBoss", Color.RED);

        for (int i = 0; i < bullets.size(); i++) {
            ImageView b = bullets.get(i);
            drawHitbox(b, "bulletDebug_" + i, Color.ORANGE);
        }
    }

    private void updateAllBossBulletHitboxes() {
        for (int i = 0; i < bossBullets.size(); i++) {
            drawHitbox(bossBullets.get(i), "bossBulletDebug_" + i, Color.CYAN);
        }
    }

    // Immediately "kill" the player and show effect
    public void killPlayer() {
        if (!playerDead) {
            playerDead = true;
            player.getView().setOpacity(0.4); // death effect visual
            System.out.println("Player killed by boss bullet!");
            // add game over effect here
        }
    }

    public boolean isPlayerDead() {
        return playerDead;
    }

    // --- Getters ---
    public Pane getRoot() {
        return root;
    }

    public Player getPlayer() {
        return player;
    }

    public List<ImageView> getBullets() {
        return bullets;
    }

    public Boss getBoss() {
        return boss;
    }
}
