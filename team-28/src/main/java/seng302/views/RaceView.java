package seng302.views;

import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Rotate;
import seng302.controllers.Controller;
import seng302.models.*;
import seng302.utilities.DisplayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mjt169 on 30/03/17.
 * Views for the live race view
 */
public class RaceView {

    private final String BOUNDARY_COLOR = "#99DEDB";

    private final String BOUNDARY_STROKE_COLOR = "#98D4D2";
    private final String MARK_COLOR = "#fff";

    private final String MARK_STROKE_COLOR = "#cdfaf4";


    /**
     * Creates a boat image, which is a boat shaped polygon with a line through the middle, parallel to the direction the image
     * is facing
     * @param color the color to draw the boat
     * @return a rendered boat image
     */
    public Polyline createBoatImage(Color color){
        Polyline boatImage = new Polyline();
        boatImage.getPoints().addAll(
                0.0,10.0,
                -3.8,10.0,
                -4.2, 8.75,
                -4.4,7.5,
                -4.6, 5.0,
                -4.7, 2.5,
                -4.8,0.0,
                -4.3,-2.5,
                -3.6,-5.0,
                -2.5,-7.5,
                -1.0, -9.0,
                0.0,-10.0,
                1.0, -9.0,
                2.5,-7.5,
                3.6,-5.0,
                4.3,-2.5,
                4.8,0.0,
                4.7,2.5,
                4.6, 5.0,
                4.4, 7.5,
                4.2, 8.75,
                3.8, 10.0,
                0.0, 10.0,
                0.0,-10.0);


        boatImage.setFill(color);
        boatImage.setId("boatIcon");
        return boatImage;
    }

    /**
     * Creates a boat wake image, which is a triangular shape with highlights
     * @return a rendered boat wake image
     */
    public Polyline createBoatWake() {
        Polyline wake = new Polyline();
        wake.getPoints().addAll(5.0, 40.0,
                0.0, -5.0,
                -5.0, 40.0);
        wake.setId("wake");
        return wake;
    }

    /**
     * Creates a Path to be used to draw a trail behind a boat
     * @param color to make the path
     * @return a new Path
     */
    public Path createBoatPath(Color color) {
        Path path = new Path();
        path.setId("boatPath");
        path.setOpacity(1);
        path.setStroke(color);
        return path;
    }

    /**
     * Creates a Polygon representing the boundaries of a course
     * @param boundaryCoordinates the coordinates describing the boundary
     * @return a boundary shaped Polygon
     */
    public Polygon createCourseBoundary(List<Coordinate> boundaryCoordinates){
        Polygon boundary = new Polygon();
        boundary.setId("boundary");
        boundary.setFill(Color.web(BOUNDARY_COLOR));
        boundary.setStroke(Color.web(BOUNDARY_STROKE_COLOR));
        boundary.setStrokeWidth(0.5);
        for(Coordinate coord : boundaryCoordinates){
            CanvasCoordinate point = DisplayUtils.convertFromLatLon(coord);
            boundary.getPoints().add(point.getX());
            boundary.getPoints().add(point.getY());
        }
        return boundary;
    }

    /**
     * Creates a Circle representing a mark
     * @param coordinate where the mark is located
     * @return a Circle object representing the mark image
     */
    public Circle createMark(Coordinate coordinate) {
        CanvasCoordinate point = DisplayUtils.convertFromLatLon(coordinate);
        Circle circle = new Circle(point.getX(), point.getY(), 4f);
        circle.setId("mark");
        circle.setFill(Color.web(MARK_COLOR));
        circle.setStroke(Color.web(MARK_STROKE_COLOR));
        circle.setStrokeWidth(2);
        return circle;
    }

    public Line createRaceLine(Coordinate end1, Coordinate end2) {
        CanvasCoordinate convertedEnd1 = DisplayUtils.convertFromLatLon(end1);
        CanvasCoordinate convertedEnd2 = DisplayUtils.convertFromLatLon(end2);
        Line line = new Line(
                convertedEnd1.getX(), convertedEnd1.getY(),
                convertedEnd2.getX(), convertedEnd2.getY()
        );
        line.setStroke(Color.web("#70aaa2"));
        line.setStrokeWidth(2);
        return line;
    }

    /**
     * Creates a vector extending from the boat with length proportional to it's speed over ground
     * @param boat the boat the SOG vector is to be drawn for
     * @param lengthOfVector the length of the vector (in terms of Nautical Miles)
     * @param color the color of the boat
     * @return a polyline extending from the boat
     */
    public Polyline createSOGVector(Boat boat, double lengthOfVector, Color color){
        Coordinate boatPosition = boat.getCurrentPosition();
        double boatBearing = boat.getHeading();
        Coordinate end2 = boatPosition.coordAt(lengthOfVector, boatBearing);
        Polyline vector = drawVectorArrow(boatPosition, end2, boatBearing, color);
        vector.setId("sogVector");
        vector.setStrokeWidth(2.8);
        return vector;
    }

    /**
     * Creates a vector extending from the boat with length proportional to it's velocity made good
     * @param boat the boat the VMG vector is to be drawn for
     * @param lengthOfVector the length of the vector (in terms of Nautical Miles)
     * @param course the course the boat is on
     * @param color the color of the boat
     * @return a polyline extending from the boat
     */
    public Polyline createVMGVector(Boat boat, double lengthOfVector, Course course, Color color){
        Coordinate boatPosition = boat.getCurrentPosition();
        int nextMark = boat.getLeg();
        List<CompoundMark> courseOrder = course.getCourseOrder();
        Coordinate markLocation;
        double lineBearing;
        if(nextMark < courseOrder.size()){
            markLocation = courseOrder.get(nextMark).getPosition();
            lineBearing = boatPosition.headingToCoordinate(markLocation);
        } else {
            lineBearing = boat.getHeading();
            lengthOfVector = 0;
        }
        Coordinate end2 = boatPosition.coordAt(lengthOfVector, lineBearing);
        Polyline vector = drawVectorArrow(boatPosition, end2, lineBearing, color);
        vector.setId("vmgVector");
        vector.setStrokeWidth(2.8);
        return vector;
    }

    /**
     * Draws a vector Arrow based from fromCoord to toCoord with the given color
     * @param fromCoord The coordinate of the base of the Arrow
     * @param toCoord The coordinate of the top (pointy-end) of the Arrow
     * @param bearing The bearing from the base to the top of the Arrow
     * @param color The color of the Arrow
     * @return a Polyline generated starting from fromCoord to toCoord in terms of CanvasCoordinates on screen.
     */
    private Polyline drawVectorArrow(Coordinate fromCoord, Coordinate toCoord, double bearing, Color color){
        double arrowHeadLength = 5;
        CanvasCoordinate arrowEnd1 = DisplayUtils.convertFromLatLon(fromCoord);
        CanvasCoordinate arrowEnd2 = DisplayUtils.convertFromLatLon(toCoord);
        double arrowLength = CanvasCoordinate.distance(arrowEnd1, arrowEnd2);

        Polyline line = new Polyline(
                0, 0,
                0, -arrowLength,
                -arrowHeadLength, -arrowLength+arrowHeadLength,
                0, -arrowLength,
                arrowHeadLength, -arrowLength+arrowHeadLength
        );
        line.getTransforms().add(new Rotate(bearing));
        line.setLayoutX(arrowEnd1.getX());
        line.setLayoutY(arrowEnd1.getY());
        line.setStroke(color);
        return line;
    }

    public Polyline drawWindArrowPolyline() {
        double arrowHeadLength = 8;
        double arrowLength = 30;

        Polyline windArrow = new Polyline(
                0, 0,
                0, -arrowLength,
                -arrowHeadLength, -arrowLength+arrowHeadLength,
                0, -arrowLength,
                arrowHeadLength, -arrowLength+arrowHeadLength
        );
        windArrow.setId("windArrow");
        windArrow.setStroke(Color.WHITE);
        windArrow.setVisible(false);
        return windArrow;
    }

    public Polyline drawNextMarkArrowPolyline() {
        double arrowHeadLength = 12;
        double arrowLength = 50;

        Polyline nextMarkArrow = new Polyline(
                0, 0,
                0, -arrowLength,
                -arrowHeadLength, -arrowLength + arrowHeadLength,
                0, -arrowLength,
                arrowHeadLength, -arrowLength + arrowHeadLength
        );
        nextMarkArrow.setId("nextMarkArrow");
        nextMarkArrow.setStroke(Color.WHITE);
        nextMarkArrow.setVisible(false);
        return nextMarkArrow;
    }

        /**
         * Creates a JavaFX line to represent a layline
         * @param bearing the angle the line is at
         * @param markCoord the source point of the layline
         * @param boatColor the color for the layline, based of the boat
         * @return a Line object
         */
    public Line drawLayline(double bearing, Coordinate markCoord, Color boatColor){
        double LAYLINELENGTH = 150;

        CanvasCoordinate markLocationXY = DisplayUtils.convertFromLatLon(markCoord.getLat(), markCoord.getLon());

        Double endPointY = LAYLINELENGTH * DisplayUtils.zoomLevel * Math.sin(Math.toRadians(bearing + 90)) + markLocationXY.getY();
        Double endPointX = LAYLINELENGTH * DisplayUtils.zoomLevel * Math.cos(Math.toRadians(bearing + 90)) + markLocationXY.getX();
        Line line = new Line(markLocationXY.getX(), markLocationXY.getY(), endPointX, endPointY);

        line.setStroke(boatColor);
        line.setStrokeWidth(2.0);
        line.setId("layline");

        return line;
    }
}
