package seng302.models;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import seng302.controllers.Controller;
import seng302.utilities.DisplayUtils;
import static org.junit.Assert.*;

/**
 * Created by Louis on 22/05/2017.
 *
 */
public class lineTest {

    private Boat boat1;
    private Boat boat2;
    private CompoundMark compoundMark;
    private DistanceLine distanceLine;

    @Before
    public void setUp() {
        Controller.setCanvasWidth(100.0);
        Controller.setCanvasHeight(100.0);
        boat1 = new Boat(0, "TestBoat", "testNickname", 10);
        boat1.setCurrentSpeed(10);
        boat1.setPosition(10,10);
        boat2 = new Boat(1, "TestBoat", "testNickname", 10);
        boat2.setCurrentSpeed(10);
        boat2.setPosition(20,20);
        boat1.setLeg(1);
        boat2.setLeg(1);
        Mark mark1 = new Mark(4, "Mark 1", new Coordinate(2, 2));
        compoundMark = new CompoundMark(2, "Mark", mark1);
        distanceLine = new DistanceLine();
        distanceLine.setFirstBoat(boat1);
        distanceLine.setSecondBoat(boat2);
        distanceLine.setMark(compoundMark);
        DisplayUtils.setMaxMinLatLon(30, 20, 150, 150);
    }

    @Test
    public void distanceLineTest(){
        assertEquals(false, distanceLine.boatsFinished());
        assertEquals(true, distanceLine.sameLeg());
    }

    @Test
    public void checkFurthermostTest(){
        Coordinate midPoint = DisplayUtils.midPointFromTwoCoords(compoundMark.getMark1().getPosition(), compoundMark.getMark1().getPosition());
        boolean dist2 = distanceLine.findFurtherestDistance(midPoint); // returns dist1 < dist2
        assertEquals(true, dist2);
        CanvasCoordinate halfway = distanceLine.halfwayBetweenBoatsCoord();
        assertEquals(-3.0, halfway.getX(),0.001);
        assertEquals(107.0, halfway.getY(), 0.001);
    }
}
