package se233.kellion.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

public class GameView2 extends GameView {
    private WritableImage bossFrame1;
    private WritableImage bossFrame2;

    public GameView2() {
        super(); // Setup core gameplay, player, boss, etc.

        // Stage 2: Set unique boss frames (after super() so boss exists)
        Image javaSheet = new Image(getClass().getResource("/se233/kellion/assets/Java.png").toExternalForm());
        int sheetW = (int) javaSheet.getWidth();
        int sheetH = (int) javaSheet.getHeight();
        int frames = 3;
        int FRAME_W = sheetW / frames;
        int FRAME_H = sheetH;
        bossFrame1 = new WritableImage(javaSheet.getPixelReader(), 0, 0, FRAME_W, FRAME_H - 17);
        bossFrame2 = new WritableImage(javaSheet.getPixelReader(), 112, 0, FRAME_W, 113);
        boss.getView().setImage(bossFrame1);
        boss.getView().setY(boss.getView().getY() - 30);
    }

    @Override
    protected void drawEnvironment() {
        // Draw unique Stage 2 (stalactite cave) background
        final int TILE_SIZE = 32, SKY_TILE_SIZE = 16, GRASS_HEIGHT = 16, SOIL_HEIGHT = 32;
        final int WATER_TILE_HEIGHT = 16, Stalactite_HEIGHT = 160;

        // Level tiles
        Image tileset = new Image(getClass().getResource("/se233/kellion/assets/Stage_2.png").toExternalForm());
        WritableImage grassTile = new WritableImage(tileset.getPixelReader(), 0, 0, TILE_SIZE, GRASS_HEIGHT);
        WritableImage soilTile = new WritableImage(tileset.getPixelReader(), 256, 0, TILE_SIZE, SOIL_HEIGHT);

        // Stalactites
        Image stalac = new Image(getClass().getResource("/se233/kellion/assets/STALACTITE.png").toExternalForm());
        WritableImage stalactiteTile = new WritableImage(stalac.getPixelReader(), 0, 0, 525, Stalactite_HEIGHT);

        // Water/waves
        Image waveset = new Image(getClass().getResource("/se233/kellion/assets/Wave_2.png").toExternalForm());
        WritableImage waterTile = new WritableImage(waveset.getPixelReader(), 0, 16, TILE_SIZE, TILE_SIZE);
        WritableImage[] waveTiles = {
            new WritableImage(waveset.getPixelReader(), 0, 0, TILE_SIZE, WATER_TILE_HEIGHT),
            new WritableImage(waveset.getPixelReader(), 32, 0, TILE_SIZE, WATER_TILE_HEIGHT)
        };

        // Sky variation tiles
        WritableImage[] skyTiles = new WritableImage[5];
        for (int i = 0; i < 5; i++)
            skyTiles[i] = new WritableImage(tileset.getPixelReader(), 48 + SKY_TILE_SIZE * i, 48, SKY_TILE_SIZE, SKY_TILE_SIZE);

        int groundY = 368;
        int skyRows = groundY / SKY_TILE_SIZE;
        int skyCols = se233.kellion.util.Config.WINDOW_WIDTH / SKY_TILE_SIZE;
        java.util.Random rand = new java.util.Random();

        // Sky background
        for (int row = 0; row < skyRows; row++)
            for (int col = 0; col < skyCols; col++) {
                ImageView skyView = new ImageView(skyTiles[rand.nextInt(skyTiles.length)]);
                skyView.setX(col * SKY_TILE_SIZE);
                skyView.setY(row * SKY_TILE_SIZE);
                root.getChildren().add(skyView);
            }

        // Stalactites across top
        int cols1 = (int) Math.ceil((double) se233.kellion.util.Config.WINDOW_WIDTH / 525.0);
        for (int c = 0; c < cols1; c++) {
            ImageView iv = new ImageView(stalactiteTile);
            iv.setX(c * 525);
            iv.setY(0);
            root.getChildren().add(iv);
            iv.toFront();
        }

        // Grass and soil tiles
        for (int col = 0; col < se233.kellion.util.Config.WINDOW_WIDTH / TILE_SIZE; col++) {
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

        // Water/waves below ground
        int waterStartY = groundY + GRASS_HEIGHT + SOIL_HEIGHT;
        int cols = se233.kellion.util.Config.WINDOW_WIDTH / TILE_SIZE;
        for (int c = 0; c < cols; c++) {
            int X = c * TILE_SIZE;
            int idx = c % 2;
            ImageView wave = new ImageView(waveTiles[idx]);
            wave.setX(X);
            wave.setY(waterStartY);
            root.getChildren().add(wave);
        }
        int Y = waterStartY + WATER_TILE_HEIGHT;
        while (Y < se233.kellion.util.Config.WINDOW_HEIGHT) {
            for (int c = 0; c < cols; c++) {
                ImageView deep = new ImageView(waterTile);
                deep.setX(c * TILE_SIZE);
                deep.setY(Y);
                root.getChildren().add(deep);
            }
            Y += (WATER_TILE_HEIGHT - 1);
        }
    }

    @Override
    protected void showBossDestroyedSprite() {
        // Custom boss defeat effect for Stage 2
        Image src = new Image(getClass().getResource("/se233/kellion/assets/Java.png").toExternalForm());
        WritableImage destroyedSprite = new WritableImage(src.getPixelReader(), 226, 0, 80, 40);
        boss.getView().setImage(destroyedSprite);
        boss.getView().setScaleX(2.0);
        boss.getView().setScaleY(2.0);
        boss.getView().setX(boss.getView().getX() + 15);
        boss.getView().setY(boss.getView().getY() - 25);
        player.getView().toFront();

        boss1Defeated = true;
        scoreManager.applyStageClearBonuses(player); // Inherited ScoreManager
    }

    @Override
    public void updateBoss() {
        // Custom attack animation/pattern for boss
        if (boss == null || boss.isDead()) {
            checkLevelTransition();
            return;
        }
        boss.getView().setX(se233.kellion.util.Config.WINDOW_WIDTH - se233.kellion.model.Boss.SPRITE_WIDTH - 20);

        bossFireCounter++;
        if (bossFireCounter >= se233.kellion.util.Config.BOSS_FIRE_INTERVAL && !playerDead) {
            double bulletX = boss.getView().getX() + boss.getView().getBoundsInParent().getWidth() * 0.10;
            double bulletY = boss.getView().getY() + boss.getView().getBoundsInParent().getHeight() * 0.30;

            javafx.geometry.Bounds p = player.getHitboxBounds();
            double tx = p.getMinX() + p.getWidth() / 2.0;
            double ty = p.getMinY() + p.getHeight();
            double s = se233.kellion.util.Config.BOSS_BULLET_SPEED + 1;

            boss.getView().setImage(bossFrame2);
            fireBossBulletAimed(bulletX - 20, bulletY - 20, tx, ty, s);
            fireBossBulletAimed(bulletX - 10, bulletY - 20, tx, ty, s);
            fireBossBulletAimed(bulletX + 0, bulletY - 20, tx, ty, s);
            fireBossBulletAimed(bulletX + 10, bulletY - 20, tx, ty, s);

            Timeline revert = new Timeline(new KeyFrame(javafx.util.Duration.millis(300),
                    e -> boss.getView().setImage(bossFrame1)));
            revert.play();

            bossFireCounter = 0;
        }
        if (DEBUG_MODE)
            updateAllHitboxes();
        // Level transition and other per-frame checks
        try {
            java.lang.reflect.Method m = getClass().getSuperclass().getDeclaredMethod("checkLevelTransition");
            m.setAccessible(true);
            m.invoke(this);
        } catch (Exception ignored) {}
    }

    // @Override
    // protected void showBossDestroyedSprite() {
    //     Image src = new Image(getClass().getResource("/se233/kellion/assets/Java.png").toExternalForm());
    //     WritableImage destroyedSprite = new WritableImage(src.getPixelReader(), 226, 0, 80, 40);
    //     boss.getView().setImage(destroyedSprite);
    //     boss.getView().setScaleX(2.0);
    //     boss.getView().setScaleY(2.0);
    //     boss.getView().setX(boss.getView().getX() + 15);
    //     boss.getView().setY(boss.getView().getY() - 25);
    //     player.getView().toFront();

    //     boss1Defeated = true;
    //     scoreManager.applyStageClearBonuses(player);
    // }
}
