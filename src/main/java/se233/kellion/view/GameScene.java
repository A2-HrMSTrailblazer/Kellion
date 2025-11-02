package se233.kellion.view;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import se233.kellion.controller.GameController;

public class GameScene {
    private final Scene scene;

    private Runnable onNextScene;
    private StackPane root;
    private GameView gameView;
    private GameController controller;
    private GameMenu gameMenu;
    private GameOverMenu gameOver;

    // --- ctor #1: no next-scene ---
    public GameScene(Runnable onQuit) {
        this.gameView = new GameView();
        Pane gameRoot = this.gameView.getRoot();
        gameRoot.setFocusTraversable(true);

        this.controller = new GameController(this.gameView);
        this.gameMenu   = new GameMenu(800, 525, "/se233/kellion/assets/Menu_screens.png");
        this.gameOver   = new GameOverMenu(800, 525);

        // callback: Game Over
        this.gameView.setOnGameOver(() -> {
            this.controller.stopGameLoop();
            this.gameOver.show(gameRoot);
        });

        // layer: game + start + gameover + pause(ใหม่)
        this.root  = new StackPane(
                gameRoot,
                this.gameMenu.getStartOverlay(),
                this.gameOver.getOverlay(),
                this.gameMenu.getPauseOverlay() // <<< ใส่ pause overlay เข้า stack
        );
        this.scene = new Scene(this.root, 800, 525);

        this.controller.attachInputHandlersToScene(this.scene);

        // ติดตั้ง ESC ให้ทำงานเป็น Pause/Resume และเบลอที่ gameRoot
        this.gameMenu.install(this.scene, gameRoot);
        this.gameMenu.setOnPause(() -> this.controller.stopGameLoop());
        this.gameMenu.setOnResume(() -> {
            this.controller.startGameLoop();
            Platform.runLater(gameRoot::requestFocus);
        });

        // start
        this.gameMenu.setOnStart(() -> {
            this.gameMenu.getStartOverlay().setVisible(false);
            this.gameMenu.getStartOverlay().setMouseTransparent(true);
            this.controller.startGameLoop();
            Platform.runLater(gameRoot::requestFocus);
        });

        // GameOver buttons
        this.gameOver.setOnRetry(() -> {
            resetStage1();
            this.gameOver.hide(this.gameView.getRoot());
        });

        this.gameMenu.setOnQuit(onQuit);
    }

    // --- ctor #2: have next-scene ---
    public GameScene(Runnable onQuit, Runnable onNextScene) {
        this.onNextScene = onNextScene;

        this.gameView = new GameView();
        Pane gameRoot = this.gameView.getRoot();
        gameRoot.setFocusTraversable(true);

        this.controller = new GameController(this.gameView);
        this.gameMenu   = new GameMenu(800, 525, "/se233/kellion/assets/Menu_screens.png");
        this.gameOver   = new GameOverMenu(800, 525);

        // next scene callback
        if (this.onNextScene != null) {
            this.gameView.setOnNextScene(this.onNextScene);
        }

        // game over callback
        this.gameView.setOnGameOver(() -> {
            this.controller.stopGameLoop();
            this.gameOver.show(gameRoot);
        });

        // layer: game + start + gameover + pause(ใหม่)
        this.root  = new StackPane(
                gameRoot,
                this.gameMenu.getStartOverlay(),
                this.gameOver.getOverlay(),
                this.gameMenu.getPauseOverlay() // <<< ใส่ pause overlay เข้า stack
        );
        this.scene = new Scene(this.root, 800, 525);

        this.controller.attachInputHandlersToScene(this.scene);

        // ติดตั้ง ESC ให้ทำงานเป็น Pause/Resume และเบลอที่ gameRoot
        this.gameMenu.install(this.scene, gameRoot);
        this.gameMenu.setOnPause(() -> this.controller.stopGameLoop());
        this.gameMenu.setOnResume(() -> {
            this.controller.startGameLoop();
            Platform.runLater(gameRoot::requestFocus);
        });

        this.gameMenu.setOnStart(() -> {
            this.gameMenu.getStartOverlay().setVisible(false);
            this.gameMenu.getStartOverlay().setMouseTransparent(true);
            this.controller.startGameLoop();
            Platform.runLater(gameRoot::requestFocus);
        });

        this.gameOver.setOnRetry(() -> {
            resetStage1();
            this.gameOver.hide(this.gameView.getRoot());
        });

        this.gameMenu.setOnQuit(onQuit);
    }

    // --- reset stage 1 ---
    private void resetStage1() {
        if (this.controller != null) this.controller.stopGameLoop();

        Pane oldRoot = (this.gameView != null) ? this.gameView.getRoot() : null;
        if (oldRoot != null) oldRoot.setEffect(null);

        this.gameView = new GameView();
        Pane newRoot = this.gameView.getRoot();
        newRoot.setFocusTraversable(true);

        this.controller = new GameController(this.gameView);
        this.controller.attachInputHandlersToScene(this.scene);

        if (this.onNextScene != null) {
            this.gameView.setOnNextScene(this.onNextScene);
        }
        this.gameView.setOnGameOver(() -> {
            this.controller.stopGameLoop();
            this.gameOver.show(newRoot);
        });

        // ต้องใส่ pause overlay กลับเข้ากองทุกครั้งที่รีเซ็ต
        ((StackPane) this.scene.getRoot()).getChildren()
                .setAll(newRoot, this.gameMenu.getStartOverlay(), this.gameOver.getOverlay(), this.gameMenu.getPauseOverlay());

        // ติดตั้ง ESC ให้ blurTarget ตัวใหม่ (newRoot) + ผูก pause/resume กับ controller ตัวใหม่
        this.gameMenu.install(this.scene, newRoot);
        this.gameMenu.setOnPause(() -> this.controller.stopGameLoop());
        this.gameMenu.setOnResume(() -> {
            this.controller.startGameLoop();
            Platform.runLater(newRoot::requestFocus);
        });

        this.gameOver.hide(newRoot);
        this.controller.startGameLoop();
        Platform.runLater(newRoot::requestFocus);
        wireStartHandler(newRoot);
    }

    private void wireStartHandler(Pane focusRoot) {
        this.gameMenu.setOnStart(() -> {
            this.gameMenu.getStartOverlay().setVisible(false);
            this.gameMenu.getStartOverlay().setMouseTransparent(true);
            this.controller.startGameLoop();
            Platform.runLater(focusRoot::requestFocus);
        });
    }

    public Scene getScene() { return this.scene; }
}
