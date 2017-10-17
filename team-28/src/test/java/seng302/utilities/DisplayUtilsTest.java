package seng302.utilities;

import org.junit.Before;
import org.junit.Test;
import seng302.controllers.Controller;
import seng302.models.CanvasCoordinate;
import seng302.models.Coordinate;
import seng302.utilities.DisplayUtils;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the DisplayUtils class
 * Created on 17/03/2017.
 */

public class DisplayUtilsTest {

    public double minLat, minLon, maxLat, maxLon;

    @Before
    public void before(){
        minLat = (32.285465 - 0.004); //Lat min
        minLon = (-64.855621 - 0.004); //Lon min
        maxLat = (32.316308 + 0.004); //Lat Max
        maxLon = (-64.830509 + 0.004); //Lon Max

        double multiplier = 0.75;
        Controller.setCanvasHeight(1440 * multiplier);
        Controller.setCanvasWidth(2560 * multiplier);
        DisplayUtils.setMaxMinLatLon(minLat, minLon, maxLat, maxLon);
    }

    @Test
    public void convertFromLatLonTest(){
        CanvasCoordinate testPoint = DisplayUtils.convertFromLatLon(32.295783, -64.855621);

        assertEquals(610, (int) testPoint.getX());
        assertEquals(681, (int) testPoint.getY());
    }

    @Test
    public void convertFromCoordinateTest(){
        Coordinate coordinate = new Coordinate(32.295783, -64.855621);
        CanvasCoordinate testPoint = DisplayUtils.convertFromLatLon(coordinate);

        assertEquals(610, (int) testPoint.getX());
        assertEquals(681, (int) testPoint.getY());
    }

    @Test
    public void testMidPoint(){
        Coordinate midPoint = DisplayUtils.midPoint(10.0,10.0,15.0,15.0);
        assertEquals(midPoint.getLat(), 12.511, 0.01);
        assertEquals(midPoint.getLon(), 12.476, 0.01);
    }


}
