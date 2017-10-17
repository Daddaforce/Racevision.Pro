package seng302.models;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import seng302.utilities.DisplayUtils;

import java.util.*;

/**
 * Created by Louis on 14-May-17.
 *
 */

public class DistanceLine {
    private Boat firstBoat = null;
    private Boat secondBoat = null;
    private CompoundMark mark;
    private Set<Line> lines;
    private double distanceBetweenBoats;
    private Label annotation;
    private Coordinate closestPoint1;
    private Coordinate closestPoint2;

    public DistanceLine(){
        lines = new HashSet<>();
    }

    public void setFirstBoat(Boat firstBoat) {
        this.firstBoat = firstBoat;
    }

    public Boat getFirstBoat(){ return firstBoat;}

    public void setSecondBoat(Boat secondBoat) {
        this.secondBoat = secondBoat;
    }

    public Boat getSecondBoat(){ return secondBoat;}

    public void setMark(CompoundMark mark) {
        this.mark = mark;
    }

    public CompoundMark getMark(){
        return mark;
    }

    public Collection<Line> getLines() {
        return Collections.unmodifiableCollection(lines);
    }

    public void reCalcLine() {
        lines.clear();
        if (mark != null){
            if (!Objects.equals(firstBoat.getId(), secondBoat.getId())) { // Line between two boats
                Coordinate midPoint = mark.getPosition();
                if (mark.hasTwoMarks()) {
                    midPoint = DisplayUtils.midPointFromTwoCoords(mark.getMark1().getPosition(), mark.getMark2().getPosition());
                }
                createLinesBetweenTwoBoats(midPoint);
            } else {
                Coordinate midPoint = mark.getPosition();
                if (mark.hasTwoMarks()) {
                    midPoint = DisplayUtils.midPointFromTwoCoords(mark.getMark1().getPosition(), mark.getMark2().getPosition());
                }
                createLine(midPoint, firstBoat.getCurrentPosition(), true);
            }
        }
    }

    public boolean findFurtherestDistance(Coordinate target){
        Coordinate boatMidPoint = DisplayUtils.midPointFromTwoCoords(firstBoat.getCurrentPosition(), secondBoat.getCurrentPosition());
        InfiniteLine infiniteLine = new InfiniteLine(target, boatMidPoint);
        closestPoint1 = infiniteLine.closestPoint(firstBoat.getCurrentPosition());
        closestPoint2 = infiniteLine.closestPoint(secondBoat.getCurrentPosition());
        double dist1 = target.greaterCircleDistance(closestPoint1);
        double dist2 = target.greaterCircleDistance(closestPoint2);
        distanceBetweenBoats = Math.abs(dist2 - dist1);
        return dist1 < dist2;
    }

    private void createLinesBetweenTwoBoats(Coordinate target){
        if (findFurtherestDistance(target)){
            createLine(target, closestPoint1, false);
        } else{
            createLine(target, closestPoint2, false);
        }
        createLine(firstBoat.getCurrentPosition(), closestPoint1, false);
        createLine(secondBoat.getCurrentPosition(), closestPoint2, false);
        createLine(closestPoint1, closestPoint2, true);
    }



    private void createLine(Coordinate point1, Coordinate point2, Boolean toColor){
        CanvasCoordinate canvasPoint1 = DisplayUtils.convertFromLatLon(point1);
        CanvasCoordinate canvasPoint2 = DisplayUtils.convertFromLatLon(point2);
        Line line = new Line(
                canvasPoint1.getX(), canvasPoint1.getY(),
                canvasPoint2.getX(), canvasPoint2.getY()
        );
        if (toColor){
            line.setStroke(Color.web("#4DC58B"));
        }
        line.setId("distanceLine");
        lines.add(line);
    }

    public boolean sameLeg(){
        return firstBoat.getLeg() == secondBoat.getLeg();
    }

    public boolean boatsFinished(){
        return firstBoat.isFinished() && secondBoat.isFinished();
    }

    public double getDistanceBetweenBoats(){
        return distanceBetweenBoats;
    }

    public Label getAnnotation() {
        if (annotation == null){
            annotation = new Label();
            annotation.setId("distanceAnnotation");
        }
        return annotation;
    }

    public void setAnnotation(Label annotation) {
        this.annotation = annotation;
    }

    public CanvasCoordinate halfwayBetweenBoatsCoord(){
        Coordinate tempCoord = DisplayUtils.midPointFromTwoCoords(closestPoint1, closestPoint2);
        CanvasCoordinate halfway = DisplayUtils.convertFromLatLon(tempCoord);
        return halfway;
    }
}
