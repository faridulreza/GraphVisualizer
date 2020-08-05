package application;

import javafx.animation.PathTransition;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Rotate;

public class Arrow {
    private final Group Parent;
    private final Point2D s;
    private final Point2D e;
    private final Color color;
    private Polygon triangle, triangle2;
    private PathTransition transition;

    public Arrow(Group Parent, Point2D s, Point2D e, Color color) {
        this.Parent = Parent;
        this.s = s;
        this.e = e;
        this.color = color;
        canvas();
    }

    private void canvas() {
        double dx = s.getX() - e.getX();
        double dy = s.getY() - e.getY();

        double a = 24, b = 12;
        triangle = new Polygon(s.getX(), s.getY(), s.getX() - a, s.getY() + b,
                s.getX() - a, s.getY() - b);
        triangle2 = new Polygon(e.getX(), e.getY(), e.getX() - a, e.getY() + b,
                e.getX() - a, e.getY() - b);

        triangle.setFill(color);
        triangle2.setFill(color);
        Rotate rotate = new Rotate(0, 0, 0, 1, Rotate.Z_AXIS);
        triangle.getTransforms().add(rotate);
        triangle.setRotate(getAngle(dy, dx));
        triangle.toBack();
        triangle.setBlendMode(BlendMode.SRC_OVER);

        triangle2.getTransforms().add(rotate);
        triangle2.setRotate(getAngle(dy, dx));
        triangle2.toBack();
        triangle2.setBlendMode(BlendMode.SRC_OVER);
        transition = new PathTransition();

        Parent.getChildren().addAll(triangle, triangle2);
    }

    public void stop() {
        Parent.getChildren().removeAll(triangle, triangle2);
    }

    private double getAngle(double dy, double dx) { return Math.toDegrees(Math.atan2(dy, dx)) + 180; }
}