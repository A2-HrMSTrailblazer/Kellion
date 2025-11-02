package se233.kellion.model;

import javafx.scene.image.ImageView;

public class Bullet {
    private int damage = 10;
    private ImageView view;
    private int speed;
    private boolean fromBoss;
    private double vx = 0, vy = 0;// true = boss bullet

    public Bullet(ImageView view, int speed, boolean fromBoss) {
        this.view = view;
        this.speed = speed;
        this.fromBoss = fromBoss;
        this.vx = (view.getScaleX() > 0 ? speed : -speed);
        this.vy = 0;
    }
    public Bullet(ImageView view, double vx, double vy, boolean fromBoss) {
        this.view = view;
        this.vx = vx;
        this.vy = vy;
        this.fromBoss = fromBoss;
        this.speed = (int)Math.hypot(vx, vy);
    }

    public void update() {
        view.setX(view.getX() + vx);
        view.setY(view.getY() + vy);
    }
    
    public boolean isOutOfBounds(int width, int height) {
        double x = view.getX(), y = view.getY();
        return x < -40 || x > width + 40 || y < -40 || y > height + 40;
    }
    
    public ImageView getView() {
        return view;
    }
    public boolean isFromBoss() {
        return fromBoss;
    }
    public int getDamage() { return damage; }
    public void setDamage(int d) { this.damage = d; }
}
