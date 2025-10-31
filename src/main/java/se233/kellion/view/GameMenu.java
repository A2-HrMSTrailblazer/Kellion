package se233.kellion.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class GameMenu {

    private final int width, height;
    private final String titleImagePath;

    private final StackPane startOverlay;
    private final StackPane pauseOverlay;

    private boolean started = false;
    private boolean paused  = false;

    private Runnable onStart  = () -> {};
    private Runnable onPause  = () -> {};
    private Runnable onResume = () -> {};
    private Runnable onQuit   = () -> {};

    public GameMenu(int width, int height, String titleImagePath) {
        this.width = width;
        this.height = height;
        this.titleImagePath = titleImagePath;

        this.startOverlay = buildStartOverlay();
        this.pauseOverlay = buildPauseOverlay();

        startOverlay.setVisible(true);   // Show start
        pauseOverlay.setVisible(false);  // Hide during game
    }

    // --- API ---
    public Node getStartOverlay() { return startOverlay; }
    public Node getPauseOverlay() { return pauseOverlay; }

    public void setOnStart(Runnable r)  { this.onStart = r != null ? r : () -> {}; }
    public void setOnPause(Runnable r)  { this.onPause = r != null ? r : () -> {}; }
    public void setOnResume(Runnable r) { this.onResume = r != null ? r : () -> {}; }
    public void setOnQuit(Runnable r)   { this.onQuit = r != null ? r : () -> {}; }

    // set ESC toggle + blurTarget when pause
    public void install(Scene scene, Node blurTarget) {
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE && started) {
                togglePause(blurTarget);
            }
        });
    }

    public void togglePause(Node blurTarget) {
        if (!paused) pause(blurTarget); else resume(blurTarget);
    }

    public void pause(Node blurTarget) {
        paused = true;
        pauseOverlay.setVisible(true);
        if (blurTarget != null) blurTarget.setEffect(new BoxBlur(3,3,1));
        onPause.run();
    }

    public void resume(Node blurTarget) {
        paused = false;
        pauseOverlay.setVisible(false);
        if (blurTarget != null) blurTarget.setEffect(null);
        onResume.run();
    }

    //  Builder
    private StackPane buildStartOverlay() {
        Rectangle dim = new Rectangle(width, height);
        dim.setFill(Color.color(0,0,0,0.6));

        Image img = new Image(getClass().getResource(titleImagePath).toExternalForm());
        ImageView title = new ImageView(img);
        title.setPreserveRatio(false);
        title.setFitWidth(width);
        title.setFitHeight(height);
        StackPane.setMargin(title, new Insets(12, 0, 0, 0));

        Image playDefault = new Image(getClass().getResource("/se233/kellion/assets/PLAY.png").toExternalForm());
        Image playHover   = new Image(getClass().getResource("/se233/kellion/assets/PLAY_2.png").toExternalForm());
        Image quitDefault = new Image(getClass().getResource("/se233/kellion/assets/Title_QUIT.png").toExternalForm());
        Image quitHover = new Image(getClass().getResource("/se233/kellion/assets/QUIT_2.png").toExternalForm());

        ImageView playIV  = new ImageView(playDefault);
        ImageView quitIV = new ImageView(quitDefault);

        Pane menuLayer = new Pane(playIV, quitIV);
        menuLayer.setPickOnBounds(false);

        playIV.setLayoutX(345);
        quitIV.setLayoutX(345);
        playIV.setLayoutY(280);
        quitIV.setLayoutY(340);

        playIV.setOnMouseEntered(e -> playIV.setImage(playHover));
        playIV.setOnMouseExited (e -> playIV.setImage(playDefault));
        quitIV.setOnMouseEntered(e -> quitIV.setImage(quitHover));
        quitIV.setOnMouseExited (e -> quitIV.setImage(quitDefault));

        playIV.setOnMousePressed(e -> playIV.setTranslateY(playIV.getTranslateY() + 1));
        playIV.setOnMouseReleased(e -> playIV.setTranslateY(playIV.getTranslateY() - 1));
        quitIV.setOnMousePressed(e -> playIV.setTranslateY(playIV.getTranslateY() + 1));
        quitIV.setOnMouseReleased(e -> playIV.setTranslateY(playIV.getTranslateY() - 1));
        playIV.setCursor(javafx.scene.Cursor.HAND);
        quitIV.setCursor(javafx.scene.Cursor.HAND);

        playIV.setOnMouseClicked(e -> {
            started = true;
            startOverlay.setVisible(false);
            onStart.run();
        });
        quitIV.setOnMouseClicked(e -> onQuit.run());

        return new StackPane(dim, title, menuLayer);
    };

    private StackPane buildPauseOverlay() {
        Rectangle dim = new Rectangle(width, height);
        dim.setFill(Color.color(0,0,0,0.6));

        Image contDefault = new Image(getClass().getResource("/se233/kellion/assets/CONTINUE.png").toExternalForm());
        Image contHover   = new Image(getClass().getResource("/se233/kellion/assets/CONTINUE_2.png").toExternalForm());
        Image quitDefault = new Image(getClass().getResource("/se233/kellion/assets/QUIT.png").toExternalForm());
        Image quitHover   = new Image(getClass().getResource("/se233/kellion/assets/QUIT_2.png").toExternalForm());

        ImageView contIV = new ImageView(contDefault);
        ImageView quitIV = new ImageView(quitDefault);

        VBox box = new VBox(14, contIV, quitIV);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(16));

        contIV.setOnMouseEntered(e -> contIV.setImage(contHover));
        contIV.setOnMouseExited (e -> contIV.setImage(contDefault));
        quitIV.setOnMouseEntered(e -> quitIV.setImage(quitHover));
        quitIV.setOnMouseExited (e -> quitIV.setImage(quitDefault));

        contIV.setOnMousePressed(e -> contIV.setTranslateY(contIV.getTranslateY() + 1));
        contIV.setOnMouseReleased(e -> contIV.setTranslateY(contIV.getTranslateY() - 1));
        quitIV.setOnMousePressed(e -> quitIV.setTranslateY(quitIV.getTranslateY() + 1));
        quitIV.setOnMouseReleased(e -> quitIV.setTranslateY(quitIV.getTranslateY() - 1));

        contIV.setCursor(javafx.scene.Cursor.HAND);
        quitIV.setCursor(javafx.scene.Cursor.HAND);

        contIV.setOnMouseClicked(e -> onResume.run());
        quitIV.setOnMouseClicked(e -> onQuit.run());

        StackPane wrapper = new StackPane(dim, box);
        wrapper.setPickOnBounds(true);
        return wrapper;
    }
}
