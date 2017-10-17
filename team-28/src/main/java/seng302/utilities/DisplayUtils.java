package seng302.utilities;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import seng302.controllers.Controller;
import seng302.controllers.MainMenuController;
import seng302.models.*;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static java.lang.Math.abs;


/**
 * Utils class used to calculated x y from lat lon and check bounds of the canvas.
 */

public class DisplayUtils {

    public static Coordinate max, min;
    public static final String GOOGLE_API_KEY = "AIzaSyAQ8WSXVS1gXdhy5v9IpjeQL842wsMU1VQ";
    public static boolean externalDragEvent = false;
    public static boolean externalTouchEvent = false;
    public static boolean externalZoomEvent = false;
    public static final int DRAG_TOLERANCE = 45;
    private static final int FIFTY_NINE_MINUTES_MS = 3540000;
    public static boolean isRaceView = true;
    private static boolean displayPromptRunning = false;

    public static double zoomLevel = 1;
    private static int prevDragX=0;
    private static int prevDragY=0;
    private static int offsetX=0;
    private static int offsetY=0;

    private static int nextColorToBeUsed = 1;
    private static HashMap<Integer, Color> assignedColors = new HashMap<>();
    private static final ArrayList<Color> PARTY_COLORS = new ArrayList<>((Arrays.asList(Color.WHITE, Color.web("#54ca95"), Color.web("#ff7387"),
            Color.web("#fad97b"), Color.web("#ffd6c0"), Color.web("#a79be5"), Color.web("#33414c"))));


    /**
     * Takes the given lat and lon and returns a x,y coordinate scaled to the canvas size
     * and the change in multiplier to the screen size.
     * @param lat This is the required Latitude to find the Y coordinate.
     * @param lon This is the required Longitude to find the X coordinate.
     * @return Returns CanvasCoordinate with an x and a y coordinate for use in placement of boats and/or marks/gates.
     */
    public static CanvasCoordinate convertFromLatLon(double lat, double lon) {
        double canvasY;
        double canvasX;
        if(isRaceView){
            canvasY = Controller.getCanvasHeight();
            canvasX = Controller.getCanvasWidth();
        }else{
            canvasY = MainMenuController.getCanvasHeight();
            canvasX = MainMenuController.getCanvasWidth();
        }

        double changeInLat = max.getLat() - min.getLat();
        double changeInLon = max.getLon() - min.getLon();

        double xPerLon = canvasX/changeInLon;
        double yPerLat = canvasY/changeInLat;

        if (yPerLat > xPerLon) {
            yPerLat = xPerLon;
        } else {
            xPerLon = yPerLat;
        }

        int xCoord = (int) ((canvasX - changeInLon * xPerLon) / 2 + (lon - min.getLon()) * xPerLon);
        int yCoord = (int) (canvasY - ((canvasY - changeInLat * yPerLat) / 2 + (lat - min.getLat()) * yPerLat));

        xCoord *= zoomLevel;
        yCoord *= zoomLevel;

        xCoord += offsetX;
        yCoord += offsetY;

        return new CanvasCoordinate(xCoord, yCoord);
    }

    public static void resetZoom(){
        zoomLevel=1;
        offsetY=0;
        offsetX=0;
    }

    /**
     * sets the zoom level for the canvas to redrawn at. moves offsets
     * to allow the zoom to occur in the center of screen.
     * @param zoomLevel the level of zoom. 1 being standard zoom, 10 being 10x zoomed in.
     */
    public static void setZoomLevel(double zoomLevel) {

        if(zoomLevel < 1) zoomLevel = 1;
        if(zoomLevel > 10) zoomLevel = 10;

        double deltaZoom = DisplayUtils.zoomLevel - zoomLevel;
        double canvasHeight = Controller.getAnchorHeight()/2;
        double canvasWidth = Controller.getAnchorWidth()/2;

        moveOffset((canvasWidth*deltaZoom), (canvasHeight*deltaZoom));

        DisplayUtils.zoomLevel = zoomLevel;

    }

    /**
     * Changes offsets centering map on a coordinate point
     * @param location A CanvasCoordinate (x/y) point for the map to be centered
     */
    public static void moveToPoint(CanvasCoordinate location){
        double locationY = location.getY();
        double locationX = location.getX();
        double canvasHeight = Controller.getAnchorHeight()/2;
        double canvasWidth = Controller.getAnchorWidth()/2;

        moveOffset(-(locationX - canvasWidth), -(locationY - canvasHeight));

    }

    /**
     * Changes offsets centering map on a coordinate point
     * @param coordinate A Coordinate (lat/lng) point for the map to be centered
     */
    public static void moveToPoint(Coordinate coordinate) {
        CanvasCoordinate location = convertFromLatLon(coordinate.getLat(), coordinate.getLon());
        moveToPoint(location);
    }

    /**
     * Detects dragging on the display and moves the map accordingly
     * @param mouseLocationX The latest screen X location of the mouse during drag operation
     * @param mouseLocationY The latest screen Y location of the mouse during drag operation
     */
    public static void dragDisplay(int mouseLocationX, int mouseLocationY){
        if(!externalDragEvent){
            if(abs(mouseLocationX - prevDragX) < DRAG_TOLERANCE &&
                    abs(mouseLocationY - prevDragY) < DRAG_TOLERANCE){
                moveOffset((mouseLocationX-prevDragX), (mouseLocationY-prevDragY));
            }
            prevDragX = mouseLocationX;
            prevDragY  = mouseLocationY;
        }else{
            externalDragEvent = false;
        }

    }

    /**
     * moves the offsets of the display when appropriate
     * @param amountX the screen X amount of change to the current offset
     * @param amountY the screen Y amount of change to the current offset
     */
    private static void moveOffset(double amountX, double amountY){
        offsetX += amountX;
        offsetY += amountY;
    }

    /**
     * method used for getting a local image of the area for the race. Relies on the course xml files being labelled
     * the same as the map images (i.e. AC35-course.xml has AC35-map.png as it's map image name)
     * @return a string that relates to a picture
     */
    public static String getLocalMapURL(Race race){
        String id = race.getId();
        switch(id) {
            case "212121":
                return DisplayUtils.class.getResource("/graphics/mapImages/AC33-map.png").toExternalForm();
            case "222222":
                return DisplayUtils.class.getResource("/graphics/mapImages/AC35-map.png").toExternalForm();
            case "232323":
                return DisplayUtils.class.getResource("/graphics/mapImages/Athens-map.png").toExternalForm();
            case "242424":
                return DisplayUtils.class.getResource("/graphics/mapImages/Malmo-map.png").toExternalForm();
            case "252525":
                return DisplayUtils.class.getResource("/graphics/mapImages/LakeTaupo-map.png").toExternalForm();
            case "262626":
                return DisplayUtils.class.getResource("/graphics/mapImages/LakeTekapo-map.png").toExternalForm();
            default:
                return DisplayUtils.class.getResource("/graphics/mapImages/default-map.png").toExternalForm();
        }
    }


    /**
     * generates Static Google Maps image url withing the current bounds of the course on screen
     * @return the image url as a string
     */
    public static String getGoogleMapsURL(){
        double canvasY = Controller.getAnchorHeight();
        double canvasX = Controller.getAnchorWidth(); //halved to keep within google size guidelines

        Coordinate bottomMarker = new Coordinate(min.getLat(), min.getLon());
        Coordinate topMarker = new Coordinate(max.getLat(), max.getLon());

        Coordinate middlePoint = DisplayUtils.midPointFromTwoCoords(bottomMarker, topMarker);

        String mapURL = "https://maps.googleapis.com/maps/api/staticmap?" +
                "center=" +
                middlePoint.getLat() + "," + middlePoint.getLon() +
                "&size=" +
                (int)canvasX/2 + "x" + (int)canvasY/2 +
                "&style=feature:water%7Ccolor:0xaae7df" +
                "&style=feature:all%7Celement:labels%7Cvisibility:off" +
                "&visible=" +
                bottomMarker.getLat() + "," + bottomMarker.getLon() +
                "%7C" +
                topMarker.getLat() + "," + topMarker.getLon() +
                "&scale=2" +
                "&key=" + GOOGLE_API_KEY;
        return mapURL;
    }


    /**
     * Wrapper for the midPoint calculation, takes two coords instead of taking 4 doubles
     * @param first coord of first object
     * @param second coord of second object
     * @return midPoint between the two objects
     */
    public static Coordinate midPointFromTwoCoords(Coordinate first, Coordinate second){
        Double halfLat = (first.getLat() + second.getLat()) / 2;
        Double halfLon = (first.getLon() + second.getLon()) / 2;
        return new Coordinate(halfLat, halfLon);
    }


    /**
     * finds the geographic midpoint between two lat/lng points
     * @param lat1 latitude of first point
     * @param lon1 longitude of first point
     * @param lat2 latitude of second point
     * @param lon2 longitude of second point
     * @return the coordingate of the midpoint
     */
    public static Coordinate midPoint(double lat1,double lon1,double lat2,double lon2){

        double dLon = Math.toRadians(lon2 - lon1);

        //convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        lon1 = Math.toRadians(lon1);

        double Bx = Math.cos(lat2) * Math.cos(dLon);
        double By = Math.cos(lat2) * Math.sin(dLon);
        double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
        double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);

        return new Coordinate(Math.toDegrees(lat3), Math.toDegrees(lon3));
    }

    /**
     * Overload for convertFromLatLon which takes a Coordinate as a param
     * @param coordinate a Coordinate with defined lat and lon
     * @return Returns CanvasCoordinate with an x and a y coordinate for use in placement of boats and/or marks/gates.
     */
    public static CanvasCoordinate convertFromLatLon(Coordinate coordinate) {
        return convertFromLatLon(coordinate.getLat(), coordinate.getLon());
    }

    /**
     * This function sets and maximum and minimum latitudes and longitudes of the course.
     * @param minLat The minimum latitude of the course
     * @param minLon The minimum longitude of the course
     * @param maxLat The maximum latitude of the course
     * @param maxLon The maximum longitude of the course
     */
    public static void setMaxMinLatLon(double minLat, double minLon, double maxLat, double maxLon) {
        DisplayUtils.max = new Coordinate(maxLat, maxLon);
        DisplayUtils.min = new Coordinate(minLat, minLon);
    }

    /**
     * Checks whether the node is within the bounds of the canvas.
     * @param node Node to check if inside canvas.
     * @return Boolean of whether it is outside the bounds.
     */
    public static boolean isOutsideBounds(Node node){
        boolean outsideBound = false;
        if(
                node.getBoundsInParent().getMaxX() > Controller.getCanvasWidth() ||
                node.getBoundsInParent().getMinX() < 0 ||
                node.getBoundsInParent().getMaxY() > Controller.getCanvasHeight() ||
                node.getBoundsInParent().getMinY() < 0)
        {
            outsideBound = true;
        }
        return outsideBound;
    }

    public static void resetOffsets(){
        offsetX = 0;
        offsetY = 0;
    }

    /**
     * adds a fade transition to a node, so that a node fades over a set period of time
     * @param node a node in the scene that will be faded
     * @param endOpacity a double that represents the nodes opacity at the end of the fade
     */
    public static void fadeNodeTransition(Node node, double endOpacity){
        FadeTransition fadeTransition = new FadeTransition();
        fadeTransition.setNode(node);
        fadeTransition.setDuration(new Duration(500));
        fadeTransition.setFromValue(node.getOpacity());
        fadeTransition.setToValue(endOpacity);
        fadeTransition.play();
    }

    /**
     * adds a fade transition to a node, so that a node fades in and out over a period of time
     * @param node the node that will be faded
     * @param endOpacity the desired opacity the node will be faded too
     * @param showTime the millis the node will be shown at endOpacity before it fades again
     */
    public static void fadeInFadeOutNodeTransition(Node node, double endOpacity, double showTime){
        double previousOpacity = node.getOpacity();

        FadeTransition fadeTransition = new FadeTransition();
        fadeTransition.setNode(node);
        fadeTransition.setDuration(new Duration(800));
        fadeTransition.setFromValue(previousOpacity);
        fadeTransition.setToValue(endOpacity);

        FadeTransition fadeTransition2 = new FadeTransition();
        fadeTransition2.setNode(node);
        fadeTransition2.setDuration(new Duration(showTime));
        fadeTransition2.setFromValue(endOpacity);
        fadeTransition2.setToValue(endOpacity);

        FadeTransition fadeTransition3 = new FadeTransition();
        fadeTransition3.setNode(node);
        fadeTransition3.setDuration(new Duration(800));
        fadeTransition3.setFromValue(endOpacity);
        fadeTransition3.setToValue(previousOpacity);
        fadeTransition3.setOnFinished(event -> displayPromptRunning = false);

        fadeTransition.setOnFinished(event -> {
            fadeTransition2.play();
            fadeTransition2.setOnFinished(event1 -> fadeTransition3.play());
        });


        if(!displayPromptRunning){
            fadeTransition.play();
            displayPromptRunning = true;
        }
    }

    /**
     * Computes the time since the previous mark label
     * @param currTime current time in epoch ms
     * @return time since last mark in mm:ss or .. if invalid
     */
    public static String getTimeSinceLastMark(long currTime, Boat boat){
        String timeSincePassed;
        if(boat.getLastRoundedMarkTime() == 0){
            timeSincePassed = "...";
        }else{
            long timeElapsed = currTime - boat.getLastRoundedMarkTime();
            Instant instant = Instant.ofEpochMilli(timeElapsed);
            ZonedDateTime zdt = ZonedDateTime.ofInstant (instant , ZoneOffset.UTC );
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern ("mm:ss");
            timeSincePassed = formatter.format(zdt);
        }
        return timeSincePassed;
    }

    /**
     * Format string for total race time
     */
    public static String formatTotalRaceTime(int secondsOfRace){
        int mins = secondsOfRace / 60;
        int secs = secondsOfRace - (mins * 60);
        return String.format("%d m %d s", mins, secs);
    }

    /**
     * Computes the time until the next mark label
     * @param timeAtMark time to the next mark in epoch ms
     * @param currTime current time in epoch ms
     * @return the time to next mark in mm:ss or ... if invalid
     */
    public static String getTimeToNextMark(long timeAtMark, long currTime, Boat boat, Course course){
        String timeTillMark;
        long convertedTime = (timeAtMark - currTime);
        Coordinate nextMark = course.getCourseOrder().get(boat.getLastRoundedMarkIndex() + 1).getPosition();
        Boolean headingToMark = MathUtils.pointBetweenTwoAngle(boat.getCurrentPosition().headingToCoordinate(nextMark), 60, boat.getHeading());
        if (timeAtMark > 0 && convertedTime < FIFTY_NINE_MINUTES_MS && headingToMark) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("mm:ss");
            Instant instant = Instant.ofEpochMilli(convertedTime);
            ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
            timeTillMark = formatter.format(zdt);
        } else {
            timeTillMark = "...";
        }
        return timeTillMark;
    }

    public static void setIsRaceView(boolean sceneChanged){
        isRaceView = sceneChanged;
    }


    /**
     * Assigns a color to a BoatDisplay to be used when drawing things for that boat
     * @param id of the boat to assign a color to
     */
    static public Color getBoatColor(Integer id) {
        if (!assignedColors.keySet().contains(id)) {
            assignedColors.put(id, PARTY_COLORS.get(nextColorToBeUsed));
            nextColorToBeUsed++;
            nextColorToBeUsed %= PARTY_COLORS.size();
        }
        return assignedColors.get(id);
    }
}
