package seng302.controllers;

import javafx.animation.FadeTransition;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import seng302.data.BoatAction;
import seng302.models.Boat;
import seng302.models.PolarTable;
import seng302.models.Race;
import seng302.utilities.PolarReader;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

import static javafx.scene.input.KeyCode.*;

/**
 * handles user key presses.
 */
public class KeyInputController extends Observable {

    private Scene scene;
    private int commandInt;
    private int clientID;
    private Race race;
    private Boat userBoat;
    private Controller controller;
    private PolarTable polarTable;
    private final Set<KeyCode> cosumedKeyCodes = new HashSet<>(Arrays.asList(KeyCode.SPACE, KeyCode.UP, KeyCode.DOWN));

    /**
     * Sets up user key press handler.
     * @param scene The scene of the client
     */
    public KeyInputController(Scene scene, Race race, Boat boat) {
        this.scene = scene;
        this.race = race;
        this.userBoat = boat;
        this.polarTable = new PolarTable(PolarReader.getPolarsForAC35Yachts(), race.getCourse());
        keyEventListener();
    }

    private void keyEventListener() {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, key -> {
            checkKeyPressed(key.getCode());
            if ( cosumedKeyCodes.contains(key.getCode()) ){
                key.consume();
            }
        });
    }

    /**
     * recorded the key that has been pressed and notifies the observes of this key
     * @param key the key that has been pressed
     */
    private void checkKeyPressed(KeyCode key){
        commandInt = BoatAction.getTypeFromKeyCode(key);
        if (commandInt != -1) {
            if(key.equals(KeyCode.ENTER)){
                generateUserFeedback();
            }
            setChanged();
            notifyObservers();
        }
        if (key.equals(SHIFT)){
            Boat boat = race.getBoatById(clientID);
            boat.changeSails();
        }
    }

    /**
     * calculates if user is tacking or gybing or unable to do either
     * and displays feedback to the user via a label
     */
    private void generateUserFeedback(){
        double optimumHeading = userBoat.getTackOrGybeHeading(race.getCourse(), polarTable);
        double boatHeading = userBoat.getHeading();
        if(optimumHeading == -1){
            controller.setUserHelpLabel("No Sail Zone, cannot tack or gybe", Color.web("#f47777"));
        }else{
            String feedback;
            double TWA = Math.abs(((race.getCourse().getWindDirection() - boatHeading)));
            if(userBoat.isTacking(TWA)){
                feedback = "Tacking";
            }else{
                feedback = "Gybing";
            }
            controller.setUserHelpLabel(feedback, Color.web("#4DC58B"));
        }
    }

    public int getCommandInt() {
        return commandInt;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public void setController(Controller controller){ this.controller = controller;}
}
