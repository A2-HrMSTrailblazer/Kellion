package se233.kellion.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class GameOverMenu {
    private final int width, height;
    private final StackPane overlay;

    private Runnable onRetry = () -> {};

    public GameOverMenu(int width, int height) {
        this.width = width;
        this.height = height;
        this.overlay = buildOverlay();
        overlay.setVisible(false);
        overlay.setPickOnBounds(false);
        overlay.setMouseTransparent(true);
    }

    public Node getOverlay() { return overlay; }
    public void setOnRetry(Runnable r) { this.onRetry = (r != null) ? r : () -> {}; }

    /** Show overlay and blur target (e.g., game layer) */
    public void show(Node blurTarget) {
        overlay.setVisible(true);
        overlay.setPickOnBounds(true);
        overlay.setMouseTransparent(false);
        if (blurTarget != null) blurTarget.setEffect(new BoxBlur(3,3,1));
    }
    /** Hide overlay and clear blur */
    public void hide(Node blurTarget) {
        overlay.setVisible(false);
        overlay.setPickOnBounds(false);
        overlay.setMouseTransparent(true);
        if (blurTarget != null) blurTarget.setEffect(null);
    }

    private StackPane buildOverlay() {
        Rectangle dim = new Rectangle(width, height);
        dim.setFill(Color.BLACK);

        // --- Background (GAME OVER) ---
        Image bgImg = new Image(getClass().getResource("/se233/kellion/assets/GameOver_Screens.png").toExternalForm());
        ImageView bg = new ImageView(bgImg);
        bg.setPreserveRatio(true);

        bg.setFitWidth(width);
        bg.setFitHeight(height);

        Image retryImg = new Image(getClass().getResource("/se233/kellion/assets/Retry.png").toExternalForm());
        Image retryHover = new Image(getClass().getResource("/se233/kellion/assets/Retry_2.png").toExternalForm());

        ImageView retry = new ImageView(retryImg);

        VBox buttons = new VBox(16, retry);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(24,12,12,12));

        retry.setOnMouseEntered(e -> retry.setImage(retryHover));
        retry.setOnMouseExited (e -> retry.setImage(retryImg));

        retry.setOnMousePressed(e -> retry.setTranslateY(retry.getTranslateY() + 1));
        retry.setOnMouseReleased(e -> retry.setTranslateY(retry.getTranslateY() - 1));

        retry.setCursor(javafx.scene.Cursor.HAND);

        retry.setOnMouseClicked(e -> onRetry.run());

        StackPane wrapper = new StackPane(dim, bg, buttons);
        StackPane.setAlignment(buttons, Pos.CENTER);

        wrapper.setVisible(false);
        wrapper.setPickOnBounds(false);
        wrapper.setMouseTransparent(true);

        return wrapper;
    }

}
