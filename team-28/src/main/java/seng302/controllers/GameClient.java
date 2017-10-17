package seng302.controllers;

import javafx.scene.input.KeyCode;
import seng302.controllers.listeners.ClientListener;
import seng302.data.*;
import seng302.data.registration.RegistrationResponse;
import seng302.data.registration.RegistrationType;
import seng302.data.registration.ServerRegistrationException;
import seng302.models.Boat;
import seng302.models.ClientOptions;
import seng302.models.Race;
import seng302.utilities.NoConnectionToServerException;

import java.util.*;

/**
 * Created by lga50 on 7/09/17.
 *
 */
public class GameClient extends Client{

    private static Race race;
    private Map<Integer, Boat> potentialCompetitors;
    private KeyInputController keyInputController;
    private TouchInputController touchInputController;
    private ClientOptions options;
    private static List<Integer> tutorialKeys = new ArrayList<Integer>();
    private static Runnable tutorialFunction = null;
    private static Integer roomCode = 1111;


    public GameClient(ClientOptions options) throws NoConnectionToServerException, ServerRegistrationException {
        this.packetBuilder = new ClientPacketBuilder();
        this.options = options;
        options.isHost();
        setUpDataStreamReader(options.getServerAddress(), options.getServerPort());
        System.out.println("Client: Waiting for connection to Server");
        manageWaitingConnection();
        RegistrationType regoType = options.isParticipant() ? RegistrationType.PLAYER : RegistrationType.SPECTATOR;
        System.out.println("Client: Connected to Server");
        this.sender = new ClientSender(clientListener.getSocket());
        sender.sendToServer(this.packetBuilder.createRegistrationRequestPacket(regoType));
        System.out.println("Client: Sent Registration Request");
        manageServerResponse();
    }

    @Override
    public void run() {
        System.out.println("Client: Successfully Joined Game");
        waitForRace();
    }

    /**
     * Waits for the race to be able to be read in
     */
    public void waitForRace(){
        while(clientListener.getRace() == null){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        race = clientListener.getRace();
    }

    /**
     * observing UserInputController and clientListener
     * @param o observable object
     */
    @Override
    public void update(Observable o, Object arg) {
        if (o == clientListener) {
            if (arg instanceof ArrayList) {
                ArrayList<Boat> boats = (ArrayList<Boat>) arg;
                potentialCompetitors = new HashMap<>();
                for (Boat boat : boats) {
                    potentialCompetitors.put(boat.getId(), boat);
                }
            } else if (arg instanceof Race) {
                Race newRace = (Race) arg;
                Race oldRace = clientListener.getRace();
                for (int newId : newRace.getCompetitorIds()) {
                    if (!oldRace.getCompetitorIds().contains(newId)) {
                        oldRace.addCompetitor(potentialCompetitors.get(newId));
                    }
                }
            } else if (arg instanceof RegistrationResponse) {
                serverRegistrationResponse = (RegistrationResponse) arg;
            } else if(arg instanceof Integer){
                GameClient.roomCode = (Integer) arg;
            }
        } else if (o == touchInputController) {
            sendBoatTouchCommandPacket();
        } else if (o == keyInputController){
            sendBoatKeyCommandPacket();
        }
    }

    /**
     * sends boat command packet to server. Sends keypress and runs tutorial callback function if required.
     */
    private void sendBoatKeyCommandPacket(){
        if(tutorialKeys.contains(keyInputController.getCommandInt())) {
            tutorialFunction.run();
        }

        if (race != null && !race.getRaceStatus().equals(RaceStatus.TERMINATED)) {
            byte[] boatCommandPacket = packetBuilder.createBoatCommandPacket(keyInputController.getCommandInt(), this.clientID);
            sender.sendToServer(boatCommandPacket);
        }
    }

    private void sendBoatTouchCommandPacket(){
        if(tutorialKeys.contains(touchInputController.getCommandInt())) {
            tutorialFunction.run();
        }
        byte[] boatCommandPacket = packetBuilder.createBoatCommandPacket(touchInputController.getCommandInt(), this.clientID);
        sender.sendToServer(boatCommandPacket);

    }

    public void setInputControllers(KeyInputController keyInputController, TouchInputController touchInputController) {
        this.keyInputController = keyInputController;
        this.touchInputController = touchInputController;
        touchInputController.setClientID(clientID);
        keyInputController.setClientID(clientID);
    }

    public TouchInputController getTouchInputController() {
        return touchInputController;
    }

    public static void setTutorialActions(List<KeyCode> keys, Runnable callbackFunction){
        for(KeyCode key : keys){
            tutorialKeys.add(BoatAction.getTypeFromKeyCode(key));
        }
        tutorialFunction = callbackFunction;
    }

    public static void clearTutorialAction(){
        tutorialKeys.clear();
        tutorialFunction = null;
    }

    public static Race getRace() {
        return race;
    }

    public int getClientID() {
        return clientID;
    }

    /**
     * notfies the server if the disconnect person is the host
     */
    public void initiateClientDisconnect() {
        clientListener.disconnectClient();
        if (options.isParticipant()) {
            race.getBoatById(clientID).setStatus(BoatStatus.DNF);
        }
    }

    @Override
    protected void setUpDataStreamReader(String serverAddress, int serverPort) {
        this.clientListener = new ClientListener(serverAddress, serverPort);
        dataStreamReaderThread = new Thread(clientListener);
        dataStreamReaderThread.setName("ClientListener");
        dataStreamReaderThread.start();
        clientListener.addObserver(this);
    }

    public ClientOptions getOptions() {
        return options;
    }

    public static Integer getRoomCode() {
        return roomCode;
    }
}
