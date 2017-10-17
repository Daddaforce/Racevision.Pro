package seng302.controllers.listeners;

import seng302.data.CourseName;
import seng302.models.Race;
import seng302.utilities.ConnectionUtils;
import seng302.views.AvailableRace;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Observable;
import java.util.zip.CRC32;

import static seng302.data.AC35StreamField.*;
import static seng302.data.AC35StreamField.HOST_GAME_CURRENT_PLAYERS;
import static seng302.data.AC35StreamField.HOST_GAME_REQUIRED_PLAYERS;

/**
 * Created by mjt169 on 18/07/17.
 *
 */
public abstract class Listener extends Observable implements Runnable{
    protected final int HEADER_LENGTH = 15;
    protected final int CRC_LENGTH = 4;
    protected final int BOAT_DEVICE_TYPE = 1;
    protected final int MARK_DEVICE_TYPE = 3;
    private boolean hasConnectionFailed = false;
    protected Socket socket;
    private InputStream dataStream;

    /**
     * Converts a range of bytes in an array from beginIndex to endIndex - 1 to an integer in little endian order.
     * Range excludes endIndex to be consistent with similar Java methods (e.g. String.subString).
     * Range Length must be greater than 0 and less than or equal to 4 (to fit within a 4 byte int).
     * @param array The byte array containing the bytes to be converted
     * @param beginIndex The starting index of range of bytes to be converted
     * @param endIndex The ending index (exclusive) of the range of bytes to be converted
     * @return The integer converted from the range of bytes in little endian order
     */
    public static int byteArrayRangeToInt(byte[] array, int beginIndex, int endIndex){
        int length = endIndex - beginIndex;
        if(length <= 0 || length > 4){
            throw new IllegalArgumentException("The length of the range must be between 1 and 4 inclusive");
        }

        int total = 0;
        for(int i = endIndex - 1; i >= beginIndex; i--){
            total = (total << 8) + (array[i] & 0xFF);
        }
        return total;
    }

    /**
     * Converts a range of bytes in an array from beginIndex to endIndex - 1 to an integer in little endian order.
     * Range excludes endIndex to be consistent with similar Java methods (e.g. String.subString).
     * Range Length must be greater than 0 and less than or equal to 8 (to fit within a 8 byte long).
     * @param array The byte array containing the bytes to be converted
     * @param beginIndex The starting index of range of bytes to be converted
     * @param endIndex The ending index (exclusive) of the range of bytes to be converted
     * @return The long converted from the range of bytes in little endian order
     */
    public static long byteArrayRangeToLong(byte[] array, int beginIndex, int endIndex){
        int length = endIndex - beginIndex;
        if(length <= 0 || length > 8){
            throw new IllegalArgumentException("The length of the range must be between 1 and 8 inclusive");
        }

        long total = 0;
        for(int i = endIndex - 1; i >= beginIndex; i--){
            total = (total << 8) + (array[i] & 0xFF);
        }
        return total;
    }

    /**
     * Calculates the CRC from header + body and checks if it is equal to the value from the expected CRC byte array
     * @param header The header of the message
     * @param body The body of the message
     * @param crc The expected CRC of the header and body combined
     * @return True if the calculated CRC is equal to the expected CRC, False otherwise
     */
    protected boolean checkCRC(byte[] header, byte[] body, byte[] crc) {
        CRC32 actualCRC = new CRC32();
        actualCRC.update(header);
        actualCRC.update(body);
        long expectedCRCValue = Integer.toUnsignedLong(byteArrayRangeToInt(crc, 0, 4));
        return expectedCRCValue == actualCRC.getValue();
    }

    public boolean isHasConnectionFailed() {
        return hasConnectionFailed;
    }

    public void setHasConnectionFailed(boolean state) {
        hasConnectionFailed = state;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    abstract public Race getRace();

    abstract public void disconnectClient();

    /**
     * Method to decode a host game packet from the server
     * @param body body of the hosted game, containing all relevant information about a game
     */
    public void parseHostedGameMessage(byte[] body){
        long serverIpLong = byteArrayRangeToLong(body, HOST_GAME_IP.getStartIndex(), HOST_GAME_IP.getEndIndex());
        String serverIP = ConnectionUtils.ipLongToString(serverIpLong);
        int serverPort = byteArrayRangeToInt(body, HOST_GAME_PORT.getStartIndex(), HOST_GAME_PORT.getEndIndex());
        int courseIndex = byteArrayRangeToInt(body, HOST_GAME_MAP.getStartIndex(), HOST_GAME_MAP.getEndIndex());
        long gameSpeed = byteArrayRangeToLong(body, HOST_GAME_SPEED.getStartIndex(), HOST_GAME_SPEED.getEndIndex());
        int gameStatus = byteArrayRangeToInt(body, HOST_GAME_STATUS.getStartIndex(), HOST_GAME_STATUS.getEndIndex());
        int gameMinPlayers = byteArrayRangeToInt(body, HOST_GAME_REQUIRED_PLAYERS.getStartIndex(), HOST_GAME_REQUIRED_PLAYERS.getEndIndex());
        int gameCurrentPlayers = byteArrayRangeToInt(body, HOST_GAME_CURRENT_PLAYERS.getStartIndex(), HOST_GAME_CURRENT_PLAYERS.getEndIndex());
        boolean isPartyGame = byteArrayRangeToInt(body, HOST_GAME_IS_PARTY_MODE.getStartIndex(), HOST_GAME_IS_PARTY_MODE.getEndIndex()) == 1;
        AvailableRace availableRace = new AvailableRace(CourseName.getCourseNameFromInt(courseIndex).getText(), gameCurrentPlayers, serverPort, serverIP, isPartyGame);
        setChanged();
        notifyObservers(availableRace);
    }

    /**
     * Sets up the connection to the data source by creating a socket and creates a InputStream from the socket.
     * returns whether connection was successful
     */
    public boolean setUpConnection(String sourceAddress, int sourcePort) {
        try {
            Socket socket = new Socket(sourceAddress, sourcePort);
            setSocket(socket);
            dataStream = getSocket().getInputStream();
            return true;
        } catch (IOException e) {
            setHasConnectionFailed(true);
            return false;
        }
    }

    public InputStream getDataStream() {
        return dataStream;
    }

    public void parseRoomCodeMessage(byte[] body){
        Integer roomCode = byteArrayRangeToInt(body, PARTY_MODE_ROOM_CODE.getStartIndex(), PARTY_MODE_ROOM_CODE.getEndIndex());
        setChanged();
        notifyObservers(roomCode);
    }
}
