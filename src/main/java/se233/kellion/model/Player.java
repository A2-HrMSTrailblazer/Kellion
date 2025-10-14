package se233.kellion.model;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Player {
    private ImageView view;
    private double x, y;
    private double speed = 5;

    public Player(double x, double y, String imagePath) {
        this.x = x;
        this.y = y;

        // Load sprite sheet from classpath
        Image image = new Image(getClass().getResource(imagePath).toExternalForm());

        // Crop to desired sprite: (130, 0) with 64x64
        view = new ImageView(image);
        view.setViewport(new Rectangle2D(130, 0, 64, 64));
        view.setFitWidth(64);             // Keep sprite size original, or scale as needed
        view.setFitHeight(64);
        view.setX(x);
        view.setY(y);
    }

    public ImageView getView() { return view; }

    public void moveLeft() { view.setX(view.getX() - speed); }
    public void moveRight() { view.setX(view.getX() + speed); }
}
