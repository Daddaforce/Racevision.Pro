package seng302.data;


import org.junit.Assert;
import org.junit.Test;
import seng302.models.*;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class RaceVisionXMLParserTest {

    private RaceVisionXMLParser raceVisionXMLParser = new RaceVisionXMLParser();

    @Test
    public void readRaceFileTest(){
        Course course = raceVisionXMLParser.importCourse(RaceVisionXMLParserTest.class.getResourceAsStream("/data/testFiles/testRace.xml"));
        Course expected = createExpectedCourse();

        Assert.assertNotNull(course);
        for (int i = 0; i < expected.getCourseOrder().size(); i++) {
            assertCompoundMarksAreEqual(expected.getCourseOrder().get(i), course.getCourseOrder().get(i));
        }
        for (Integer key : expected.getCompoundMarks().keySet()) {
            assertCompoundMarksAreEqual(expected.getCompoundMarks().get(key), course.getCompoundMarks().get(key));
        }

        Assert.assertTrue(course.getCompoundMarks().get(1).isStartLine());
        Assert.assertTrue(course.getCompoundMarks().get(11).isFinishLine());
        Assert.assertTrue(course.getCompoundMarks().get(1) instanceof RaceLine);
        Assert.assertTrue(course.getCompoundMarks().get(11) instanceof RaceLine);
        Assert.assertTrue(course.getCompoundMarks().get(2) != null);
        Assert.assertFalse(course.getCompoundMarks().get(14) != null);

        //Boundary
        Assert.assertEquals(course.getBoundary().size(), 10);
        for(int i = 0; i < 10; i++){
            Assert.assertEquals(course.getBoundary().get(i), expected.getBoundary().get(i));
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void nonExistentCourseFileTest(){
        InputStream fakeFileInputStream = RaceVisionXMLParserTest.class.getResourceAsStream("I am a fake file");
        Course course = raceVisionXMLParser.importCourse(fakeFileInputStream);
        Assert.assertNull(course);
    }

    /** This is a clone of the course that testRace.xml is expected to create */
    private Course createExpectedCourse() {
        Course expected = new Course();

        Mark startLine1 = new Mark(122, "Start Line 1", new Coordinate(57.6703330, 11.8278330));
        Mark startLine2 = new Mark(123, "Start Line 2", new Coordinate(57.6703330, 11.8278330));
        RaceLine start = new RaceLine(1, "Start", startLine1, startLine2);
        start.setMarkAsStart();

        Mark mark1 = new Mark(131, "Mark1", new Coordinate(57.6675700, 11.8359880));
        CompoundMark compoundMark2 = new CompoundMark(2, "Mark1", mark1);

        Mark leeGate1 = new Mark(124, "Lee Gate 1", new Coordinate(57.6708220, 11.8433900));
        Mark leeGate2 = new Mark(125, "Lee Gate 2", new Coordinate(57.6708220, 11.8433900));
        CompoundMark compoundMark3 = new CompoundMark(3, "Mark2", leeGate1, leeGate2);

        Mark windGate1 = new Mark(126, "Wind Gate 1", new Coordinate(57.6650170, 11.8279170));
        Mark windGate2 = new Mark(127, "Wind Gate 2", new Coordinate(57.6650170, 11.8279170));
        CompoundMark compoundMark4 = new CompoundMark(4, "Mark3", windGate1, windGate2);

        CompoundMark compoundMark5 = new CompoundMark(5, "Mark2", leeGate1, leeGate2);

        CompoundMark compoundMark6 = new CompoundMark(6, "Mark3", windGate1, windGate2);

        CompoundMark compoundMark7 = new CompoundMark(7, "Mark2", leeGate1, leeGate2);

        CompoundMark compoundMark8 = new CompoundMark(8, "Mark3", windGate1, windGate2);

        CompoundMark compoundMark9 = new CompoundMark(9, "Mark2", leeGate1, leeGate2);

        CompoundMark compoundMark10 = new CompoundMark(10, "Mark3", windGate1, windGate2);

        Mark finishLine1 = new Mark(128, "Finish Line 1", new Coordinate(57.6715240, 11.8444950));
        Mark finishLine2 = new Mark(129, "Finish Line 2", new Coordinate(57.6715240, 11.8444950));
        RaceLine finish = new RaceLine(11, "Finish", finishLine1, finishLine2);
        finish.setMarkAsFinish();

        expected.addNewCompoundMark(start);
        expected.addNewCompoundMark(compoundMark2);
        expected.addNewCompoundMark(compoundMark3);
        expected.addNewCompoundMark(compoundMark4);
        expected.addNewCompoundMark(compoundMark5);
        expected.addNewCompoundMark(compoundMark6);
        expected.addNewCompoundMark(compoundMark7);
        expected.addNewCompoundMark(compoundMark8);
        expected.addNewCompoundMark(compoundMark9);
        expected.addNewCompoundMark(compoundMark10);
        expected.addNewCompoundMark(finish);

        for(int i = 1; i <= 11; i++){
            expected.addMarkInOrder(i, RoundingSide.STBD_PORT);
        }

        expected.addToBoundary(new Coordinate(57.6739450, 11.8417100));
        expected.addToBoundary(new Coordinate(57.6709520, 11.8485010));
        expected.addToBoundary(new Coordinate(57.6690260, 11.8472790));
        expected.addToBoundary(new Coordinate(57.6693140, 11.8457610));
        expected.addToBoundary(new Coordinate(57.6665370, 11.8432910));
        expected.addToBoundary(new Coordinate(57.6641400, 11.8385840));
        expected.addToBoundary(new Coordinate(57.6629430, 11.8332030));
        expected.addToBoundary(new Coordinate(57.6629480, 11.8249660));
        expected.addToBoundary(new Coordinate(57.6686890, 11.8250920));
        expected.addToBoundary(new Coordinate(57.6708220, 11.8321340));
        return expected;
    }

    private void assertMarksAreEqual(Mark mark1, Mark mark2){
        Assert.assertEquals(mark1.getSourceID(), mark2.getSourceID());
        Assert.assertEquals(mark1.getPosition().getLat(), mark2.getPosition().getLat(), 0);
        Assert.assertEquals(mark1.getPosition().getLon(), mark2.getPosition().getLon(), 0);
    }

    /** Compares two marks for equality */
    private void assertCompoundMarksAreEqual(CompoundMark compoundMark1, CompoundMark compoundMark2){
        Assert.assertEquals(compoundMark1.getCompoundMarkID(), compoundMark2.getCompoundMarkID());
        Assert.assertEquals(compoundMark1.getName(), compoundMark2.getName());
        assertMarksAreEqual(compoundMark1.getMark1(), compoundMark2.getMark1());
        Assert.assertEquals(compoundMark1.hasTwoMarks(), compoundMark2.hasTwoMarks());
        if (compoundMark1.hasTwoMarks()) {
            assertMarksAreEqual(compoundMark1.getMark2(), compoundMark2.getMark2());
            if (compoundMark1 instanceof RaceLine) {
                Assert.assertTrue(compoundMark2 instanceof RaceLine);
            }
        }
    }


    @Test
    public void importStartersTest(){

        List<Boat> boats = raceVisionXMLParser.importStarters(RaceVisionXMLParserTest.class.getResourceAsStream("/data/testFiles/testBoat.xml"));
        List<Boat> expectedBoats = createExpectedBoats();
        Assert.assertEquals(expectedBoats.size(), boats.size());
        for(int i = 0; i < boats.size(); i++){
            assertBoatEquals(expectedBoats.get(i), boats.get(i));
        }
    }

    private void assertBoatEquals(Boat boat1, Boat boat2){
        Assert.assertEquals(boat1.getId(), boat2.getId());
        Assert.assertEquals(boat1.getName(), boat2.getName());
        Assert.assertEquals(boat1.getNickName(), boat2.getNickName());
    }

    private List<Boat> createExpectedBoats(){
        List<Boat> boats = new ArrayList<Boat>();
        boats.add(new Boat(101, "ORACLE TEAM USA", "USA", 0));
        boats.add(new Boat(102, "ARTEMIS RACING", "SWE", 0));
        boats.add(new Boat(103, "EMIRATES TEAM NZ", "NZL", 0));
        boats.add(new Boat(104, "SOFTBANK TEAM JAPAN", "JPN", 0));
        boats.add(new Boat(105, "GROUPAMA TEAM FRANCE", "FRA", 0));
        boats.add(new Boat(106, "LAND ROVER BAR", "GBR", 0));
        return boats;
    }

    @Test
    public void importRegattaTest(){
        Race race = new Race("test", null, new ArrayList<>());
        InputStream regattaInputStream = RaceVisionXMLParserTest.class.getResourceAsStream("/data/testFiles/testRegatta.xml");
        raceVisionXMLParser.importRegatta(regattaInputStream, race);
        Assert.assertEquals(2, race.getUTCOffset(), 0);
        Assert.assertEquals("Gothenburg World Series 2015", race.getRegattaName());
    }

    @Test
    public void updateRaceTest(){
        InputStream raceStream = RaceVisionXMLParserTest.class.getResourceAsStream("/data/testFiles/testRace.xml");
        long currentTime = Instant.now().toEpochMilli();
        InputStream result = raceVisionXMLParser.injectRaceXMLFields(raceStream,"11", currentTime, new ArrayList<>());

        Race race = raceVisionXMLParser.importRace(result);

        Assert.assertEquals("282828", race.getId());
        Assert.assertEquals(currentTime - (currentTime % 1000),race.getStartTimeInEpochMs());
    }
}