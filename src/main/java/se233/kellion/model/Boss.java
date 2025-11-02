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
    private int maxHp;
    private boolean paused = false;

    public Boss(double x, double y, String spritePath,int hp) {
        Image src = new Image(getClass().getResource(spritePath).toExternalForm());
        WritableImage bossSprite = new WritableImage(
                src.getPixelReader(),
                0, 0,
                SPRITE_WIDTH, SPRITE_HEIGHT
        );
        view = new ImageView(bossSprite);
        view.setX(x);
        view.setY(y);
        setHealthFull(hp);
    }

    public static Boss JavaBoss(double x, double y) {
        Image src = new Image(Boss.class.getResource("/se233/kellion/assets/Java.png").toExternalForm());
        WritableImage frame = new WritableImage(src.getPixelReader(), 0, 0, 112, 113);
        return new Boss(x, y, frame, 500);
    }

    public static Boss GomeramosKingBoss(double x, double y) {
        Image src = new Image(Boss.class.getResource("/se233/kellion/assets/Gomeramos_King.png").toExternalForm());
        WritableImage frame = new WritableImage(src.getPixelReader(), 0, 0, 88, 144);
        return new Boss(x, y, frame, 500);
    }

    private Boss(double x, double y, WritableImage croppedFrame, int hp) {
        this.view = new ImageView(croppedFrame);
        this.view.setX(x);
        this.view.setY(y);
        setHealthFull(hp);
    }

    private void setHealthFull(int hp) {
        this.maxHp = Math.max(1, hp);
        this.health = this.maxHp;
    }

    public void setPaused(boolean value) {
        this.paused = value;
    }

    public boolean isPaused() { return paused;}

    public ImageView getView() {
        return view;
    }

    public Bounds getBounds() {
        return view.getBoundsInParent();
    }

    public int getHp() { return health; }

    public int getMaxHp() { return maxHp; }

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
