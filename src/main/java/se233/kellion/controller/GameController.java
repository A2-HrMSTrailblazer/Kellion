package se233.kellion.controller;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import se233.kellion.view.GameView;
import se233.kellion.model.Player;
import se233.kellion.exception.*;

public class GameController {
    private final GameView view;
    private final Player player;
    private boolean left, right;
    private AnimationTimer gameLoop;

    public GameController(GameView view) {
        if (view == null || view.getPlayer() == null)
            throw new KellionInputException("GameView and Player cannot be null in GameController.");
        this.view = view;
        this.player = view.getPlayer();
    }

    public void attachInputHandlersToScene(Scene scene) {
        scene.setOnKeyPressed(e -> {
            try {
                switch (e.getCode()) {
                    case LEFT, A -> left = true;
                    case RIGHT, D -> right = true;
                    case W, UP, SPACE -> player.jump();
                    case J -> view.fireBullet(player.getGunX(), player.getGunY(), player.isFacingRight());
                    case DOWN, CONTROL -> player.prone();
                    default -> {}
                }
            } catch (Exception ex) {
                KellionExceptionHandler.handle(new KellionInputException("Input error: " + e.getCode(), ex));
            }
        });

        scene.setOnKeyReleased(e -> {
            try {
                switch (e.getCode()) {
                    case LEFT, A -> left = false;
                    case RIGHT, D -> right = false;
                    case DOWN, CONTROL -> player.standUp();
                    default -> {}
                }
            } catch (Exception ex) {
                KellionExceptionHandler.handle(new KellionInputException("Input error: " + e.getCode(), ex));
            }
        });
    }

    public void startGameLoop() {
        if (gameLoop != null) return;
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    update();
                } catch (Exception ex) {
                    KellionExceptionHandler.handle(new KellionGameLoopException("Game loop failure", ex));
                    stopGameLoop(); // Optionally halt the game on fatal errors
                }
            }
        };
        gameLoop.start();
        System.out.println("Game loop started");
    }

    public void stopGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
            gameLoop = null;
            System.out.println("Game loop stopped");
        }
    }

    private void update() {
        try {
            if (!view.isPlayerDead()) {
                player.update();
                if (left)
                    player.moveLeft();
                else if (right)
                    player.moveRight();
                else
                    player.stopMoving();
            }

            view.updateBullets();
            view.updateBossBullets();
            view.checkCollisions();
            view.updateBoss();
        } catch (Exception ex) {
            // This is usually redundant with the game loop outer handler, but could be used for finer-grained action
            throw new KellionGameLoopException("Update cycle failed", ex);
        }
    }
}
