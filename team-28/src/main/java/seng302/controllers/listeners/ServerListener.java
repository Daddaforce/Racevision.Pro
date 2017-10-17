package seng302.controllers.listeners;


import seng302.data.AC35StreamMessage;
import seng302.data.CourseName;
import seng302.utilities.ConnectionUtils;
import seng302.views.AvailableRace;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import static seng302.data.AC35StreamField.*;
import static seng302.data.AC35StreamField.HOST_GAME_CURRENT_PLAYERS;

/**
 * Created by atc60 on 22/09/17.
 */
public class ServerListener extends AbstractServerListener {

    private DataInput dataInput;

    public ServerListener(Socket socket, BufferedInputStream socketData) throws IOException {
        setSocket(socket);
        dataInput = new DataInputStream(socketData);
    }

    /**
     * The main run method of the serverListener. Continuously loops listening on a socket and then decoding it
     * and calling the needed method. Deals with client registration
     */
    @Override
    public void run() {
        boolean receivedCode = false;
        while(clientConnected && !receivedCode){
            try {
                byte[] header = new byte[HEADER_LENGTH];
                dataInput.readFully(header);

                int messageLength = byteArrayRangeToInt(header, MESSAGE_LENGTH.getStartIndex(), MESSAGE_LENGTH.getEndIndex());
                int messageTypeValue = byteArrayRangeToInt(header, MESSAGE_TYPE.getStartIndex(), MESSAGE_TYPE.getEndIndex());
                int sourceId = byteArrayRangeToInt(header, HEADER_SOURCE_ID.getStartIndex(), HEADER_SOURCE_ID.getEndIndex());
                AC35StreamMessage messageType = AC35StreamMessage.fromInteger(messageTypeValue);
                byte[] body = new byte[messageLength];
                dataInput.readFully(body);
                byte[] crc = new byte[CRC_LENGTH];
                dataInput.readFully(crc);
                if (checkCRC(header, body, crc)) {
                    switch (messageType) {
                        case HOST_GAME_MESSAGE:
                            recordHostGameMessage(body);
                            break;
                        case GAME_CANCEL:
                            removeHostedGame(body);
                            break;
                        case REGISTRATION_REQUEST:
                            parseRegistrationRequestMessage(body);
                            break;
                        case BOAT_ACTION_MESSAGE:
                            if (sourceId != -1) {
                                parseBoatActionMessage(body);
                            }
                            break;
                        case PARTY_MODE_CODE_MESSAGE:
                            parseRoomCodeMessage(body);
                            receivedCode = true;
                            break;
                        default:
                            System.out.println("Unknown");
                    }
                } else{
                    System.out.println("Incorrect CRC");
                }
            } catch (SocketException e) {
                break;
            }catch (IOException e) {
                System.out.println("Server Listener: connection closed");
                clientConnected = false;
            }
        }
        System.out.println("ServerListener Stopped");
    }

    private void recordHostGameMessage(byte[] body){
        AvailableRace race = createAvailableRace(body);
        race.setPacket(body);
        setChanged();
        notifyObservers(race);
    }

    /**
     * Method to decode a host game packet from the server
     * @param body body of the hosted game, containing all relevant information about a game
     */
    private AvailableRace createAvailableRace(byte[] body){
        long serverIpLong = byteArrayRangeToLong(body, HOST_GAME_IP.getStartIndex(), HOST_GAME_IP.getEndIndex());
        String serverIP = ConnectionUtils.ipLongToString(serverIpLong);
        int serverPort = byteArrayRangeToInt(body, HOST_GAME_PORT.getStartIndex(), HOST_GAME_PORT.getEndIndex());
        int courseIndex = byteArrayRangeToInt(body, HOST_GAME_MAP.getStartIndex(), HOST_GAME_MAP.getEndIndex());
        long gameSpeed = byteArrayRangeToLong(body, HOST_GAME_SPEED.getStartIndex(), HOST_GAME_SPEED.getEndIndex());
        int gameStatus = byteArrayRangeToInt(body, HOST_GAME_STATUS.getStartIndex(), HOST_GAME_STATUS.getEndIndex());
        int gameMinPlayers = byteArrayRangeToInt(body, HOST_GAME_REQUIRED_PLAYERS.getStartIndex(), HOST_GAME_REQUIRED_PLAYERS.getEndIndex());
        int gameCurrentPlayers = byteArrayRangeToInt(body, HOST_GAME_CURRENT_PLAYERS.getStartIndex(), HOST_GAME_CURRENT_PLAYERS.getEndIndex());
        boolean isPartyMode = byteArrayRangeToInt(body, HOST_GAME_IS_PARTY_MODE.getStartIndex(), HOST_GAME_IS_PARTY_MODE.getEndIndex()) == 1;
        return new AvailableRace(CourseName.getCourseNameFromInt(courseIndex).getText(), gameCurrentPlayers, serverPort, serverIP, isPartyMode);
    }

    /**
     * notifes observes to remove a race by its IP address
     * @param body body of the packet of the game to remove
     */
    private void removeHostedGame(byte[] body){
        System.out.println("GameRecorder: Received remove game message");
        long serverIpLong = byteArrayRangeToLong(body, HOST_GAME_IP.getStartIndex(), HOST_GAME_IP.getEndIndex());
        String serverIP = ConnectionUtils.ipLongToString(serverIpLong);
        int port = byteArrayRangeToInt(body, HOST_GAME_PORT.getStartIndex(), HOST_GAME_PORT.getEndIndex());
        AvailableRace raceToRemove = new AvailableRace("", 0, port, serverIP, false);
        raceToRemove.setDeleted(true);
        setChanged();
        notifyObservers(raceToRemove);
    }
}
