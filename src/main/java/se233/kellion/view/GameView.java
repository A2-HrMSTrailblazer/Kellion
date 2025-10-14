package se233.kellion.view;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import se233.kellion.model.Player;

public class GameView {
    private Pane root;
    private Player player;

    public GameView() {
        root = new Pane();

        // ✅ Load background correctly from classpath
        Image bgImage = new Image(
                getClass().getResource("/se233/kellion/assets/Stage.png").toExternalForm()
        );
        ImageView bgView = new ImageView(bgImage);

        // crop region from the right end of image
        double bossX = 3266;
        double bossY = 0;
        double bossWidth = 188;
        double bossHeight = 224;

        bgView.setViewport(new Rectangle2D(bossX, bossY, bossWidth, bossHeight));
        bgView.setFitWidth(800);
        bgView.setFitHeight(600);

        root.getChildren().add(bgView);

        // ✅ Load player from classpath
        player = new Player(100, 500, "/se233/kellion/assets/Player.png");
        root.getChildren().add(player.getView());
    }

    public Pane getRoot() { return root; }
    public Player getPlayer() { return player; }
}
