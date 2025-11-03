package se233;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se233.kellion.model.DummyPlayer;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {
    private DummyPlayer player;

    @BeforeEach
    public void setup() {
        player = new DummyPlayer();
    }

    @Test
    public void initialPosition_shouldSetCorrectly() {
        assertEquals(0, player.getX());
        assertEquals(100, player.getY());
    }

    @Test
    public void moveLeft_shouldDecreaseX() {
        double startX = player.getX();
        player.moveLeft();
        assertTrue(player.getX() < startX);
    }

    @Test
    public void moveRight_shouldIncreaseX() {
        double startX = player.getX();
        player.moveRight();
        assertTrue(player.getX() > startX);
    }

    @Test
    public void jump_shouldSetIsJumpingTrue() {
        player.jump();
        assertTrue(player.isJumping());
    }

    @Test
    public void loseLife_shouldDecreaseLives() {
        int lives = player.getLives();
        player.loseLife();
        assertEquals(lives - 1, player.getLives());
    }
}
