package seng302.utilities;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import seng302.controllers.GameServer;
import seng302.data.RoundingSide;
import seng302.models.CompoundMark;
import seng302.models.Coordinate;
import seng302.models.Mark;
import seng302.models.WindAngleAndSpeed;
import seng302.models.*;

import static junit.framework.TestCase.assertEquals;
import static seng302.utilities.MathUtils.*;

/**
 * Created by gla42 on 11/05/17.
 *
 */
public class MathUtilsTest {

    private double DELTA = 1e-6;
    private static Course defaultCourse;

    @BeforeClass
    public static void beforeClass() {
        //Initialise a default course for testing.
        defaultCourse = new Course();

        Mark startLine1 = new Mark(0, "Start Line 1", new Coordinate(32.296577, -64.854304));
        Mark startLine2 = new Mark(1, "Start Line 2", new Coordinate(32.293771, -64.855242));
        RaceLine start = new RaceLine(1, "Start Line", startLine1, startLine2);

        Mark mark1 = new Mark(2, "Mark 1", new Coordinate(32.293039, -64.843983));
        CompoundMark mark = new CompoundMark(2, "Mark", mark1);

        defaultCourse.setStartLine(start);

        defaultCourse.addNewCompoundMark(start);
        defaultCourse.addNewCompoundMark(mark);
        defaultCourse.addMarkInOrder(1, RoundingSide.PORT_STBD);
        defaultCourse.addMarkInOrder(2, RoundingSide.PORT_STBD);
    }

    @Test
    public void pointBetweenTwoAngleTest(){
        //if TWD is 0, then if a boat bearing is between 270 and 90 it should be true
        Assert.assertTrue(pointBetweenTwoAngle(0,90,271));
        Assert.assertTrue(pointBetweenTwoAngle(0,90,0));
        Assert.assertTrue(pointBetweenTwoAngle(0,90,89));
        Assert.assertFalse(pointBetweenTwoAngle(0,90,269));
        Assert.assertFalse(pointBetweenTwoAngle(0,90,180));
        Assert.assertFalse(pointBetweenTwoAngle(0,90,91));
        //if TWD is 20, then if a boat bearing is between 290 and 110 it should be true
        Assert.assertTrue(pointBetweenTwoAngle(20,90,291));
        Assert.assertTrue(pointBetweenTwoAngle(20,90,10));
        Assert.assertTrue(pointBetweenTwoAngle(20,90,109));
        Assert.assertFalse(pointBetweenTwoAngle(20,90,289));
        Assert.assertFalse(pointBetweenTwoAngle(20,90,180));
        Assert.assertFalse(pointBetweenTwoAngle(20,90,111));
        //if TWD is 0 and the boat bearing is 341 and TWA is 20
        Assert.assertTrue(pointBetweenTwoAngle(0,20,341));
        Assert.assertTrue(pointBetweenTwoAngle(0,20,0));
        Assert.assertTrue(pointBetweenTwoAngle(0,20,19));
        Assert.assertFalse(pointBetweenTwoAngle(0,20,21));
        Assert.assertFalse(pointBetweenTwoAngle(0,20,339));
        Assert.assertFalse(pointBetweenTwoAngle(0,20,180));
        //if TWD is 280 and boat bearing is 271 and TWA is 30
        Assert.assertTrue(pointBetweenTwoAngle(280,30,271));
        Assert.assertTrue(pointBetweenTwoAngle(280,30,300));
        Assert.assertFalse(pointBetweenTwoAngle(280, 30, 0));
        Assert.assertFalse(pointBetweenTwoAngle(280, 30, 249));

        Assert.assertFalse(pointBetweenTwoAngle((131+270)%360,90,165));
        Assert.assertFalse(pointBetweenTwoAngle(131+90,90,27));

        Assert.assertTrue(pointBetweenTwoAngle(131+90,90,165));
        Assert.assertTrue(pointBetweenTwoAngle((131+270)%360,90,27));

        Assert.assertTrue(pointBetweenTwoAngle(169,90,174));

    }

        @Test
    public void VMGTest(){
        assertEquals(6.427876097, VMG(10,50), DELTA);
        assertEquals(10, VMG(10,0), DELTA);
        assertEquals(9.998476952, VMG(10,1), DELTA);
    }

    @Test
    public void LagrangeInterpolationTest(){
        WindAngleAndSpeed A = new WindAngleAndSpeed(44.7,8.843719);
        WindAngleAndSpeed B = new WindAngleAndSpeed(80.9,2.982861);
        WindAngleAndSpeed C = new WindAngleAndSpeed(101.9,-4.66228);
        WindAngleAndSpeed D = new WindAngleAndSpeed(1.0,2.0);
        WindAngleAndSpeed E = new WindAngleAndSpeed(2.0,3.0);
        WindAngleAndSpeed F = new WindAngleAndSpeed(5.0,2.0);
        double x = 50.0;
        assertEquals(8.564421885974385, lagrangeInterpolation(A,B,C,x), DELTA);
        assertEquals(3, lagrangeInterpolation(D,E,F,4.0), DELTA);
    }

    @Test
    public void calculateMidPointTest(){
        Coordinate mark1Coord = new Coordinate(32, 64);
        Coordinate mark2Coord = new Coordinate(35, 68);
        Mark mark1 = new Mark(0, "Test",mark1Coord);
        Mark mark2 = new Mark(0, "Test",mark2Coord);
        CompoundMark cm1 = new CompoundMark(0, "Test",mark1,mark2);
        Coordinate testAnswer = new Coordinate(33.5, 66);
        assertEquals(testAnswer, calculateMidPoint(cm1));
    }

    @Test
    public void boatBeforeStartlineTest(){
        Coordinate boat = new Coordinate(32.2937, -64.855242);
        Coordinate boat1 = new Coordinate(32.293039, -64.845045);
        Coordinate boat2 = new Coordinate(32.296577, -64.854304);
        CompoundMark startline = new CompoundMark(1, "", new Mark(1, "", new Coordinate(32.296577, -64.854304)), new Mark(1, "", new Coordinate(-32.293771, -64.855242)));
        CompoundMark mark = new CompoundMark(1, "", new Mark(1, "", new Coordinate(32.293039, -64.845045)));
        CompoundMark mark1 = new CompoundMark(1, "", new Mark(1, "", new Coordinate(32.2937, -64.855242)));
        Assert.assertTrue(boatBeforeStartline(boat, startline,mark));
        Assert.assertTrue(boatBeforeStartline(boat1,startline,mark1));
        Assert.assertFalse(boatBeforeStartline(boat2,startline,mark1));
    }

    @Test
    public void boatHeadingToLineTest(){
        Assert.assertTrue(boatHeadingToLine(90,0,270));
        Assert.assertFalse(boatHeadingToLine(90,0,85));
        Assert.assertTrue(boatHeadingToLine(165,131,27));

    }

    @Test
    public void boatHeadingToStartTest(){

        Boat boat = new Boat(0, "TestBoat", "testNickname", 10);
        boat.setCurrentSpeed(10);
        boat.setPosition(new Coordinate(32.2937, -64.855242));

        boat.setHeading(90);
        Assert.assertTrue(boatHeadingToStart(defaultCourse, boat));

        boat.setHeading(270);
        Assert.assertFalse(boatHeadingToStart(defaultCourse, boat));
    }

    @Test
    public void boatOnStartSideTest(){
        Boat boat = new Boat(0, "TestBoat", "testNickname", 10);
        boat.setCurrentSpeed(10);

        boat.setPosition(new Coordinate(32.2967, -64.855242));
        Assert.assertTrue(boatOnStartSide(defaultCourse, boat));

        boat.setPosition(new Coordinate(32.2937, -64.855242));
        Assert.assertFalse(boatOnStartSide(defaultCourse, boat));
    }

    @Test
    public void distanceToStartLineTest() {
        Boat boat = new Boat(0, "TestBoat", "testNickname", 10);
        boat.setCurrentSpeed(10);

        boat.setPosition(new Coordinate(32.2967, -64.855242));

        Assert.assertEquals(0.048033120, distanceToStartLine(defaultCourse, boat), DELTA);

    }

    @Test
    public void generateFourDigitPartyCodeTest() {
        Integer code = generateFourDigitPartyCode();
        Assert.assertTrue(code >= 0 && code <= 9999);
    }
}
