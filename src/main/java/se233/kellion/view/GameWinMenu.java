package se233.kellion.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class GameWinMenu {
    private final int width, height;
    private final StackPane overlay;
    private Runnable onEnd = () -> {};

    public GameWinMenu(int width, int height) {
        this.width = width;
        this.height = height;
        this.overlay = buildOverlay();
        overlay.setVisible(false);
        overlay.setPickOnBounds(false);
        overlay.setMouseTransparent(true);
    }

    public StackPane buildOverlay() {
        Rectangle dim = new Rectangle(width, height);
        dim.setFill(Color.BLACK);
        Image bgImg = new Image(getClass().getResource("/se233/kellion/assets/GameWin_Screens.png").toExternalForm());
        ImageView bg = new ImageView(bgImg);
        bg.setFitWidth(width);
        bg.setFitHeight(height);
        bg.setPreserveRatio(true);

        Image endImg = new Image(getClass().getResource("/se233/kellion/assets/END.png").toExternalForm());

        ImageView end = new ImageView(endImg);
        end.setPreserveRatio(true);
        end.setFitWidth(240);
        end.setCursor(javafx.scene.Cursor.HAND);
        end.setOnMouseClicked(e -> onEnd.run());

        VBox buttons = new VBox(16, end);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(24,12,12,12));

        StackPane wrapper = new StackPane(dim, bg, buttons);
        StackPane.setAlignment(buttons, Pos.CENTER);

        wrapper.setVisible(false);
        wrapper.setPickOnBounds(false);
        wrapper.setMouseTransparent(true);

        return wrapper;
    }

    public StackPane getOverlay() { return overlay; }
    public void setOnEnd(Runnable onEnd) { this.onEnd = (onEnd != null) ? onEnd : () -> {}; }

    public void show(Pane gameLayer) {
        overlay.setVisible(true);
        overlay.setMouseTransparent(false);
        if (gameLayer != null) gameLayer.setDisable(true);
    }

    public void hide(Pane gameLayer) {
        overlay.setVisible(false);
        overlay.setMouseTransparent(true);
        if (gameLayer != null) gameLayer.setDisable(false);
    }
}
