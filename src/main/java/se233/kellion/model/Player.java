package se233.kellion.model;

import javafx.animation.Animation;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import se233.kellion.util.SpriteAnimation;

public class Player {
    private final ImageView view;
    private final double speed = 2;
    private final SpriteAnimation walkAnimation;
    private final SpriteAnimation jumpAnimation;
    private boolean facingRight = true;
    private boolean moving = false;

    // Sprite sheet constants (approximation)
    private static final int FRAME_WIDTH = 64;
    private static final int FRAME_HEIGHT = 64;
    private static final int COLUMNS = 12;
    private static final int WALK_FRAME_COUNT = 6; // number of frames used for walking
    private static final int WALK_OFFSET_X = 6 * FRAME_WIDTH;
    private static final int WALK_OFFSET_Y = 0; // 3rd row (0-indexed), since walking is near bottom
    private static final int JUMP_FRAME_COUNT = 5; // number of frames used for jumping
    private static final int JUMP_OFFSET_X = 11 * FRAME_WIDTH;
    private static final int JUMP_OFFSET_Y = 1 * FRAME_HEIGHT;

    private boolean isJumping = false;
    private double velocityY = 0;
    private final double gravity = 0.15;
    private final double jumpStrength = -7;
    private final double groundY = 300;
    // private int jumpCount = 0;
    private final double minY = 0;

    public Player(double x, double y, String imagePath) {
        Image spriteSheet = new Image(getClass().getResource(imagePath).toExternalForm());
        view = new ImageView(spriteSheet);
        view.setFitWidth(FRAME_WIDTH);
        view.setFitHeight(FRAME_HEIGHT);
        view.setViewport(new Rectangle2D(WALK_OFFSET_X, WALK_OFFSET_Y, FRAME_WIDTH, FRAME_HEIGHT));
        view.setX(x);
        view.setY(Math.min(y, groundY));

        walkAnimation = new SpriteAnimation(
                view,
                Duration.millis(600), // speed of animation
                WALK_FRAME_COUNT,
                COLUMNS,
                WALK_OFFSET_X,
                WALK_OFFSET_Y,
                FRAME_WIDTH,
                FRAME_HEIGHT);
        walkAnimation.setCycleCount(Animation.INDEFINITE);

        jumpAnimation = new SpriteAnimation(view,
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
            view.setViewport(new Rectangle2D(JUMP_OFFSET_X, JUMP_OFFSET_Y, FRAME_WIDTH, FRAME_HEIGHT));
        });
    }

    public ImageView getView() {
        return view;
    }

    public void moveLeft() {
        moving = true;
        facingRight = false;
        view.setScaleX(-1);
        startWalking();
        if (view.getX() > 0)
            view.setX(view.getX() - speed);
    }

    public void moveRight() {
        moving = true;
        facingRight = true;
        view.setScaleX(1);
        startWalking();
        if (view.getX() + view.getFitWidth() < 800)
            view.setX(view.getX() + speed);
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
        // reset to idle frame
        view.setViewport(new Rectangle2D(WALK_OFFSET_X, WALK_OFFSET_Y, FRAME_WIDTH, FRAME_HEIGHT));
    }

    // public void jump() {
    //     if (!isJumping && isGrounded()) {
    //         isJumping = true;
    //         velocityY = jumpStrength;
    //         jumpAnimation.playFromStart();
    //         // jumpCount++;
    //     }
    // }

    // public void update() {
    //     if (isJumping) {
    //         velocityY += gravity;
    //         view.setY(view.getY() + velocityY);

    //         if (view.getY() < minY) {
    //             view.setY(minY);
    //             velocityY = 0;
    //         }
    //     }

    //     if (view.getY() >= groundY) {
    //         view.setY(groundY);
    //         velocityY = 0;
    //         isJumping = false;
    //         // jumpCount = 0;
    //         stopJumping();
    //     }
    // }

    // public void stopJumping() {
    //     jumpAnimation.stop();
    //     view.setViewport(new Rectangle2D(JUMP_OFFSET_X, JUMP_OFFSET_Y, FRAME_WIDTH, FRAME_HEIGHT));
    // }

    // public boolean isJumping() {
    //     return isJumping;
    // }

    // public boolean isGrounded() {
    //     return view.getY() >= groundY;
    // }
}
