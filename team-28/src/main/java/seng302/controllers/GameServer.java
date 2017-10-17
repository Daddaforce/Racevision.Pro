package seng302.controllers;

import javafx.scene.paint.Color;
import seng302.controllers.listeners.AbstractServerListener;
import seng302.controllers.listeners.ServerListener;
import seng302.controllers.listeners.WebSocketServerListener;
import seng302.data.*;
import seng302.data.registration.RegistrationResponseStatus;
import seng302.data.registration.RegistrationType;
import seng302.models.*;
import seng302.utilities.ConnectionUtils;
import seng302.utilities.DisplayUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import static seng302.data.AC35StreamXMLMessage.BOAT_XML_MESSAGE;
import static seng302.data.AC35StreamXMLMessage.RACE_XML_MESSAGE;
import static seng302.data.AC35StreamXMLMessage.REGATTA_XML_MESSAGE;
import static seng302.data.registration.RegistrationResponseStatus.OUT_OF_SLOTS;
import static seng302.data.registration.RegistrationResponseStatus.RACE_UNAVAILABLE;
import static seng302.data.registration.RegistrationResponseStatus.SPECTATOR_SUCCESS;

/**
 * Created by dda40 on 11/09/17.
 *
 */
public class GameServer implements Runnable, Observer {
    private final double SECONDS_PER_UPDATE = 0.2;
    private final int MAX_SPECTATORS = 100; //mostly because our boats sourceIDs start at 101

    private Map<AC35StreamXMLMessage, Integer> xmlSequenceNumber = new HashMap<>();
    private Map<Boat, Integer> boatSequenceNumbers = new HashMap<>();
    private Map<Boat, Integer> lastMarkRoundingSent = new HashMap<>();
    private int nextViewerID = 0;
    private ConnectionManager connectionManager;
    private ServerPacketBuilder packetBuilder;
    private ServerOptions options;
    private RaceUpdater raceUpdater;
    private Thread raceUpdaterThread;
    private CollisionManager collisionManager;
    private ClientSender gameRecorderConnection;
    private Integer roomCode;
    private Socket gameRecorderSocket;
    private double speedScale = 15;

    public GameServer(ServerOptions options) throws IOException {
        this.options = options;
        packetBuilder = new ServerPacketBuilder();
        connectionManager = new ConnectionManager(options.getPort(), true);
        connectionManager.addObserver(this);
        if (options.isOnline()) {
            connectToGameRecorder();
        }
        if(options.getSpeedScale() > speedScale){
            speedScale = options.getSpeedScale();
        }
        setupNewRaceUpdater(options);
        createPacketForGameRecorder();
        if(options.isPartyMode()){
            waitForRoomCode(gameRecorderSocket);
        }
    }


    /**
     * Attempts to connect to the Game Recorder server as defined in ConnectionUtils.
     * If there's no connection to the Game Recorder, catches the Exception and leaves gameRecorderConnection as null
     */
    private void connectToGameRecorder() {
        gameRecorderSocket = null;
        try {
            gameRecorderSocket = new Socket();
            gameRecorderSocket.connect(new InetSocketAddress(ConnectionUtils.getGameRecorderIP(), ConnectionUtils.getGameRecorderPort()), 1000);
            gameRecorderConnection = new ClientSender(gameRecorderSocket);
        } catch (ConnectException e) {
            System.err.println("Game Server cannot connect to Game Recorder.");
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void waitForRoomCode(Socket gameRecorderSocket) {
        ServerListener gameRecorderListener = null;
        try {
            gameRecorderListener = new ServerListener(gameRecorderSocket, new BufferedInputStream(gameRecorderSocket.getInputStream()));
            gameRecorderListener.addObserver(this);
            gameRecorderListener.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes a new raceUpdater with settings provided in options
     * @param options for the race
     */
    private void setupNewRaceUpdater(ServerOptions options) {
        raceUpdater = new RaceUpdater(options.getRaceXML());
        if(options.isTutorial()) raceUpdater.skipPrerace();
        raceUpdater.setScaleFactor(options.getSpeedScale());
        if(options.getAIDifficulty() != AIDifficulty.NO_AI) raceUpdater.addAICompetitor(options.getAIDifficulty());

        raceUpdaterThread = new Thread(raceUpdater);
        raceUpdaterThread.setName("Race Updater");
        collisionManager = raceUpdater.getCollisionManager();
    }

    /**
     * Initializes the sequence numbers for the boats and xml messages
     * @throws IOException throws this
     * @throws NullPointerException also throws this
     */
    private void initialize() throws IOException, NullPointerException  {
        xmlSequenceNumber.put(REGATTA_XML_MESSAGE, 0);
        xmlSequenceNumber.put(RACE_XML_MESSAGE, 0);
        xmlSequenceNumber.put(BOAT_XML_MESSAGE, 0);
        for (Boat boat: raceUpdater.getRace().getCompetitors()){
            boatSequenceNumbers.put(boat, boat.getId());
            lastMarkRoundingSent.put(boat, -1);
        }
    }

    /**
     * Sends all the data to the socket while the boats have not all finished.
     */
    @Override
    public void run() throws NullPointerException {
        Integer timesRun = 0;
        while (options.alwaysRerun() || timesRun < options.getNumRacesToRun()) {
            setupNewRaceUpdater(options);
            System.out.println("Server: Ready to Run New Race");
            try {
                initialize();
                sendInitialRaceMessages();
                Thread managerThread = new Thread(connectionManager);
                managerThread.setName("Connection Manager");
                managerThread.start();
                while (!raceUpdater.raceHasEnded()) {
                    if (!raceUpdater.getRace().getCompetitors().isEmpty() || isPartyMode()) {
                        sendRaceUpdates();
                        if (isPartyMode()) sendWebClientUpdates();
                    }
                    Thread.sleep((long) (SECONDS_PER_UPDATE * 1000 / speedScale));
                }
                sendRaceUpdates(); //send one last message block with ending data
                connectionManager.closeClientConnections();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            timesRun++;
        }
        System.out.println("Server: Shutting Down");
        initiateServerDisconnect();
    }

    private void sendWebClientUpdates() {
        for (Boat boat : raceUpdater.getRace().getCompetitors()) {
            int totalCompetitors = raceUpdater.getRace().getCompetitors().size();
            byte[] packet = packetBuilder.createWebClientUpdatePacket(boat.getId(), boat.getCurrentSpeed(), boat.getCurrPlacing(), totalCompetitors, (int) boat.getBoatHealth());
            connectionManager.sendToClient(boat.getId(), packet);
        }
    }

    private boolean isPartyMode() {
        return options.isPartyMode();
    }

    /**
     * Exit out of run
     */
    public void stop(){
        raceUpdater.getRace().updateRaceStatus(RaceStatus.TERMINATED);
    }

    /**
     * Sends the XML messages when the client has connected
     */
    private void sendInitialRaceMessages() {
        sendXmlMessage(RACE_XML_MESSAGE, options.getRaceXML());
        sendXmlMessage(BOAT_XML_MESSAGE, "Boat.xml");
        sendXmlMessage(REGATTA_XML_MESSAGE, "Regatta.xml");
    }

    private void sendAllBoatStates() {
        for(Boat boat : raceUpdater.getRace().getCompetitors()){
            sendBoatStateMessage(boat);
        }
    }

    private void sendBoatStateMessage(Boat boat) {
        sendPacketToNonWebClients(packetBuilder.createBoatStateMessagePacket(boat));
    }

    /**
     * Sends Race Status, Boat Location and Mark Rounding messages that are currently necessary
     * @throws IOException
     */
    private void sendRaceUpdates() throws IOException {
        try {
            byte[] raceUpdateMessage = packetBuilder.createRaceUpdateMessage(raceUpdater.getRace());
            sendPacketToNonWebClients(raceUpdateMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendBoatMessagesForAllBoats();
        sendYachtEventMessages();
    }

    /**
     * send a yacht event message to the server
     * @throws IOException throws this because of sending data
     */
    private void sendYachtEventMessages() throws IOException {
        for (Collision collision : collisionManager.getCollisions()) {
            for (Integer boatId : collision.getInvolvedBoats()) {
                Boat boat = raceUpdater.getRace().getBoatById(boatId);
                sendYachtEventMessage(boat, raceUpdater.getRace(), collision.getIncidentId(), YachtEventCode.COLLISION);
                if (collision.isOutOfBounds()){
                    sendYachtEventMessage(boat, raceUpdater.getRace(), collision.getIncidentId(), YachtEventCode.OUT_OF_BOUNDS);
                }else{
                    if (collision.boatIsAtFault(boatId)) {
                        sendYachtEventMessage(boat, raceUpdater.getRace(), collision.getIncidentId(), YachtEventCode.COLLISION_PENALTY);
                    }
                    if(collision.getInvolvedBoats().size() == 1) {
                        sendYachtEventMessage(boat, raceUpdater.getRace(), collision.getIncidentId(), YachtEventCode.COLLISION_MARK);
                    }
                }
                sendBoatStateMessage(boat);
            }
            collisionManager.removeCollision(collision);
        }
    }

    private void sendBoatMessagesForAllBoats() throws IOException {
        for (Boat boat : raceUpdater.getRace().getCompetitors()) {
            sendBoatMessages(boat);
        }
    }

    /**
     * Sends a Boat Location Message and possible a Mark Rounding message for a boat
     * @param boat the boat to send messages for
     * @throws IOException throws this because of sending packet
     */
    private void sendBoatMessages(Boat boat) throws IOException {
        Integer currentSequenceNumber = boatSequenceNumbers.get(boat);
        if (currentSequenceNumber != null) {
            boatSequenceNumbers.put(boat, currentSequenceNumber + 1);
            sendPacketToNonWebClients(packetBuilder.createBoatLocationMessage(boat, raceUpdater.getRace(), currentSequenceNumber));
            if (lastMarkRoundingSent.get(boat) != boat.getLastRoundedMarkIndex()) {
                raceUpdater.getRace().updateRaceOrder();
                lastMarkRoundingSent.put(boat, boat.getLastRoundedMarkIndex());
                sendPacketToNonWebClients(packetBuilder.createMarkRoundingMessage(boat, raceUpdater.getRace()));
            }
        }
    }

    /**
     * sends a Yacht Event message
     * @param boat a boat
     * @param race a race
     * @throws IOException needed for sending a packet that fails
     */
    private void sendYachtEventMessage(Boat boat, Race race, int incidentID, YachtEventCode eventCode) throws IOException {
        sendPacketToNonWebClients(packetBuilder.createYachtEventMessage(boat, race, incidentID, eventCode));
    }

    /**
     * Sends an xml message type to the socket including the header, body and CRC
     * @param type subtype of the xml message
     * @param fileName name of the file to send
     */
    private void sendXmlMessage(AC35StreamXMLMessage type, String fileName) {
        int sequenceNo = xmlSequenceNumber.get(type) + 1;
        xmlSequenceNumber.put(type, sequenceNo);
        byte[] packet = packetBuilder.buildXmlMessage(type, fileName, sequenceNo, raceUpdater.getRace(), options.getRaceXML());
        connectionManager.setXmlMessage(type, packet);
    }


    /**
     * Adds a competing client to the race model and sends new xml messages out to all clients
     * @param serverListener the listener for the client
     */
    private void addClientToRace(AbstractServerListener serverListener){
        int numCompetitors = raceUpdater.getRace().getCompetitors().size();
        int newId = -1;
        if (anotherCompetitorAllowed(numCompetitors)) {
            newId = raceUpdater.addCompetitor();
        }
        boolean success = newId != -1;
        byte[] packet;
        if (success) {
            numCompetitors++;
            Boat boat = raceUpdater.getRace().getBoatById(newId);
            boatSequenceNumbers.put(boat, newId);
            lastMarkRoundingSent.put(boat, -1);
            packet = packetBuilder.createRegistrationResponsePacket(newId, RegistrationResponseStatus.PLAYER_SUCCESS);
            if (!raceUpdaterThread.isAlive()){
                if (numCompetitors >= options.getMinParticipants()) {
                    raceUpdaterThread.start();
                }
            }
        } else {
            packet = packetBuilder.createRegistrationResponsePacket(newId, RegistrationResponseStatus.OUT_OF_SLOTS);
        }
        connectionManager.addConnection(newId, serverListener);
        serverListener.setClientId(newId);
        connectionManager.sendToClient(newId, packet);
        if(success){
            if (options.isPartyMode()) {
                Boat boat = raceUpdater.getRace().getBoatById(newId);
                Color color = DisplayUtils.getBoatColor(newId);
                String boatName = boat.getName()+ " (" + boat.getNickName() + ")";
                byte[] initPacket = packetBuilder.createWebClientInitPacket(newId, boatName, color);
                connectionManager.sendToClient(newId, initPacket);
            }
            createPacketForGameRecorder();
            sendXmlMessage(RACE_XML_MESSAGE, options.getRaceXML());
            sendAllBoatStates();
        }
    }

    /**
     * Checks whether the maximum number of competitors are in the race to determine
     * whether another competitor should be allowed.
     * @param numCompetitors the number already in the race
     * @return true if another competitor can be added, false otherwise
     */
    private Boolean anotherCompetitorAllowed(int numCompetitors) {
        int competitorsAllowed = 1;
        if (options.isOnline()) {
            return true;
        }
        if (!options.getAIDifficulty().equals(AIDifficulty.NO_AI)) {
            competitorsAllowed++;
        }
        return numCompetitors < competitorsAllowed;
    }

    private void createPacketForGameRecorder(){
        if (gameRecorderConnection != null) {
            updateGameRecorder(options, ConnectionUtils.getPublicIp(), CourseName.getCourseIntFromName(raceUpdater.getRace().getRegattaName()));
        }
    }

    /**
     * sends a packet to the Vm server notifying it that a game is being hosted
     * @param options options for the hosted game
     * @param publicIp public ip of the hosted game
     * @param currentCourseIndex index of the current course running on the game
     * @throws IOException needed for sending to the vm
     */
    private void updateGameRecorder(ServerOptions options, String publicIp, int currentCourseIndex) {
        byte[] registerGamePacket = this.packetBuilder.createGameRegistrationPacket(options.getSpeedScale(), options.getMinParticipants(),
                options.getPort(), publicIp, currentCourseIndex, raceUpdater.getRace().getCompetitors().size(), options.isPartyMode());
        System.out.println("GameServer: Send updates to Game Recorder" );
        gameRecorderConnection.sendToServer(registerGamePacket);
    }

    /**
     * Method that gets called when Server is notified as an observer
     * If the observable is a ConnectionManager then a new client has connected and a server listener
     * is started for the client
     * If the observable is a AbstractServerListener then a registration message is received
     * @param observable The observable either a ConnectionManager or AbstractServerListener
     * @param arg A Socket or Boat ID if observable is a ConnectionManager else it is the registration type of the client
     */
    @Override
    public void update(Observable observable, Object arg) {
        if (observable.equals(connectionManager)) {
            if(arg instanceof Socket){
                try {
                    AbstractServerListener serverListener = ServerListener.createServerListener((Socket) arg);
                    startServerListener(serverListener);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                setBoatToDNF((int) arg);
                createPacketForGameRecorder();
            }
        } else if(observable instanceof AbstractServerListener){
            if(arg instanceof RegistrationType){
                System.out.println("Server: Adding player to game");
                manageRegistration((AbstractServerListener) observable, (RegistrationType) arg);
            } else if(arg instanceof Integer){
                roomCode = (Integer) arg;
            }
        }
    }

    /**
     * Starts a new server listener on new thread for which listens to a client
     * @param serverListener the serverListener for the client socket
     */
    private void startServerListener(AbstractServerListener serverListener) throws IOException {
        serverListener.setRace(raceUpdater.getRace());
        Thread serverListenerThread = new Thread(serverListener);
        serverListenerThread.setName("Server Listener");
        serverListenerThread.start();
        serverListener.addObserver(this);
    }

    /**
     * Deal with the different types of registrations clients can attempt to connect with
     * Currently ignores GHOST or TUTORIAL connection attempts.
     * @param serverListener the serverListener with the clients socket
     * @param registrationType the type of registration being attempted
     */
    private void manageRegistration(AbstractServerListener serverListener, RegistrationType registrationType) {
        switch (registrationType) {
            case PLAYER:
                addClientToRace(serverListener);
                break;
            case SPECTATOR:
                addSpectatorToRace(serverListener);
                break;
            case GHOST:
                System.out.println("Server: Client attempted to connect as ghost, ignoring.");
                break;
            case TUTORIAL:
                System.out.println("Server: Client attempted to connect as control tutorial, ignoring.");
                break;
        }
    }

    /** Responds to a Spectator requesting to join
     * Can fail due to the race not being started, otherwise we let them join
     * @param serverListener
     */
    private void addSpectatorToRace(AbstractServerListener serverListener) {
        RegistrationResponseStatus response = SPECTATOR_SUCCESS;
        if(!options.isPartyMode()){
            if (!raceUpdaterThread.isAlive() || !options.isMultiplayer()) {
                response = RACE_UNAVAILABLE;
            } else if (nextViewerID >= MAX_SPECTATORS) {
                response = OUT_OF_SLOTS;
            }
        }
        byte[] packet = packetBuilder.createRegistrationResponsePacket(0, response);
        connectionManager.addConnection(nextViewerID, serverListener);
        connectionManager.sendToClient(nextViewerID, packet);
        if (response.equals(SPECTATOR_SUCCESS)) {
            sendAllBoatStates();
            nextViewerID++;
        } else {
            connectionManager.removeConnection(nextViewerID);
        }
        if (options.isPartyMode()){
            byte[] roomCodePacket = packetBuilder.createPartyModeRoomCodeMessage(roomCode);
            connectionManager.sendToClients(roomCodePacket);
        }
    }

    public void initiateServerDisconnect() {
        byte[] gameClosePacket = packetBuilder.createGameCancelPacket(options.getPort());
        if (gameRecorderConnection != null) {
            gameRecorderConnection.sendToServer(gameClosePacket);
            gameRecorderConnection.closeConnection();
        }
        connectionManager.closeAllConnections();
        raceUpdater.stopRunning();
    }

    private void setBoatToDNF(int arg){
        for(Boat boat : raceUpdater.getRace().getCompetitors()){
            if(boat.getId().equals(arg)){
                if(!boat.getStatus().equals(BoatStatus.FINISHED)){
                    boat.setStatus(BoatStatus.DNF);
                    boat.changeSails();
                }
            }
        }
    }

    /**
     * Sends a packet to all non-web clients
     * @param packet the packet to send
     */
    private void sendPacketToNonWebClients(byte[] packet) {
        connectionManager.sendToClients(packet);
    }
}
