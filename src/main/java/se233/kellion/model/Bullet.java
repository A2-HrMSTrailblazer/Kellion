package se233.kellion.model;

import javafx.scene.image.ImageView;

public class Bullet {
    private ImageView view;
    private int speed;
    private boolean fromBoss; // true = boss bullet

    public Bullet(ImageView view, int speed, boolean fromBoss) {
        this.view = view;
        this.speed = speed;
        this.fromBoss = fromBoss;
    }

    public void update() {
        view.setX(view.getX() + (view.getScaleX() > 0 ? speed : -speed));
    }
    
    public boolean isOutOfBounds(int width, int height) {
        double x = view.getX();
        return x < -40 || x > width + 40;
    }
    
    public ImageView getView() {
        return view;
    }

    public boolean isFromBoss() {
        return fromBoss;
    }
}
