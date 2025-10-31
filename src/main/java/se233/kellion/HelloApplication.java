package se233.kellion;

import javafx.application.Application;
import javafx.stage.Stage;
import se233.kellion.view.GameScene;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {
        GameScene game = new GameScene(stage::close); // Pass close action for "Quit"
        stage.setScene(game.getScene());
        stage.setTitle("Kellion - Gameplay");
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
