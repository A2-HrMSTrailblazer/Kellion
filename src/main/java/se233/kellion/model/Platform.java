package se233.kellion.model;

import javafx.geometry.Bounds;
import javafx.scene.Node;

public class Platform {
    private Node view;

    public Platform(Node view) {
        this.view = view;
    }

    public Node getView() {
        return view;
    }

    public void setView(Node view) {
        this.view = view;
    }

    public Bounds getBounds() {
        return view.getBoundsInParent();
    }
}
