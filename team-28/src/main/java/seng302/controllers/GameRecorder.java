package seng302.controllers;

import seng302.controllers.listeners.AbstractServerListener;
import seng302.controllers.listeners.ServerListener;
import seng302.data.ConnectionManager;
import seng302.data.CourseName;
import seng302.data.ServerPacketBuilder;
import seng302.data.registration.RegistrationType;
import seng302.utilities.ConnectionUtils;
import seng302.utilities.MathUtils;
import seng302.views.AvailableRace;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

import static seng302.data.registration.RegistrationType.REQUEST_RUNNING_GAMES;

import static seng302.data.AC35StreamField.HOST_GAME_CURRENT_PLAYERS;

/**
 * Created by dda40 on 11/09/17.
 *
 */
public class GameRecorder implements Observer {

    private final ServerPacketBuilder packetBuilder;
    private final ConnectionManager connectionManager;
    private ArrayList<AvailableRace> availableRaces = new ArrayList<>();
    private int nextHostID = 0;
    private Thread serverListenerThread = null;
    private HashMap<Integer, AvailableRace> availablePartyGames = new HashMap<>();

    public GameRecorder() throws IOException {
        packetBuilder = new ServerPacketBuilder();
        connectionManager = new ConnectionManager(ConnectionUtils.getGameRecorderPort(), false);
        connectionManager.addObserver(this);
        System.out.println("Server: Waiting for races");
        Thread managerThread = new Thread(connectionManager);
        managerThread.setName("Connection Manager");
        managerThread.start();
    }

    @Override
    public void update(Observable observable, Object arg) {
        if (observable.equals(connectionManager)) {
            if (arg instanceof Socket) {
                Socket socket = (Socket) arg;
                try {
                    AbstractServerListener serverListener = ServerListener.createServerListener(socket);
                    startServerListener(serverListener);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } else if(observable instanceof AbstractServerListener){
            System.out.println("received update");
            AbstractServerListener serverListener = (AbstractServerListener)observable;

            if (arg instanceof RegistrationType) {
                if (arg.equals(REQUEST_RUNNING_GAMES)) {
                    respondToRequestForGames(serverListener.getSocket());
                }
            } else if (arg instanceof AvailableRace) {
                updateAvailableRace(((AvailableRace) arg), serverListener.getSocket());
            } else if(arg instanceof Integer){
                Integer roomCode = (Integer) arg;
                if(availablePartyGames.containsKey(roomCode)){
                    respondToRequestPartyGame(availablePartyGames.get(roomCode), serverListener.getSocket());
                } else{
                    sendIncorrectRoomCodeResponse(serverListener.getSocket());
                }
            }
        }
    }

    private void sendIncorrectRoomCodeResponse(Socket socket) {
        byte[] packet =  packetBuilder.createGameRegistrationPacket(0d, 0, 0,  "0.0.0.0", 0, 0, false);
        packet = packetBuilder.wrapPacket(packet);
        connectionManager.sendToSocket(socket, packet);
    }

    private void respondToRequestPartyGame(AvailableRace availableRace, Socket socket) {
        //-1 for unknown/unused values
        byte[] packet =  packetBuilder.createGameRegistrationPacket(-1.0, 0, availableRace.getPort(),
                availableRace.getIpAddress(), -1, availableRace.getNumBoats(), availableRace.isPartyGame());
        packet = packetBuilder.wrapPacket(packet);
        connectionManager.sendToSocket(socket, packet);
    }

    /**
     * Runs through the entire list of available races, updating the ones that have had changes
     * @param newRace the new available race
     * @param socket the socket to respond to
     */
    private void updateAvailableRace(AvailableRace newRace, Socket socket){
        if (newRace.isDeleted()) {
            removeAvailableRace(newRace);
            return;
        }
        if (newRace.isPartyGame()) {
            updatePartyGame(newRace, socket);
        } else {
            updateNormalRace(newRace);
        }
    }

    /**
     * Adds or updates a party game to the availablePartyGames hashmap
     * @param newRace the race to add or update
     * @param socket the socket to send the response to
     */
    private void updatePartyGame(AvailableRace newRace, Socket socket) {
        boolean updatedRace = updateRaceIfExists(newRace, availablePartyGames.values());
        if (!updatedRace) {
            Integer code = MathUtils.generateFourDigitPartyCode();
            while (availablePartyGames.keySet().contains(code)) {
                code = MathUtils.generateFourDigitPartyCode();
            }
            newRace.setCode(code);
            availablePartyGames.put(code, newRace);
            sendRoomCodeResponse(socket, code);
        }
    }

    private void sendRoomCodeResponse(Socket socket, Integer code) {
        byte[] packet = packetBuilder.createPartyModeRoomCodeMessage(code);
        connectionManager.sendToSocket(socket, packet);
    }

    /**
     * Iterate through the provided available races and determine if it matches the given race.
     * Update if we find a match.
     * @param newRace the race to match
     * @param availableRaces the Collection to search through
     * @return true if we updated a race, false otherwise
     */
    private boolean updateRaceIfExists(AvailableRace newRace, Collection<AvailableRace> availableRaces) {
        boolean updatedRace = false;
        for (AvailableRace runningRace : availableRaces){
            if (runningRace.equals(newRace)){
                System.out.println("Game Recorder: Updating running race");
                updatedRace = true;
                updateNumberOfBoats(runningRace, newRace.getNumBoats());
            }
        }
        return updatedRace;
    }

    /**
     * Adds or updates a normal game to the availableRaces list
     * @param newRace the race to add or update
     */
    private void updateNormalRace(AvailableRace newRace) {
        boolean updatedRace = updateRaceIfExists(newRace, availableRaces);
        int raceMapIndex = CourseName.getCourseIntFromName(newRace.mapNameProperty().getValue());
        if (!updatedRace && raceMapIndex != -1) {
            System.out.println("Game Recorder: Recording new server");
            updateNumberOfBoats(newRace, newRace.getNumBoats());
            availableRaces.add(newRace);
        }
    }

    /**
     * changes the number of boats in a known race to be stored on the vm
     * @param race the new race
     * @param numBoats the new number of boats in the race
     */
    private void updateNumberOfBoats(AvailableRace race, int numBoats){
        byte[] packet = race.getPacket();
        packet[HOST_GAME_CURRENT_PLAYERS.getStartIndex()] = (byte) numBoats;
    }

    /**
     * removes a race with a specific IP address
     * @param deletedRace dummy race with matching IP and port to remove
     */
    private void removeAvailableRace(AvailableRace deletedRace){
        Boolean raceDeleted = false;
        for (AvailableRace race : availableRaces) {
            if (race.equals(deletedRace)) {
                raceDeleted = true;
                availableRaces.remove(race);
                System.out.println("Game Recorder: removed canceled race: " + race.getIpAddress());
                break;
            }
        }
        if (!raceDeleted) {
            for (AvailableRace race : availablePartyGames.values()) {
                if (race.equals(deletedRace)) {
                    availablePartyGames.remove(race.getCode());
                    System.out.println("Game Recorder: removed canceled party game race: " + race.getIpAddress());
                    break;
                }
            }
        }
    }

    /**
     * Respond to a request for running games
     * @param socket the clients socket
     */
    private void respondToRequestForGames(Socket socket) {
        for(AvailableRace race : availableRaces) {
            byte[] racePacket = packetBuilder.createGameRegistrationPacket(race.getPacket());
            connectionManager.sendToSocket(socket, racePacket);
        }
    }

    /**
     * Starts a new server listener on new thread for which listens to a client
     * @param serverListener the serverListener for the client socket
     */
    protected void startServerListener(AbstractServerListener serverListener) throws IOException {
        serverListenerThread = new Thread(serverListener);
        serverListenerThread.setName("Game Recorder Listener");
        serverListenerThread.start();
        serverListener.addObserver(this);
    }
}
