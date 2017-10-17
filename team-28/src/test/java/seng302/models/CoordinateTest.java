package seng302.models;

import org.junit.Test;
import seng302.models.Coordinate;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the Coordinate class
 * Created on 30/03/17.
 */
public class CoordinateTest {

    @Test
    public void gcDistanceTest(){
        Coordinate coord1 = new Coordinate(50, 30);
        Coordinate coord2 = new Coordinate(60, 60);
        assertEquals(1179, (int)Math.round(coord1.greaterCircleDistance(coord2)));
    }

    @Test
    public void headingToCoordinateTest(){
        Coordinate coord1 = new Coordinate(50, 45);
        Coordinate coord2 = new Coordinate(100, 28);
        assertEquals(4, Math.round(coord1.headingToCoordinate(coord2)));

        Coordinate latLong1 = new Coordinate(32.293039, -64.843983);
        Coordinate latlong2 = new Coordinate(32.280164, -64.847591);
        assertEquals(193, Math.round(latLong1.headingToCoordinate(latlong2)));

    }

    @Test
    public void headingToSouthCoordinateTest(){
        Coordinate coord1 = new Coordinate(120, 30);
        Coordinate coord2 = new Coordinate(100, 30);
        assertEquals(180, Math.round(coord1.headingToCoordinate(coord2)));
    }

    @Test
    public void headingToSameCoordinateTest(){
        Coordinate coord1 = new Coordinate(50, 30);
        Coordinate coord2 = new Coordinate(50, 30);
        assertEquals(0, Math.round(coord1.headingToCoordinate(coord2)));
    }

    @Test
    public void coordAtDefaultTest()  {
        Coordinate coord1 = new Coordinate(50, 30);
        Coordinate coord2 = coord1.coordAt(1, 12);
        assertEquals(50.016302, coord2.getLat(), 1e-6);
        assertEquals(30.005393, coord2.getLon(), 1e-6);
    }

    @Test
    public void coordAtTest()  {
        Coordinate coord1 = new Coordinate(50, 30);
        Coordinate coord2 = coord1.coordAt(1, 12, 1);
        assertEquals(70.555264, coord2.getLat(), 1e-6);
        assertEquals(178.295254, coord2.getLon(), 1e-6);
    }

}
