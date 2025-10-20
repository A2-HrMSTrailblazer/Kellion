package se233.kellion.model;

import java.util.List;

import javafx.animation.Animation;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import se233.kellion.util.SpriteAnimation;

public class Player {
    // === Visuals and Animations ===
    private final ImageView view;
    private final SpriteAnimation walkAnimation;
    private final SpriteAnimation jumpAnimation;
    private final SpriteAnimation proneAnimation;

    // === Player States ===
    private boolean facingRight = true;
    private boolean moving = false;
    private boolean isJumping = false;
    private boolean isProne = false;
    private boolean onGround = false;

    // === Physics Constants ===
    private static final double SPEED = 2.0;
    private static final double GRAVITY = 0.15;
    private static final double JUMP_STRENGTH = -7.0;
    private static final double GROUND_OFFSET = 20;
    private static final double PLATFORM_OFFSET = 18;

    private final double groundY;
    private double velocityY = 0;
    private double lastY = 0;

    // === Sprite Sheet Configuration ===
    private static final int FRAME_WIDTH = 64;
    private static final int FRAME_HEIGHT = 64;
    private static final int COLUMNS = 12;

    // Walking
    private static final int WALK_FRAME_COUNT = 6;
    private static final int WALK_OFFSET_X = 6 * FRAME_WIDTH;
    private static final int WALK_OFFSET_Y = 0;

    // Jumping
    private static final int JUMP_FRAME_COUNT = 5;
    private static final int JUMP_OFFSET_X = 11 * FRAME_WIDTH;
    private static final int JUMP_OFFSET_Y = 1 * FRAME_HEIGHT;

    // Prone
    private static final int PRONE_FRAME_COUNT = 1;
    private static final int PRONE_OFFSET_X = 14 * FRAME_WIDTH;
    private static final int PRONE_OFFSET_Y = 0;

    public Player(double x, double y, String imagePath, double groundY) {
        this.groundY = groundY;

        // === Load Sprite Sheet ===
        Image spriteSheet = new Image(getClass().getResource(imagePath).toExternalForm());
        view = new ImageView(spriteSheet);
        view.setFitWidth(FRAME_WIDTH);
        view.setFitHeight(FRAME_HEIGHT);
        view.setViewport(new Rectangle2D(WALK_OFFSET_X, WALK_OFFSET_Y, FRAME_WIDTH, FRAME_HEIGHT));
        view.setX(x);
        view.setY(y);

        // === Initialize Animations ===
        walkAnimation = new SpriteAnimation(view, Duration.millis(600), WALK_FRAME_COUNT, COLUMNS,
                WALK_OFFSET_X, WALK_OFFSET_Y, FRAME_WIDTH, FRAME_HEIGHT);
        walkAnimation.setCycleCount(Animation.INDEFINITE);

        jumpAnimation = new SpriteAnimation(view, Duration.millis(600), JUMP_FRAME_COUNT, COLUMNS,
                JUMP_OFFSET_X, JUMP_OFFSET_Y, FRAME_WIDTH, FRAME_HEIGHT);
        jumpAnimation.setCycleCount(1);
        jumpAnimation.setOnFinished(_ -> {
            isJumping = false;
            resetToIdle();
        });

        proneAnimation = new SpriteAnimation(view, Duration.millis(400), PRONE_FRAME_COUNT, COLUMNS,
                PRONE_OFFSET_X, PRONE_OFFSET_Y, FRAME_WIDTH, FRAME_HEIGHT);
        proneAnimation.setCycleCount(Animation.INDEFINITE);
    }

    // === Public Getters ===
    public ImageView getView() {
        return view;
    }

    public boolean isJumping() {
        return isJumping;
    }

    public boolean isProne() {
        return isProne;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public boolean isGrounded() {
        return onGround;
    } // single unified check

    // === Movement ===
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
        if (walkAnimation.getStatus() != Animation.Status.RUNNING)
            startWalking();
        if (view.getX() + view.getFitWidth() < 800)
            view.setX(view.getX() + SPEED);
    }

    public void stopMoving() {
        moving = false;
        stopWalking();
    }

    private void startWalking() {
        if (!walkAnimation.getStatus().equals(Animation.Status.RUNNING)) {
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
        if (!isJumping && onGround && !isProne) {
            isJumping = true;
            velocityY = JUMP_STRENGTH;
            onGround = false;
            jumpAnimation.playFromStart();
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

    // === Physics + Collision ===
    public void update(List<Platform> platforms) {
        lastY = view.getY();
        onGround = false; // reset each frame

        // Gravity
        velocityY += GRAVITY;
        view.setY(view.getY() + velocityY);
        onGround = false;

        double feetY = view.getY() + view.getFitHeight();
        double newY = view.getY();
        double minLandY = Double.MAX_VALUE;

        double groundTop = groundY + PLATFORM_OFFSET;
        if (feetY >= groundTop && velocityY >= 0) {
            minLandY = groundTop;
        }

        Bounds playerBounds = view.getBoundsInParent();
        double playerBottom = playerBounds.getMaxY();
        double playerLeft = playerBounds.getMinX();
        double playerRight = playerBounds.getMaxX();

        for (Platform platform : platforms) {
            Bounds platformBounds = platform.getBounds();

            boolean horizontallyAligned = playerRight > platformBounds.getMinX() + 5 &&
                                          playerLeft < platformBounds.getMaxX() - 5;

            if (velocityY >= 0 && horizontallyAligned) {
                double platformTop = platformBounds.getMinY();
                if (playerBottom <= platformTop + 10 && feetY >= platformTop) {
                    minLandY = Math.min(minLandY, platformTop);
                }
            }
        }

        if (minLandY != Double.MAX_VALUE && feetY >= minLandY) {
            newY = minLandY - view.getFitHeight();
            view.setY(newY);
            velocityY = 0;
            onGround = true;

            if (isJumping) {
                isJumping = false;
                jumpAnimation.stop();
                resetToIdle();
            }
        }
    }

    public void handleCollisions(List<Platform> platforms) {
        
    }

    // === Helpers ===
    private void resetToIdle() {
        view.setViewport(new Rectangle2D(WALK_OFFSET_X, WALK_OFFSET_Y, FRAME_WIDTH, FRAME_HEIGHT));
    }

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

    // === Utility Setters/Getters ===
    public void setY(double newY) {
        view.setY(newY);
    }

    public double getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }
}
