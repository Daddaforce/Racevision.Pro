package seng302.models;

import javafx.scene.shape.Polygon;
import seng302.controllers.CollisionManager;
import seng302.controllers.RoundingMechanics;
import seng302.data.BoatStatus;
import seng302.data.RoundingSide;
import seng302.utilities.MathUtils;
import seng302.utilities.PolarReader;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import static seng302.models.AIDifficulty.HARD;

/**
 * Created by atc60 on 8/09/17.
 * A class to control an AI boat for the user
 * to practice racing against
 */
public class AIBoat extends Boat{
    private static final Double ROUNDING_DISTANCE = 0.08; //Distance out from mark to round it
    private static final Double PASSING_DELTA = 0.01;
    private static final Double COLLISION_CHECK_DISTANCE = 0.15; //Distance to look ahead for future collisions
    private static final Double BOUNDARY_OFFSET = 0.05;
    public static final Integer START_MOVING_TIME_MS = 10000; // Milliseconds before race to start moving
    private static final Double TURN_SPEED = 9.0;

    private List<Coordinate> nextCoordinates;
    private Integer targetPositionIndex;
    private Course course;
    private PolarTable polarTable;
    private Polygon boundary;
    private AIDifficulty difficulty;
    private Boolean currentlyAvoiding;

    public AIBoat(Integer id, String name, String nickName, double speed, Course course, AIDifficulty difficulty) {
        super(id, name, nickName, speed);
        this.difficulty = difficulty;
        this.polarTable = new PolarTable(PolarReader.getPolarsForAC35Yachts(), course);
        this.course = course;
        nextCoordinates = new ArrayList<>();
        setNextRoundingCoordinates();
        targetPositionIndex = 0;
        CollisionManager collisionManager = new CollisionManager();
        this.boundary = collisionManager.createCourseBoundary(course.getBoundary());
        currentlyAvoiding = false;
    }

    /**
     * This function checks whether an AI boat has rounded a mark
     * If it has, it updates the next mark to round to be the next mark
     * in the course using setNextRoundingCoordinates
     */
    public void checkRounding() {
        Coordinate targetPosition = nextCoordinates.get(targetPositionIndex);
        if(getCurrentPosition().greaterCircleDistance(targetPosition) < PASSING_DELTA && getStatus() != BoatStatus.FINISHED){
            targetPositionIndex++;
            currentlyAvoiding = false;
            if(targetPositionIndex == nextCoordinates.size()){
                targetPositionIndex = 0;
                setLastRoundedMarkIndex(getLastRoundedMarkIndex()+1);
                setNextRoundingCoordinates();
            }
        }
    }

    /**
     * This function sets the next rounding coordinate of the AI
     * boat to be that of the next mark in the course
     * If the last rounded mark is the finish line
     * the boat has its status set as finished.
     */
    private void setNextRoundingCoordinates(){
        List<CompoundMark> courseOrder = course.getCourseOrder();
        Integer lastRoundedIndex = getLastRoundedMarkIndex();
        if(lastRoundedIndex == courseOrder.size() - 1){
            setStatus(BoatStatus.FINISHED);
            setFinalTargetCoordinate();
            return;
        }
        CompoundMark currentMark = courseOrder.get(getLastRoundedMarkIndex()+1);
        nextCoordinates.clear();

        if(currentMark != course.getStartLine() && difficulty == HARD){
            addTackandGybeMarks(getCurrentPosition(), currentMark.getPosition(), 0);
        }

        if(currentMark instanceof RaceLine){
            nextCoordinates.add(currentMark.getPosition());
        } else {
            addMarkRoundingCoordinates(lastRoundedIndex+1);
        }
    }

    /**
     * Sets the final coordinate for the AI to be some distance behind the finish line
     */
    private void setFinalTargetCoordinate() {
        nextCoordinates.clear();
        CompoundMark finishLine = course.getFinishLine();
        nextCoordinates.add(finishLine.getPosition().coordAt(1, getHeading()));
    }

    /**
     * Adds the mark rounding coordinates for a mark to next coordinates
     * @param markIndex The index of the mark to be rounded in the race order
     */
    private void addMarkRoundingCoordinates(Integer markIndex) {
        List<CompoundMark> courseOrder = course.getCourseOrder();
        CompoundMark previousMark = courseOrder.get(markIndex-1);
        CompoundMark currentMark = courseOrder.get(markIndex);
        CompoundMark nextMark = courseOrder.get(markIndex+1);

        if(currentMark.hasTwoMarks() && RoundingMechanics.nextCompoundMarkAfterGate(nextMark, currentMark, previousMark)) {
            nextCoordinates.add(currentMark.getPosition());
        } else {
            Double heading = previousMark.getPosition().headingToCoordinate(currentMark.getPosition());
            Double nextHeading = currentMark.getPosition().headingToCoordinate(nextMark.getPosition());
            RoundingSide roundingSide = course.getRoundingOrder().get(getLastRoundedMarkIndex()+1).firstMarkRoundingSide();
            nextCoordinates.addAll(RoundingMechanics.markRoundingCoordinates(currentMark.getMark1(), heading, nextHeading, roundingSide, ROUNDING_DISTANCE));
        }
    }

    /**
     * Updates the target heading of the boat
     * using the current position and the
     * desired position of the boat
     */
    public void updateTargetHeading() {
        Coordinate targetPosition = nextCoordinates.get(targetPositionIndex);
        Double headingToNextMark = getCurrentPosition().headingToCoordinate(targetPosition);
        setTargetHeading(headingToNextMark);
    }

    /**
     * Checks whether the AI boat is in line to collide with future
     * marks
     * @param mark The mark the AI boat is potentially colliding with
     * @return true if the AI boat will collide with mark, false otherwise
     */
    public boolean checkFutureCollision(Mark mark) {
        Coordinate targetPosition = nextCoordinates.get(targetPositionIndex);
        Coordinate checkPointEnd = getCurrentPosition().coordAt(COLLISION_CHECK_DISTANCE, heading);
        Double distance = MathUtils.distanceToLineSegment(getCurrentPosition(), checkPointEnd, mark.getPosition());

        return distance <= CollisionManager.MARK_SENSITIVITY &&
                getCurrentPosition().greaterCircleDistance(targetPosition) > getCurrentPosition().greaterCircleDistance(mark.getPosition());
    }

    /**
     * If the AI boat is on course for a collision
     * it will add a new mark to its target position
     * list.  This new mark will set the boat onto a non-colliding route
     */
    public void avoidFutureCollision() {
        if(currentlyTurning() || currentlyAvoiding) return;
        for(Integer markID : course.getAllMarks().keySet()){
            Mark mark = course.getAllMarks().get(markID);
            if(checkFutureCollision(mark)){
                Coordinate targetPosition = nextCoordinates.get(targetPositionIndex);
                Coordinate avoidCoordinate = coordinateToAvoid(mark.getPosition());
                nextCoordinates.add(targetPositionIndex, avoidCoordinate);
                currentlyAvoiding = true;
            }
        }
    }

    /**
     * Returns true if boat is currently turning (if there is a difference between current heading and target heading
     * @return boolean if boat is current turning
     */
    private boolean currentlyTurning() {
        return Math.abs(heading - targetHeading) > 1e-6;
    }

    /**
     * Function computes position of new avoidance
     * target coordinate for the boat to head
     * towards
     * @param collisionPosition the position of the potential colliding mark
     * @return the new target coordinate
     */
    private Coordinate coordinateToAvoid(Coordinate collisionPosition) {
        Coordinate targetPosition = nextCoordinates.get(targetPositionIndex);
        Double avoidHeading = (heading + 270) % 360;
        Coordinate coord1 = collisionPosition.coordAt(ROUNDING_DISTANCE, avoidHeading);

        Double avoidHeading2 = (heading + 90) % 360;
        Coordinate coord2 = collisionPosition.coordAt(ROUNDING_DISTANCE, avoidHeading2);

        Double dist1 = getCurrentPosition().greaterCircleDistance(coord1) + coord1.greaterCircleDistance(targetPosition);
        Double dist2 = getCurrentPosition().greaterCircleDistance(coord2) + coord2.greaterCircleDistance(targetPosition);
        return dist1 < dist2 ? coord1 : coord2;
    }

    /**
     * Checks whether the AI boat needs to be tacking or gybing
     * and adds the new tacking/gybing marks to the target
     * coordinate list if needed
     * @param lastCoordinate the previous Coordinate
     * @param nextCoordinate the next Coordinate
     * @param headingOption the heading switch option (allows boat to zigzag)
     */
    public void addTackandGybeMarks(Coordinate lastCoordinate, Coordinate nextCoordinate, Integer headingOption) {
        double TWD = course.getWindDirection();
        double headingBetweenMarks = lastCoordinate.headingToCoordinate(nextCoordinate);

        double trueWindAngle;
        double optimumAngle;

        if(MathUtils.pointBetweenTwoAngle(TWD, polarTable.getOptimumTWA(true), headingBetweenMarks)){
            trueWindAngle = polarTable.getOptimumTWA(true);
        } else if (MathUtils.pointBetweenTwoAngle((TWD + 180) % 360, 180 - polarTable.getOptimumTWA(false), headingBetweenMarks)){
            trueWindAngle = polarTable.getOptimumTWA(false);
        } else{
            //Don't need to tack/gybe
            return;
        }

        double optimumHeadingA = (TWD - trueWindAngle + 360) % 360;
        double optimumHeadingB = (TWD + trueWindAngle + 360) % 360;

        double angleToOptimumA = MathUtils.getAngleBetweenTwoHeadings(heading, optimumHeadingA);
        double angleToOptimumB = MathUtils.getAngleBetweenTwoHeadings(heading, optimumHeadingB);
        if(headingOption == 1){
            optimumAngle = optimumHeadingA;
        } else if(headingOption == -1){
            optimumAngle = optimumHeadingB;
        } else{
            if (angleToOptimumA <= angleToOptimumB) {
                optimumAngle = optimumHeadingB;
                headingOption = -1;
            } else {
                optimumAngle = optimumHeadingA;
                headingOption = 1;
            }
        }

        double lengthOfTack = Math.abs(calculateLengthOfTack(trueWindAngle, nextCoordinate, lastCoordinate));

        Coordinate tackingCoord = lastCoordinate.coordAt(lengthOfTack,optimumAngle);
        if(boundary != null && !boundary.contains(tackingCoord.getLat(), tackingCoord.getLon())) {
            Double distance = distanceToBoundary(lastCoordinate, tackingCoord, course.getBoundary());
            tackingCoord = lastCoordinate.coordAt(distance - BOUNDARY_OFFSET, optimumAngle);
            nextCoordinates.add(tackingCoord);
            addTackandGybeMarks(tackingCoord, nextCoordinate, headingOption * -1);
        } else{
            nextCoordinates.add(tackingCoord);
        }
    }

    /**
     * Calculates the distance to the boundary that a boat
     * on route to target position, may exit through
     * @param lastMark the last position of the boat
     * @param tackingCoord the current tacking coordinate (out of bounds)
     * @param boundary the list of coordinates on the boundary
     * @return if a crossing is found, returns the distance, otherwise returns null
     */
    private Double distanceToBoundary(Coordinate lastMark, Coordinate tackingCoord, ArrayList<Coordinate> boundary) {
        for(int i = 0; i < boundary.size(); i++){
            Coordinate boundaryEnd1 = boundary.get(i);
            Coordinate boundaryEnd2 = boundary.get((i+1) % boundary.size());
            if(crossingLines(lastMark, tackingCoord, boundaryEnd1, boundaryEnd2)){
                InfiniteLine infiniteLine1 = new InfiniteLine(lastMark, tackingCoord);
                InfiniteLine infiniteLine2 = new InfiniteLine(boundaryEnd1, boundaryEnd2);
                Coordinate intersectionPoint = InfiniteLine.intersectionPoint(infiniteLine1, infiniteLine2);
                return lastMark.greaterCircleDistance(intersectionPoint);
            }
        }
        return null;
    }

    /**
     * Calculates if given coordinates (forming two lines) intersect
     * @param lastMark the start of boats target position line
     * @param tackingCoord the end of the boats target position line
     * @param boundaryEnd1 the start of a boundary line
     * @param boundaryEnd2 the end of a boundary line
     * @return true if the two finite lines cross, false otherwise.
     */
    private boolean crossingLines(Coordinate lastMark, Coordinate tackingCoord, Coordinate boundaryEnd1, Coordinate boundaryEnd2) {
        return Line2D.linesIntersect(lastMark.getLon(), lastMark.getLat(), tackingCoord.getLon(), tackingCoord.getLat(),
                                    boundaryEnd1.getLon(), boundaryEnd1.getLat(), boundaryEnd2.getLon(), boundaryEnd2.getLat());
    }

    /**
     * Updates the location of a given boat to be displayed to the clients
     * @param timePassed time passed since last update
     * @param course the course the boat is racing on
     */
    @Override
    public void updateLocation(Double timePassed, Course course) {
        double distanceGained = timePassed * getCurrentSpeed() / (60 * 60);
        Coordinate targetPosition = nextCoordinates.get(targetPositionIndex);
        distanceGained = Math.min(distanceGained, currentPosition.greaterCircleDistance(targetPosition));
        Coordinate newPos = currentPosition.coordAt(distanceGained, heading);
        setPosition(new Coordinate(newPos.getLat(), newPos.getLon()));
        setCurrentVMG(calculateVMGToMark(course));
    }

    /**
     * calculates the optimum length of a tack (one that gives minimum number of turns)
     * @param TrueWindAngle the optimum angle the boat should head for maximum speed
     * @param nextMark the target position of the boat
     * @param lastMark the previous position of the boat
     * @return length before next turn
     */
    public double calculateLengthOfTack(double TrueWindAngle,Coordinate nextMark, Coordinate lastMark){
        double lengthOfLeg = lastMark.greaterCircleDistance(nextMark);
        double lengthOfTack = (lengthOfLeg/2.0)/(Math.cos(Math.toRadians(TrueWindAngle)));
        return lengthOfTack;
    }

    /**
     * Slowly turns the boat heading towards the target heading.
     * Speed of turning depends on amount of time passed.
     * @param raceSecondsPassed The time passed since last update.
     */
    public void updateBoatHeading(double raceSecondsPassed){
        Double defaultTurnAngle = TURN_SPEED * raceSecondsPassed;
        Double upDiff = (targetHeading - heading + 360) % 360;
        Double downDiff = (heading - targetHeading + 360) % 360;
        if(upDiff < downDiff){
            Double angleOfRotation = Math.min(defaultTurnAngle, upDiff);
            heading = (heading + angleOfRotation) % 360;
        } else {
            Double angleOfRotation = Math.min(defaultTurnAngle, downDiff);
            heading = (heading - angleOfRotation + 360) % 360;
        }
    }

    /**
     * Updates the location of a given boat to be displayed to the clients
     * @param raceSecondsPassed time passed since last update in seconds
     * @param course the course the boat is racing on
     */
    @Override
    public void move(Double raceSecondsPassed, Course course) {
        updateTargetHeading();
        updateBoatHeading(raceSecondsPassed);
        avoidFutureCollision();
        updateLocation(raceSecondsPassed, course);
        checkRounding();
    }
}
