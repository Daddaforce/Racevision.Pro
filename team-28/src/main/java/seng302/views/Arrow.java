package seng302.views;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.transform.Rotate;
import seng302.models.CanvasCoordinate;
import seng302.models.Coordinate;
import seng302.utilities.AnimationUtils;
import seng302.utilities.DisplayUtils;

/**
 * Polyline wrapper to make arrows easy
 */
public class Arrow extends Polyline {

    private Polyline arrowLine;
    private CanvasCoordinate center;
    private Coordinate coordinate;

    Arrow(double length, double width, Coordinate coordinate) {
        this.coordinate = coordinate;
        this.center = DisplayUtils.convertFromLatLon(coordinate);
        double bottomY = this.center.getY() - length;
        arrowLine = new Polyline(this.center.getX() - width / 2, bottomY,
                                this.center.getX(), this.center.getY(),
                                width / 2  + this.center.getX(), bottomY);

        arrowLine.relocate(center.getX() - width / 2, center.getY() - length);
        arrowLine.setStrokeLineCap(StrokeLineCap.ROUND);
        arrowLine.setStrokeLineJoin(StrokeLineJoin.ROUND);
        arrowLine.setOpacity(0);
        fade();
    }

    Arrow(double length, double width, Coordinate center, double heading) {
        this(length, width, center);
        rotate(heading);
    }

    /**
     * Rotates the arrow by the given amount
     * @param amount The angle to rotate by
     */
    public void rotate(double amount){
        arrowLine.getTransforms().clear();
        arrowLine.getTransforms().add(new Rotate(amount, center.getX(), center.getY()));
    }

    public void addToCanvas(Group root){
        root.getChildren().add(arrowLine);
        FadeTransition ft = AnimationUtils.changeCourseRouteNode(arrowLine, true);
        ft.play();
    }

    public void removeFromCanvas(Group root){
        FadeTransition ft = AnimationUtils.changeCourseRouteNode(arrowLine, true);
        ft.setOnFinished(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent AE){
                root.getChildren().remove(arrowLine);
            }});
        ft.play();
    }

    public void addToMenu(Pane root){
        root.getChildren().add(arrowLine);
        arrowLine.setLayoutX(arrowLine.getLayoutX() + 155);
        arrowLine.setLayoutY(arrowLine.getLayoutY() + 30);
        FadeTransition ft = AnimationUtils.changeCourseRouteNode(arrowLine, true);
        ft.play();
    }

    public void removeFromMenu(Pane root){
        FadeTransition ft = AnimationUtils.changeCourseRouteNode(arrowLine, true);
        ft.setOnFinished(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent AE){
                root.getChildren().remove(arrowLine);
            }});
        ft.play();
    }

    public void setColour(Color color){
        arrowLine.setStroke(color);
    }

    /**
     * Emphasizes the arrow
     */
    public void emphasize(){
        AnimationUtils.fadeNodeCustom(arrowLine, 0.8);
        arrowLine.setStrokeWidth(5.0);
    }

    /**
     * Makes the arrow appear faded out
     */
    public void fade(){
        AnimationUtils.fadeNodeCustom(arrowLine, 0);
        arrowLine.setStrokeWidth(4.0);
    }

    /**
     * Scales the arrow by zoomLevel in X and Y directions
     * @param zoomLevel the amount to scale by
     */
    public void setScale(Double zoomLevel) {
        arrowLine.setScaleY(zoomLevel);
        arrowLine.setScaleX(zoomLevel);
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }
}
