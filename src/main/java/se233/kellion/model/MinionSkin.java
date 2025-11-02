// se233.kellion.model.MinionSkin.java
package se233.kellion.model;

import javafx.scene.image.Image;

public final class MinionSkin {
    private final Image walkLeft;
    private final Image walkRight;
    private final Image attack;
    private final double fitHeight;

    public MinionSkin(Image walkLeft, Image walkRight, Image attack, double fitHeight) {
        this.walkLeft = walkLeft;
        this.walkRight = walkRight;
        this.attack = attack;
        this.fitHeight = fitHeight;
    }

    public Image walkLeft()  { return walkLeft; }
    public Image walkRight() { return walkRight; }
    public Image attack()    { return attack; }
    public double fitHeight(){ return fitHeight; }

    public static MinionSkin fromPaths(String left, String right, String atk, double fitHeight) {
        return new MinionSkin(
                new Image(MinionSkin.class.getResource(left).toExternalForm()),
                new Image(MinionSkin.class.getResource(right).toExternalForm()),
                new Image(MinionSkin.class.getResource(atk).toExternalForm()),
                fitHeight
        );
    }
}
