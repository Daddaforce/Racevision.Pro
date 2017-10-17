package seng302.data;

import seng302.data.registration.RegistrationResponseStatus;
import seng302.models.*;
import seng302.utilities.ConnectionUtils;

import javafx.scene.paint.Color;
import java.io.*;
import java.util.*;

import static seng302.data.AC35StreamField.*;
import static seng302.data.AC35StreamMessage.*;
import static seng302.utilities.TimeUtils.convertKnotsToMmPerSecond;

public class ServerPacketBuilder extends PacketBuilder {

    private final int ROUNDING_MARK_TYPE = 1;
    private final int GATE_TYPE = 2;
    private final String DEFAULT_RESOURCES_FOLDER = "/defaultFiles/";

    public ServerPacketBuilder() {}

    /**
     * Creates a header byte array for a Race Status Message
     * @param numBoatsInRace
     * @return a byte array representing a correctly formed Race Status Message header
     */
    private byte[] createRaceStatusHeader(int numBoatsInRace) {
        byte[] header = createHeader(RACE_STATUS_MESSAGE);
        addFieldToByteArray(header, MESSAGE_LENGTH, 24 + 20 * (numBoatsInRace));
        return header;
    }

    public byte[] createRaceUpdateMessage(Race race) throws Exception {
        Collection<Boat> boatsInRace = race.getCompetitors();
        byte[] raceStatusBody = initialiseRaceStatusMessage(boatsInRace.size(), race);
        int offset = 24;

        for (Boat boat : boatsInRace) {
            byte[] boatStatus = createBoatStatus(boat);
            for (int i = 0; i < boatStatus.length; i++) {
                raceStatusBody[i + offset] = boatStatus[i];
            }
            offset += 20;
        }

        byte[] header = createRaceStatusHeader(boatsInRace.size());
        return generatePacket(header, raceStatusBody);
    }

    /**
     * creates mark rounding message body
     * @param boat the boat that rounded
     * @param race the race
     * @return byte array of the body of the message
     */
    public byte[] createMarkRoundingMessage(Boat boat, Race race) {
        Course course = race.getCourse();
        byte[] header = createHeader(MARK_ROUNDING_MESSAGE);
        int passedStartLineId = 102;
        int passedFinishLineId = 103;

        byte[] body = new byte[MARK_ROUNDING_MESSAGE.getLength()];
        body[0] = 1;
        addFieldToByteArray(body, BOAT_TIMESTAMP, race.getCurrentTimeInEpochMs());
        addFieldToByteArray(body, MARK_ACK, 0); //todo make proper ack
        addFieldToByteArray(body, MARK_RACE_ID, Integer.parseInt(race.getId()));
        addFieldToByteArray(body, ROUNDING_SOURCE_ID, boat.getId());
        addFieldToByteArray(body, MARK_BOAT_STATUS, boat.getStatus().getValue());
        addFieldToByteArray(body, ROUNDING_SIDE, 0); //todo present correct side

        CompoundMark lastRoundedMark = course.getCourseOrder().get(boat.getLastRoundedMarkIndex());
        if (lastRoundedMark.hasTwoMarks()) {
            addFieldToByteArray(body, MARK_TYPE, GATE_TYPE);
        } else {
            addFieldToByteArray(body, MARK_TYPE, ROUNDING_MARK_TYPE);
        }
        int markId = boat.getLastRoundedMarkIndex();
        if(lastRoundedMark.isStartLine()){
            markId = passedStartLineId;
        } else if(lastRoundedMark.isFinishLine()){
            markId = passedFinishLineId;
        }
        addFieldToByteArray(body, MARK_ID, markId);
        return generatePacket(header, body);
    }

    public byte[] buildXmlMessage(AC35StreamXMLMessage type, String fileName, int sequenceNo, Race race, String course) {
        byte[] header = createHeader(XML_MESSAGE);
        byte[] xmlBody = generateXmlBody(type, fileName, sequenceNo, race, course);
        addFieldToByteArray(header, MESSAGE_LENGTH, xmlBody.length);
        return generatePacket(header,xmlBody);
    }

    /**
     * initialise Race Status Message
     * @param numBoats to send the number
     * @return byte array of body
     */
    private byte[] initialiseRaceStatusMessage(int numBoats, Race race) {
        byte[] body = new byte[24 + (20 * numBoats)];
        addFieldToByteArray(body, STATUS_MESSAGE_VERSION_NUMBER, 2);
        addFieldToByteArray(body, BOAT_TIMESTAMP, race.getCurrentTimeInEpochMs());
        addFieldToByteArray(body, STATUS_RACE_ID, Integer.parseInt(race.getId()));
        addFieldToByteArray(body, RACE_STATUS, race.getRaceStatus().getValue());
        addFieldToByteArray(body, EXPECTED_START_TIME, race.getStartTimeInEpochMs());
        addFieldToByteArray(body, CURRENT_TIME, race.getCurrentTimeInEpochMs());
        addFieldToByteArray(body, RACE_COURSE_WIND_DIRECTION, convertHeadingToInt(race.getCourse().getWindDirection()));
        addFieldToByteArray(body, RACE_COURSE_WIND_SPEED, convertKnotsToMmPerSecond(race.getCourse().getTrueWindSpeed()));
        addFieldToByteArray(body, NUMBER_OF_BOATS_IN_RACE, numBoats);
        addFieldToByteArray(body, RACE_TYPE, 2); //fleet race

        return body;
    }

    /**
     * Converts a heading in degrees into the AC35 heading format
     * @param heading the heading in degress
     * @return the converted heading, represented by a long
     */
    private long convertHeadingToInt(double heading) {
        return (long)(heading * Math.pow(2, 16)) / 360;
    }

    /**
     * add Boat To Race Status Message
     * @param boat boat to add
     * @return new boatStatus message
     */
    private byte[] createBoatStatus(Boat boat){
        byte[] boatStatus = new byte[20];
        addFieldToByteArray(boatStatus, STATUS_SOURCE_ID, boat.getId());
        addFieldToByteArray(boatStatus, BOAT_STATUS, boat.getStatus().getValue());
        addFieldToByteArray(boatStatus, LEG_NUMBER, boat.getLastRoundedMarkIndex() + 1);
        addFieldToByteArray(boatStatus, NUMBER_PENALTIES_AWARDED, 0); //todo make penalties a thing
        addFieldToByteArray(boatStatus, NUMBER_PENALTIES_SERVED, 0);
        addFieldToByteArray(boatStatus, ESTIMATED_TIME_AT_NEXT_MARK, boat.getTimeAtNextMark());
        addFieldToByteArray(boatStatus, ESTIMATED_TIME_AT_FINISH, boat.getTimeTillFinish());

        return boatStatus;
    }

    /**
     * Builds a byte array representing a boat location message
     * @param boat the boat that is the subject of the message
     * @return a byte array representing the boat location message
     */
    public byte[] createBoatLocationMessage(Boat boat, Race race, int sequenceNumber) {
        byte[] header = createHeader(BOAT_LOCATION_MESSAGE);
        long windDirection = (long) race.getCourse().getWindDirection();
        byte[] body = initialiseLocationPacket(windDirection);
        Coordinate location = boat.getCurrentPosition();

        int lat = (int) Math.round(location.getLat() * Math.pow(2, 31) / 180);
        int lon = (int) Math.round(location.getLon() * Math.pow(2, 31) / 180);
        addFieldToByteArray(body, BOAT_TIMESTAMP, race.getCurrentTimeInEpochMs());
        addFieldToByteArray(body, BOAT_SOURCE_ID, boat.getId());
        addFieldToByteArray(body, BOAT_SEQUENCE_NUM, sequenceNumber);
        addFieldToByteArray(body, LATITUDE, lat);
        addFieldToByteArray(body, LONGITUDE, lon);
        addFieldToByteArray(body, HEADING, (int) (boat.getHeading() * Math.pow(2, 16) / 360));
        addFieldToByteArray(body, SPEED_OVER_GROUND, boat.getSpeedInMMS());
        addFieldToByteArray(body, TRUE_WIND_DIRECTION, convertHeadingToInt(race.getCourse().getWindDirection() ));
        addFieldToByteArray(body, TRUE_WIND_ANGLE, (long) (boat.getTWAofBoat() * Math.pow(2, 15) / 180)); //convert decimal to unsigned short binary,
        addFieldToByteArray(body, SAIL_STATE, boat.isSailsIn() ? 0 : 1); //convert decimal to unsigned short binary,


        return generatePacket(header, body);
    }

    /**
     * Builds a byte array representing a Yacht Event message
     * @param boat the boat involved in the collision
     * @param race the current race
     * @return a byte array representing a Yacht Event message
     */
    public byte[] createYachtEventMessage(Boat boat, Race race, int incidentID, YachtEventCode eventCode){
        byte[] header = createHeader(YACHT_EVENT_CODE);
        byte[] body = initialiseYachtEventPacket();
        addFieldToByteArray(body, EVENT_TIME, race.getCurrentTimeInEpochMs());
        addFieldToByteArray(body, EVENT_ACK_NUM, 0);
        addFieldToByteArray(body, RACE_ID, Integer.parseInt(race.getId()));
        addFieldToByteArray(body, DESTINATION_SOURCE_ID, boat.getId());
        addFieldToByteArray(body, INCIDENT_ID, incidentID);
        addFieldToByteArray(body, EVENT_ID, eventCode.code());

        return generatePacket(header, body);
    }

    /**
     * Reads a file as bytes and adds the information about the version, sequence number set to 1, subtype, timestamp,
     * xml sequence number and length then
     * @param subType the integer number of the subtype of the xml message
     * @param fileName the file which to read the bytes from
     * @return a byte array which is the body of the xml message
     */
    private byte[] generateXmlBody(AC35StreamXMLMessage subType, String fileName, int sequenceNo, Race race, String course) {
        try {
            byte[] bodyContent = readXMLIntoByteArray(DEFAULT_RESOURCES_FOLDER, fileName, race, course);
            byte[] body = new byte[XML_BODY.getStartIndex() + bodyContent.length];
            long timestamp = race.getCurrentTimeInEpochMs();
            addFieldToByteArray(body, XML_VERSION, 1);
            addFieldToByteArray(body, XML_ACK, 1);
            addFieldToByteArray(body, XML_TIMESTAMP, timestamp);
            addFieldToByteArray(body, XML_SUBTYPE, subType.getType());
            addFieldToByteArray(body, XML_SEQUENCE, sequenceNo);
            addFieldToByteArray(body, XML_LENGTH, bodyContent.length);

            for (int i = 0; i < bodyContent.length; i++){
                body[i + XML_BODY.getStartIndex()] = bodyContent[i];
            }
            return body;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Read an XML resource into a byte array
     * @param fileName the path to the file in the resources folder
     * @return a byte array containing the data from the file
     * @throws IOException
     */
    private byte[] readXMLIntoByteArray(String filePath, String fileName, Race race, String course) throws IOException {
        InputStream resourceStream = ServerPacketBuilder.class.getResourceAsStream(filePath + fileName);
        if(fileName.equals(course)){
            ArrayList<Integer> participantIds = new ArrayList<>();
            for (Boat boat : race.getCompetitors()){
                participantIds.add(boat.getId());
            }
            RaceVisionXMLParser raceVisionXMLParser = new RaceVisionXMLParser();
            resourceStream = raceVisionXMLParser.injectRaceXMLFields(resourceStream, race.getId(), race.getStartTimeInEpochMs(), participantIds);
        }
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        int read = resourceStream.read();
        while (read != -1){
            byteOutputStream.write(read);
            read = resourceStream.read();
        }

        return byteOutputStream.toByteArray();
    }

    /**
     * Initialises the static contents of the location packet body
     * @return a byte array of the version number, device type, altitude
     */
    private byte[] initialiseLocationPacket(long windDirection) {

        byte[] body = new byte[BOAT_LOCATION_MESSAGE.getLength()];
        body[0] = (byte) 1;
        body[15] = (byte) 1;
        body[24] = (byte) 0;
        addFieldToByteArray(body, TRUE_WIND_DIRECTION, windDirection);

        return body;
    }

    private byte[] initialiseYachtEventPacket(){
        byte[] body = new byte[YACHT_EVENT_CODE.getLength()];
        return body;
    }

    /**
     * Builds a byte array representing a registration response packet
     * @param sourceID boatID if player, 0 if spectator, -1 in case of failure
     * @param status the success or failure status of the registration
     * @return byte array
     */
    public byte[] createRegistrationResponsePacket(Integer sourceID, RegistrationResponseStatus status){
        byte[] header = createHeader(REGISTRATION_RESPONSE);
        byte[] body = new byte[REGISTRATION_RESPONSE.getLength()];
        addFieldToByteArray(body, REGISTRATION_SOURCE_ID, sourceID);
        addFieldToByteArray(body, REGISTRATION_RESPONSE_STATUS, status.value());
        return generatePacket(header, body);
    }

    /**
     * Builds a byte array for the Boat State Message packet
     * @param boat The boat the message is about
     * @return A byte array with the boat state message
     */
    public byte[] createBoatStateMessagePacket(Boat boat){
        byte[] header = createHeader(BOAT_STATE_MESSAGE);
        byte[] body = new byte[BOAT_STATE_MESSAGE.getLength()];
        addFieldToByteArray(body, BOAT_STATE_SOURCE_ID, boat.getId());
        addFieldToByteArray(body, BOAT_HEALTH, (int)boat.getBoatHealth());
        return generatePacket(header, body);
    }

    /**
     * creates a packet that is sent when a host quits a race, used to update the VM
     * @param port the port the server being cancelled is run on
     * @return the packet to send to the VM
     */
    public byte[] createGameCancelPacket(int port) {
        byte[] header = super.createHeader(GAME_CANCEL);
        byte[] body = new byte[GAME_CANCEL.getLength()];
        long ip = ConnectionUtils.ipStringToLong(ConnectionUtils.getPublicIp());
        addFieldToByteArray(body, HOST_GAME_IP, ip);
        addFieldToByteArray(body, HOST_GAME_PORT, port);
        return generatePacket(header, body);
    }

    /**
     * creates a packet that is sent to the web client containing boat name and boat color
     * @param id the id of the client
     * @param boatName  the boat name
     * @param boatColour the boat color
     */
    public byte[] createWebClientInitPacket(Integer id, String boatName, Color boatColour) {
        byte[] header = super.createHeader(WEB_CLIENT_INIT);
        byte[] body = new byte[WEB_CLIENT_INIT.getLength()];
        addFieldToByteArray(body, WEB_CLIENT_ID, id);
        byte[] nameInBytes = boatName.getBytes();
        for (int i = 0; i < nameInBytes.length && i < WEB_CLIENT_NAME.getLength(); i++) {
            body[WEB_CLIENT_NAME.getStartIndex() + i] = nameInBytes[i];
        }
        body[WEB_CLIENT_COLOUR.getStartIndex()] = (byte) (boatColour.getRed() * 255);
        body[WEB_CLIENT_COLOUR.getStartIndex() + 1] = (byte) (boatColour.getGreen() * 255);
        body[WEB_CLIENT_COLOUR.getStartIndex() + 2] = (byte) (boatColour.getBlue() * 255);
        return generatePacket(header, body);
    }

    /**
     * creates a packet to send to the webclient updating it on speed, position, health
     */
    public byte[] createWebClientUpdatePacket(Integer id, Double speed, int placing, int totalCompetitors, int healthPercentage) {
        byte[] header = super.createHeader(WEB_CLIENT_UPDATE);
        byte[] body = new byte[WEB_CLIENT_UPDATE.getLength()];
        addFieldToByteArray(body, WEB_CLIENT_ID, id);
        long convertedSpeed = convertKnotsToMmPerSecond(speed);
        addFieldToByteArray(body, WEB_CLIENT_SPEED, convertedSpeed);
        addFieldToByteArray(body, WEB_CLIENT_POSITION, placing);
        addFieldToByteArray(body, WEB_CLIENT_TOTAL_COMPETITORS, totalCompetitors);
        addFieldToByteArray(body, WEB_CLIENT_HEALTH, healthPercentage);
        return generatePacket(header, body);
    }

    /**
     * creates a packet containing the code for a party game
     */
    public byte[] createPartyModeRoomCodeMessage(Integer code) {
        byte[] header = super.createHeader(PARTY_MODE_CODE_MESSAGE);
        byte[] body = new byte[PARTY_MODE_CODE_MESSAGE.getLength()];
        addFieldToByteArray(body, PARTY_MODE_ROOM_CODE, code);
        return generatePacket(header, body);
    }

    /**
     * wraps a packet so that it can be sent to a web client
     * @param sendpacket the packet to be wrapped
     */
    public byte[] wrapPacket(byte[] sendpacket) {
        byte[] wrappedPacket = new byte[sendpacket.length + 2];
        wrappedPacket[0] = (byte) 130;
        wrappedPacket[1] = (byte) (sendpacket.length);
        for(int i = 0; i < sendpacket.length; i++){
            wrappedPacket[i+2] = sendpacket[i];
        }
        return wrappedPacket;
    }
}
