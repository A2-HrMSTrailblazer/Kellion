package se233.kellion.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.util.Duration;
import se233.kellion.model.Boss;
import se233.kellion.model.MinionKind;
import se233.kellion.util.Config;

public class GameView2 extends GameView {
    private WritableImage bossFrame1;
    private WritableImage bossFrame2;
    private Image rapidBulletSprite;

    public GameView2() {
        super();

        rapidBulletSprite = new Image(
                getClass().getResource("/se233/kellion/assets/BulletBoss_1.gif").toExternalForm()
        );
        this.bossBulletSprite = new Image(getClass().getResource("/se233/kellion/assets/BulletBoss_2.gif").toExternalForm());

        Image javaSheet = new Image(getClass().getResource("/se233/kellion/assets/Java.png").toExternalForm());
        int sheetW = (int) javaSheet.getWidth();
        int sheetH = (int) javaSheet.getHeight();
        int frames = 3;
        int FRAME_W = sheetW / frames;
        int FRAME_H = sheetH;
        bossFrame1 = new WritableImage(javaSheet.getPixelReader(), 0,   0, FRAME_W, FRAME_H - 17);
        bossFrame2 = new WritableImage(javaSheet.getPixelReader(), 112, 0, FRAME_W, 113);
    }

    @Override
    protected void drawEnvironment() {
        final int TILE_SIZE = 32, SKY_TILE_SIZE = 16, GRASS_HEIGHT = 16, SOIL_HEIGHT = 32;
        final int WATER_TILE_HEIGHT = 16, STALACTITE_HEIGHT = 160;

        Image tileset = new Image(getClass().getResource("/se233/kellion/assets/Stage_2.png").toExternalForm());
        WritableImage grassTile = new WritableImage(tileset.getPixelReader(), 0, 0, TILE_SIZE, GRASS_HEIGHT);
        WritableImage soilTile  = new WritableImage(tileset.getPixelReader(), 256, 0, TILE_SIZE, SOIL_HEIGHT);

        Image stalac = new Image(getClass().getResource("/se233/kellion/assets/STALACTITE.png").toExternalForm());
        WritableImage stalactiteTile = new WritableImage(stalac.getPixelReader(), 0, 0, 525, STALACTITE_HEIGHT);

        Image waveset = new Image(getClass().getResource("/se233/kellion/assets/Wave_2.png").toExternalForm());
        WritableImage waterTile = new WritableImage(waveset.getPixelReader(), 0, 16, TILE_SIZE, TILE_SIZE);
        WritableImage[] waveTiles = {
                new WritableImage(waveset.getPixelReader(), 0,  0, TILE_SIZE, WATER_TILE_HEIGHT),
                new WritableImage(waveset.getPixelReader(), 32, 0, TILE_SIZE, WATER_TILE_HEIGHT)
        };

        WritableImage[] skyTiles = new WritableImage[5];
        for (int i = 0; i < 5; i++)
            skyTiles[i] = new WritableImage(tileset.getPixelReader(), 48 + SKY_TILE_SIZE * i, 48, SKY_TILE_SIZE, SKY_TILE_SIZE);

        int groundY = 368;
        int skyRows = groundY / SKY_TILE_SIZE;
        int skyCols = Config.WINDOW_WIDTH / SKY_TILE_SIZE;
        java.util.Random rand = new java.util.Random();

        // sky
        for (int row = 0; row < skyRows; row++)
            for (int col = 0; col < skyCols; col++) {
                ImageView skyView = new ImageView(skyTiles[rand.nextInt(skyTiles.length)]);
                skyView.setX(col * SKY_TILE_SIZE);
                skyView.setY(row * SKY_TILE_SIZE);
                root.getChildren().add(skyView);
            }

        // stalactites
        int cols1 = (int) Math.ceil((double) Config.WINDOW_WIDTH / 525.0);
        for (int c = 0; c < cols1; c++) {
            ImageView iv = new ImageView(stalactiteTile);
            iv.setX(c * 525);
            iv.setY(0);
            root.getChildren().add(iv);
            iv.toFront();
        }

        // ground
        for (int col = 0; col < Config.WINDOW_WIDTH / TILE_SIZE; col++) {
            int x = col * TILE_SIZE;
            ImageView grassView = new ImageView(grassTile);
            grassView.setX(x); grassView.setY(368);
            root.getChildren().add(grassView);

            ImageView soilView = new ImageView(soilTile);
            soilView.setX(x); soilView.setY(368 + GRASS_HEIGHT);
            root.getChildren().add(soilView);
        }

        // water
        int waterStartY = 368 + GRASS_HEIGHT + SOIL_HEIGHT;
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

    @Override
    protected void showBossDestroyedSprite() {
        if (boss == null) return;
        Image src = new Image(getClass().getResource("/se233/kellion/assets/Java.png").toExternalForm());
        WritableImage destroyedSprite = new WritableImage(src.getPixelReader(), 226, 0, 80, 40);
        boss.getView().setImage(destroyedSprite);
        boss.getView().setScaleX(2.0);
        boss.getView().setScaleY(2.0);
        boss.getView().setX(boss.getView().getX() + 15);
        boss.getView().setY(boss.getView().getY() - 25);
        player.getView().toFront();

        removeBossHpBar();
        boss1Defeated = true;
        scoreManager.applyStageClearBonuses(player);
        se233.kellion.util.TotalScore.addStageScore(scoreManager.getScore());
    }

    @Override
    public void updateBoss() {
        if (!bossSpawned || boss == null) {
            updateBossHpBar();
            checkLevelTransition();
            return;
        }
        if (boss.isDead()) {
            updateBossHpBar();
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
            double s  = Config.BOSS_BULLET_SPEED +0.3;

            double baseAngle = Math.atan2(ty - bulletY, tx - bulletX);
            double[] offsetsDeg = { -20, -4, 10};
            double range = 1200.0;

            for (double deg : offsetsDeg) {
                double ang = baseAngle + Math.toRadians(deg);
                double tx2 = bulletX + Math.cos(ang) * range;
                double ty2 = bulletY + Math.sin(ang) * range;
                fireBossBulletAimed(bulletX, bulletY, tx2, ty2, s);
            }

            Timeline revert = new Timeline(new KeyFrame(Duration.millis(50),
                    e -> { if (boss != null && !boss.isDead()) boss.getView().setImage(bossFrame1); }));
            revert.play();

            bossFireCounter = 0;
        }
        updateBossHpBar();
        if (DEBUG_MODE) updateAllHitboxes();
        checkLevelTransition();
    }

    @Override
    protected void spawnMinionsForThisStage() {
        addMinion(MinionKind.M2, 500, 340, 500,  550);
        addMinion(MinionKind.M2, 450, 340, 450, 520);
        addMinion(MinionKind.M2, 650, 340, 500, 570);
        addMinion(MinionKind.M2, 720, 340, 400, 600);
    }

    @Override
    protected void spawnBoss() {
        int groundY = 368;
        double bossX = Config.WINDOW_WIDTH - Boss.SPRITE_WIDTH - 55;
        double bossY = groundY + 15 - Boss.SPRITE_HEIGHT -80;
        boss = Boss.JavaBoss(bossX, bossY);
        boss.getView().setScaleX(2);
        boss.getView().setScaleY(2);
        root.getChildren().add(boss.getView());
        boss.getView().toFront();
        player.getView().toFront();
        createBossHpBar();
        updateBossHpBar();
    }

    @Override
    protected void addMinion(MinionKind kind, double x, double y, double L, double R) {
        super.addMinion(kind, x, y, L, R);
    }

    @Override
    public void updateMinions(long now) {
        super.updateMinions(now);
    }

    @Override
    protected Runnable createPowerUpEffect() {
        return () -> {
            applyRapidMode();
            setBulletSpriteImage(rapidBulletSprite);
            markPowerUpCollected();
            showPickupToast("Rapid Fire!");
        };
    }
}
