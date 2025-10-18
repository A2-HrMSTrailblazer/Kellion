package se233.kellion.view;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import se233.kellion.model.Player;
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

    public GameView() {
        root = new Pane();
        Image tileset = new Image(getClass().getResource("/se233/kellion/assets/Stage_1.png").toExternalForm());
        WritableImage grassTile = new WritableImage(tileset.getPixelReader(), 0, 0, TILE_SIZE, GRASS_HEIGHT);
        WritableImage soilTile = new WritableImage(tileset.getPixelReader(), 256, 0, TILE_SIZE, SOIL_HEIGHT);

        // Sky tiles: crop 5 randomizable 16x16 starting from (48,48)
        WritableImage[] skyTiles = new WritableImage[5];
        for (int i = 0; i < 5; i++)
            skyTiles[i] = new WritableImage(tileset.getPixelReader(), 48 + SKY_TILE_SIZE * i, 48, SKY_TILE_SIZE, SKY_TILE_SIZE);

        // Set the groundY so that grass and soil fill the lower part of the window
        int groundY = 480; // Top of grass
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
        for (int col = 0; col < groundCols; col++) {
            int x = col * TILE_SIZE;

            // Grass at groundY
            ImageView grassView = new ImageView(grassTile);
            grassView.setX(x);
            grassView.setY(groundY);
            root.getChildren().add(grassView);

            // Soil directly below
            ImageView soilView = new ImageView(soilTile);
            soilView.setX(x);
            soilView.setY(groundY + GRASS_HEIGHT);
            root.getChildren().add(soilView);
        }

        // Place player just above the grass level
        int playerY = groundY - 48; // adjust -48 to your player sprite height
        player = new Player(100, playerY, "/se233/kellion/assets/Player.png");
        root.getChildren().add(player.getView());
    }

    public Pane getRoot() { return root; }
    public Player getPlayer() { return player; }
}
