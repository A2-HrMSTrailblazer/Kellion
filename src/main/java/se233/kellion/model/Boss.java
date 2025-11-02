package se233.kellion.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;

public class Boss {
    public static final int SPRITE_WIDTH = 122;
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
        health = 200;
    }

    public static Boss JavaBoss(double x, double y) {
        Image src = new Image(Boss.class.getResource("/se233/kellion/assets/Java.png").toExternalForm());
        WritableImage frame = new WritableImage(src.getPixelReader(), 0, 0, 112, 113);
        Boss boss = new Boss(x, y, frame);
        boss.health = 250;
        return boss;
    }

    public static Boss GomeramosKingBoss(double x, double y) {
        Image src = new Image(Boss.class.getResource("/se233/kellion/assets/Gomeramos_King.png").toExternalForm());
        WritableImage frame = new WritableImage(src.getPixelReader(), 0, 0, 88, 144);
        Boss boss = new Boss(x, y, frame);
        boss.health = 300;
        return boss;
    }

    private Boss(double x, double y, WritableImage croppedFrame) {
        view = new ImageView(croppedFrame);
        view.setX(x);
        view.setY(y);
        health = 200;
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
