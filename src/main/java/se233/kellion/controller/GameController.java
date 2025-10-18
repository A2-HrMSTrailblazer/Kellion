package se233.kellion.controller;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import se233.kellion.view.GameView;
import se233.kellion.model.Player;

public class GameController {
    private final GameView view;
    private final Player player;
    private boolean left, right;

    public GameController(GameView view) {
        this.view = view;
        this.player = view.getPlayer();

        // Attach input when the Scene becomes available
        view.getRoot().sceneProperty().addListener((_, _, newScene) -> {
            if (newScene != null) {
                initInput(newScene);
            }
        });
    }

    private void initInput(Scene scene) {
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case LEFT, A -> left = true;
                case RIGHT, D -> right = true;
                // case W, UP, SPACE -> {if (player.isGrounded()){player.jump();}}
                case J -> {
                    double px = player.getView().getX() + player.getView().getFitWidth() / 2;
                    double py = player.getView().getY() + player.getView().getFitHeight() / 2;
                    view.fireBullet(px, py, player.isFacingRight());
                }
                case S, DOWN -> player.prone();
            }
        });

        scene.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case LEFT, A -> left = false;
                case RIGHT, D -> right = false;
                // case W, UP, SPACE -> player.stopJumping();
                case S, DOWN -> player.standUp();
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
        // player.update();

        // if (!player.isJumping()) {
        if (left) {
            player.moveLeft();
        } else if (right) {
            player.moveRight();
        } else {
            player.stopMoving();
        }

        view.updateBullets();
        // }
    }
}
