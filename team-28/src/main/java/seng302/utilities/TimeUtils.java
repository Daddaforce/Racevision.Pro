package seng302.utilities;

import seng302.models.Coordinate;

import java.time.*;
import java.util.DoubleSummaryStatistics;
import java.util.TimeZone;

/**
 * A utility class for converting between various time units to aid in clean and readable code in other places.
 */
public class TimeUtils {

    private static final double NANOSECONDS_IN_SECOND = 1e9f;
    private static final double MILLISECONDS_IN_SECOND = 1e3f;
    private static final double SECONDS_IN_MINUTE = 60;
    private static final double MINUTES_IN_HOUR = 60;
    private static final int CONVERSION_RATE_NAUTICALM_TO_METRES = 1852;

    private static boolean incorrectTimeZone = true;
    private static String foundId = new String();

    public static double convertSecondsToHours(double seconds){
        return seconds / (SECONDS_IN_MINUTE * MINUTES_IN_HOUR);
    }


    /**
     * Takes the UTC offset given by the regatta xml file and returns the local time of the race
     * @param UTCOffset The UTC Offset from the Data Stream
     * @return String containing the correct time for the given time zone
     */
    public static String setTimeZone(double UTCOffset, long epochMs) {
        String utcFormat = "";
        try {
            utcFormat = formatUTCOffset(UTCOffset);
            if(utcFormat.equals("")) {
                throw new Exception("Incorrect TimeZone in XML file. TimeZone reset to default.");
            }
        } catch (Exception e){
            utcFormat = "+00:00";
            System.out.print(e.getMessage());
        } finally {
            Instant instant = Instant.ofEpochMilli(epochMs);
            ZoneId zone = ZoneId.of(utcFormat);
            ZonedDateTime zonedDateTime = instant.atZone(zone);
            int hours = zonedDateTime.getHour();
            int minutes = zonedDateTime.getMinute();
            int seconds = zonedDateTime.getSecond();
            return String.format("%02d:%02d:%02d UTC%s", hours, minutes, seconds, utcFormat);
        }
    }

    /**
     * Takes the UTC offset and returns it in acceptable ZoneId format
     * @param UTCOffset
     * @return utcFormat - String of the formatted UTC offset
     */
    private static String formatUTCOffset(double UTCOffset) {
        String utcFormat = "";
        String positiveRounded = String.format("%02d", (int)UTCOffset);
        String negativeRounded = String.format("%03d", (int)UTCOffset);
        if ((UTCOffset >= 0) && (UTCOffset < 10)) {
            if ((UTCOffset % 1) == 0) {
                utcFormat = "+" + positiveRounded + ":00";
            } else {
                utcFormat = "+" + positiveRounded + ":30";
            }
        } else if ((UTCOffset > 10) && (UTCOffset <= 14)) {
            if ((UTCOffset % 1) == 0) {
                utcFormat = "+" + positiveRounded + ":00";
            } else {
                utcFormat = "+" + positiveRounded + ":30";
            }
        } else if ((UTCOffset < 0) && (UTCOffset > -10)) {
            if ((UTCOffset % 1) == 0) {
                utcFormat = negativeRounded + ":00";
            } else {
                utcFormat = negativeRounded + ":30";
            }
        } else if ((UTCOffset <= -10) && (UTCOffset >= -12)) {
            if ((UTCOffset % 1) == 0) {
                utcFormat = negativeRounded + ":00";
            } else {
                utcFormat = negativeRounded + ":30";
            }
        }
        return utcFormat;
    }


    /**
     * Wrapper for calcDistance to allow it to take two coordinates instead of 4 doubles
     * @param point1 coordinate of the first point
     * @param point2 coordinate of the second point
     * @return a distance double in nautical
     */
    public static double calcDistance(Coordinate point1, Coordinate point2){
        double lat1 = point1.getLat();
        double lat2 = point2.getLat();
        double lon1 = point1.getLon();
        double lon2 = point2.getLon();
        return calcDistance(lat1, lat2, lon1, lon2);
    }


    /**
     * taken from sprint 2 work, however the original code is http://www.geodatasource.com/developers/java
     * @param lat1 latitude of the boat
     * @param lat2 latitude of the mark
     * @param lon1 longitude of the boat
     * @param lon2 longitude of the mark
     * @return a distance double in nautical
     */
    public static double calcDistance(double lat1, double lat2, double lon1, double lon2){
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist * 0.8684);
    }

    private static double deg2rad(double deg) {return (deg * Math.PI / 180.0); }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    public static double convertNanosecondsToSeconds(double nanoseconds){
        return nanoseconds / NANOSECONDS_IN_SECOND;
    }

    public static double convertHoursToSeconds(double hours){
        return hours * (SECONDS_IN_MINUTE * MINUTES_IN_HOUR);
    }

    public static double convertMinutesToSeconds(double minutes){
        return minutes * SECONDS_IN_MINUTE;
    }

    public static Double convertMmPerSecondToKnots(long mmPerSecond){
        Double kilometersInNauticalMile = (double) CONVERSION_RATE_NAUTICALM_TO_METRES / 1000;
        Double kilometersPerSecond = mmPerSecond / 1e6;
        Double kilometersPerHour = kilometersPerSecond * SECONDS_IN_MINUTE * MINUTES_IN_HOUR;
        Double knots = kilometersPerHour / kilometersInNauticalMile;
        return knots;
    }

    public static long convertKnotsToMmPerSecond(double knots){
        Double kilometersInNauticalMile = (double) CONVERSION_RATE_NAUTICALM_TO_METRES / 1000;
        double converted = knots * kilometersInNauticalMile * 1e6;
        converted = converted / (SECONDS_IN_MINUTE * MINUTES_IN_HOUR);
        return (long) converted;
    }

    public static String getFormatUTCOffset(double UTCOffset) {
        return formatUTCOffset(UTCOffset);
    }

    public static double convertNauticalMilesToMetres(double nautMiles){
        return nautMiles * CONVERSION_RATE_NAUTICALM_TO_METRES;
    }

    public static double secondsToMilliseconds(double seconds) {
        return seconds * MILLISECONDS_IN_SECOND;
    }
}
