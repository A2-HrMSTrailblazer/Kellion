package se233.kellion.view;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import se233.kellion.controller.GameController;

public class GameScene {
    private final Scene scene;

    public GameScene(Runnable onQuit) {
        GameView gameView = new GameView();
        Pane gameRoot = gameView.getRoot();
        gameRoot.setFocusTraversable(true);

        GameController controller = new GameController(gameView);

        GameMenu gameMenu = new GameMenu(800, 525, "/se233/kellion/assets/Menu_screens.png");

        StackPane root = new StackPane(gameRoot, gameMenu.getStartOverlay(), gameMenu.getPauseOverlay());
        this.scene = new Scene(root, 800, 525);
        controller.attachInputHandlersToScene(scene);

        gameMenu.setOnStart(() -> {
            gameMenu.getStartOverlay().setVisible(false);
            gameMenu.getStartOverlay().setMouseTransparent(true);
            controller.startGameLoop();
            Platform.runLater(() -> gameRoot.requestFocus());
        });
        gameMenu.setOnPause(controller::pauseGameLoop);
        gameMenu.setOnResume(() -> {
            controller.resumeGameLoop();
            Platform.runLater(() -> gameRoot.requestFocus());
        });
        gameMenu.setOnQuit(onQuit);
    }

    public Scene getScene() { return scene; }
}
