package seng302.data;

import seng302.utilities.ConnectionUtils;

import java.time.Instant;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import static seng302.data.AC35StreamField.*;
import static seng302.data.AC35StreamMessage.HOST_GAME_MESSAGE;

/**
 * Created by mjt169 on 18/07/17.
 *
 */
public abstract class PacketBuilder {

    private final int HEADER_LENGTH = 15;

    private int sourceID = -1;

    /**
     * Simplifier function for adding stream field to byte array
     * @param array array which to add the int
     * @param field the AC35StreamField field to add
     * @param item the item to add
     */
    protected void addFieldToByteArray(byte[] array, AC35StreamField field, long item){
        addIntIntoByteArray(array, field.getStartIndex(), item, field.getLength());
    }

    /**
     * Splits an integer into a few bytes and adds it to a byte array
     * @param array array which to add the int
     * @param start index it start adding
     * @param item item to add
     * @param numBytes number of bytes to split the int into
     */
    protected void addIntIntoByteArray(byte[] array, int start, long item, int numBytes){
        for (int i = 0; i < numBytes; i ++) {
            array[start + i] = (byte) (item >> i * 8);
        }
    }

    /**
     * creates a header byte array which has 2 snyc bytes, a type, timestamp, source id which is an identifier for who
     * is sending the message
     * message length if it is not variable.
     * @param type the integer type of the message
     * @return a byte array of the header
     */
    protected byte[] createHeader(AC35StreamMessage type) {
        byte[] header = new byte[HEADER_LENGTH];

        header[0] = (byte) 0x47; //first sync byte
        header[1] = (byte) 0x83; //second sync byte
        addFieldToByteArray(header, MESSAGE_TYPE, type.getValue());
        addFieldToByteArray(header, HEADER_TIMESTAMP, Instant.now().toEpochMilli());
        addFieldToByteArray(header, HEADER_SOURCE_ID, this.sourceID);
        if (type.getLength() != -1) {
            addFieldToByteArray(header, MESSAGE_LENGTH, type.getLength());
        }
        return header;
    }



    /**
     * Computes and sends the CRC checksum to the socket
     * @param header The header byte array used to compute the CRC
     * @param body the body byte array used to compute the CRC
     */
    private byte[] generateCRC(byte[] header, byte[] body){
        final int CRC_LENGTH = 4;
        Checksum crc = new CRC32();
        byte[] toCRC = new byte[header.length + body.length];
        for (int i = 0; i < toCRC.length; i ++){
            if (i < HEADER_LENGTH) {
                toCRC[i] = header[i];
            } else {
                toCRC[i] = body[i - HEADER_LENGTH];
            }
        }
        crc.update(toCRC, 0, toCRC.length);

        byte[] crcArray = new byte[CRC_LENGTH];
        addIntIntoByteArray(crcArray, 0, (int) crc.getValue(), CRC_LENGTH);
        return crcArray;

    }

    /**
     * Generates the packet that is to be sent by calculating the crc and combining the messages together
     * @param header the message header
     * @param body the message body
     * @return the combined messages with calculated crc
     */
    public byte[] generatePacket(byte[] header, byte[] body){
        byte[] crc = generateCRC(header,body);
        return combineMessageParts(header,body,crc);
    }

    /**
     * Combines the header, body and crc byte arrays together
     * @param header the message header
     * @param body the message body
     * @param crc the computed crc to be sent
     * @return the combined message consisting of all three parts
     */
    private byte[] combineMessageParts(byte[] header, byte[] body, byte[] crc){
        byte[] combined = new byte[header.length + body.length + crc.length];
        for(int i = 0; i < header.length; i++){
            combined[i] = header[i];
        }
        for(int i = 0; i < body.length; i++){
            combined[i+header.length] = body[i];
        }
        for(int i = 0; i < crc.length; i++){
            combined[i+header.length + body.length] = crc[i];
        }
        return combined;
    }

    public byte[] createGameRegistrationPacket(byte[] payload){
        byte[] header = createHeader(HOST_GAME_MESSAGE);
        return generatePacket(header, payload);
    }

    /**
     * Creates a game registeration packet to be sent to the VM
     * @param speedScale speed of the game
     * @param minParticipants number of participants for the game to start
     * @param serverPort port of the server
     * @param publicIp ip of the server
     * @param currentCourseIndex current course
     * @return a packet to be sent
     */
    public byte[] createGameRegistrationPacket(Double speedScale, Integer minParticipants, Integer serverPort, String publicIp, int currentCourseIndex, int currentPlayers, boolean isPartyMode) {
        byte[] header = createHeader(HOST_GAME_MESSAGE);
        byte[] body = new byte[HOST_GAME_MESSAGE.getLength()];
        long ip = ConnectionUtils.ipStringToLong(publicIp);
        addFieldToByteArray(body, HOST_GAME_IP, ip);
        addFieldToByteArray(body, HOST_GAME_PORT, serverPort);
        addFieldToByteArray(body, HOST_GAME_MAP, currentCourseIndex);
        addFieldToByteArray(body, HOST_GAME_SPEED, speedScale.longValue());
        addFieldToByteArray(body, HOST_GAME_STATUS, 1);
        addFieldToByteArray(body, HOST_GAME_REQUIRED_PLAYERS, minParticipants);
        addFieldToByteArray(body, HOST_GAME_CURRENT_PLAYERS, currentPlayers);
        addFieldToByteArray(body, HOST_GAME_IS_PARTY_MODE, isPartyMode ? 1 : 0);
        return generatePacket(header, body);
    }

}
