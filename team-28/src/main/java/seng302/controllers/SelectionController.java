package seng302.controllers;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Rotate;
import seng302.models.Mark;
import seng302.utilities.DisplayUtils;
import seng302.views.BoatDisplay;

import java.util.*;

import static java.lang.Math.abs;
import static seng302.utilities.DisplayUtils.DRAG_TOLERANCE;
import static seng302.utilities.DisplayUtils.isOutsideBounds;
import static seng302.utilities.DisplayUtils.zoomLevel;

/**
 * Created by clbmi on 10/07/2017.
 *
 */
public class SelectionController extends Observable {

    private Group root;
    private ScoreBoardController scoreBoardController;
    private List<BoatDisplay> displayBoats;
    private Controller controller;
    private Set<BoatDisplay> selectedBoats = new HashSet<>();
    private ImageCursor boatCursor = new ImageCursor(new Image("graphics/boat-select-cursor.png"), 7, 7);
    private Mark selectedMark = null;
    private BoatDisplay trackingBoat = null;
    private BoatDisplay clientBoat = null;
    private boolean isTrackingPoint = false;
    private double rotationOffset = 0;
    private boolean isRotationEnabled = false;

    public SelectionController(Group root, ScoreBoardController scoreBoardController, Controller controller) {
        this.root = root;
        this.scoreBoardController = scoreBoardController;
        this.controller = controller;
    }

    /**
     * Gets the current selected boat and changes the tracking boolean. Also notify observers of this change
     */
    void trackBoat() {
        BoatDisplay selectedBoat = getTrackingBoat();
        if (selectedBoat != null) {
            if (isTrackingPoint()) {
                controller.setZoomSliderValue(1);
                setTrackingPoint(false);
            } else {
                controller.setZoomSliderValue(3);
                setTrackingPoint(true);
                setMapVisibility(false);
            }
            setChanged();
            notifyObservers(true);
        }
    }

    /**
     * If the user is tracking a course feature, this method deals with when and what to rotate and the zooming level
     * along with notifying the observers of the changes
     */
    void zoomTracking() {
        if (isTrackingPoint && selectedMark != null){
            DisplayUtils.moveToPoint(selectedMark.getPosition());
            setChanged();
            notifyObservers(true);
       }
        if (isTrackingPoint && trackingBoat != null) {
            trackingBoat.getIcon().toFront();
           if(isRotationEnabled){
                if(zoomLevel > 1){
                    rotationOffset = -trackingBoat.getBoat().getHeading();
                    updateRotation();
                }
            }
            DisplayUtils.moveToPoint(trackingBoat.getBoat().getCurrentPosition());
            setChanged();
            notifyObservers(true);
        }

    }


    /**
     * adds Event handlers to areas of the course than don't contain boat, so deselect of boat
     * can be detected
     */
    void addDeselectEvents(Polygon boundary){
        boundary.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (!e.isSynthesized()){
                deselectBoat();
            }
        });

        boundary.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            if (!e.isSynthesized()){
                deselectBoat();
            }
        });

        controller.mapImageView.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (!e.isSynthesized()){
                deselectBoat();
            }
        });

        controller.mapImageView.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            if (!e.isSynthesized()){
                deselectBoat();
            }
        });


    }

    /**
     * Controls the users ability to deselect the boat if the boat is currently selected
     */
    void deselectBoat() {
        for(BoatDisplay boat : displayBoats){
            boat.focus();
            boat.getLaylines().removeDrawnLines(root);
            selectedBoats.remove(boat);
            boat.getSeries().getNode().setOpacity(1);
            trackingBoat = clientBoat;
            if(trackingBoat == null){
                scoreBoardController.btnTrack.setVisible(false);
            }
            selectedMark = null;
            isTrackingPoint = false;
            rotationOffset = 0;
            updateRotation();
            scoreBoardController.highlightUserBoat();

        }
        setChanged();
        notifyObservers(false);
    }

    /**
     * adds event handler to marks so we can detect if selected by the user
     * @param mark mark that the user has clicked on
     */
    void addMarkSelectionHandlers(Mark mark){

        Circle circle = mark.getIcon();

        circle.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {

            rotationOffset = 0;
            updateRotation();

            if(mark != selectedMark){
                zoomToMark(mark);
            }else{
                controller.setZoomSliderValue(1);
                selectedMark = null;
                isTrackingPoint = false;

                DisplayUtils.resetOffsets();
                setMapVisibility(true);

            }
            setChanged();
            notifyObservers(true);
        });

        circle.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            root.setCursor(boatCursor);
        });

        circle.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            root.setCursor(Cursor.DEFAULT);
        });
    }

    /**
     * Zooms view to the specified mark
     * @param mark the mark to zoom to
     */
    public void zoomToMark(Mark mark) {
        controller.setZoomSliderValue(3);
        DisplayUtils.moveToPoint(mark.getPosition());
        selectedMark = mark;
        isTrackingPoint = true;
        trackingBoat = null;
        setMapVisibility(false);
    }

    /**
     * updates the rotation of the canvas to the rotationOffset class variable
     */
    private void updateRotation(){
        root.getTransforms().clear();
        root.getTransforms().add(new Rotate(rotationOffset, controller.getCanvasWidth()/2, controller.getCanvasHeight()/2));
    }

    /**
     * controls the display of showing the track button
     */
    private void setBoatFocus(){
        scoreBoardController.btnTrack.setVisible(true);
        for (BoatDisplay boatDisplay : selectedBoats) {
            boatDisplay.getIcon().toFront();
        }
        for(BoatDisplay boat : displayBoats){
            if(!selectedBoats.contains(boat)){
                boat.unFocus();
                boat.getLaylines().removeDrawnLines(root);
                boat.getSeries().getNode().setOpacity(0.2);
            }else{
                boat.focus();
                boat.getSeries().getNode().setOpacity(1);
            }
        }
    }

    /**
     * adds event hadnlers so we can detect if the user has selected a boat
     * @param boat
     */
    void addBoatSelectionHandler(BoatDisplay boat){
        Shape boatImage = boat.getIcon();
        boatImage.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            trackingBoat = boat;
            if (selectedBoats.isEmpty() || event.isControlDown()) {
                if (selectedBoats.contains(boat)){
                    selectedBoats.remove(boat);
                } else {
                    selectedBoats.add(boat);
                }

            } else {
                selectedBoats.clear();
                selectedBoats.add(boat);
            }
            if (selectedBoats.isEmpty()){
                deselectBoat();
            } else {
                setBoatFocus();
            }
            selectedMark = null;
            setChanged();
            notifyObservers(false);

        });


        boatImage.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            root.setCursor(boatCursor);
        });

        boatImage.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            root.setCursor(Cursor.DEFAULT);
        });
    }

    public void setTrackingBoat(BoatDisplay boat){this.trackingBoat = boat;}

    public Set<BoatDisplay> getSelectedBoats() {
        return Collections.unmodifiableSet(selectedBoats);
    }

    public BoatDisplay getTrackingBoat() {
        return trackingBoat;
    }

    public boolean isClientBoat(BoatDisplay boat) {
        return boat == clientBoat;
    }

    public boolean isTrackingPoint() {
        return isTrackingPoint;
    }

    public void setTrackingPoint(boolean trackingPoint) {
        this.isTrackingPoint = trackingPoint;
    }

    public void setMapVisibility(boolean visible){
        controller.mapImageView.setVisible(visible);
    }

    public double getRotationOffset() {
        return rotationOffset;
    }

    public boolean isRotationEnabled() {
        return isRotationEnabled;
    }

    public void setRotationOffset(double rotationOffset) {
        this.rotationOffset = rotationOffset;
    }

    @FXML
    public void zoomToggle(boolean zoomed){
        isRotationEnabled = zoomed;
        rotationOffset = 0;
        updateRotation();
    }

    public void setDisplayBoats(List<BoatDisplay> displayBoats) {
        this.displayBoats = displayBoats;
        for(BoatDisplay boatDisplay : displayBoats ){
            if(boatDisplay.getBoat().getId() == Main.getClient().getClientID()){
                trackingBoat = boatDisplay;
                clientBoat = boatDisplay;
                scoreBoardController.btnTrack.setVisible(true);
            }
        }
    }
}
