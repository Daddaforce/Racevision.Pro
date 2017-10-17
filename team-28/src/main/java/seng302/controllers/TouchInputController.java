package seng302.controllers;

import javafx.event.EventType;
import javafx.scene.Group;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import seng302.data.BoatAction;
import seng302.models.*;
import seng302.utilities.DisplayUtils;
import seng302.utilities.MathUtils;
import seng302.utilities.PolarReader;
import seng302.views.DisplayTouchController;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

import static java.lang.Math.abs;
import static java.lang.Math.multiplyExact;

/**
 * handles user touches and swipes.
 */
public class TouchInputController extends Observable {

    private int commandInt;
    private int clientID;
    private Race race;
    private final Set<EventType<TouchEvent>> consumedTouchEvents = new HashSet<>(Arrays.asList(TouchEvent.TOUCH_MOVED, TouchEvent.TOUCH_PRESSED));
    private DisplayTouchController displayTouchController;
    private CanvasCoordinate swipeStart;
    private Boat playersBoat;
    private double timeElapsed;
    private double touchTime;
    private boolean multipleFingers;
    private PolarTable polarTable;
    private CanvasCoordinate previousCoordinate;
    private Pane touchPane;
    private Group root;
    private Controller controller;


    private int MAXIMUM_SWIPE_DISTANCE = 130;
    private int MAXIMUM_SWIPE_TIME = 300; //ms
    private int SAILS_SWIPE_ANGLE = 15;
    private int TACK_GYBE_SWIPE_ANGLE = 50;


    /**
     * constructor for class. assigns initial values
     * @param race the current race
     * @param boat the users boat
     */
    public TouchInputController(Race race, Boat boat) {
        this.race = race;
        this.polarTable = new PolarTable(PolarReader.getPolarsForAC35Yachts(), race.getCourse());
        this.playersBoat = boat;
    }

    /**
     * Initilises the listeners for the swipe/touch/scroll events ons the touchpane
     */
    private void touchEventListener() {
        touchPane.addEventFilter(TouchEvent.ANY, touch -> {
            if (touch.getEventType() == TouchEvent.TOUCH_PRESSED) {
                touchTime = System.currentTimeMillis();
            }
            checkTouchMoved(touch);
            if (consumedTouchEvents.contains(touch.getEventType())) {
                touch.consume();
            }
        });

        touchPane.setOnScrollStarted(swipe -> {
            swipeStart = new CanvasCoordinate(swipe.getScreenX(), swipe.getScreenY());
            multipleFingers = swipe.getTouchCount() != 1;
            timeElapsed = System.currentTimeMillis();
        });

        touchPane.setOnScroll(swipe -> {
            CanvasCoordinate currentCoordinate = new CanvasCoordinate(swipe.getX(), swipe.getY());
            if (previousCoordinate == null) {
                previousCoordinate = currentCoordinate;
            }
            if (swipe.getTouchCount() > 0) {
                displayTouchController.displaySwipe(currentCoordinate, previousCoordinate);
                previousCoordinate = currentCoordinate;
            }

        });

        touchPane.setOnScrollFinished(swipe -> {
            CanvasCoordinate swipeEnd = new CanvasCoordinate(swipe.getScreenX(), swipe.getScreenY());
            double swipeDistance = MathUtils.distanceBetweenTwoPoints(swipeStart, swipeEnd);
            if (swipeDistance > MAXIMUM_SWIPE_DISTANCE && Math.abs(System.currentTimeMillis() - timeElapsed) < MAXIMUM_SWIPE_TIME && !multipleFingers && !DisplayUtils.externalTouchEvent) {
                displayTouchController.displaySwipe(swipeEnd, swipeStart);
                double swipeBearing = MathUtils.getHeadingBetweenTwoCoordinates(swipeStart, swipeEnd);
                swipeAction(swipeBearing);
                previousCoordinate = null;
            }
        });
    }



    /**
     * acts upon the action of a swipe gestures. Detects action from the swipe bearing and performs action.
     * provides user feedback if action cannot be performed
     * @param swipeBearing the bearing of the swipe gesture
     */
    private void swipeAction(double swipeBearing) {
        double boatHeading = race.getBoatById(clientID).getHeading();
        double headingDifference = abs(boatHeading - swipeBearing) % 180;
        if (root.getTransforms().size() > 1) {
            headingDifference = swipeBearing;
        }
        if (headingDifference <= SAILS_SWIPE_ANGLE || headingDifference >= (180 - SAILS_SWIPE_ANGLE)) {
            if (Math.abs(boatHeading - swipeBearing) < SAILS_SWIPE_ANGLE || Math.abs(boatHeading - swipeBearing) > (360 - SAILS_SWIPE_ANGLE)) {
                if (playersBoat.isSailsIn()) {
                    commandInt = BoatAction.SAILS_OUT.getType();
                    setChanged();
                }else{
                    controller.setUserHelpLabel("Sails are already out", Color.web("#f47777"));
                }
            } else {
                if (!playersBoat.isSailsIn()) {
                    commandInt = BoatAction.SAILS_OUT.getType();
                    setChanged();
                }else{
                    controller.setUserHelpLabel("Sails are already in", Color.web("#f47777"));
                }
            }
        } else {
            double optimumHeading = playersBoat.getTackOrGybeHeading(race.getCourse(), polarTable);
            if (Math.abs(optimumHeading - swipeBearing) < TACK_GYBE_SWIPE_ANGLE || Math.abs(optimumHeading - swipeBearing) > (360 - TACK_GYBE_SWIPE_ANGLE)) {
                commandInt = BoatAction.TACK_GYBE.getType();
                setChanged();
            }else{
                controller.setUserHelpLabel("Cannot tack or gybe in that direction", Color.web("#f47777"));
            }
        }
        notifyObservers();
    }

    /**
     * moves the heading of the boat towards a touch on the screen
     * @param touchEvent the touch on the screen
     */
    private void checkTouchMoved(TouchEvent touchEvent) {
        Boat playersBoat = race.getBoatById(clientID);
        if (touchEvent.getTouchPoints().size() == 1 && !DisplayUtils.externalTouchEvent && (System.currentTimeMillis() - touchTime) > 200) {
            CanvasCoordinate touchPoint = new CanvasCoordinate(touchEvent.getTouchPoint().getSceneX(), touchEvent.getTouchPoint().getSceneY());
            CanvasCoordinate boatPosition = DisplayUtils.convertFromLatLon(playersBoat.getCurrentPosition());
            double windAngle = race.getCourse().getWindDirection() + 180;
            double touchAngle = touchPoint.getAngleFromSceneCentre(boatPosition) - 270;

            touchAngle = (touchAngle +360)%360;
            windAngle = (windAngle +360)%360;

            double boatHeading = playersBoat.getHeading();
            double oppositeWindAngle = (windAngle +540)%360;

            double newWindAngle;
            double newBoatHeading;
            double newTouchAngle;

            if (!((boatHeading > (touchAngle - 2)) && (boatHeading < (touchAngle + 2)))) {
                if ((((boatHeading - touchAngle) < 180) && ((boatHeading - touchAngle) > 0)) ||
                        (((boatHeading - touchAngle) < -180) && ((boatHeading - touchAngle) > -360))) {             //Anti Clockwise
                    if (boatHeading > touchAngle) {
                        commandInt = changeRotationDirection(windAngle, oppositeWindAngle, boatHeading, touchAngle);
                    } else if (boatHeading < touchAngle) {
                        if (windAngle < 180) {
                            newWindAngle = windAngle + 360;
                        } else {
                            newWindAngle = windAngle;
                            oppositeWindAngle += 360;
                        }
                        newBoatHeading = boatHeading + 360;

                        commandInt = changeRotationDirection(newWindAngle, oppositeWindAngle, newBoatHeading, touchAngle);
                    }
                } else if ((((boatHeading - touchAngle) > 180) && ((boatHeading - touchAngle) < 360)) ||
                        (((boatHeading - touchAngle) > -180) && ((boatHeading - touchAngle) < 0))) {                //Clockwise
                    if (boatHeading < touchAngle) {
                        commandInt = changeOppositeRotationDirection(windAngle, oppositeWindAngle, boatHeading, touchAngle);
                    } else if (boatHeading > touchAngle) {
                        if (windAngle < 180) {
                            newWindAngle = windAngle + 360;
                        } else {
                            newWindAngle = windAngle;
                            oppositeWindAngle += 360;
                        }
                        newTouchAngle = touchAngle + 360;

                        commandInt = changeOppositeRotationDirection(newWindAngle, oppositeWindAngle, boatHeading, newTouchAngle);
                    }
                }
            } else {
                commandInt = -1;
            }

            if (commandInt != -1) {
                setChanged();
                notifyObservers();
            }
        }
    }


    /**
     * determines direction of boat rotation based upon touch location
     * @param windAngle the angle of the wind
     * @param oppositeWindAngle the inverse of the wind angle
     * @param boatHeading the heading of the boat
     * @param touchAngle the angle of the touch compared to the angle of the boat
     * @return the direction of the rotation
     */
    private int changeRotationDirection(double windAngle, double oppositeWindAngle, double boatHeading, double touchAngle) {

        int direction = -1;

        if (boatHeading > windAngle && touchAngle < windAngle) {
            direction = BoatAction.DOWNWIND.getType();
        } else if (boatHeading > oppositeWindAngle && touchAngle < oppositeWindAngle) {
            direction = BoatAction.UPWIND.getType();
        } else if (((touchAngle - windAngle) < 180 && (touchAngle - windAngle) > 0) || ((touchAngle - windAngle) < -180 && (touchAngle - windAngle) > -360)) {
            direction = BoatAction.DOWNWIND.getType();
        } else if (((touchAngle - oppositeWindAngle) < 180 && (touchAngle - oppositeWindAngle) > 0) || ((touchAngle - oppositeWindAngle) < -180 && (touchAngle - oppositeWindAngle) > -360)) {
            direction = BoatAction.UPWIND.getType();
        }

        return direction;
    }


    /**
     * determines the inverse direction of boat rotation based upon touch location
     * @param windAngle the angle of the wind
     * @param oppositeWindAngle the inverse of the wind angle
     * @param boatHeading the heading of the boat
     * @param touchAngle the angle of the touch compared to the angle of the boat
     * @return the inverse direction of the rotation
     */
    private int changeOppositeRotationDirection(double windAngle, double oppositeWindAngle, double boatHeading, double touchAngle) {

        int direction = -1;

        if (boatHeading < windAngle && touchAngle > windAngle) {
            direction = BoatAction.DOWNWIND.getType();
        } else if (boatHeading < oppositeWindAngle && touchAngle > oppositeWindAngle) {
            direction = BoatAction.UPWIND.getType();
        } else if (((touchAngle - windAngle) < 180 && (touchAngle - windAngle) > 0) || ((touchAngle - windAngle) < -180 && (touchAngle - windAngle) > -360)) {
            direction = BoatAction.UPWIND.getType();
        } else if (((touchAngle - oppositeWindAngle) < 180 && (touchAngle - oppositeWindAngle) > 0) || ((touchAngle - oppositeWindAngle) < -180 && (touchAngle - oppositeWindAngle) > -360)) {
            direction = BoatAction.DOWNWIND.getType();
        }
        return direction;
    }

    int getCommandInt() {
        return commandInt;
    }

    /**
     * sets up parameters and inits listeners for the class
     * @param root
     * @param touchPane
     * @param controller
     */
    public void setUp(Group root, Pane touchPane, Controller controller){
        this.touchPane = touchPane;
        this.root = root;
        this.displayTouchController = new DisplayTouchController(touchPane);
        this.controller = controller;
        touchEventListener();
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }
}

