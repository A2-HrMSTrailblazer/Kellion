package se233.kellion.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;

public class Boss {
    public static final int SPRITE_WIDTH = 112;
    public static final int SPRITE_HEIGHT = 160;

    private final ImageView view;
    private int health;

    public Boss(double x, double y, String spritePath) {
        Image src = new Image(getClass().getResource(spritePath).toExternalForm());
        WritableImage bossSprite = new WritableImage(
                src.getPixelReader(),
                0, 0,
                SPRITE_WIDTH, SPRITE_HEIGHT
        );
        view = new ImageView(bossSprite);
        view.setX(x);
        view.setY(y);
        health = 100;
    }

    public ImageView getView() {
        return view;
    }

    public Bounds getBounds() {
        return view.getBoundsInParent();
    }

    public int getHealth() {
        return health;
    }

    public void damage(int amount) {
        health -= amount;
        if (health < 0) health = 0;
    }

    public boolean isDead() {
        return health <= 0;
    }

    public boolean collidesWith(Bounds other) {
        return getBounds().intersects(other);
    }

    public Bounds getCustomBounds(double expandX, double expandY) {
        Bounds b = view.getBoundsInParent();
        return new BoundingBox(b.getMinX() - expandX, b.getMinY() - expandY, b.getWidth() + 2 * expandX, b.getHeight() + 2 * expandY);
    }
}
