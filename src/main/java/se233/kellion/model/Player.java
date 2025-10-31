package se233.kellion.model;

import javafx.animation.Animation;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import se233.kellion.util.SpriteAnimation;

public class Player {
    private final ImageView view;
    private final SpriteAnimation walkAnimation;
    private final SpriteAnimation jumpAnimation;
    private final SpriteAnimation proneAnimation;

    private boolean facingRight = true;
    private boolean moving = false;
    private boolean isJumping = false;
    private boolean isProne = false;

    // === Physics constants ===
    private static final double SPEED = 2.0;
    private static final double GRAVITY = 0.15;
    private static final double JUMP_STRENGTH = -7.0;
    private static final double GROUND_OFFSET = 20.0;

    private final double groundY;
    private double velocityY = 0;

    // === Sprite sheet configuration ===
    private static final int FRAME_WIDTH = 64;
    private static final int FRAME_HEIGHT = 64;
    private static final int COLUMNS = 12;

    // Walking animation
    private static final int WALK_FRAME_COUNT = 6;
    private static final int WALK_OFFSET_X = 6 * FRAME_WIDTH;
    private static final int WALK_OFFSET_Y = 0;

    // Jumping animation
    private static final int JUMP_FRAME_COUNT = 5;
    private static final int JUMP_OFFSET_X = 11 * FRAME_WIDTH;
    private static final int JUMP_OFFSET_Y = 1 * FRAME_HEIGHT;

    // Prone animation
    private static final int PRONE_FRAME_COUNT = 1;
    private static final int PRONE_OFFSET_X = 14 * FRAME_WIDTH;
    private static final int PRONE_OFFSET_Y = 0;

    private static final Rectangle2D[] DEAD_FRAME_RECTS = {
            new Rectangle2D(781, 131, 64, 64),
            new Rectangle2D(846, 131, 64, 64),
            new Rectangle2D(974, 194, 32, 32)
    };
    private int lives = 3;

    private static final double HITBOX_SCALE_X = 0.3; // 80% ของกว้างจริง
    private static final double HITBOX_SCALE_Y = 0.4;

    public Player(double x, double y, String imagePath, double groundY) {
        this.groundY = groundY;

        // === Initialize sprite sheet ===
        Image spriteSheet = new Image(getClass().getResource(imagePath).toExternalForm());
        view = new ImageView(spriteSheet);
        view.setFitWidth(FRAME_WIDTH);
        view.setFitHeight(FRAME_HEIGHT);
        view.setViewport(new Rectangle2D(WALK_OFFSET_X, WALK_OFFSET_Y, FRAME_WIDTH, FRAME_HEIGHT));
        view.setX(x);
        view.setY(Math.min(y, groundY));

        // === Animations ===
        walkAnimation = new SpriteAnimation(
                view,
                Duration.millis(600),
                WALK_FRAME_COUNT,
                COLUMNS,
                WALK_OFFSET_X,
                WALK_OFFSET_Y,
                FRAME_WIDTH,
                FRAME_HEIGHT);
        walkAnimation.setCycleCount(Animation.INDEFINITE);

        jumpAnimation = new SpriteAnimation(
                view,
                Duration.millis(600),
                JUMP_FRAME_COUNT,
                COLUMNS,
                JUMP_OFFSET_X,
                JUMP_OFFSET_Y,
                FRAME_WIDTH,
                FRAME_HEIGHT);
        jumpAnimation.setCycleCount(1);
        jumpAnimation.setOnFinished(_ -> {
            isJumping = false;
            resetToIdle();
        });

        proneAnimation = new SpriteAnimation(
                view,
                Duration.millis(400),
                PRONE_FRAME_COUNT,
                COLUMNS,
                PRONE_OFFSET_X,
                PRONE_OFFSET_Y,
                FRAME_WIDTH,
                FRAME_HEIGHT);
        proneAnimation.setCycleCount(Animation.INDEFINITE);
    }

    // === Getters ===
    public ImageView getView() {
        return view;
    }

    public boolean isJumping() {
        return isJumping;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public boolean isGrounded() {
        double playerBottom = view.getY() + view.getFitHeight() - GROUND_OFFSET;
        return playerBottom >= groundY - 0.5;
    }

    public Bounds getHitboxBounds() {
        Bounds b = getView().getBoundsInParent();
        double w = b.getWidth() * HITBOX_SCALE_X;
        double h = b.getHeight() * HITBOX_SCALE_Y;
        double minX = b.getMinX() + (b.getWidth() - w) / 2.0;
        double minY = b.getMinY() + (b.getHeight() - h) / 2.0;
        return new javafx.geometry.BoundingBox(minX, minY, w, h);
    }

    // === Movement ===
    public void moveLeft() {
        if (isProne)
            return;
        moving = true;
        facingRight = false;
        view.setScaleX(-1);
        startWalking();
        if (view.getX() > 0) {
            view.setX(view.getX() - SPEED);
        }
    }

    public void moveRight() {
        if (isProne)
            return;
        moving = true;
        facingRight = true;
        view.setScaleX(1);
        startWalking();
        if (view.getX() + view.getFitWidth() < 800) {
            view.setX(view.getX() + SPEED);
        }
    }

    public void stopMoving() {
        moving = false;
        stopWalking();
    }

    private void startWalking() {
        if (walkAnimation.getStatus() != Animation.Status.RUNNING) {
            walkAnimation.play();
        }
    }

    private void stopWalking() {
        walkAnimation.stop();
        if (!isProne && !isJumping)
            resetToIdle();
    }

    // === Jumping ===
    public void jump() {
        if (!isJumping && isGrounded() && !isProne) {
            isJumping = true;
            velocityY = JUMP_STRENGTH;
            jumpAnimation.playFromStart();
        }
    }

    public void update() {
        // Apply gravity if mid-air or going upward
        if (!isGrounded() || velocityY < 0) {
            velocityY += GRAVITY;
            view.setY(view.getY() + velocityY);
        }

        // Check ground collision
        double playerBottom = view.getY() + view.getFitHeight() - GROUND_OFFSET;
        if (playerBottom >= groundY) {
            view.setY(groundY - view.getFitHeight() + GROUND_OFFSET);
            velocityY = 0;

            if (isJumping) {
                isJumping = false;
                jumpAnimation.stop();
                resetToIdle();
            }
        }
    }

    // === Prone ===
    public void prone() {
        if (isJumping)
            return;
        isProne = true;
        walkAnimation.stop();
        jumpAnimation.stop();
        proneAnimation.play();
    }

    public void standUp() {
        isProne = false;
        proneAnimation.stop();
        resetToIdle();
    }

    // === Helpers ===
    public void resetToIdle() {
        view.setViewport(new Rectangle2D(WALK_OFFSET_X, WALK_OFFSET_Y, FRAME_WIDTH, FRAME_HEIGHT));
    }

    // === Gun positioning ===
    public double getGunX() {
        return isProne
                ? view.getX() + (facingRight ? 52 : 0)
                : view.getX() + (facingRight ? 42 : 14);
    }

    public double getGunY() {
        return isProne
                ? view.getY() + view.getFitHeight() / 2
                : view.getY() + 20;
    }

    public void playDeathAnimation() {
        view.setViewport(DEAD_FRAME_RECTS[0]);
        view.setFitWidth(DEAD_FRAME_RECTS[0].getWidth());
        view.setFitHeight(DEAD_FRAME_RECTS[0].getHeight());

        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(0), e -> {
                    view.setViewport(DEAD_FRAME_RECTS[0]);
                    view.setFitWidth(DEAD_FRAME_RECTS[0].getWidth());
                    view.setFitHeight(DEAD_FRAME_RECTS[0].getHeight());
                }),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(110), e -> {
                    view.setViewport(DEAD_FRAME_RECTS[1]);
                    view.setFitWidth(DEAD_FRAME_RECTS[1].getWidth());
                    view.setFitHeight(DEAD_FRAME_RECTS[1].getHeight());
                }),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(220), e -> {
                    view.setViewport(DEAD_FRAME_RECTS[2]);
                    view.setFitWidth(DEAD_FRAME_RECTS[2].getWidth());
                    view.setFitHeight(DEAD_FRAME_RECTS[2].getHeight());
                }));
        timeline.play();
    }

    public int getLives() {
        return lives;
    }

    public void loseLife() {
        lives--;
    }

    public void setLives(int l) {
        lives = l;
    }

}
