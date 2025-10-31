package se233.kellion;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import se233.kellion.view.GameMenu;
import se233.kellion.view.GameView;
import se233.kellion.controller.GameController;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {
        GameView gameView = new GameView();
        GameController controller = new GameController(gameView);
        Pane gameRoot = gameView.getRoot();

        GameMenu gameMenu = new GameMenu( 800,525,"/se233/kellion/assets/Menu_screens.png");

        gameMenu.setOnStart(() -> {
            gameRoot.requestFocus();
            controller.startGameLoop();   //Play
        });

        gameMenu.setOnPause(controller::pauseGameLoop);
        gameMenu.setOnResume(() -> {
            controller.resumeGameLoop();
            gameRoot.requestFocus();
            gameMenu.resume(gameRoot); // Clear Blur from resume with CONTINUE
        });

        gameMenu.setOnQuit(stage::close);

        // start and pause overlays
        StackPane rootLayer = new StackPane(
                gameRoot,
                gameMenu.getStartOverlay(),
                gameMenu.getPauseOverlay()
        );

        Scene scene = new Scene(rootLayer, 800, 525);
        gameMenu.install(scene, gameRoot);
        stage.setTitle("Kellion");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        gameRoot.requestFocus();
    }

    public static void main(String[] args) {
        launch();
    }
}
