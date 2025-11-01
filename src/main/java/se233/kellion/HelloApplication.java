package se233.kellion;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import se233.kellion.controller.GameController;
import se233.kellion.exception.KellionExceptionHandler;
import se233.kellion.view.GameScene;
import se233.kellion.view.GameView2;
import se233.kellion.view.GameView3;

public class HelloApplication extends Application {
    private boolean scene2Once = false;
    private boolean scene3Once = false;

    // HelloApplication.java (start)
    @Override
    public void start(Stage stage) {

        Runnable goToScene3 = () -> {
            if (scene3Once) return;
            scene3Once = true;

            GameView3 view3 = new GameView3();
            GameController controller3 = new GameController(view3);
            Scene scene3 = new Scene(view3.getRoot(), 800, 525);
            controller3.attachInputHandlersToScene(scene3);
            controller3.startGameLoop();

            stage.setScene(scene3);
            stage.setTitle("Kellion - Stage 3");
        };

        Runnable goToScene2 = () -> {
            if (scene2Once) return;
            scene2Once = true;

            GameView2 view2 = new GameView2();
            GameController controller2 = new GameController(view2);
            view2.setOnNextScene(() -> {
                controller2.stopGameLoop();
                goToScene3.run();
            });

            Scene scene2 = new Scene(view2.getRoot(), 800, 525);
            controller2.attachInputHandlersToScene(scene2);
            controller2.startGameLoop();

            stage.setScene(scene2);
            stage.setTitle("Kellion - Stage 2");
        };

        GameScene scene1 = new GameScene(stage::close, goToScene2);
        stage.setScene(scene1.getScene());
        stage.setTitle("Kellion - Gameplay");
        stage.setResizable(false);
        stage.show();
    }


    public static void main(String[] args) {
        try{ launch(); }
        catch (Throwable e) { KellionExceptionHandler.handle(e); }
    }
}
