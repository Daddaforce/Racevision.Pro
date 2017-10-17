package seng302.controllers.listeners;

import seng302.data.AC35StreamMessage;
import seng302.models.Race;
import java.io.*;
import java.net.SocketTimeoutException;
;

import static seng302.data.AC35StreamField.*;

/**
 * Created by lga50 on 12/09/17.
 *
 */
public class MainMenuClientListener extends Listener implements Runnable{

    private String sourceAddress;
    private int sourcePort;
    private Race race;


    public MainMenuClientListener(String sourceAddress, int sourcePort){
        this.sourceAddress = sourceAddress;
        this.sourcePort = sourcePort;
    }

    /**
     * Runs the reader by setting up the connection and start reading in data
     */
    @Override
    public void run(){
        if(setUpConnection(sourceAddress, sourcePort)){
            readData();
        }
    }

    /**
     * Keeps reading in from the data stream and parses each message header and hands off the payload to the
     * corresponding method. Ignores the message if the message type is not needed.
     */
    private void readData(){
        DataInput dataInput = new DataInputStream(getDataStream());
        Boolean serverRunning = true;
        while(serverRunning) {
            try {
                byte[] header = new byte[HEADER_LENGTH];
                dataInput.readFully(header);
                int messageLength = byteArrayRangeToInt(header, MESSAGE_LENGTH.getStartIndex(), MESSAGE_LENGTH.getEndIndex());
                int messageTypeValue = byteArrayRangeToInt(header, MESSAGE_TYPE.getStartIndex(), MESSAGE_TYPE.getEndIndex());
                AC35StreamMessage messageType = AC35StreamMessage.fromInteger(messageTypeValue);
                int sourceID = byteArrayRangeToInt(header, HEADER_SOURCE_ID.getStartIndex(), HEADER_SOURCE_ID.getEndIndex());
                byte[] body = new byte[messageLength];
                dataInput.readFully(body);
                byte[] crc = new byte[CRC_LENGTH];
                dataInput.readFully(crc);
                if (checkCRC(header, body, crc)) {
                    if (messageType == AC35StreamMessage.HOST_GAME_MESSAGE){
                        parseHostedGameMessage(body);
                    }
                } else {
                    System.err.println("Incorrect CRC. Message Ignored: " + messageType);
                }
            } catch (SocketTimeoutException e){
                disconnectClient();
            } catch (IOException e) {
                if(race != null){
                    if(!race.isTerminated()){
                        race.terminateRace();
                        race.setAbruptEnd(true);
                    }
                }
                if (!getSocket().isClosed()) {
                    serverRunning = false;
                    e.printStackTrace();
                    System.out.println("Client: disconnected from Server");
                }
            }
        }
    }

    @Override
    public void disconnectClient() {
        try {
            getSocket().close();
            getDataStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setRace(Race race) {
        this.race = race;
    }

    public Race getRace() {
        return race;
    }
}
