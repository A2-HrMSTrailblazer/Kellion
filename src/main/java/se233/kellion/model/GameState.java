package se233.kellion.model;

public class GameState {
    private boolean playerDead = false;
    public boolean isPlayerDead() { return playerDead; }
    public void killPlayer() { playerDead = true; }
}
