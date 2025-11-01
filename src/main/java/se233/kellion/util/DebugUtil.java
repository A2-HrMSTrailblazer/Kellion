package se233.kellion.util;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class DebugUtil {
    public static void drawHitbox(Pane root, Node node, String id, Color color) {
        if (node == null) return;
        Bounds b = node.getBoundsInParent();
        Rectangle r = new Rectangle(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
        r.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.25));
        r.setStroke(color);
        r.setStrokeWidth(1.5);
        r.setId(id);
        root.getChildren().add(r);
    }

    public static void updateAllHitboxes(Pane root, Node playerView, Node bossView, boolean showBoss, 
                                         java.util.List<? extends Node> bullets, boolean debugHitbox) {
        if (!debugHitbox) {
            root.getChildren().removeIf(
                    n -> n instanceof Rectangle && n.getId() != null &&
                         (n.getId().startsWith("debug") || n.getId().contains("bulletDebug"))
            );
            return;
        }
        root.getChildren().removeIf(
                n -> n instanceof Rectangle && n.getId() != null &&
                     (n.getId().startsWith("debug") || n.getId().contains("bulletDebug"))
        );
        drawHitbox(root, playerView, "debugPlayer", Color.LIME);
        if (showBoss && bossView != null)
            drawHitbox(root, bossView, "debugBoss", Color.RED);
        for (int i = 0; i < bullets.size(); i++)
            drawHitbox(root, bullets.get(i), "bulletDebug_" + i, Color.ORANGE);
    }
}
