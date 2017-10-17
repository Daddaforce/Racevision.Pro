package seng302.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seng302.controllers.listeners.MainMenuClientListener;
import seng302.data.*;
import seng302.data.registration.RegistrationType;
import seng302.data.registration.ServerFullException;
import seng302.utilities.ConnectionUtils;
import seng302.utilities.NoConnectionToServerException;
import seng302.views.AvailableRace;

import java.io.IOException;
import java.util.*;

/**
 * Created by lga50 on 7/09/17.
 *
 */
public class MainMenuClient extends Client {
    private ObservableList<AvailableRace> availableRaces = FXCollections.observableArrayList();
    private ArrayList<AvailableRace> receivedRaces = new ArrayList<>();
    private Boolean pollForRaces;

    public MainMenuClient() throws ServerFullException, NoConnectionToServerException {
        this.packetBuilder = new ClientPacketBuilder();
        setUpDataStreamReader(ConnectionUtils.getGameRecorderIP(), ConnectionUtils.getGameRecorderPort());
        manageWaitingConnection();
        pollForRaces = true;
    }

    @Override
    public void run(){
        this.sender = new ClientSender(clientListener.getSocket());
        while(pollForRaces){
            try {
                checkForRaces();
                Thread.sleep(1000);
                updateRaces();
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            clientListener.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o instanceof MainMenuClientListener){
            if(arg instanceof AvailableRace){
                receivedRaces.add((AvailableRace) arg);
            }
        }
    }

    /**
     * hands the updating of the available races array, removes
     * races that no longer exist, keeps races that are still running
     */
    private void updateRaces(){
        for(AvailableRace newRace : receivedRaces) {
            Boolean updatedRace = false;
            for (AvailableRace oldRace : availableRaces) {
                if (oldRace.equals(newRace)) {
                    updatedRace = true;
                    oldRace.setNumBoats(newRace.getNumBoats());
                }
            }
            if (!updatedRace) {
                availableRaces.add(newRace);
            }
        }
        removeDeadRaces();
    }

    /**
     * Clears any races that were not in the last receivedRaces list
     * from the availableRaces list
     */
    private void removeDeadRaces() {
        Iterator<AvailableRace> iter = availableRaces.iterator();
        while (iter.hasNext()) {
            AvailableRace race = iter.next();
            boolean exists = false;
            for(AvailableRace newRace : receivedRaces){
                if (newRace.equals(race) && newRace.getNumBoats() == race.getNumBoats()){
                    exists = true;
                }
            }
            if(!exists){
                iter.remove();
            }
        }
    }

    public ObservableList<AvailableRace> getAvailableRaces() {
        return availableRaces;
    }

    /**
     * queries the known VM address for any pollForRaces games
     */
    private void checkForRaces(){
        receivedRaces.clear();
        RegistrationType regoType = RegistrationType.REQUEST_RUNNING_GAMES;
        sender.sendToServer(this.packetBuilder.createRegistrationRequestPacket(regoType));
    }

    @Override
    protected void setUpDataStreamReader(String serverAddress, int serverPort) {
        this.clientListener = new MainMenuClientListener(serverAddress, serverPort);
        dataStreamReaderThread = new Thread(clientListener);
        dataStreamReaderThread.setName("ClientListener");
        dataStreamReaderThread.start();
        clientListener.addObserver(this);
    }

    public void stopPolling(){
        pollForRaces = false;
    }
}
