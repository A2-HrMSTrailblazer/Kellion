package se233.kellion.view;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import se233.kellion.model.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView {
    private Pane root;
    private Player player;
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 525;
    private static final int TILE_SIZE = 32;
    private static final int SKY_TILE_SIZE = 16;
    private static final int GRASS_HEIGHT = 16;
    private static final int SOIL_HEIGHT = 32;
    private static final int WATER_TILE_HEIGHT = 16;
    private List<ImageView> bullets = new ArrayList<>();
    private WritableImage bulletSprite;
    private static final int BULLET_SPEED = 5;

    public GameView() {
        root = new Pane();
        Image tileset = new Image(getClass().getResource("/se233/kellion/assets/Stage_1.png").toExternalForm());
        WritableImage grassTile = new WritableImage(tileset.getPixelReader(), 0, 0, TILE_SIZE, GRASS_HEIGHT);
        WritableImage soilTile = new WritableImage(tileset.getPixelReader(), 256, 0, TILE_SIZE, SOIL_HEIGHT);
        Image waveset = new Image(getClass().getResource("/se233/kellion/assets/Wave.png").toExternalForm());
        WritableImage waterTile = new WritableImage(waveset.getPixelReader(), 0, 16, TILE_SIZE, TILE_SIZE);
        WritableImage[] waveTiles = new WritableImage[]{
                new WritableImage(waveset.getPixelReader(), 0, 0, TILE_SIZE, WATER_TILE_HEIGHT),
                new WritableImage(waveset.getPixelReader(), 32, 0, TILE_SIZE, WATER_TILE_HEIGHT),
        };

        // Sky tiles: crop 5 randomizable 16x16 starting from (48,48)
        WritableImage[] skyTiles = new WritableImage[5];
        for (int i = 0; i < 5; i++)
            skyTiles[i] = new WritableImage(tileset.getPixelReader(), 48 + SKY_TILE_SIZE * i, 48, SKY_TILE_SIZE, SKY_TILE_SIZE);

        // Set the groundY so that grass and soil fill the lower part of the window
        int groundY = 368; // Top of grass
        int skyRows = groundY / SKY_TILE_SIZE;
        int skyCols = WINDOW_WIDTH / SKY_TILE_SIZE;
        java.util.Random rand = new java.util.Random();

        // Draw sky tiles to fill from top to (just above ground)
        for (int row = 0; row < skyRows; row++) {
            for (int col = 0; col < skyCols; col++) {
                int x = col * SKY_TILE_SIZE;
                int y = row * SKY_TILE_SIZE;
                ImageView skyView = new ImageView(skyTiles[rand.nextInt(skyTiles.length)]);
                skyView.setX(x);
                skyView.setY(y);
                root.getChildren().add(skyView);
            }
        }

        // Draw grass and soil across bottom
        int groundCols = WINDOW_WIDTH / TILE_SIZE;
        int SOIL_LAYERS = 1;
        for (int col = 0; col < groundCols; col++) {
            int x = col * TILE_SIZE;

            // Grass at groundY
            ImageView grassView = new ImageView(grassTile);
            grassView.setX(x);
            grassView.setY(groundY);
            root.getChildren().add(grassView);

            // Soil directly below
            int currentY = groundY + GRASS_HEIGHT;
            for (int i = 0; i < SOIL_LAYERS && currentY < WINDOW_HEIGHT; i++) {
                ImageView soilView = new ImageView(soilTile);
                soilView.setX(x);
                soilView.setY(currentY);
                root.getChildren().add(soilView);
                currentY += SOIL_HEIGHT;
            }

            // Fill Wave surface
            int waterStartY = currentY;
            int rows = (int) Math.ceil((double)(WINDOW_HEIGHT - waterStartY) / WATER_TILE_HEIGHT);
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

            //Fill water
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
        }

        // Place player just above the grass level
        int playerY = groundY - 48; // adjust -48 to your player sprite height
        player = new Player(100, playerY, "/se233/kellion/assets/Player.png");
        root.getChildren().add(player.getView());

        // Bullet sprite
        Image characterSheet = new Image(getClass().getResource("/se233/kellion/assets/Characters.png").toExternalForm());
        bulletSprite = new WritableImage(characterSheet.getPixelReader(), 287, 805, 8, 16);
    }

    // Fire bullet
    public void fireBullet(double x, double y, boolean facingRight) {
        ImageView bullet = new ImageView(bulletSprite);
        bullet.setX(x);
        bullet.setY(y);
        bullet.setScaleX(facingRight ? 1 : -1);
        root.getChildren().add(bullet);
        bullets.add(bullet);
    }

    // bullet update
    public void updateBullets() {
        List<ImageView> toRemove = new ArrayList<>();
        for (ImageView bullet : bullets) {
            bullet.setX(bullet.getX() + (bullet.getScaleX() > 0 ? BULLET_SPEED : -BULLET_SPEED));
            // Remove bullet if out of bounds
            if (bullet.getX() < -20 || bullet.getX() > 820) {
                toRemove.add(bullet);
                root.getChildren().remove(bullet);
            }
        }
        bullets.removeAll(toRemove);
    }

    public Pane getRoot() { return root; }
    public Player getPlayer() { return player; }
    public List<ImageView> getBullets() { return bullets; }
}
