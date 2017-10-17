package seng302.models;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by raych on 19/05/2017.
 */
public class InfiniteLineTest {
    private final double EPS = 1e-9;

    @Test
    public void lineFromTwoCoordTest(){
        Coordinate coord1 = new Coordinate(2, 3);
        Coordinate coord2 = new Coordinate(5, 7);
        InfiniteLine line = new InfiniteLine(coord1, coord2);
        Assert.assertEquals(-0.75, line.getA(), EPS);
        Assert.assertEquals(1, line.getB(), EPS);
        Assert.assertEquals(0.25, line.getC(), EPS);

        InfiniteLine line2 = new InfiniteLine(coord2, coord1); //Should be the same line
        Assert.assertEquals(line.getA(), line2.getA(), EPS);
        Assert.assertEquals(line.getB(), line2.getB(), EPS);
        Assert.assertEquals(line.getC(), line2.getC(), EPS);
    }

    @Test
    public void verticalLineTest(){
        Coordinate coord1 = new Coordinate(2, 3);
        Coordinate coord2 = new Coordinate(5, 3);
        InfiniteLine line = new InfiniteLine(coord1, coord2);
        Assert.assertTrue(line.isVertical());

        Coordinate coord3 = new Coordinate(2, 4);
        InfiniteLine line2= new InfiniteLine(coord1, coord3);
        Assert.assertFalse(line2.isVertical());
    }

    @Test
    public void horizontalLineTest() {
        Coordinate coord1 = new Coordinate(2, 3);
        Coordinate coord2 = new Coordinate(2, 5);
        InfiniteLine line = new InfiniteLine(coord1, coord2);
        Assert.assertTrue(line.isHorizontal());

        Coordinate coord3 = new Coordinate(4, 3);
        InfiniteLine line2 = new InfiniteLine(coord1, coord3);
        Assert.assertFalse(line2.isHorizontal());
    }

    @Test
    public void closestPointTest(){
        Coordinate coord1 = new Coordinate(0, 3);
        Coordinate coord2 = new Coordinate(0, 5);
        InfiniteLine line = new InfiniteLine(coord1, coord2);

        Coordinate point = new Coordinate(18, 1);
        Coordinate closestPoint = line.closestPoint(point);
        Assert.assertEquals(closestPoint.getLat(), 0, EPS);
        Assert.assertEquals(closestPoint.getLon(), 1, EPS);
    }

    @Test
    public void closestPointVerticalTest(){
        Coordinate coord1 = new Coordinate(-19999, 2);
        Coordinate coord2 = new Coordinate(100, 2);
        InfiniteLine line = new InfiniteLine(coord1, coord2);

        Coordinate point = new Coordinate(500, 1237);
        Coordinate closestPoint = line.closestPoint(point);
        Assert.assertEquals(closestPoint.getLat(), 500, EPS);
        Assert.assertEquals(closestPoint.getLon(), 2, EPS);
    }

    @Test
    public void intersectionPointTest(){
        Coordinate coord1 = new Coordinate(-100, 0);
        Coordinate coord2 = new Coordinate(1000, 0);
        InfiniteLine verticalLine = new InfiniteLine(coord1, coord2);

        Coordinate coord3 = new Coordinate(2, 12);
        Coordinate coord4 = new Coordinate(2, 1203);
        InfiniteLine horizontalLine = new InfiniteLine(coord3, coord4);

        Coordinate coord = InfiniteLine.intersectionPoint(verticalLine, horizontalLine);

        Assert.assertEquals(2, coord.getLat(), EPS);
        Assert.assertEquals(0, coord.getLon(), EPS);
    }

    @Test
    public void pointSlopeToLineTest(){
        Coordinate coord = new Coordinate(1, 2);
        double slope = 19;
        InfiniteLine line = InfiniteLine.pointSlopeToLine(coord, slope);
        Assert.assertEquals(-19, line.getA(), EPS);
        Assert.assertEquals(1, line.getB(), EPS);
        Assert.assertEquals(37, line.getC(), EPS);
    }
}
