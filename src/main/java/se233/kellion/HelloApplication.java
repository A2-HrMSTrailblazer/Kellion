package se233.kellion;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import se233.kellion.view.GameView;
import se233.kellion.controller.GameController;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {
        GameView gameView = new GameView();
        GameController controller = new GameController(gameView);

        Scene scene = new Scene(gameView.getRoot(), 800, 525);
        stage.setTitle("Kellion");
        stage.setScene(scene);
        stage.setResizable(false); // <-- Make window fixed!
        stage.show();

        gameView.getRoot().requestFocus();
        controller.startGameLoop(); // begins the animation
    }

    public static void main(String[] args) {
        launch();
    }
}
