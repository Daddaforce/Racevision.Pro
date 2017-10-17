package seng302.controllers;

import seng302.controllers.listeners.Listener;
import seng302.data.*;
import seng302.data.registration.RegistrationResponse;
import seng302.data.registration.ServerFullException;
import seng302.data.registration.*;
import seng302.utilities.NoConnectionToServerException;
import seng302.utilities.TimeUtils;

import java.util.Observer;

/**
 * Created by mjt169 on 18/07/17.
 *
 */
public abstract class Client implements Runnable, Observer {


    private int MAX_CONNECTION_ATTEMPTS = 200;
    private double CONNECTION_TIMEOUT = TimeUtils.secondsToMilliseconds(10.0);
    private int WAIT_MILLISECONDS = 10;

    protected ClientPacketBuilder packetBuilder;
    protected ClientSender sender;
    protected Listener clientListener;
    Thread dataStreamReaderThread;
    protected RegistrationResponse serverRegistrationResponse;
    protected int clientID;

    /**
     * Waits for the server to accept the socket connection
     * @throws NoConnectionToServerException if we timeout whilst waiting for the connection
     */
    protected void manageWaitingConnection() throws NoConnectionToServerException {
        int connectionAttempts = 0;
        while(clientListener.getSocket() == null) {
            if(clientListener.isHasConnectionFailed()){
                throw new NoConnectionToServerException(true, "Connection Failed.");
            }else if(connectionAttempts < MAX_CONNECTION_ATTEMPTS){
                try {
                    Thread.sleep(WAIT_MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                connectionAttempts++;
            } else {
                stopDataStreamReader();
                System.out.println("Client: Server not found \uD83D\uDD25 \uD83D\uDE2B");
                throw new NoConnectionToServerException(false, "Maximum connection attempts exceeded while trying to connect to server. Port or IP may not be valid.");
            }
        }
    }

    /**
     * Waits for a RegistrationResponse to be received from the server and acts upon that response
     * @throws ServerFullException if the Response from the server was received, but the server was full
     * @throws NoConnectionToServerException if we timeout whilst waiting for the response
     */
    protected void manageServerResponse() throws ServerRegistrationException, NoConnectionToServerException {
        double waitTime = 0;
        while (serverRegistrationResponse == null) {
            try {
                Thread.sleep(WAIT_MILLISECONDS);
                waitTime += WAIT_MILLISECONDS;
                if (waitTime > CONNECTION_TIMEOUT) {
                    throw new NoConnectionToServerException(false, "Connection to server timed out while waiting for registration response.");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        switch(serverRegistrationResponse.getStatus()) {
            case PLAYER_SUCCESS:
            case SPECTATOR_SUCCESS:
                this.clientID = serverRegistrationResponse.getSourceId();
                break;
            case OUT_OF_SLOTS:
                throw new ServerFullException();
            case RACE_UNAVAILABLE:
                throw new RaceUnavailableException();
            case GENERAL_FAILURE:
            case GHOST_SUCCESS:
            case TUTORIAL_SUCCESS:
                System.out.println("Client: Server response not understood.");
        }
    }

    abstract protected void setUpDataStreamReader(String serverAddress, int serverPort);

    protected void stopDataStreamReader(){
        if(dataStreamReaderThread != null){
            dataStreamReaderThread.stop();
            this.clientListener = null;
        }
    }

    public ClientSender getSender() {
        return sender;
    }


}
