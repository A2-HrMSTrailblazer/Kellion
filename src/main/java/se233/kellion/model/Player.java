package se233.kellion.model;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import se233.kellion.util.SpriteAnimation;

public class Player {
    private static final String SPRITE_PATH = "/se233/kellion/assets/Player.png";

    // --- Physics Constants ---
    private static final double SPEED = 2.0;
    private static final double GRAVITY = 0.15;
    private static final double JUMP_STRENGTH = -7.0;
    private static final double GROUND_OFFSET = 20.0;

    // --- Animation Frames ---
    private static final int FRAME_WIDTH = 64;
    private static final int FRAME_HEIGHT = 64;
    private static final int COLUMNS = 12;

    private static final int WALK_FRAME_COUNT = 6;
    private static final int WALK_OFFSET_X = 6 * FRAME_WIDTH;
    private static final int WALK_OFFSET_Y = 0;

    private static final int JUMP_FRAME_COUNT = 5;
    private static final int JUMP_OFFSET_X = 11 * FRAME_WIDTH;
    private static final int JUMP_OFFSET_Y = 1 * FRAME_HEIGHT;

    private static final int PRONE_FRAME_COUNT = 1;
    private static final int PRONE_OFFSET_X = 14 * FRAME_WIDTH;
    private static final int PRONE_OFFSET_Y = 0;

    // --- Death Animation Frame RECTANGLES ---
    private final Rectangle2D[] deadFrameRects;

    // --- State ---
    private final Image spriteSheet;
    private final ImageView view;
    private final SpriteAnimation walkAnimation;
    private final SpriteAnimation jumpAnimation;
    private final SpriteAnimation proneAnimation;
    private boolean facingRight = true;
    private boolean moving = false;
    private boolean isJumping = false;
    private boolean isProne = false;

    private final double groundY;
    private double velocityY = 0;

    // --- Lives Support ---
    private int lives = 3;

    public Player(double x, double y, double groundY) {
        this.groundY = groundY;

        // Load sprite sheet
        spriteSheet = new Image(getClass().getResource(SPRITE_PATH).toExternalForm());
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

        // Dead frames, as RECTANGLES ON SPRITESHEET!
        deadFrameRects = new Rectangle2D[] {
            new Rectangle2D(781, 131, 64, 64),
            new Rectangle2D(846, 131, 64, 64),
            new Rectangle2D(974, 194, 64, 64)
        };
    }

    // --------- Movement ---------
    public void moveLeft() {
        if (isProne) return;
        moving = true;
        facingRight = false;
        view.setScaleX(-1);
        startWalking();
        if (view.getX() > 0) view.setX(view.getX() - SPEED);
    }
    public void moveRight() {
        if (isProne) return;
        moving = true;
        facingRight = true;
        view.setScaleX(1);
        startWalking();
        if (view.getX() + view.getFitWidth() < 800) view.setX(view.getX() + SPEED);
    }
    public void stopMoving() {
        moving = false;
        stopWalking();
    }

    // --------- Jumping ---------
    public void jump() {
        if (!isJumping && isGrounded() && !isProne) {
            isJumping = true;
            velocityY = JUMP_STRENGTH;
            jumpAnimation.playFromStart();
        }
    }

    public void update() {
        // Physics/jump
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

    // --------- Prone ---------
    public void prone() {
        if (isJumping) return;
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

    // --------- Respawn ---------
    public void respawn(double x, double y) {
        lives = Math.max(lives, 1); // prevent negative
        view.setX(x);
        view.setY(y);
        resetToIdle();
    }

    // --------- Death Animation: Use Viewport, Never Set Image! ---------
    public void playDeathAnimation() {
        view.setImage(spriteSheet); // always use full sheet
        view.setViewport(deadFrameRects[0]);
        view.setFitWidth(deadFrameRects[0].getWidth());
        view.setFitHeight(deadFrameRects[0].getHeight());

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(0), e -> {
                view.setViewport(deadFrameRects[0]);
                view.setFitWidth(deadFrameRects[0].getWidth());
                view.setFitHeight(deadFrameRects[0].getHeight());
            }),
            new KeyFrame(Duration.millis(120), e -> {
                view.setViewport(deadFrameRects[1]);
                view.setFitWidth(deadFrameRects[1].getWidth());
                view.setFitHeight(deadFrameRects[1].getHeight());
            }),
            new KeyFrame(Duration.millis(240), e -> {
                view.setViewport(deadFrameRects[2]);
                view.setFitWidth(deadFrameRects[2].getWidth());
                view.setFitHeight(deadFrameRects[2].getHeight());
            })
        );
        timeline.play();
    }

    // --------- Animation helpers ---------
    private void startWalking() {
        if (walkAnimation.getStatus() != Animation.Status.RUNNING) walkAnimation.play();
    }
    private void stopWalking() {
        walkAnimation.stop();
        if (!isProne && !isJumping) resetToIdle();
    }
    public void resetToIdle() {
        view.setViewport(new Rectangle2D(WALK_OFFSET_X, WALK_OFFSET_Y, FRAME_WIDTH, FRAME_HEIGHT));
        view.setFitWidth(FRAME_WIDTH);
        view.setFitHeight(FRAME_HEIGHT);
        view.setImage(spriteSheet);
    }

    // --------- Lives API ---------
    public int getLives() { return lives; }
    public void setLives(int l) { lives = l; }
    public void loseLife() { lives = Math.max(lives - 1, 0); }

    // --------- Physics & State API ---------
    public ImageView getView() { return view; }
    public boolean isJumping() { return isJumping; }
    public boolean isFacingRight() { return facingRight; }
    public boolean isGrounded() {
        double playerBottom = view.getY() + view.getFitHeight() - GROUND_OFFSET;
        return playerBottom >= groundY - 0.5;
    }
    // Gun/barrel helper coordinates
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
    public boolean isProne() { return isProne; }
}
