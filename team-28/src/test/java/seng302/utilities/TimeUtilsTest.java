package seng302.utilities;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import seng302.models.Coordinate;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static org.junit.Assert.*;

/**
 * Created by Michael Trotter on 3/25/2017.
 */
public class TimeUtilsTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void cleanUpStreams() {
        System.setOut(null);
    }

    @Test
    public void convertMinutesToSecondsTest() throws Exception {
        assertEquals(90, TimeUtils.convertMinutesToSeconds(1.5), 0);
    }

    @Test
    public void convertSecondsToHoursTest() throws Exception {
        assertEquals(0.5, TimeUtils.convertSecondsToHours(1800), 0);
    }

    @Test
    public void convertNanosecondsToSecondsTest() throws Exception {
        assertEquals(2, TimeUtils.convertNanosecondsToSeconds(2e9f), 0);
    }

    @Test
    public void convertHoursToSecondsTest() throws Exception {
        assertEquals(5400, TimeUtils.convertHoursToSeconds(1.5), 0);
    }

    @Test
    public void convertMmPerSecondToKnotsTest(){
        assertEquals(0, TimeUtils.convertMmPerSecondToKnots(0), 0);
        assertEquals(233.261339093, TimeUtils.convertMmPerSecondToKnots(120000), 1e-9);
    }

    @Test
    public void setTimeZoneTest() {
        String test = TimeUtils.setTimeZone(17.0, 0);
        assertEquals("Incorrect TimeZone in XML file. TimeZone reset to default.", outContent.toString());
    }

    @Test
    public void formatUTCTest() {
        assertEquals("-11:30", TimeUtils.getFormatUTCOffset(-11.5));
        assertEquals("-11:00", TimeUtils.getFormatUTCOffset(-11.0));
        assertEquals("-09:00", TimeUtils.getFormatUTCOffset(-9.0));
        assertEquals("-09:30", TimeUtils.getFormatUTCOffset(-9.5));
        assertEquals("+09:00", TimeUtils.getFormatUTCOffset(9.0));
        assertEquals("+09:30", TimeUtils.getFormatUTCOffset(9.5));
        assertEquals("+11:30", TimeUtils.getFormatUTCOffset(11.5));
        assertEquals("+11:00", TimeUtils.getFormatUTCOffset(11.0));
    }

    @Test
    public void calcDistanceTest() {
        Coordinate testCoord = new Coordinate(10, 10);
        Coordinate testCoord2Two = new Coordinate(100, 100);
        assertEquals(0, TimeUtils.calcDistance(10, 10, 10, 10), 0.0005);
        assertEquals(0, TimeUtils.calcDistance(0, 0, 0, 0), 0);
        assertEquals(TimeUtils.calcDistance(testCoord, testCoord2Two), TimeUtils.calcDistance(10, 100, 10, 100), 0.0005);

    }

    @Test
    public void testNauticalMilesToMetres(){
        assertEquals(1852.0, TimeUtils.convertNauticalMilesToMetres(1), 0.001);
        assertEquals(0.0, TimeUtils.convertNauticalMilesToMetres(0), 0.001);
        assertEquals(18520.0, TimeUtils.convertNauticalMilesToMetres(10), 0.001);
    }
}
