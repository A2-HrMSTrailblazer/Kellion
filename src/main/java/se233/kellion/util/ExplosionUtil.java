package se233.kellion.util;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.*;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class ExplosionUtil {
    // Only one place manages frames, so no duplication between stages!
    public static WritableImage[] loadExplosionFrames(Image characterSheet, int startX, int startY, int frameSize, int frameSpace) {
        WritableImage[] frames = new WritableImage[3];
        for (int i = 0; i < 3; i++)
            frames[i] = new WritableImage(
                characterSheet.getPixelReader(), startX + i * (frameSize + frameSpace), startY, frameSize, frameSize
            );
        return frames;
    }

    public static void playExplosion(Pane root, WritableImage[] frames, double x, double y) {
        ImageView explosion = new ImageView(frames[0]);
        explosion.setFitWidth(32); explosion.setFitHeight(32);
        explosion.setX(x - 16); explosion.setY(y - 16);
        root.getChildren().add(explosion);
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(0),     e -> explosion.setImage(frames[0])),
            new KeyFrame(Duration.millis(80),    e -> explosion.setImage(frames[1])),
            new KeyFrame(Duration.millis(160),   e -> explosion.setImage(frames[2])),
            new KeyFrame(Duration.millis(240),   e -> root.getChildren().remove(explosion))
        );
        timeline.play();
    }
}
