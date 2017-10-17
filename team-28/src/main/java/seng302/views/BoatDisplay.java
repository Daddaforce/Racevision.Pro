package seng302.views;

import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Path;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Rotate;
import seng302.data.StartTimingStatus;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.chart.XYChart.Data;
import seng302.models.*;
import seng302.utilities.DisplayUtils;
import seng302.utilities.MathUtils;

import java.util.Observable;
import java.util.Observer;

import static seng302.utilities.DisplayUtils.fadeNodeTransition;
import static seng302.utilities.DisplayUtils.zoomLevel;


/**
 * Encapsulates the display properties of the boat.
 */
public class BoatDisplay implements Observer {

    private Boat boat;
    private Shape icon;
    private Polyline wake;
    private VBox annotation;
    private Path path;
    private Line annotationLine;
    private double annoOffsetX;
    private double annoOffsetY;
    private boolean annoHasMoved = false;
    private Polyline SOGVector;
    private Polyline VMGVector;
    private Line predictedStartLine;
    private Series series;
    private final double FADEDBOAT = 0.3;
    public CubicCurve sail;
    public boolean collisionInProgress = false;

    private Laylines laylines;
    private PolarTable polarTable;
    private Color color;

    public BoatDisplay(Boat boat, PolarTable polarTable) {
        this.boat = boat;
        this.annotation = new VBox();
        this.laylines = new Laylines();
        this.polarTable = polarTable;
        this.series = new Series();
        series.getData().add(new Data(boat.getLastRoundedMarkIndex(), boat.getCurrPlacing()));

        this.annoOffsetX = 25;
        this.annoOffsetY = 30;
    }

    public boolean getAnnoHasMoved() {
        return annoHasMoved;
    }

    public void setAnnoHasMoved(boolean hasMoved) {
        annoHasMoved = hasMoved;
    }

    public Line getAnnotationLine() {return annotationLine;}

    public void setAnnotationLine(Line line) {this.annotationLine = line;}

    public double getAnnoOffsetX() {
        return annoOffsetX;
    }

    public void setAnnoOffsetX(double annoOffsetX) {
        this.annoOffsetX = annoOffsetX;
    }

    public double getAnnoOffsetY() {
        return annoOffsetY;
    }

    public void setAnnoOffsetY(double annoOffsetY) {
        this.annoOffsetY = annoOffsetY;
    }

    public VBox getAnnotation() {return annotation;}

    public void setWake(Polyline wake) {
        this.wake = wake;
    }

    public Polyline getWake() {
        return wake;
    }

    public Polyline getSOGVector() {
        return SOGVector;
    }

    public void setSOGVector(Polyline SOGVector) {
        this.SOGVector = SOGVector;
    }

    public Polyline getVMGVector() {return VMGVector;}

    public void setVMGVector(Polyline VMGVector) {this.VMGVector = VMGVector;}

    public void setIcon(Shape icon) {
        this.icon = icon;
    }

    public void setAnnotation(VBox annotation) {this.annotation = annotation;}

    public Shape getIcon() {
        return icon;
    }

    public Boat getBoat() {
        return boat;
    }

    public void setPath(Path path) {this.path = path;}

    public Path getPath() {return
            path;}

    public String getSpeed(){
        return String.format("%.1fkn", boat.getCurrentSpeed());
    }

    public Color getColor() {
        return color;
    }

    public boolean isCollisionInProgress() {
        return collisionInProgress;
    }

    public void setCollisionInProgress(boolean collisionInProgress) {
        this.collisionInProgress = collisionInProgress;
    }

    public CubicCurve getSail() {
        return sail;
    }

    public void setSail(CubicCurve sail) {
        this.sail = sail;
    }

    public void setColor(Color color) {
        this.color = color;
    }



    /**
     * A getter for the start timing annotation
     * @return a string representation of the boat being early/late or on time
     */
    public String getStartTimingAnnotation(){
            if(boat.getTimeStatus().equals(StartTimingStatus.EARLY)){

                return "- Early";
            } else if(boat.getTimeStatus().equals(StartTimingStatus.LATE)){
                return "+ Late";
            }
            return null;
    }

    /**
     * Set's the boats start timing status to Late if the boat is going to be more than 5sec late, early if it'll cross the start line early or nothing if neither
     * @param race the race the boat is in
     */
    public void getStartTiming(Race race){
        long secondsElapsed = (race.getCurrentTimeInEpochMs() - race.getStartTimeInEpochMs()) / 1000; //time till race starts
        double timeToStart;
        double timeToCrossStartLine = 0;
        double distanceToStart = MathUtils.distanceToStartLine(race.getCourse(), boat);

        if(MathUtils.boatOnStartSide(race.getCourse(), boat) && MathUtils.boatHeadingToStart(race.getCourse(), boat)){
            timeToStart = distanceToStart/boat.getCurrentSpeed() * 60 * 60; //converted to seconds (nautical miles/knots = hours)
            timeToCrossStartLine = timeToStart + secondsElapsed;
        } else if(!MathUtils.boatOnStartSide(race.getCourse(), boat) && !MathUtils.boatHeadingToStart(race.getCourse(), boat)){ // If boat is on the wrong side of the line but heading to the mark (from wrong direction), this checks if it is possible for the boat to even get there in time
            timeToStart = distanceToStart/boat.getCurrentSpeed() * 60 * 60; //converted to seconds (nautical miles/knots = hours)
            timeToCrossStartLine = timeToStart + secondsElapsed;
            if(timeToCrossStartLine < 5.0){ //if it is possible for the boat to get to the other side then we can't tell much else about it
                timeToCrossStartLine = 0.0;
            }
        }
        if(timeToCrossStartLine > 5.0){ //Time to cross the line is greater than 5 sec so will be late
            boat.setTimeStatus(StartTimingStatus.LATE);
        } else if(timeToCrossStartLine < 0.0){ //Time to cross the line is less than zero so will be early
            boat.setTimeStatus(StartTimingStatus.EARLY);
        } else { //else the boat is essentially on time
            boat.setTimeStatus(StartTimingStatus.ONTIME);
        }
    }

    /**
     * Function to compute the predicted place of a parallel virtual startline based on the boats
     * current position and speed and what time the race start
     * Line will always be parallel to the startline and only shown if the boat is heading towards
     * the start line from the correct side and the race has not yet started.
     * @param race The race which the boat is in
     */
    public void getVirtualStartline(Race race){

        RaceLine startingLine = race.getCourse().getStartLine();
        CompoundMark startingEnd2 = new CompoundMark(-2, "", new Mark(-2, "", startingLine.getMark1().getPosition()));
        CompoundMark startingEnd1 = new CompoundMark(-1, "", new Mark(-1, "", startingLine.getMark2().getPosition()));
        double heading1 = (MathUtils.calculateBearingBetweenTwoPoints(startingEnd1,startingEnd2));
        double heading2 = (MathUtils.calculateBearingBetweenTwoPoints(startingEnd2,startingEnd1));

        long secondsElapsed = (race.getStartTimeInEpochMs() - race.getCurrentTimeInEpochMs()) / 1000;
        double distanceToVirtualStartLine = (boat.getCurrentSpeed() / 3600) * secondsElapsed;

        Coordinate startLineMidPoint = MathUtils.calculateMidPoint(startingLine);

        double distanceToStartLine = boat.getCurrentPosition().greaterCircleDistance(startLineMidPoint);

        Coordinate boatStartLinePos1 = startingEnd1.getPosition().coordAt(distanceToStartLine, heading2 + 90);
        Coordinate boatStartLinePos2 = startingEnd2.getPosition().coordAt(distanceToStartLine, heading2 + 90);

        Coordinate startPosition1 = boatStartLinePos1.coordAt(distanceToVirtualStartLine, heading1 + 90);
        Coordinate startPosition2 = boatStartLinePos2.coordAt(distanceToVirtualStartLine, heading1 + 90);

        CanvasCoordinate point1 = DisplayUtils.convertFromLatLon(startPosition1);
        CanvasCoordinate point2 = DisplayUtils.convertFromLatLon(startPosition2);

        Line predictedLine = new Line(point1.getX(), point1.getY(), point2.getX(), point2.getY());
        predictedLine.setStroke(color);
        predictedLine.setStrokeWidth(4);
        predictedLine.setId("predictedStartline");

        if(!MathUtils.boatOnStartSide(race.getCourse(), boat) || !MathUtils.boatHeadingToStart(race.getCourse(), boat)) {
            predictedLine.setOpacity(0);
        }

        if(boat.getCurrentSpeed() <= 0) {
            predictedLine.setOpacity(0);
        }

        predictedStartLine = predictedLine;
    }

    public void showVectors() {
        SOGVector.setVisible(true);
        VMGVector.setVisible(true);
    }

    public void hideVectors(){
        SOGVector.setVisible(false);
        VMGVector.setVisible(false);
    }


    public void unFocus(){
        fadeNodeTransition(icon, FADEDBOAT);
        fadeNodeTransition(wake, FADEDBOAT);
        fadeNodeTransition(annotation, FADEDBOAT);
        if(path != null){
            fadeNodeTransition(path, FADEDBOAT);
        }
        annotationLine.setOpacity(FADEDBOAT);
    }

    public void focus(){
        fadeNodeTransition(icon, 1.0);
        fadeNodeTransition(wake, 1.0);
        fadeNodeTransition(annotation, 0.8);
        if(path != null){
            fadeNodeTransition(path, 1.0);
        }
        if(annotationLine != null){
            annotationLine.setOpacity(1);
        }
        SOGVector.setOpacity(1);
        VMGVector.setOpacity(1);
    }

    public Series getSeries() {return series;}

    /**
     * updates display boat when boat passes a mark and positon updates. Adds new position to sparkline
     * @param boatObservable the boat that has an updated placing
     * @param arg update variable
     */
    @Override
    public void update(Observable boatObservable, Object arg) {
        Boat boat = (Boat) boatObservable;
        series.getData().add(new Data(boat.getLastRoundedMarkIndex(), boat.getCurrPlacing()));
    }


    /**
     * moves the sail location and sets its cubic shape and rotation
     * @param point  location
     * @param controlX1 cubic x bend
     * @param controlX2 cubic x bend
     * @param controlY1 cubic y bend
     * @param controlY2 cubic y bend
     * @param rotation angle of rotation from 0,0 pivot
     */
    public void moveSail(CanvasCoordinate point, double controlX1, double controlX2, double controlY1, double controlY2, double rotation){
        sail.setLayoutX(point.getX());
        sail.setLayoutY(point.getY());
        sail.setEndX(14 * zoomLevel);
        sail.setControlX1(controlX1);
        sail.setControlX2(controlX2);
        sail.setControlY1(controlY1);
        sail.setControlY2(controlY2);
        sail.getTransforms().clear();
        sail.getTransforms().add(new Rotate(rotation, 0,0 ));
        sail.toFront();
    }


    public Laylines getLaylines() {
        return laylines;
    }

    public void setLaylines(Laylines laylines) {
        this.laylines = laylines;
    }

    public PolarTable getPolarTable() {
        return polarTable;
    }

    public boolean collisionInProgress() {
        return collisionInProgress;
    }

    public Line getPredictedStartLine() {
        return predictedStartLine;
    }
}

