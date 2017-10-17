package seng302.views;

import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import seng302.controllers.RoundingMechanics;
import seng302.data.RoundingSide;
import seng302.models.CompoundMark;
import seng302.models.Coordinate;
import seng302.models.Course;
import seng302.models.Mark;
import seng302.utilities.DisplayUtils;

import java.util.*;

import static seng302.data.RoundingSide.PORT;
import static seng302.data.RoundingSide.STBD;

/**
 * Class to draw the arrows to display the course order
 */
public class CourseRouteArrows {

    private final int REFRESH_THRESHOLD = 6;
    private final int ARROW_ITERATIONS_SHOWN = 3;
    private final double ARROW_DISTANCE_FROM_MARK = 0.05;

    private Course course;
    private Parent root;
    private Integer refreshTimer;
    private Color ARROW_PATH_COLOR = Color.color(0.25, 0.8, 0.25); //Light Green

    private ArrowOrderGraph arrowOrderGraph;
    private List<Set<Arrow>> shownArrows;
    private Map<CompoundMark, List<Arrow>> roundingArrowMap;

    public CourseRouteArrows(Course course, Parent root) {
        this.course = course;
        this.root = root;
        createArrowedRoute();
        refreshTimer = 0;
        shownArrows = new ArrayList<>();

    }

    /**
     * Every REFRESH_THRESHOLD method calls, this method calls the updateArrowAnimation to the
     * draw the next step in the arrow path.
     */
    public void updateCourseArrows(){
        if (refreshTimer == 0){
            updateArrowAnimation();
        }
        refreshTimer = (refreshTimer + 1) % REFRESH_THRESHOLD;
    }

    /**
     * Moves the arrow path animation to the next step.
     */
    private void updateArrowAnimation(){
        for(Arrow currentArrow : arrowOrderGraph.getAllArrows()){
            currentArrow.fade();
        }
        arrowOrderGraph.moveToNextArrows();
        updateShownArrowList(arrowOrderGraph.getCurrentArrows());
        for(Set<Arrow> arrowIteration : shownArrows){
            for(Arrow currentArrow : arrowIteration){
                currentArrow.emphasize();
            }
        }
    }

    /**
     * Updates the list of shown arrows by adding the next iteration of arrows. Removes the oldest arrow iteration
     * if needed.
     * @param nextArrowIteration The set of arrows to be shown in the upcoming iteration
     */
    private void updateShownArrowList(Set<Arrow> nextArrowIteration) {
        if(shownArrows.size() == ARROW_ITERATIONS_SHOWN) {
            shownArrows.remove(0);
        }
        shownArrows.add(nextArrowIteration);
    }

    /**
     * Creates and adds arrows to the arrowRoute
     * @param previousMark The starting compound mark for the leg
     * @param currentMark The ending compound mark for the leg
     */
    private List<Arrow> addLegArrows(CompoundMark previousMark, CompoundMark currentMark) {
        Coordinate previousPosition = previousMark.getPosition();
        Coordinate nextPosition = currentMark.getPosition();
        if(!previousMark.hasTwoMarks() && roundingArrowMap.containsKey(previousMark)){
            List<Arrow> previousArrows = roundingArrowMap.get(previousMark);
            previousPosition = previousArrows.get(previousArrows.size()-1).getCoordinate();
        }
        if(!currentMark.hasTwoMarks() && roundingArrowMap.containsKey(currentMark)){
            List<Arrow> nextArrows = roundingArrowMap.get(currentMark);
            nextPosition = nextArrows.get(0).getCoordinate();
        }

        List<Arrow> arrowList = new ArrayList<>();
        double legLength = nextPosition.greaterCircleDistance(previousPosition);
        Integer numArrows = (int)((legLength / 0.1) - 1);
        if (numArrows < 1){
            numArrows = 1;
        }
        double heading = previousPosition.headingToCoordinate(nextPosition);
        for (int num = 1; num <= numArrows; num++) {
            Coordinate position = previousPosition.coordAt((legLength / (numArrows + 1)) * num, heading);
            Arrow arrow = new Arrow(5, 10, position);
            arrow.rotate(heading + 180);
            arrowList.add(arrow);
        }
        arrowOrderGraph.addEdges(arrowList);
        return arrowList;
    }

    /**
     * Creates and returns a list of arrows that show the direction the current compound mark is rounded.
     * @param currentMark The current mark the arrows are showing the rounding for
     * @param previousMark The previous mark before the current mark
     * @param nextMark The mark after the current mark
     * @param roundingSide The rounding
     * @return
     */
    private List<Arrow> compoundMarkRoundingArrows(CompoundMark currentMark, CompoundMark previousMark, CompoundMark nextMark, RoundingSide roundingSide){
        Double heading = previousMark.getPosition().headingToCoordinate(currentMark.getPosition());
        double nextHeading = currentMark.getPosition().headingToCoordinate(nextMark.getPosition());

        List<Arrow> arrowList = new ArrayList<>();

        // Return if goes straight through gate without rounding
        if(currentMark.hasTwoMarks() && RoundingMechanics.nextCompoundMarkAfterGate(nextMark, currentMark, previousMark)){
            return arrowList;
        }

        switch(roundingSide){
            case PORT:
                arrowList.addAll(createMarkRoundingArrows(currentMark.getMark1(), heading, nextHeading, PORT));
                break;
            case STBD:
                arrowList.addAll(createMarkRoundingArrows(currentMark.getMark1(), heading, nextHeading, STBD));
                break;
            case PORT_STBD:
                arrowList.addAll(createMarkRoundingArrows(currentMark.getMark1(), heading, nextHeading, PORT));
                arrowList.addAll(createMarkRoundingArrows(currentMark.getMark2(), heading, nextHeading, STBD));
                break;
            case STBD_PORT:
                arrowList.addAll(createMarkRoundingArrows(currentMark.getMark1(), heading, nextHeading, STBD));
                arrowList.addAll(createMarkRoundingArrows(currentMark.getMark2(), heading, nextHeading, PORT));
                break;
        }

        return arrowList;
    }

    /**
     * Creates the arrows to show the rounding side of a single mark
     * @param mark The current mark
     * @param heading The heading to this mark
     * @param nextHeading The heading to the next mark after
     * @param roundingSide The rounding side of the mark, either port or stbd
     * @return A list of arrows around a single mark positioned to show the rounding side
     */
    private List<Arrow> createMarkRoundingArrows(Mark mark, double heading, double nextHeading, RoundingSide roundingSide) {
        List<Arrow> arrowList = new ArrayList<>();

        List<Coordinate> positions = RoundingMechanics.markRoundingCoordinates(mark, heading, nextHeading, roundingSide, ARROW_DISTANCE_FROM_MARK);
        Double interpolatedHeading = positions.get(0).headingToCoordinate(positions.get(2));

        Arrow mark1Arrow = new Arrow(5, 10, positions.get(0), (heading + 180));
        arrowList.add(mark1Arrow);
        Arrow mark1ArrowInterpolated = new Arrow(5, 10, positions.get(1), (interpolatedHeading + 180));
        arrowList.add(mark1ArrowInterpolated);
        Arrow mark1ArrowNext = new Arrow(5, 10, positions.get(2), (nextHeading + 180));
        arrowList.add(mark1ArrowNext);

        arrowOrderGraph.addEdges(arrowList);
        return arrowList;
    }


    /**
     * Creates the arrow route based on the course and colours it
     */
    private void createArrowedRoute() {
        arrowOrderGraph = new ArrowOrderGraph();
        roundingArrowMap = new HashMap<>();

        List<CompoundMark> courseOrder = course.getCourseOrder();
        Arrow prevArrow = null;
        for (int i = 1; i < courseOrder.size(); i ++) {
            CompoundMark prevMark = courseOrder.get(i-1);
            CompoundMark currentMark = courseOrder.get(i);

            if (i != courseOrder.size() - 1){
                CompoundMark nextMark = courseOrder.get(i+1);
                List<Arrow> roundingArrows = compoundMarkRoundingArrows(currentMark, prevMark, nextMark, course.getRoundingOrder().get(i));
                roundingArrowMap.put(currentMark, roundingArrows);
            }

            List<Arrow> legArrows = addLegArrows(prevMark, currentMark);
            if(prevArrow == null){
                arrowOrderGraph.setStartingArrow(legArrows.get(0));
            } else{
                arrowOrderGraph.addArrowEdge(prevArrow, legArrows.get(0));
            }
            prevArrow = legArrows.get(legArrows.size()-1);
            if (i != courseOrder.size() - 1){
                List<Arrow> roundingArrows = roundingArrowMap.get(currentMark);
                if(roundingArrows.size() != 0){
                    arrowOrderGraph.addArrowEdge(prevArrow, roundingArrows.get(0));
                    if(roundingArrows.size() > 3){
                        arrowOrderGraph.addArrowEdge(prevArrow, roundingArrows.get(3));
                    }
                    prevArrow = roundingArrows.get(2);
                }
            }
        }
    }

    /**
     * Removes the all the arrows in the route from the canvas
     */
    public void removeRaceRoute(){
        for(Arrow arrow : arrowOrderGraph.getAllArrows()){
            if(root instanceof Group){
                arrow.removeFromCanvas((Group)root);
            }else{
                arrow.removeFromMenu((Pane) root);
            }

        }
    }

    /**
     * Removes, recreates and redraws all the route arrows on the canvas.
     */
    public void drawRaceRoute(){
        removeRaceRoute();
        createArrowedRoute();
        for (Arrow arrow : arrowOrderGraph.getAllArrows()){
            if(root instanceof Group){
                arrow.addToCanvas((Group)root);
                arrow.setScale(DisplayUtils.zoomLevel);
            }else{
                arrow.addToMenu((Pane) root);
                arrow.setScale(0.7);
            }
            arrow.setColour(ARROW_PATH_COLOR);
            arrow.fade();
        }
    }
}
