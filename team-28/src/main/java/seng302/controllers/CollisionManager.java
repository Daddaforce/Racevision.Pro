package seng302.controllers;

import javafx.scene.shape.Polygon;
import seng302.data.BoatStatus;
import seng302.data.RaceStatus;
import seng302.data.RaceVisionXMLParser;
import seng302.models.*;
import seng302.utilities.MathUtils;
import seng302.utilities.TimeUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by mjt169 on 25/07/17.
 * Class to check for and manage collisions
 */
public class CollisionManager {

    private static final Double BOAT_SENSITIVITY = 0.045;
    public static final Double MARK_SENSITIVITY = 0.03;
    private static final Double AT_FAULT_DELTA = 30.0;
    private static final Double COLLISION_DELTA = 60.0;
    private static final int SPAWN_IMMUNITY_SECONDS = 30;
    private Penalties penalties = new Penalties();
    private Polygon boundary = null;

    private Set<Collision> currentCollisions = new CopyOnWriteArraySet<>();

    public CollisionManager() {}


    /**
     * creates a polygon of the course boundary marks
     * @param boundaryCoordinates the coordinates of the boundary
     * @return the polygon
     */
    public Polygon createCourseBoundary(List<Coordinate> boundaryCoordinates){
        Polygon boundary = new Polygon();
        for(Coordinate coord : boundaryCoordinates){
            boundary.getPoints().add(coord.getLat());
            boundary.getPoints().add(coord.getLon());
        }
        return boundary;
    }

    /**
     * Checks all boats in the race to see if they are colliding with each other or course marks
     * Adds any collisions to a set.
     * @param race current race
     */
    public void checkForCollisions(Race race){
        if(boundary == null) boundary = createCourseBoundary(race.getCourse().getBoundary());
        boolean isPractice = RaceVisionXMLParser.courseFile.equals("PracticeStart-course.xml");
        boolean isTutorial = RaceVisionXMLParser.courseFile.equals("GuidedPractice-course.xml");
        List<Boat> boats = new ArrayList<>();
        boats.addAll(race.getCompetitors());
        for (int i = 0; i < boats.size(); i++) {
            Boat boat = boats.get(i);
            if (race.getCurrentTimeInEpochMs() - boat.getSpawnTime() < TimeUtils.secondsToMilliseconds(SPAWN_IMMUNITY_SECONDS)) {
                continue;
            }
            if(boat.getStatus() != BoatStatus.DNF && !boat.isFinished() && boat.getStatus() != BoatStatus.DISQUALIFIED) {
                for (int j = i + 1; j < boats.size(); j++) {
                    Boat otherBoat = boats.get(j);
                    if (otherBoat.getStatus() != BoatStatus.DNF && !otherBoat.isFinished()) {
                        checkForCollisionBetweenBoats(boat, otherBoat);
                    }
                }
            }
            if(!isPractice && !isTutorial) checkForOutOfBounds(boat, race.getRaceStatus());
            for (Mark mark : race.getCourse().getAllMarks().values()) {
                if (!isPractice || mark.getSourceID() == 1 || mark.getSourceID() == 2) {
                    checkForCollisionBetweenBoatAndMark(boat, mark);
                }
            }
        }
    }

    /**
     * Check whether a boat is colliding with a mark and create a Collision if necessary
     * @param boat the boat to check
     * @param mark the mark to check
     */
    private void checkForCollisionBetweenBoatAndMark(Boat boat, Mark mark) {
        if (collisionOfBounds(boat.getCurrentPosition(), mark.getPosition(), MARK_SENSITIVITY)) {
            if (boatHeadingTowardsCoordinate(boat, mark.getPosition(), COLLISION_DELTA)) {
                Collision collision = new Collision();
                collision.addBoat(boat.getId());
                currentCollisions.add(collision);
                penalties.markCollision(boat);
            }
        }
    }

    /**
     * determines whether boat is out of bounds and creates collision and penalties if so
     * @param boat the boat to check
     * @param raceStatus the status of the race
     */
    private void checkForOutOfBounds(Boat boat, RaceStatus raceStatus){
        if  (raceStatus.equals(RaceStatus.STARTED) && !boat.isFinished() && !boundary.contains(boat.getCurrentLat(), boat.getCurrentLon())){
            Collision collision = new Collision();
            collision.setOutOfBounds(true);
            collision.addBoat(boat.getId());
            penalties.boatOutOfBounds(boat);
            currentCollisions.add(collision);
        }
    }

    /**
     * Check whether two boats are colliding and create a Collision if necessary
     * @param boat1 the first boat to check
     * @param boat2 the second boat to check
     */
    private void checkForCollisionBetweenBoats(Boat boat1, Boat boat2) {
        if (collisionOfBounds(boat1.getCurrentPosition(), boat2.getCurrentPosition(), BOAT_SENSITIVITY)) {
            Collision collision = new Collision();
            boolean hasCollided = false;
            if (boatHeadingTowardsCoordinate(boat1, boat2.getCurrentPosition(), AT_FAULT_DELTA)) {
                collision.addAtFaultBoat(boat1.getId());
                penalties.boatCollision(boat1,boat2);
                hasCollided = true;
            }
            if (boatHeadingTowardsCoordinate(boat2, boat1.getCurrentPosition(), AT_FAULT_DELTA)) {
                collision.addAtFaultBoat(boat2.getId());
                penalties.boatCollision(boat2,boat1);
                hasCollided = true;
            }
            if(hasCollided) {
                collision.addBoat(boat1.getId());
                collision.addBoat(boat2.getId());
            }
            currentCollisions.add(collision);
        }
    }

    /**
     * Determines whether boat1 is heading in a direction approximately towards boat2
     * @param boat1 the boat whose heading we are checking
     * @param coordinate the coordinate to check if we boat1 is heading towards
     */
    private boolean boatHeadingTowardsCoordinate(Boat boat1, Coordinate coordinate, Double delta) {
        double boat1To2Heading = boat1.getCurrentPosition().headingToCoordinate(coordinate);
        return MathUtils.pointBetweenTwoAngle(boat1To2Heading, delta, boat1.getHeading());
    }

    /**
     * takes two circles and calculates if there is a collision
     * @param object1LatLon Coordinate of the first object
     * @param object2LatLon Coordinate of the second object
     * @return boolean of collision
     */
    private boolean collisionOfBounds(Coordinate object1LatLon, Coordinate object2LatLon, double sensitivity){
        double distance = object1LatLon.greaterCircleDistance(object2LatLon);
        return distance < sensitivity;
    }

    /**
     * Checks currently stored collisions to see if any of them contain the specified boat
     * @param boat the boat to check for
     * @return true if the boat is currently in a collision, false otherwise
     */
    public boolean boatIsInCollision(Boat boat) {
        for (Collision collision : currentCollisions) {
            if (!collision.isOutOfBounds() && collision.boatIsInCollision(boat.getId())) {
                return true;
            }
        }
        return false;
    }

    public Set<Collision> getCollisions() {
        return Collections.unmodifiableSet(currentCollisions);
    }

    public void removeCollision(Collision collision) {
        currentCollisions.remove(collision);
    }
}
