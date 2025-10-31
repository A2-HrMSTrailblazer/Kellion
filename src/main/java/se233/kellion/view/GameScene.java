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

        StackPane root = new StackPane(gameRoot, gameMenu.getStartOverlay());
        this.scene = new Scene(root, 800, 525);
        controller.attachInputHandlersToScene(scene);

        gameMenu.setOnStart(() -> {
            gameMenu.getStartOverlay().setVisible(false);
            gameMenu.getStartOverlay().setMouseTransparent(true);
            controller.startGameLoop();
            Platform.runLater(() -> gameRoot.requestFocus());
        });
        gameMenu.setOnQuit(onQuit);
    }

    public GameScene(Runnable onQuit, Runnable onNextScene) {
        GameView gameView = new GameView();
        Pane gameRoot = gameView.getRoot();
        gameRoot.setFocusTraversable(true);

        GameController controller = new GameController(gameView);

        GameMenu gameMenu = new GameMenu(800, 525, "/se233/kellion/assets/Menu_screens.png");

        StackPane root = new StackPane(gameRoot, gameMenu.getStartOverlay());
        this.scene = new Scene(root, 800, 525);
        controller.attachInputHandlersToScene(scene);

        //callback To Scene 2
        gameView.setOnNextScene(onNextScene);
        controller.stopGameLoop();

        gameMenu.setOnStart(() -> {
            gameMenu.getStartOverlay().setVisible(false);
            gameMenu.getStartOverlay().setMouseTransparent(true);
            controller.startGameLoop();
            Platform.runLater(() -> gameRoot.requestFocus());
        });
        gameMenu.setOnQuit(onQuit);
    }


    public Scene getScene() { return scene; }
}
