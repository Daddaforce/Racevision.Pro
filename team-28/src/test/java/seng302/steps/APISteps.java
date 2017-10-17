package seng302.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import javafx.scene.input.KeyCode;
import seng302.controllers.listeners.AbstractServerListener;
import seng302.controllers.RaceUpdater;
import seng302.controllers.GameServer;
import seng302.controllers.listeners.ServerListener;
import seng302.data.*;
import seng302.models.Boat;
import seng302.models.PolarTable;
import seng302.models.Race;
import seng302.utilities.PolarReader;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by mjt169 on 21/07/17.
 */
public class APISteps {

    private GameServer server;
    private Boat sallysBoat;
    private Race race;
    private String selectedCourse = "AC35-course.xml";
    private Set<Socket> sockets = new HashSet<>();
    private Socket serverSocket;
    private static int port = 1234;
    private double VMGHeading;
    private double previousHeading;

    @Given("^Sally has a boat$")
    public void sallyHasABoat() throws Throwable {
        sallysBoat = new Boat(102, "Sally's Boat", "SB", 15.0);
        sallysBoat.setHeading(100.0);
        sallysBoat.setCurrentSpeed(15.0);
        RaceVisionXMLParser rc = new RaceVisionXMLParser();
        Race tempRace = rc.importRace();
        race = new Race("test", tempRace.getCourse(), Arrays.asList(sallysBoat));
        race.getCourse().setTrueWindSpeed(10);
        race.getCourse().setWindDirection(0);
        race.setId("1");
        race.updateRaceStatus(RaceStatus.STARTED);
        RaceVisionXMLParser raceVisionXMLParser = mock(RaceVisionXMLParser.class);
        when(raceVisionXMLParser.importRace()).thenReturn(race);
        RaceUpdater raceRunner = new RaceUpdater(selectedCourse);
        PolarTable polarTable = new PolarTable(PolarReader.getPolarsForAC35Yachts(), race.getCourse());

        VMGHeading = sallysBoat.getVMGHeading(race.getCourse(), polarTable);
        raceRunner.setStartingPosition(sallysBoat);
        sallysBoat.setHeading(100.0);

        raceRunner.setRace(race);
        Thread runnerThread = new Thread(raceRunner);
        runnerThread.setName("Race Updater");
        runnerThread.start();
        server = mock(GameServer.class);
        Thread serverThread = new Thread(server);
        serverThread.start();
    }

    @When("^Sally presses the \"([^\"]*)\" key$")
    public void sallyPressesTheKey(String key) throws Throwable {
        previousHeading = sallysBoat.getHeading();
        ClientPacketBuilder packetBuilder = new ClientPacketBuilder();
        serverSocket = mock(Socket.class);

        AbstractServerListener listener = new ServerListener(serverSocket, new BufferedInputStream(serverSocket.getInputStream()));
        listener.setClientId(102);
        listener.setRace(race);
        KeyCode keyCode = KeyCode.valueOf(key.toUpperCase());
        byte[] packet = packetBuilder.createBoatCommandPacket(BoatAction.getTypeFromKeyCode(keyCode), 102);
        when(serverSocket.getInputStream()).thenReturn(new ByteArrayInputStream(packet));
        Thread serverListenerThread = new Thread(listener);
        serverListenerThread.start();
    }

    @Then("^the heading of Sally's boat has been changed$")
    public void theHeadingOfSallySBoatHasBeenChanged() throws Throwable {
        Thread.sleep(100);
        assert(109.16 > 209.16 - sallysBoat.getHeading());
        tearDown();
    }

    @Then("^the sails should be brought in so that the speed becomes (\\d+)$")
    public void theSailsShouldBeBroughtInSoThatTheSpeedBecomes(int speed) throws Throwable {
        Thread.sleep(100);
        assert(sallysBoat.getCurrentSpeed() < 15);
        assert(sallysBoat.isSailsIn());
        tearDown();
    }

    @Then("^the boats heading should move towards the optimal angle$")
    public void theBoatsHeadingShouldMoveTowardsTheOptimalAngle() throws Throwable {
        Thread.sleep(100);
        double headingDifference = sallysBoat.getHeading() - VMGHeading;
        assert(Math.abs(headingDifference) < Math.abs(100 - VMGHeading));
    }

    @Then("^the boats heading should be increased$")
    public void theBoatsHeadingShouldBeIncreased() throws Throwable {
        Thread.sleep(100);
        assert(sallysBoat.getHeading() > previousHeading);
        tearDown();
    }

    @Then("^the boats heading should be decreased$")
    public void theBoatsHeadingShouldBeDecreased() throws Throwable {
        Thread.sleep(100);
        assert(sallysBoat.getHeading() < previousHeading);
        tearDown();
    }


    /**
     * Stop server and close sockets
     * @throws IOException
     */
    private void tearDown() throws IOException {
        server.stop();
        for (Socket socket : sockets) {
            socket.close();
        }
        serverSocket.close();
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }
}
