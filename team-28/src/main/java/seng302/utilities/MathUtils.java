package seng302.utilities;

import seng302.models.*;
import seng302.models.CompoundMark;
import seng302.models.Coordinate;

import java.util.Random;

import static java.lang.StrictMath.abs;

/**
 * Created by gla42 on 11/05/17.
 *
 */
public class MathUtils {

    /**
     * A function to determine whether a given direction is within an angle of a target direction
     * @param targetDirection the direction to check against
     * @param deltaAngle the maximum allowed variance of directions
     * @param actualDirection the direction to test
     * @return true if he boat is within angle
     */
    public static boolean pointBetweenTwoAngle(double targetDirection, double deltaAngle, double actualDirection){
        double diff;
        double middle;
        if(Math.abs(targetDirection - actualDirection) <= deltaAngle){
            return true;
        }
        if(targetDirection + deltaAngle < 350 && targetDirection - deltaAngle > 0) {
            return actualDirection >= targetDirection - deltaAngle && actualDirection <= targetDirection + deltaAngle;
        }
        if(targetDirection > 180){
            actualDirection -= 180;
            middle = targetDirection - 180;
        } else {
            middle = 90;
            diff = Math.abs(90 - targetDirection);
            actualDirection += diff;
            actualDirection = (actualDirection + 360) % 360;}
        return (middle - deltaAngle) <= actualDirection && actualDirection <= (middle + deltaAngle);
    }

    /**
     * A function for interpolating three values, only does three as the math is easier and this provides an accurate enough
     * interpolation for its current uses
     * @param A first pair
     * @param B second pair
     * @param C third pair
     * @param x the value to interpolate on
     * @return the interpolated value using x as input
     */
    public static double lagrangeInterpolation(WindAngleAndSpeed A, WindAngleAndSpeed B, WindAngleAndSpeed C, double x) {
        return (((x - B.getWindAngle()) * (x - C.getWindAngle())) / ((A.getWindAngle() - B.getWindAngle()) * (A.getWindAngle() - C.getWindAngle()))) * A.getSpeed() + (((x - A.getWindAngle()) * (x - C.getWindAngle())) / ((B.getWindAngle() - A.getWindAngle()) * (B.getWindAngle() - C.getWindAngle()))) * B.getSpeed() + (((x - A.getWindAngle()) * (x - B.getWindAngle())) / ((C.getWindAngle() - A.getWindAngle()) * (C.getWindAngle() - B.getWindAngle()))) * C.getSpeed();}

    /**
     * Calculates the VMG of the boat
     * @param boatSpeed the speed of the boat
     * @param TWA the true wind angle that the boat is heading at
     * @return the velocity made good of the boat
     */
    public static double VMG(double boatSpeed, double TWA){
        return boatSpeed * Math.cos(Math.toRadians(TWA));
    }

    /**
     * Function to determine if a boat is on the correct side of the start line (e.g if the boat is on the side that the course isn't on)
     * @param boatPos the position of the boat
     * @param startLine the position of the startline
     * @param mark1 the position of the first mark
     * @return true if the boat is on the correct side of the start line
     */
    public static Boolean boatBeforeStartline(Coordinate boatPos, CompoundMark startLine, CompoundMark mark1) {
        double determinantOfMark = (mark1.getMark1().getPosition().getLon() -
                startLine.getMark1().getPosition().getLon()) * (startLine.getMark2().getPosition().getLat() -
                startLine.getMark1().getPosition().getLat()) - (mark1.getMark1().getPosition().getLat() -
                startLine.getMark1().getPosition().getLat()) * (startLine.getMark2().getPosition().getLon() -
                startLine.getMark1().getPosition().getLon());
        double determinantOfBoat = (boatPos.getLon() - startLine.getMark1().getPosition().getLon()) *
                (startLine.getMark2().getPosition().getLat() - startLine.getMark1().getPosition().getLat()) -
                (boatPos.getLat() - startLine.getMark1().getPosition().getLat()) *
                        (startLine.getMark2().getPosition().getLon() - startLine.getMark1().getPosition().getLon());
        return determinantOfBoat > 0 && determinantOfMark < 0 || determinantOfBoat < 0 && determinantOfMark > 0;
    }

    /**
     * Calculates the midpoint between two marks
     * @param mark first mark
     * @return new coordinate at half way between the two marks
     */
    public static Coordinate calculateMidPoint(CompoundMark mark){
        Coordinate mark1Coord = mark.getMark1().getPosition();
        Coordinate mark2Coord = mark.getMark2().getPosition();
        Double halfLat = (mark1Coord.getLat() + mark2Coord.getLat()) / 2;
        Double halfLong = (mark1Coord.getLon() + mark2Coord.getLon()) / 2;
        return new Coordinate(halfLat,halfLong);
    }

    /**
     * Function to determine if the boat which is on the correct side of the sideline is heading towards it
     * @param boatsHeading heading of boat
     * @param lineHeading the heading of the line, if the boat is heading towards it the boat will essentially be heading on either side of the perpendicular line ot it
     * @param headingOfMark the heading from the mark to the startline
     * @return true if the boat is heading in the general direction of the line
     */
    public static Boolean boatHeadingToLine(double boatsHeading, double lineHeading, double headingOfMark){
        Boolean towardsLine = false;
        if(pointBetweenTwoAngle((lineHeading + 270)%360, 90, boatsHeading) &&
                pointBetweenTwoAngle(lineHeading + 90, 90, headingOfMark)){
            towardsLine = true;
        } else if(pointBetweenTwoAngle(lineHeading + 90, 90, boatsHeading) &&
                pointBetweenTwoAngle((lineHeading + 270)%360, 90, headingOfMark)){
            towardsLine = true;
        }
        return towardsLine;
    }


    /**
     * Computes the bearing in degrees between two given marks (mark1 and mark2).
     * Note that mark1 and mark2 are compound marks, so in the case of a gate it
     * calculates form the midpoint of the two marks.
     * Note that bearing is calculated starting from north direction from mark1
     *  and clockwise around to mark2
     * @param mark1 the first mark that will be used as the first point
     * @param mark2 the second mark that will be used as the last point
     * @return heading from the mark1 to mark2
     */
    public static double calculateBearingBetweenTwoPoints(CompoundMark mark1, CompoundMark mark2){
        double mark1lat = mark1.getMark1().getPosition().getLat();
        double mark1lng = mark1.getMark1().getPosition().getLon();
        double mark2lat = mark2.getMark1().getPosition().getLat();
        double mark2lng = mark2.getMark1().getPosition().getLon();

        boolean mark1isCompound = mark1.hasTwoMarks();
        boolean mark2isCompound = mark2.hasTwoMarks();

        if(mark1isCompound){
            mark1lat = calculateMidPoint(mark1).getLat();
            mark1lng = calculateMidPoint(mark1).getLon();
        }
        if(mark2isCompound){
            mark2lat = calculateMidPoint(mark2).getLat();
            mark2lng = calculateMidPoint(mark2).getLon();
        }

        double y = Math.sin(mark2lng-mark1lng) * Math.cos(mark2lat);
        double x = Math.cos(mark1lat)*Math.sin(mark2lat) - Math.sin(mark1lat)*Math.cos(mark2lat)*Math.cos(mark2lng-mark1lng);
        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (bearing + 360) % 360;
    }


    /**
     * A function for calculating the bilinear interpolation of a third variable (e.g if windspeed and heading are
     * known this function will calculate the boat speed at those values
     * @param x0 first value in x axis
     * @param x1 second value in x axis
     * @param y0 first value in y axis
     * @param y1 second value in y axis
     * @param z00 the z value at (x0,y0)
     * @param z01 the z value at (x0,y1)
     * @param z10 the z value at (x1,y0)
     * @param z11 the z value at (x1,y1)
     * @param x the x value you want to compute z at (e.g the current windspeed)
     * @param y the y value you want to compute z at (e.g the current boat heading)
     * @return the interpolated z value (e.g the new boat speed at current heading and wind speed)
     */
    public static double bilinearInterpolation(double x0, double x1, double y0, double y1, double z00, double z01, double z10, double z11, double x, double y){

        double r1 = (((x1 - x)/(x1 - x0)) * z00 + ((x - x0)/(x1 - x0)) * z10);
        double r2 = (((x1 - x)/(x1 - x0)) * z01 + ((x - x0)/(x1 - x0)) * z11);

        return (((y1 - y)/(y1 - y0)) * r1 + ((y - y0)/ (y1 - y0)) * r2);
    }

    /**
     * Computes whether the boat is heading towards the startline or not
     * @param course the course the boat is on
     * @param boat the boat which we are checking
     * @return true if the boat is heading towards the startline, false otherwise
     */
    public static boolean boatHeadingToStart(Course course, Boat boat){
        Coordinate startLine1 = course.getStartLine().getMark1().getPosition();
        Coordinate startLine2 = course.getStartLine().getMark2().getPosition();
        Coordinate mark = course.getCourseOrder().get(1).getPosition();
        double boatsHeading = boat.getHeading();
        double headingOfStartLine = startLine1.headingToCoordinate(startLine2);
        double headingOfMark = mark.headingToCoordinate(startLine1);
        return MathUtils.boatHeadingToLine(boatsHeading, headingOfStartLine, headingOfMark);
    }

    /**
     * Computes if the boat is on the starting side of the course
     * @param course the course the boat is on
     * @param boat the boat we want to check
     * @return true if the boat is on the opposite of course side, false otherwise
     */
    public static boolean boatOnStartSide(Course course, Boat boat){
        Coordinate position = boat.getCurrentPosition();
        return MathUtils.boatBeforeStartline(position,course.getStartLine(),course.getCourseOrder().get(1));
    }

    /**
     * Calculates the distance a boat is form the startline
     * @param course the course the boat is on
     * @param boat the boat
     * @return the distance in nautical miles that the boat is away from the startline
     */
    public static double distanceToStartLine(Course course, Boat boat){
        Coordinate position = boat.getCurrentPosition();
        Coordinate startLine1 = course.getStartLine().getMark1().getPosition();
        Coordinate startLine2 = course.getStartLine().getMark2().getPosition();

        return distanceToLineSegment(startLine1, startLine2, position);
    }

    /**
     * Returns the shortest distance from a line segment starting from a position
     * @param lineStart The starting coordinate of the line segment
     * @param lineEnd The ending coordinate of the line segment
     * @param position The position to find the distance from
     * @return The shortest distance from the line segment to the position
     */
    public static double distanceToLineSegment(Coordinate lineStart, Coordinate lineEnd, Coordinate position){
        InfiniteLine startlineInf = new InfiniteLine(lineStart, lineEnd);
        Coordinate closestPoint = startlineInf.closestPoint(position);
        double distanceToStart;
        if(closestPoint.getLat() < Math.min(lineStart.getLat(),lineEnd.getLat()) || closestPoint.getLat() > Math.max(lineStart.getLat(),lineEnd.getLat())){
            double distanceToStartLine1 = position.greaterCircleDistance(lineStart);
            double distanceToStartLine2 = position.greaterCircleDistance(lineEnd);
            distanceToStart = Math.min(distanceToStartLine1,distanceToStartLine2);
        } else {
            distanceToStart = position.greaterCircleDistance(closestPoint);
        }
        return distanceToStart;
    }

    /**
     * Calculates smallest angle between to headings (acute angle)
     * @param headingA
     * @param headingB
     * @return angle
     */
    public static double getAngleBetweenTwoHeadings(double headingA, double headingB) {
        double angle = abs(headingA - headingB);
        if(angle > 180) {
            angle = 360 - angle;
        }
        return angle;
    }

    /**
     * Calculates the heading of the line starting at start Coordinate.
     * @param startCoordinate point to start
     * @param endCoordinate point to Coordinate
     * @return heading between points
     */
    public static double getHeadingBetweenTwoCoordinates(CanvasCoordinate startCoordinate, CanvasCoordinate endCoordinate){
        double bearing = Math.toDegrees(Math.atan2(startCoordinate.getY() - endCoordinate.getY(), startCoordinate.getX() - endCoordinate.getX()));
        if (bearing < 0){
            bearing += 360;
        }
        return (bearing + 270) % 360;
    }

    /**
     * Returns distance between two points on a canvas (pixels)
     * @param point1
     * @param point2
     * @return distance
     */
    public static double distanceBetweenTwoPoints(CanvasCoordinate point1, CanvasCoordinate point2) {
        double distance = Math.sqrt(Math.pow(point1.getX()-point2.getX(), 2)+Math.pow(point1.getY()-point2.getY(), 2));
        return distance;
    }


    /**
     * Generates a random 4 digit code
     * @return 4 digit code between [0000, 9999]
     */
    public static Integer generateFourDigitPartyCode() {
        Random rand = new Random();
        return rand.nextInt(10000);
    }
}
