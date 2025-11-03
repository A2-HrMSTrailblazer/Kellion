package se233.kellion.model;

/**
 * DummyPlayer is a test-only stand-in for Player without any graphics.
 * Supports lives, position, movement, jumping, and prone state.
 */
public class DummyPlayer {
    private int lives;
    private double x;
    private double y;
    private boolean facingRight;
    private boolean isJumping;
    private boolean isProne;

    public DummyPlayer() {
        this(0, 100, 3); // Default position and lives
    }

    public DummyPlayer(double x, double y, int lives) {
        this.x = x;
        this.y = y;
        this.lives = lives;
        this.facingRight = true;
        this.isJumping = false;
        this.isProne = false;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int l) {
        this.lives = l;
    }

    public void loseLife() {
        this.lives--;
    }

    public double getX() { return x; }
    public double getY() { return y; }

    // Movement simulation: arbitrary units
    public void moveLeft() {
        if (isProne) return;
        facingRight = false;
        x -= 2;
    }
    public void moveRight() {
        if (isProne) return;
        facingRight = true;
        x += 2;
    }

    // Jump simulation
    public void jump() {
        if (!isJumping && !isProne) {
            isJumping = true;
        }
    }
    public boolean isJumping() { return isJumping; }

    // Prone simulation
    public void prone() {
        if (isJumping) return;
        isProne = true;
    }
    public void standUp() {
        isProne = false;
        if (isJumping) isJumping = false;
    }
    public boolean isProne() { return isProne; }
}
