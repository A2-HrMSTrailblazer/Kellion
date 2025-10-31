package se233.kellion.controller;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import se233.kellion.view.GameView;
import se233.kellion.model.Player;

public class GameController {
    private final GameView view;
    private final Player player;
    private boolean left, right;
    private AnimationTimer gameLoop;

    public GameController(GameView view) {
        this.view = view;
        this.player = view.getPlayer();
    }

    public void attachInputHandlersToScene(Scene scene) {
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case LEFT, A -> left = true;
                case RIGHT, D -> right = true;
                case W, UP, SPACE -> player.jump();
                case J -> view.fireBullet(player.getGunX(), player.getGunY(), player.isFacingRight());
                case DOWN, CONTROL -> player.prone();
                default -> {}
            }
        });

        scene.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case LEFT, A -> left = false;
                case RIGHT, D -> right = false;
                case DOWN, CONTROL -> player.standUp();
                default -> {}
            }
        });
    }

    public void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
            }
        };
        gameLoop.start();
        System.out.println("Game loop started");
    }

    private void update() {
        if (!view.isPlayerDead()) {
            player.update();

            if (left) player.moveLeft();
            else if (right) player.moveRight();
            else player.stopMoving();
        }

        view.updateBullets();
        view.updateBossBullets();
        view.checkCollisions();
        view.updateBoss();
    }

    public void pauseGameLoop() {
        if (gameLoop != null) gameLoop.stop();
    }

    public void resumeGameLoop() {
        if (gameLoop != null) gameLoop.start();
    }
}
