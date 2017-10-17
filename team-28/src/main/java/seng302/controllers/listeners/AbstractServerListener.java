package seng302.controllers.listeners;

import seng302.data.*;
import seng302.data.registration.RegistrationType;
import seng302.models.Boat;
import seng302.models.PolarTable;
import seng302.models.Race;
import seng302.utilities.PolarReader;

import java.io.*;
import java.net.Socket;

import static seng302.data.AC35StreamField.*;
import static seng302.data.registration.RegistrationType.REQUEST_RUNNING_GAMES;

/**
 * Created by mjt169 on 19/07/17.
 *
 */
public abstract class AbstractServerListener extends Listener implements Runnable{

    protected Race race;
    protected Integer clientId;
    protected boolean clientConnected = true;
    protected final Integer CRC_LENGTH = 4;
    protected static final int SYNC_BYTE_1 = 71;
    protected static final int SYNC_BYTE_2 = 131;


    /**
     * The main run method of the serverListener. Continuously loops listening on a socket and then decoding it
     * and calling the needed method. Deals with client registration
     */
    @Override
    public abstract void run();

    /**
     * parses body of the a registration request message by extracting request type and notifying
     * @param body the body of a RegistrationRequest message
     */
    protected void parseRegistrationRequestMessage(byte[] body) {
        byte registrationByte = body[REGISTRATION_REQUEST_TYPE.getStartIndex()];
        if (registrationByte != REQUEST_RUNNING_GAMES.value()){
            System.out.println("Server: Received Registration Request");
        }
        setChanged();
        notifyObservers(RegistrationType.getTypeFromByte(registrationByte));
    }

    /**
     * parses body of the boat action message that is incoming from the client.
     * @param body currently a single number that corresponds to a control from the client
     */
    protected void parseBoatActionMessage(byte[] body){
        int sourceId = byteArrayRangeToInt(body, BOAT_ACTION_SOURCE_ID.getStartIndex(), BOAT_ACTION_SOURCE_ID.getEndIndex());
        if(sourceId != clientId){
            System.out.printf("Incorrect Client Id Received: Expected: %d Actual: %d\n", clientId, sourceId);
            return;
        }
        int action = byteArrayRangeToInt(body, BOAT_ACTION_BODY.getStartIndex(), BOAT_ACTION_BODY.getEndIndex());
        Boat boat = race.getBoatById(sourceId);
        PolarTable polarTable = new PolarTable(PolarReader.getPolarsForAC35Yachts(), race.getCourse());
        BoatAction boatAction = BoatAction.getBoatActionFromInt(action);
        switch (boatAction){
            case BOAT_VMG:
                boat.VMG(race.getCourse(), polarTable);
                break;
            case SAILS_IN:
                boat.changeSails();
                boat.setSailsNeedUpdate(true);
                break;
            case SAILS_OUT:
                boat.changeSails();
                boat.setSailsNeedUpdate(true);
                break;
            case TACK_GYBE:
                boat.tackOrGybe(race.getCourse(), polarTable);
                break;
            case UPWIND:
                boat.upWind(race.getCourse().getWindDirection());
                break;
            case DOWNWIND:
                boat.downWind(race.getCourse().getWindDirection());
                break;
            case CLOCKWISE:
                boat.clockwise();
                break;
            case ANTI_CLOCKWISE:
                boat.antiClockwise();
                break;
            default:
                break;
        }
    }

    @Override
    public Race getRace() {
        return null;
    }

    @Override
    public void disconnectClient() {
    }

    public void setRace(Race race) {
        this.race = race;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    /**
     * Creates a server listener based on what type of connection it is. If the first two bytes meets the expected
     * sync bytes of the AC35 protocol, then a normal socket is assumed, otherwise assumes it is a WebSocket and
     * the corresponding server listener is created.
     * @param socket The connection socket
     * @return The corresponding server listener for the socket type
     */
    public static AbstractServerListener createServerListener(Socket socket){
        BufferedInputStream socketData;
        try{
            socketData = new BufferedInputStream(socket.getInputStream());
            socketData.mark(10);
            int sync1 = socketData.read();
            int sync2 = socketData.read();
            socketData.reset();
            if (sync1 != SYNC_BYTE_1 || sync2 != SYNC_BYTE_2) {
                return new WebSocketServerListener(socket, socketData);
            } else {
                return new ServerListener(socket, socketData);
            }
        } catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
