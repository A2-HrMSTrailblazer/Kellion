package se233.kellion.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.util.Duration;

public class GameView3 extends GameView {
    private WritableImage bossFrame1;
    private WritableImage bossFrame2;

    public GameView3() {
        super();
        // Boss unique animation frames (after super so boss exists!)
        Image kingSheet = new Image(getClass().getResource("/se233/kellion/assets/Gomeramos_King.png").toExternalForm());
        int sheetW = (int) kingSheet.getWidth();
        int sheetH = (int) kingSheet.getHeight();
        int frames = 3;
        int FRAME_W = sheetW / frames, FRAME_H = sheetH;
        bossFrame1 = new WritableImage(kingSheet.getPixelReader(), 0, 0, FRAME_W, FRAME_H);
        bossFrame2 = new WritableImage(kingSheet.getPixelReader(), 88, 0, FRAME_W, FRAME_H);

        boss.getView().setImage(bossFrame1);
        boss.getView().setY(boss.getView().getY() - 30);
    }

    @Override
    protected void drawEnvironment() {
        // Stage 3 background and stalactites (reuse your working code here)
        final int TILE_SIZE = 32, SKY_TILE_SIZE = 16, GRASS_HEIGHT = 16, SOIL_HEIGHT = 32;
        final int WATER_TILE_HEIGHT = 16, Stalactite_HEIGHT = 160;

        Image tileset = new Image(getClass().getResource("/se233/kellion/assets/Stage_2.png").toExternalForm());
        WritableImage grassTile = new WritableImage(tileset.getPixelReader(), 0, 0, TILE_SIZE, GRASS_HEIGHT);
        WritableImage soilTile = new WritableImage(tileset.getPixelReader(), 256, 0, TILE_SIZE, SOIL_HEIGHT);

        Image stalac = new Image(getClass().getResource("/se233/kellion/assets/STALACTITE_2.png").toExternalForm());
        WritableImage stalactiteTile = new WritableImage(stalac.getPixelReader(), 0, 0, 800, Stalactite_HEIGHT);

        Image waveset = new Image(getClass().getResource("/se233/kellion/assets/Wave_2.png").toExternalForm());
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
        int skyCols = se233.kellion.util.Config.WINDOW_WIDTH / SKY_TILE_SIZE;
        java.util.Random rand = new java.util.Random();

        // Sky
        for (int row = 0; row < skyRows; row++)
            for (int col = 0; col < skyCols; col++) {
                ImageView skyView = new ImageView(skyTiles[rand.nextInt(skyTiles.length)]);
                skyView.setX(col * SKY_TILE_SIZE);
                skyView.setY(row * SKY_TILE_SIZE);
                root.getChildren().add(skyView);
            }

        // Wide stalactites across top
        int cols1 = (int) Math.ceil((double) se233.kellion.util.Config.WINDOW_WIDTH / 800.0);
        for (int c = 0; c < cols1; c++) {
            ImageView iv = new ImageView(stalactiteTile);
            iv.setX(c * 800);
            iv.setY(0);
            root.getChildren().add(iv);
            iv.toFront();
        }

        // Grass and soil
        for (int col = 0; col < se233.kellion.util.Config.WINDOW_WIDTH / TILE_SIZE; col++) {
            int x = col * TILE_SIZE;
            ImageView grassView = new ImageView(grassTile);
            grassView.setX(x); grassView.setY(groundY);
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
    public void updateBoss() {
        if (boss == null || boss.isDead()) {
            checkLevelTransition();
            return;
        }
        boss.getView().setX(se233.kellion.util.Config.WINDOW_WIDTH - se233.kellion.model.Boss.SPRITE_WIDTH - 20);

        bossFireCounter++;
        if (bossFireCounter >= se233.kellion.util.Config.BOSS_FIRE_INTERVAL && !playerDead) {
            double bulletX = boss.getView().getX() + boss.getView().getBoundsInParent().getWidth() * 0.10;
            double bulletY = boss.getView().getY() + boss.getView().getBoundsInParent().getHeight() * 0.30;

            // Unique multi-spread pattern (customize offsets as you wish)
            int[][] OFFSETS = {
                { -20, +100 }, { +50, +100 }, { -20, -120 }, { +50, -120 }
            };

            javafx.geometry.Bounds p = player.getHitboxBounds();
            double tx = p.getMinX() + p.getWidth() / 2.0;
            double ty = p.getMinY() + p.getHeight();
            double s = se233.kellion.util.Config.BOSS_BULLET_SPEED + 1;

            boss.getView().setImage(bossFrame2);
            for (int[] d : OFFSETS) {
                fireBossBulletAimed(bulletX + d[0], bulletY + d[1], tx, ty, s);
            }

            Timeline revert = new Timeline(new KeyFrame(Duration.millis(300),
                    e -> boss.getView().setImage(bossFrame1)));
            revert.play();

            bossFireCounter = 0;
        }
        if (DEBUG_MODE) updateAllHitboxes();
        checkLevelTransition();
    }

    @Override
    protected void showBossDestroyedSprite() {
        Image src = new Image(getClass().getResource("/se233/kellion/assets/Gomeramos_King.png").toExternalForm());
        WritableImage destroyedSprite = new WritableImage(src.getPixelReader(), 177, 0, 85, 144);
        boss.getView().setImage(destroyedSprite);
        boss.getView().setScaleX(2.0);
        boss.getView().setScaleY(2.0);
        player.getView().toFront();

        boss1Defeated = true;
        scoreManager.applyStageClearBonuses(player);
    }
}
