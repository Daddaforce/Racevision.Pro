package seng302.models;

/**
 * Defines a line with equation ax + by + c = 0
 * (Equation used instead of y = mx + c to avoid dealing with infinite m when line is vertical)
 */
public class InfiniteLine {
    private static final double EPS = 1e-9;
    private double a, b, c;

    private InfiniteLine(double a, double b, double c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    /**
     * Creates an infinite line that crosses through the two given points
     * @param point1 The first point which defines the line
     * @param point2 The second point which defines the line
     */
    public InfiniteLine(Coordinate point1, Coordinate point2){
        if(Math.abs(point1.getLon() - point2.getLon()) < EPS){
            a = 1.0;
            b = 0.0;
            c = -point1.getLon();
        } else{
            a = -(point1.getLat() - point2.getLat()) / (point1.getLon() - point2.getLon());
            b = 1.0;
            c = -(a * point1.getLon()) - point1.getLat();
        }
    }

    /**
     * Finds the coordinate on this line that is closest to the given point
     * @param point The point to find the closest point for
     * @return The closest point to the given point
     */
    public Coordinate closestPoint(Coordinate point){
        Coordinate closest;
        if(this.isVertical()){
            closest = new Coordinate(point.getLat(), -c);
        } else if(this.isHorizontal()){
            closest = new Coordinate(-c, point.getLon());
        } else{
            InfiniteLine perpendicular = pointSlopeToLine(point, 1 / a);
            closest = InfiniteLine.intersectionPoint(this, perpendicular);
        }
        return closest;
    }

    /**
     * Finds the intersection point between two lines. Assumes the two lines are not the same or parallel
     * @param infiniteLine1 The first line
     * @param infiniteLine2 The second line
     * @return The point that lies on both line 1 and line 2
     */
    public static Coordinate intersectionPoint(InfiniteLine infiniteLine1, InfiniteLine infiniteLine2) {
        double lon = (infiniteLine1.getB() * infiniteLine2.getC() - infiniteLine2.getB() * infiniteLine1.getC()) /
                (infiniteLine1.getA() * infiniteLine2.getB() - infiniteLine2.getA() * infiniteLine1.getB());
        double lat;
        if(!infiniteLine1.isVertical()){
            lat = -(infiniteLine1.getA() * lon + infiniteLine1.getC());
        } else{
            lat = -(infiniteLine2.getA() * lon + infiniteLine2.getC());
        }
        return new Coordinate(lat, lon);
    }

    /**
     * Given a point and slope, finds the line with the given slope that crosses through the given point
     * @param point The point the line will pass through
     * @param slope The slope of the line
     * @return The line created by the given values of point and slope
     */
    public static InfiniteLine pointSlopeToLine(Coordinate point, double slope) {
        double a = -slope;
        double b = 1;
        double c = -((a * point.getLon()) + (b * point.getLat()));
        return new InfiniteLine(a, b ,c);
    }

    public boolean isVertical() {
        return Math.abs(b) < EPS;
    }

    public boolean isHorizontal() {
        return Math.abs(a) < EPS;
    }

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }

    public double getC() {
        return c;
    }
}
