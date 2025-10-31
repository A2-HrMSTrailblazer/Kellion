package se233.kellion.util;

import javafx.scene.image.ImageView;
import javafx.geometry.Bounds;

public class CollisionUtil {
    public static boolean intersects(ImageView a, ImageView b) {
        return a.getBoundsInParent().intersects(b.getBoundsInParent());
    }
}
