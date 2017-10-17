package seng302.data;

import javafx.scene.paint.Color;
import org.junit.Test;
import seng302.controllers.listeners.Listener;
import seng302.data.registration.RegistrationResponseStatus;
import seng302.models.Boat;
import seng302.models.CompoundMark;
import seng302.models.Course;
import seng302.models.Race;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static seng302.data.AC35StreamField.*;
import static seng302.data.AC35StreamField.START_TIME;
import static seng302.data.AC35StreamField.WIND_SPEED;
import static seng302.controllers.listeners.Listener.byteArrayRangeToInt;
import static seng302.controllers.listeners.Listener.byteArrayRangeToLong;

/**
 * Created by Michael Trotter on 7/24/2017.
 */
public class ServerPacketBuilderTest {

    private int CRC_LENGTH = 4;

    private int HEADER_LENGTH = 15;
    @Test
    public void createWebClientInitPacket() {
        ServerPacketBuilder builder = new ServerPacketBuilder();
        Color color = Color.web("#EE4326");
        byte[] fullPacket = builder.createWebClientInitPacket(101, "Emirates Team New Zealand", color);
        byte[] body = Arrays.copyOfRange(fullPacket, HEADER_LENGTH, fullPacket.length - CRC_LENGTH); //extract body
        int id = byteArrayRangeToInt(body, WEB_CLIENT_ID.getStartIndex(), WEB_CLIENT_ID.getEndIndex());
        assertEquals(101, id);
        char[] charName = new char[WEB_CLIENT_NAME.getLength()];
        int i;
        for (i = 0; i < charName.length; i++) {
            char chr = (char)body[WEB_CLIENT_NAME.getStartIndex() + i];
            charName[i] = chr;
            if (chr == '\0') break;
        }
        String boatName = String.copyValueOf(charName, 0, i);
        assertEquals("Emirates Team New Zealand", boatName);
        //int id = byteArrayRangeToInt(body, WEB_CLIENT_ID.getStartIndex(), WEB_CLIENT_ID.getEndIndex());
        int colStart = WEB_CLIENT_COLOUR.getStartIndex();
        int r = byteArrayRangeToInt(body, colStart, colStart + 1);
        int g = byteArrayRangeToInt(body, colStart + 1, colStart + 2);
        int b = byteArrayRangeToInt(body, colStart + 2, colStart + 3);
        assertEquals(color.getRed() * 255, r, 0.01);
        assertEquals(color.getGreen() * 255, g, 0.01);
        assertEquals(color.getBlue() * 255, b, 0.01);
    }

    @Test
    public void createRaceUpdateMessage() throws Exception {
        ServerPacketBuilder builder = new ServerPacketBuilder();
        Course course = new Course();
        course.setWindDirection(10);
        course.setTrueWindSpeed(20);
        Boat boat1 = new Boat(100, "Test Boat 1", "TB1", 10);
        Boat boat2 = new Boat(101, "Test Boat 2", "TB2", 10);
        List<Boat> boats = new ArrayList<>();
        boats.add(boat1);
        boats.add(boat2);
        Race race = new Race("Test Race", course, boats);
        race.setId("1");
        race.updateRaceStatus(RaceStatus.STARTED);
        byte[] fullPacket = builder.createRaceUpdateMessage(race);
        byte[] body = Arrays.copyOfRange(fullPacket, HEADER_LENGTH, fullPacket.length - CRC_LENGTH); //extract body

        int raceStatus = byteArrayRangeToInt(body, RACE_STATUS.getStartIndex(), RACE_STATUS.getEndIndex());
        int raceCourseWindDirection = byteArrayRangeToInt(body, WIND_DIRECTION.getStartIndex(), WIND_DIRECTION.getEndIndex());
        long currentTime = byteArrayRangeToLong(body, CURRENT_TIME.getStartIndex(), CURRENT_TIME.getEndIndex());
        long expectedStartTime = byteArrayRangeToLong(body, START_TIME.getStartIndex(), START_TIME.getEndIndex());
        long windSpeed = byteArrayRangeToInt(body, WIND_SPEED.getStartIndex(), WIND_SPEED.getEndIndex());

        assertEquals(RaceStatus.STARTED, RaceStatus.fromInteger(raceStatus));
        assertEquals(1820, raceCourseWindDirection);
        assertEquals(10288, windSpeed);
        assertEquals(race.getCurrentTimeInEpochMs(), currentTime);
        assertEquals(race.getStartTimeInEpochMs(), expectedStartTime);

        int boat1ID = byteArrayRangeToInt(body, 24, 28);
        int boat1Status = byteArrayRangeToInt(body, 28, 29);
        int boat1Leg = byteArrayRangeToInt(body, 29, 32);
        assertEquals(boat1.getId(), (Integer) boat1ID);
        assertEquals(boat1.getStatus(), BoatStatus.values()[boat1Status]);
        assertEquals(boat1.getLeg(), boat1Leg);

        int offset = 20;
        int boat2ID = byteArrayRangeToInt(body, 24 + offset, 28 + offset);
        int boat2Status = byteArrayRangeToInt(body, 28 + offset, 29 + offset);
        int boat2Leg = byteArrayRangeToInt(body, 29 + offset, 32 + offset);
        assertEquals(boat2.getId(), (Integer) boat2ID);
        assertEquals(boat2.getStatus(), BoatStatus.values()[boat2Status]);
        assertEquals(boat2.getLeg(), boat2Leg);
    }

    @Test
    public void createMarkRoundingMessage() throws Exception {
        ServerPacketBuilder builder = new ServerPacketBuilder();
        Course course = mock(Course.class);
        ArrayList<CompoundMark> courseOrder = new ArrayList<>();
        CompoundMark mark1 = mock(CompoundMark.class);
        CompoundMark mark2 = mock(CompoundMark.class);
        courseOrder.add(mark1);
        courseOrder.add(mark2);
        when(course.getCourseOrder()).thenReturn(courseOrder);
        Boat boat1 = new Boat(100, "Test Boat 1", "TB1", 10);
        Boat boat2 = new Boat(101, "Test Boat 2", "TB2", 10);
        boat1.setLastRoundedMarkIndex(0);
        boat2.setLastRoundedMarkIndex(1);
        List<Boat> boats = new ArrayList<>();
        boats.add(boat1);
        boats.add(boat2);
        Race race = new Race("Test Race", course, boats);
        race.setId("1");
        race.updateRaceStatus(RaceStatus.STARTED);

        byte[] fullPacket = builder.createMarkRoundingMessage(boat1, race);
        byte[] body = Arrays.copyOfRange(fullPacket, HEADER_LENGTH, fullPacket.length - CRC_LENGTH); //extract body
        int sourceID = byteArrayRangeToInt(body, ROUNDING_SOURCE_ID.getStartIndex(), ROUNDING_SOURCE_ID.getEndIndex());
        int markIndex = byteArrayRangeToInt(body, ROUNDING_MARK_ID.getStartIndex(), ROUNDING_MARK_ID.getEndIndex());
        assertEquals(100, sourceID);
        assertEquals(0, markIndex);

        fullPacket = builder.createMarkRoundingMessage(boat2, race);
        body = Arrays.copyOfRange(fullPacket, HEADER_LENGTH, fullPacket.length - CRC_LENGTH); //extract body
        sourceID = byteArrayRangeToInt(body, ROUNDING_SOURCE_ID.getStartIndex(), ROUNDING_SOURCE_ID.getEndIndex());
        markIndex = byteArrayRangeToInt(body, ROUNDING_MARK_ID.getStartIndex(), ROUNDING_MARK_ID.getEndIndex());
        assertEquals(101, sourceID);
        assertEquals(1, markIndex);
    }

    @Test
    public void buildXmlMessage() {
    }

    @Test
    public void createBoatLocationMessage() {
        ServerPacketBuilder builder = new ServerPacketBuilder();
        Course course = new Course();
        course.setWindDirection(10);
        Boat boat1 = new Boat(100, "Test Boat 1", "TB1", 10);
        boat1.setHeading(50.3);
        boat1.setCurrentSpeed(23.9);
        boat1.setPosition(23.5, 28.7);
        boat1.setTWAofBoat(23);

        List<Boat> boats = new ArrayList<>();
        boats.add(boat1);
        Race race = new Race("Test Race", course, boats);
        race.setId("1");
        race.updateRaceStatus(RaceStatus.STARTED);

        byte[] fullPacket = builder.createBoatLocationMessage(boat1, race, 3);
        byte[] body = Arrays.copyOfRange(fullPacket, HEADER_LENGTH, fullPacket.length - CRC_LENGTH); //extract body

        int sourceID = byteArrayRangeToInt(body, BOAT_SOURCE_ID.getStartIndex(), BOAT_SOURCE_ID.getEndIndex());
        int latScaled = byteArrayRangeToInt(body, LATITUDE.getStartIndex(), LATITUDE.getEndIndex());
        int lonScaled = byteArrayRangeToInt(body, LONGITUDE.getStartIndex(), LONGITUDE.getEndIndex());
        int headingScaled = byteArrayRangeToInt(body, HEADING.getStartIndex(), HEADING.getEndIndex());
        int boatSpeed = byteArrayRangeToInt(body, SPEED_OVER_GROUND.getStartIndex(), SPEED_OVER_GROUND.getEndIndex());

        int deviceType = byteArrayRangeToInt(body, DEVICE_TYPE.getStartIndex(), DEVICE_TYPE.getEndIndex());
        int trueWindAngleScaled = byteArrayRangeToInt(body, TRUE_WIND_ANGLE.getStartIndex(), TRUE_WIND_ANGLE.getEndIndex());

        assertEquals(100, sourceID);
        assertEquals(280365921, latScaled);
        assertEquals(342404337, lonScaled);
        assertEquals(9156, headingScaled);
        assertEquals(12295, boatSpeed);
        assertEquals(1, deviceType);
        assertEquals(4187, trueWindAngleScaled);
    }

    @Test
    public void createRegistrationAcceptancePacket() {
        ServerPacketBuilder builder = new ServerPacketBuilder();
        byte[] fullPacket = builder.createRegistrationResponsePacket(100, RegistrationResponseStatus.PLAYER_SUCCESS);
        byte[] body = Arrays.copyOfRange(fullPacket, HEADER_LENGTH, fullPacket.length - CRC_LENGTH); //extract body

        int id = byteArrayRangeToInt(body, REGISTRATION_SOURCE_ID.getStartIndex(), REGISTRATION_SOURCE_ID.getEndIndex());
        byte status = body[REGISTRATION_RESPONSE_STATUS.getStartIndex()];

        assertEquals(100, id);
        assertEquals(RegistrationResponseStatus.PLAYER_SUCCESS.value(), status);
    }

    @Test
    public void createRegistrationRejectionPacket() {
        ServerPacketBuilder builder = new ServerPacketBuilder();
        byte[] fullPacket = builder.createRegistrationResponsePacket(100, RegistrationResponseStatus.OUT_OF_SLOTS);
        byte[] body = Arrays.copyOfRange(fullPacket, HEADER_LENGTH, fullPacket.length - CRC_LENGTH); //extract body

        int id = byteArrayRangeToInt(body, REGISTRATION_SOURCE_ID.getStartIndex(), REGISTRATION_SOURCE_ID.getEndIndex());
        byte status = body[REGISTRATION_RESPONSE_STATUS.getStartIndex()];

        assertEquals(100, id);
        assertEquals(RegistrationResponseStatus.OUT_OF_SLOTS.value(), status);
    }

}
