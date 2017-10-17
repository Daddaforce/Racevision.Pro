package seng302.views;

import javafx.animation.ScaleTransition;
import javafx.scene.Scene;
import javafx.scene.input.TouchPoint;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import seng302.models.CanvasCoordinate;
import seng302.utilities.AnimationUtils;
import seng302.utilities.MathUtils;

public class DisplayTouchController {

    public static Pane touchPane;

    public DisplayTouchController(Pane touchPane) {
        this.touchPane = touchPane;
    }

    /**
     * creates and displays a line of touch between two points
     * @param currentCoordinate end of swipe point
     * @param previousCoordinate start of swipe point
     */
    public void displaySwipe(CanvasCoordinate currentCoordinate, CanvasCoordinate previousCoordinate) {
        if(MathUtils.distanceBetweenTwoPoints(currentCoordinate, previousCoordinate) > 100){
            previousCoordinate = currentCoordinate;
        }
        Line line = new Line(previousCoordinate.getX(), previousCoordinate.getY(), currentCoordinate.getX(), currentCoordinate.getY());
        line.setStrokeWidth(10.0);
        line.setStroke(Color.color(123.0/255.0, 209.0/255.0, 197.0/255.0));
        line.setId("touchLine");
        AnimationUtils.swipeAnimation(touchPane, line);
    }

}
