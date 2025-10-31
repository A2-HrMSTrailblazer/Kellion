package se233.kellion.model;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.util.Duration;
import se233.kellion.util.SpriteAnimation;

public class Player {
    private static final String SPRITE_PATH = "/se233/kellion/assets/Player.png";

    // --- Constants for physics ---
    private static final double SPEED = 2.0;
    private static final double GRAVITY = 0.15;
    private static final double JUMP_STRENGTH = -7.0;
    private static final double GROUND_OFFSET = 20.0;

    // --- Sprite and animation sizes/locations ---
    private static final int FRAME_WIDTH = 64;
    private static final int FRAME_HEIGHT = 64;
    private static final int COLUMNS = 12;

    // Walking animation info
    private static final int WALK_FRAME_COUNT = 6;
    private static final int WALK_OFFSET_X = 6 * FRAME_WIDTH;
    private static final int WALK_OFFSET_Y = 0;

    // Jump animation info
    private static final int JUMP_FRAME_COUNT = 5;
    private static final int JUMP_OFFSET_X = 11 * FRAME_WIDTH;
    private static final int JUMP_OFFSET_Y = 1 * FRAME_HEIGHT;

    // Prone animation info
    private static final int PRONE_FRAME_COUNT = 1;
    private static final int PRONE_OFFSET_X = 14 * FRAME_WIDTH;
    private static final int PRONE_OFFSET_Y = 0;

    // Dead animation frame info
    private static final int DEAD_FRAME_X = 992; // update if needed
    private static final int DEAD_FRAME_Y = 224; // update if needed
    private static final int DEAD_FRAME_WIDTH = 32;
    private static final int DEAD_FRAME_HEIGHT = 32;

    // --- State ---
    private final ImageView view;
    private final SpriteAnimation walkAnimation;
    private final SpriteAnimation jumpAnimation;
    private final SpriteAnimation proneAnimation;
    private final WritableImage[] deadFrames;
    private boolean facingRight = true;
    private boolean moving = false;
    private boolean isJumping = false;
    private boolean isProne = false;

    private final double groundY;
    private double velocityY = 0;

    public Player(double x, double y, double groundY) {
        this.groundY = groundY;

        // Load sprite sheet
        Image spriteSheet = new Image(getClass().getResource(SPRITE_PATH).toExternalForm());
        view = new ImageView(spriteSheet);
        view.setFitWidth(FRAME_WIDTH);
        view.setFitHeight(FRAME_HEIGHT);
        view.setViewport(new Rectangle2D(WALK_OFFSET_X, WALK_OFFSET_Y, FRAME_WIDTH, FRAME_HEIGHT));
        view.setX(x);
        view.setY(Math.min(y, groundY));

        // Prepare animations
        walkAnimation = new SpriteAnimation(
                view, Duration.millis(600), WALK_FRAME_COUNT, COLUMNS,
                WALK_OFFSET_X, WALK_OFFSET_Y, FRAME_WIDTH, FRAME_HEIGHT);
        walkAnimation.setCycleCount(Animation.INDEFINITE);

        jumpAnimation = new SpriteAnimation(
                view, Duration.millis(600), JUMP_FRAME_COUNT, COLUMNS,
                JUMP_OFFSET_X, JUMP_OFFSET_Y, FRAME_WIDTH, FRAME_HEIGHT);
        jumpAnimation.setCycleCount(1);
        jumpAnimation.setOnFinished(_ -> {
            isJumping = false;
            resetToIdle();
        });

        proneAnimation = new SpriteAnimation(
                view, Duration.millis(400), PRONE_FRAME_COUNT, COLUMNS,
                PRONE_OFFSET_X, PRONE_OFFSET_Y, FRAME_WIDTH, FRAME_HEIGHT);
        proneAnimation.setCycleCount(Animation.INDEFINITE);

        // Extract dead frame
        deadFrames = new WritableImage[3];
        deadFrames[0] = new WritableImage(spriteSheet.getPixelReader(), 781, 131, 64, 64);
        deadFrames[1] = new WritableImage(spriteSheet.getPixelReader(), 846, 131, 64, 64);
        deadFrames[2] = new WritableImage(spriteSheet.getPixelReader(), 974, 194, 32, 32);

        //view.setImage(deadFrames[0]);
    }

    // --- Movement API ---
    public void moveLeft() {
        if (isProne)
            return;
        moving = true;
        facingRight = false;
        view.setScaleX(-1);
        startWalking();
        if (view.getX() > 0)
            view.setX(view.getX() - SPEED);
    }

    public void moveRight() {
        if (isProne)
            return;
        moving = true;
        facingRight = true;
        view.setScaleX(1);
        startWalking();
        if (view.getX() + view.getFitWidth() < 800)
            view.setX(view.getX() + SPEED);
    }

    public void stopMoving() {
        moving = false;
        stopWalking();
    }

    // --- Jumping ---
    public void jump() {
        if (!isJumping && isGrounded() && !isProne) {
            isJumping = true;
            velocityY = JUMP_STRENGTH;
            jumpAnimation.playFromStart();
        }
    }

    public void update() {
        // Physics for gravity/jump
        if (!isGrounded() || velocityY < 0) {
            velocityY += GRAVITY;
            view.setY(view.getY() + velocityY);
        }
        double playerBottom = view.getY() + view.getFitHeight() - GROUND_OFFSET;
        if (playerBottom >= groundY) { // Hit ground
            view.setY(groundY - view.getFitHeight() + GROUND_OFFSET);
            velocityY = 0;
            if (isJumping) {
                isJumping = false;
                jumpAnimation.stop();
                resetToIdle();
            }
        }
    }

    // --- Prone ---
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

    // --- Death animation ---
    public void playDeathAnimation() {
        // Start with first frame & size
        view.setImage(deadFrames[0]);
        view.setFitWidth(deadFrames[0].getWidth());
        view.setFitHeight(deadFrames[0].getHeight());
        view.setViewport(null);

        Timeline timeline = new Timeline(
                new KeyFrame(javafx.util.Duration.millis(0), e -> {
                    view.setImage(deadFrames[0]);
                    view.setFitWidth(deadFrames[0].getWidth());
                    view.setFitHeight(deadFrames[0].getHeight());
                }),
                new KeyFrame(javafx.util.Duration.millis(120), e -> {
                    view.setImage(deadFrames[1]);
                    view.setFitWidth(deadFrames[1].getWidth());
                    view.setFitHeight(deadFrames[1].getHeight());
                }),
                new KeyFrame(javafx.util.Duration.millis(240), e -> {
                    view.setImage(deadFrames[2]);
                    view.setFitWidth(deadFrames[2].getWidth());
                    view.setFitHeight(deadFrames[2].getHeight());
                })
        );
        timeline.play();
    }

    // --- Animation helpers ---
    private void startWalking() {
        if (walkAnimation.getStatus() != Animation.Status.RUNNING)
            walkAnimation.play();
    }

    private void stopWalking() {
        walkAnimation.stop();
        if (!isProne && !isJumping)
            resetToIdle();
    }

    private void resetToIdle() {
        view.setViewport(new Rectangle2D(
                WALK_OFFSET_X, WALK_OFFSET_Y, FRAME_WIDTH, FRAME_HEIGHT));
        view.setFitWidth(FRAME_WIDTH);
        view.setFitHeight(FRAME_HEIGHT);
        view.setImage(view.getImage());
    }

    // --- Physics & State Getters ---
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

    // --- Gun/barrel helper coordinates ---
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

    // --- State checks ---
    public boolean isProne() {
        return isProne;
    }
}
