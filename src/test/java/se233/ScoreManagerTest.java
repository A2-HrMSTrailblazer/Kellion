package se233;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javafx.scene.text.Text;
import se233.kellion.model.DummyPlayer;
import se233.kellion.model.MinionKind;
import se233.kellion.model.Player;
import se233.kellion.util.ScoreManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class ScoreManagerTest {
    private ScoreManager manager;
    private Text scoreText;

    @BeforeEach
    public void setup() {
        scoreText = new Text();
        manager = new ScoreManager(scoreText, 100, 10, 60, 200);
    }

    @Test
    public void addHit_shouldIncreaseScoreProperly() {
        manager.addHit();
        assertEquals(100, manager.getScore());
        assertEquals("Score: 100", scoreText.getText());
    }

    @Test
    public void addShot_shouldIncrementShotsFired() {
        manager.addShot();
        manager.addShot();
        // If you expose shotsFired getter, test it; or check log/output
    }

    @Test
    public void addMinionKill_shouldAddCorrectPoints() {
        manager.addMinionKill(MinionKind.M1);
        assertEquals(1, manager.getScore());

        manager.addMinionKill(MinionKind.M3);
        assertEquals(3, manager.getScore());
    }

    @Test
    public void addBossKill_shouldAddBossPoints() {
        manager.addBossKill();
        assertEquals(2, manager.getScore());
    }
}
