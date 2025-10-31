package se233.kellion;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import se233.kellion.controller.GameController;
import se233.kellion.view.GameScene;
import se233.kellion.view.GameView2;

public class HelloApplication extends Application {
    private boolean scene2Once = false;
    // HelloApplication.java (start)
    @Override
    public void start(Stage stage) {

        Runnable goToScene2 = () -> {
            if (scene2Once) return;
            scene2Once = true;

            GameView2 view2 = new GameView2();
            GameController controller2 = new GameController(view2);
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
        launch();
    }
}
