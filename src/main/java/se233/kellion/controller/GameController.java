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
    private boolean shooting;
    private boolean running = false;
    private boolean isPaused = false;

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
                    case J -> {
                        shooting = true;
                        view.onFirePress(System.nanoTime());
                    }

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
                    case J -> {
                        shooting = false;
                        view.onFireRelease(System.nanoTime());
                    }
                    case DOWN, CONTROL -> player.standUp();
                    default -> {}
                }
            } catch (Exception ex) {
                KellionExceptionHandler.handle(new KellionInputException("Input error: " + e.getCode(), ex));
            }
        });
    }

    public void startGameLoop() {
        if (running) return;
        running = true;
        isPaused = false;
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    update(now);
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
        if (!running) return;
        running = false;
        if (gameLoop != null) gameLoop.stop();
        gameLoop = null;
    }

    public void pauseGame() {
        if (!running || isPaused) return;      // << กัน pause ซ้ำ
        isPaused = true;
        stopGameLoop();
        view.setPaused(true);
    }

    public void resumeGame() {
        if (running || !isPaused) return;      // << กัน resume ซ้ำ
        view.setPaused(false);
        isPaused = false;
        startGameLoop();
    }

    private void update(long now) {
        try {
            if (!view.isPlayerDead()) {
                player.update();
                if (left)
                    player.moveLeft();
                else if (right)
                    player.moveRight();
                else
                    player.stopMoving();
                if (shooting) {
                    view.tryFirePlayerBullet(now);
                }
            }

            view.updateLivesHUD();
            view.updateChargingVisual(now);
            view.updateMinions(now);
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
