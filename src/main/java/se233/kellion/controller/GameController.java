package se233.kellion.controller;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import se233.kellion.view.GameView;
import se233.kellion.model.Player;

public class GameController {
    private GameView view;
    private Player player;
    private boolean left, right;

    public GameController(GameView view) {
        this.view = view;
        this.player = view.getPlayer();

        // Attach input once Scene is available
        view.getRoot().sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                initInput(newScene);
            }
        });
    }

    private void initInput(Scene scene) {
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case LEFT -> left = true;
                case RIGHT -> right = true;
            }
        });
        scene.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case LEFT -> left = false;
                case RIGHT -> right = false;
            }
        });
    }

    public void startGameLoop() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
            }
        };
        timer.start();
    }

    private void update() {
        if (left) player.moveLeft();
        if (right) player.moveRight();
    }
}
