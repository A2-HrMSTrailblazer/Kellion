package se233.kellion.util;

import javafx.scene.text.Text;
import se233.kellion.model.Player;

public class ScoreManager {
    private int score = 0;
    private int shotsFired = 0;
    private long stageStartTimeMillis;
    private final int SCORE_PER_HIT, TIME_BONUS_FACTOR, PAR_TIME_SECONDS, LIFE_BONUS;
    private final Text scoreText;

    public ScoreManager(Text scoreText, int SCORE_PER_HIT, int TIME_BONUS_FACTOR, int PAR_TIME_SECONDS, int LIFE_BONUS) {
        this.scoreText = scoreText;
        this.SCORE_PER_HIT = SCORE_PER_HIT;
        this.TIME_BONUS_FACTOR = TIME_BONUS_FACTOR;
        this.PAR_TIME_SECONDS = PAR_TIME_SECONDS;
        this.LIFE_BONUS = LIFE_BONUS;
        this.stageStartTimeMillis = System.currentTimeMillis();
        updateScoreText();
    }

    public void addHit() {
        score += SCORE_PER_HIT;
        updateScoreText();
    }
    public void addShot() {
        shotsFired++;
    }
    public void applyStageClearBonuses(Player player) {
        long elapsedMillis = System.currentTimeMillis() - stageStartTimeMillis;
        int elapsedSeconds = (int) (elapsedMillis / 1000);
        int timeBonus = Math.max(0, (PAR_TIME_SECONDS - elapsedSeconds) * TIME_BONUS_FACTOR);
        int livesBonus = Math.max(0, player.getLives() * LIFE_BONUS);
        score += timeBonus + livesBonus;
        updateScoreText();
    }
    public int getScore() { return score; }
    private void updateScoreText() { scoreText.setText("Score: " + score); }
}
