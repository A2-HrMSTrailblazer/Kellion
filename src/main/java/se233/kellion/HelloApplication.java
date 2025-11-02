package se233.kellion;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.application.Application;
import se233.kellion.controller.GameController;
import se233.kellion.exception.KellionExceptionHandler;
import se233.kellion.view.*;

public class HelloApplication extends Application {
    private boolean scene2Once = false;
    private boolean scene3Once = false;

    @Override
    public void start(Stage stage) {
        // forward refs
        final Runnable[] goToScene3Ref = new Runnable[1];

        // --- Scene 2 ---
        Runnable goToScene2 = () -> {
            if (scene2Once) return;
            scene2Once = true;

            GameView2 view2 = new GameView2();
            GameController controller2 = new GameController(view2);
            GameOverMenu gameOver2 = new GameOverMenu(800, 525);

            // GameMenu สำหรับ Pause ของซีน 2 (ซ่อน start overlay)
            GameMenu gm2 = new GameMenu(800, 525, "/se233/kellion/assets/Menu_screens.png");
            gm2.getStartOverlay().setVisible(false);
            gm2.getStartOverlay().setMouseTransparent(true);
            gm2.markStarted();

            //  Scene 3
            view2.setOnNextScene(() -> {
                controller2.stopGameLoop();
                if (goToScene3Ref[0] != null) goToScene3Ref[0].run();
            });

            StackPane root2 = new StackPane(
                    view2.getRoot(),
                    gameOver2.getOverlay(),
                    gm2.getPauseOverlay() // << เพิ่ม pause overlay
            );
            Scene scene2 = new Scene(root2, 800, 525);
            controller2.attachInputHandlersToScene(scene2);

            // ติดตั้ง ESC สำหรับ Pause/Resume + เบลอฉากหลังที่ root ของเกม
            gm2.install(scene2, view2.getRoot());
            gm2.setOnPause(controller2::pauseGame);
            gm2.setOnResume(() -> {
                controller2.startGameLoop();
                Platform.runLater(view2.getRoot()::requestFocus);
            });
            gm2.setOnQuit(Platform::exit);

            view2.setOnGameOver(() -> {
                controller2.stopGameLoop();
                gameOver2.show(view2.getRoot());
            });

            // Retry:
            gameOver2.setOnRetry(() -> {
                controller2.stopGameLoop();
                gameOver2.hide(view2.getRoot());

                GameView2 v = new GameView2();
                GameController c = new GameController(v);
                v.setOnNextScene(() -> { c.stopGameLoop(); if (goToScene3Ref[0] != null) goToScene3Ref[0].run(); });

                GameOverMenu newGameOver2 = new GameOverMenu(800, 525);

                // GameMenu ใหม่สำหรับฉาก Retry ของซีน 2
                GameMenu gm2b = new GameMenu(800, 525, "/se233/kellion/assets/Menu_screens.png");
                gm2b.getStartOverlay().setVisible(false);
                gm2b.getStartOverlay().setMouseTransparent(true);
                gm2b.markStarted();

                StackPane r = new StackPane(
                        v.getRoot(),
                        newGameOver2.getOverlay(),
                        gm2b.getPauseOverlay() // << เพิ่ม pause overlay
                );
                Scene s = new Scene(r, 800, 525);
                c.attachInputHandlersToScene(s);

                v.setOnGameOver(() -> { c.stopGameLoop(); newGameOver2.show(v.getRoot()); });

                // ติดตั้ง ESC ให้ฉาก Retry
                gm2b.install(s, v.getRoot());
                gm2b.setOnPause(controller2::pauseGame);
                gm2b.setOnResume(() -> {
                    c.startGameLoop();
                    Platform.runLater(v.getRoot()::requestFocus);
                });
                gm2b.setOnQuit(Platform::exit);

                stage.setScene(s);
                stage.setTitle("Kellion - Stage 2");
                c.startGameLoop();
                Platform.runLater(v.getRoot()::requestFocus);
            });

            stage.setScene(scene2);
            stage.setTitle("Kellion - Stage 2");
            controller2.startGameLoop();
            Platform.runLater(view2.getRoot()::requestFocus);
        };

        // --- Scene 3 ---
        goToScene3Ref[0] = () -> {
            if (scene3Once) return;
            scene3Once = true;

            GameView3 view3 = new GameView3();
            GameController controller3 = new GameController(view3);
            GameOverMenu gameOver3 = new GameOverMenu(800, 525);
            GameWinMenu  gameWin3  = new GameWinMenu(800, 525);

            // GameMenu สำหรับ Pause ของซีน 3 (ซ่อน start overlay)
            GameMenu gm3 = new GameMenu(800, 525, "/se233/kellion/assets/Menu_screens.png");
            gm3.getStartOverlay().setVisible(false);
            gm3.getStartOverlay().setMouseTransparent(true);
            gm3.markStarted();

            StackPane root3 = new StackPane(
                    view3.getRoot(),
                    gameOver3.getOverlay(),
                    gameWin3.getOverlay(),
                    gm3.getPauseOverlay() // << เพิ่ม pause overlay
            );
            Scene scene3 = new Scene(root3, 800, 525);
            controller3.attachInputHandlersToScene(scene3);

            // ติดตั้ง ESC สำหรับ Pause/Resume + เบลอฉากหลังที่ root ของเกม
            gm3.install(scene3, view3.getRoot());
            gm3.setOnPause(controller3::pauseGame);
            gm3.setOnResume(() -> {
                controller3.startGameLoop();
                Platform.runLater(view3.getRoot()::requestFocus);
            });
            gm3.setOnQuit(Platform::exit);

            view3.setOnGameOver(() -> {
                controller3.stopGameLoop();
                gameOver3.show(view3.getRoot());
            });

            view3.setOnGameWin(() -> {
                controller3.stopGameLoop();
                gameWin3.setTotalScore(se233.kellion.util.TotalScore.get());
                gameWin3.show(view3.getRoot());
            });

            // END
            gameWin3.setOnEnd(Platform::exit);

            // Retry:
            gameOver3.setOnRetry(() -> {
                controller3.stopGameLoop();
                gameOver3.hide(view3.getRoot());

                GameView3 v = new GameView3();
                GameController c = new GameController(v);

                GameOverMenu newGameOver3 = new GameOverMenu(800, 525);
                GameWinMenu  newGameWin3  = new GameWinMenu(800, 525);

                // GameMenu ใหม่สำหรับฉาก Retry ของซีน 3
                GameMenu gm3b = new GameMenu(800, 525, "/se233/kellion/assets/Menu_screens.png");
                gm3b.getStartOverlay().setVisible(false);
                gm3b.getStartOverlay().setMouseTransparent(true);
                gm3b.markStarted();

                StackPane r = new StackPane(
                        v.getRoot(),
                        newGameOver3.getOverlay(),
                        newGameWin3.getOverlay(),
                        gm3b.getPauseOverlay() // << เพิ่ม pause overlay
                );
                Scene s = new Scene(r, 800, 525);
                c.attachInputHandlersToScene(s);

                v.setOnGameOver(() -> { c.stopGameLoop(); newGameOver3.show(v.getRoot()); });
                v.setOnGameWin(()  -> { c.stopGameLoop(); newGameWin3.show(v.getRoot()); });

                // ติดตั้ง ESC ให้ฉาก Retry
                gm3b.install(s, v.getRoot());
                gm3b.setOnPause(controller3::pauseGame);
                gm3b.setOnResume(() -> {
                    c.startGameLoop();
                    Platform.runLater(v.getRoot()::requestFocus);
                });
                gm3b.setOnQuit(Platform::exit);

                newGameWin3.setOnEnd(Platform::exit);

                stage.setScene(s);
                stage.setTitle("Kellion - Stage 3");
                c.startGameLoop();
                Platform.runLater(v.getRoot()::requestFocus);
            });

            stage.setScene(scene3);
            stage.setTitle("Kellion - Stage 3");
            controller3.startGameLoop();
            Platform.runLater(view3.getRoot()::requestFocus);
        };
        se233.kellion.util.TotalScore.reset();
        goToScene(stage, goToScene2);
    }

    private void goToScene(Stage stage, Runnable goToScene2) {
        GameScene scene1 = (goToScene2 == null)
                ? new GameScene(stage::close)
                : new GameScene(stage::close, goToScene2);
        stage.setScene(scene1.getScene());
        stage.setTitle("Kellion - Gameplay");
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        try { launch(); }
        catch (Throwable e) { KellionExceptionHandler.handle(e); }
    }
}
