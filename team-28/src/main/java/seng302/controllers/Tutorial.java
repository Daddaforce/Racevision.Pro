package seng302.controllers;

import javafx.scene.input.KeyCode;
import seng302.models.Boat;
import seng302.models.PolarTable;
import seng302.models.Race;
import seng302.utilities.PolarReader;

import java.util.ArrayList;
import java.util.List;

import static seng302.data.RaceStatus.PREPARATORY;
import static seng302.data.RaceStatus.STARTED;

/**
 * Created by sjh298 on 8/08/17.
 *
 */
public class Tutorial {

    private Controller controller;
    private Race race;
    public enum TutorialStage { UPWINDDOWNWIND, VMG, TACK, GYBE, SAILSIN, SAILSOUT, END, TACKFAIL }
    private int tutorialUpwindDownwindCounter = 0;
    private int UPWIND_DOWNWIND_TIME = 60;
    private Boat tutorialBoat;
    private PolarTable polarTable;

    private TutorialStage tutorialStage = TutorialStage.UPWINDDOWNWIND;
    private TutorialStage stepFailed;
    private List<KeyCode> keysFailed;

    public Tutorial(Controller controller, Race race){
        this.controller = controller;
        this.race = race;
        if (!race.getCompetitors().isEmpty()) tutorialBoat = race.getCompetitors().get(0);
        polarTable = new PolarTable(PolarReader.getPolarsForAC35Yachts(), race.getCourse());
        controller.setUpTutorialMode();
    }

    /**
     * ran every frame. Displays the correct step of the tutorial
     */
     public void runTutorial(){
        switch(tutorialStage){
            case UPWINDDOWNWIND:
               upwindTutorial();
                break;
            case VMG:
                vmgTutorial();
                break;
            case TACK:
                tackGybeTutorial(false);
                break;
            case GYBE:
                tackGybeTutorial(true);
                break;
            case SAILSIN:
                sailsTutorial(false);
                break;
            case SAILSOUT:
                sailsTutorial(true);
                break;
            case END:
                GameClient.clearTutorialAction();
                controller.showTutorialOverlay("Complete", "Tutorial is complete. Become familiar with the controls before you race online.\n\n" +
                        "UP or DOWN - upwind or downwind\n" +
                        "ENTER - tack or gybe\n" +
                        "SHIFT - sails in or out\n" +
                        "SPACE - VMG autopilot\n\n" +
                        "Press the X button on the bottom right of your screen to return to the Menu.");
                break;
            case TACKFAIL:
                tackFailTutorial();
        }
    }

    /**
     * Shows tutorial for up and down arrows.
     */
    private void upwindTutorial(){
        controller.showTutorialOverlay("Upwind/Downwind", "Welcome to the Objective Sea Tutorial. Follow along to master all the keys. \n\n" +
                "Press the UP and DOWN arrow keys to turn the boat to the upwind or downwind direction. \n\n" +
                "Note the wind direction shown on the indicator to the right.\n\n" +
                "If you have a touch capable device, you can change the direction of your boat by touching and holding down on the screen.\n\n" +
                "Your boat will follow wherever you move your finger. ");
        List<KeyCode> keycodes = new ArrayList<KeyCode>();
        keycodes.add(KeyCode.PAGE_DOWN);
        keycodes.add(KeyCode.DOWN);
        keycodes.add(KeyCode.UP);
        keycodes.add(KeyCode.PAGE_UP);
        GameClient.setTutorialActions(keycodes, () -> tutorialUpwindDownwindCounter++);
        if (tutorialUpwindDownwindCounter > UPWIND_DOWNWIND_TIME){
            tutorialStage = TutorialStage.VMG;
        }
    }

    /**
     * Shows tutorial for space key. Has callback that checks whether boat moves
     */
    private void vmgTutorial(){
        List<KeyCode> keycodes = new ArrayList<KeyCode>();
        GameClient.clearTutorialAction();
        keycodes.add(KeyCode.SPACE);
        controller.showTutorialOverlay("AutoPilot", "The VMG is the angle between your boat and the wind that allows for the maximum speed.\n\n" +
                "Press SPACE to automatically move your boat to the nearest VMG angle.\n\n" +
                "If you are within 45 degrees of the wind direction, you may be in a no-sail zone. You will not automatically move to a VMG at this angle.");
        GameClient.setTutorialActions(keycodes, () -> {
            double vmhHeading = tutorialBoat.getVMGHeading(race.getCourse(), polarTable);
            if(vmhHeading != -1){
                tutorialStage = TutorialStage.TACK;
            }else{
                stepFailed = tutorialStage;
                keysFailed = keycodes;
                tutorialStage = TutorialStage.TACKFAIL;
            }
        });
    }

    /**
     * Shows a tutorial step alerting user to the failure of a step.
     */
    private void tackFailTutorial(){
        GameClient.clearTutorialAction();

        controller.showTutorialOverlay("Try Again", "Your boat did not move. \n\nYou are heading towards the wind in the no-sail zone (45 degrees either side of the wind direction).\n\n" +
                "Try use the UP and DOWN keys to move yourself to the away from the wind direction and try the key again");
        GameClient.setTutorialActions(keysFailed, () -> {
            double vmhHeading = tutorialBoat.getVMGHeading(race.getCourse(), polarTable);
            if(vmhHeading != -1){
                tutorialStage = TutorialStage.values()[stepFailed.ordinal() + 1];
            }
        });
    }

    /**
     * Shows tutorial for the enter key
     * @param hasGybe indicates the first or second run-through. allows the function to know what step is next.
     */
    private void tackGybeTutorial(boolean hasGybe){
        List<KeyCode> keycodes = new ArrayList<KeyCode>();
        GameClient.clearTutorialAction();
        keycodes.add(KeyCode.ENTER);
        if(!hasGybe){
            controller.showTutorialOverlay("Tack/Gybe", "Tacking and Gybing are manoeuvre that flips the heading of your boat towards the opposite VMG.\n\n" +
                    "if you are not in the no-sail zone, press ENTER now to tack or gybe.\n\n" +
                    "If you have a touch capable device, you can tack or gybe by swiping the screen in the correct direction.");
            GameClient.setTutorialActions(keycodes, () -> tackGybeCallback(TutorialStage.GYBE, keycodes));
        }else{
            controller.showTutorialOverlay("Tack/Gybe", "Nice work. Lets do that again.\n\nTacking and Gybing are manoeuvre that flips the heading of your boat towards the opposite VMG.\n\n" +
                    "if you are not in the no-sail zone, press ENTER now to tack or gybe.");
            GameClient.setTutorialActions(keycodes, () -> tackGybeCallback(TutorialStage.SAILSIN, keycodes));
        }
    }

    /**
     * the callback function when the enter key is pressed
     * @param nextStage the tutorial stage to go to upon success
     * @param keycodes the keys required for the callback, to be recycled upon failure
     */
    private void tackGybeCallback(TutorialStage nextStage, List<KeyCode> keycodes){
        double tackHeading = tutorialBoat.getTackOrGybeHeading(race.getCourse(), polarTable);
        if(tackHeading != -1){
            tutorialStage = nextStage;
        }else{
            stepFailed = tutorialStage;
            keysFailed = keycodes;
            tutorialStage = TutorialStage.TACKFAIL;
        }
    }

    /**
     * Shows a tutorial for the shift key
     * @param isOut indicates the current position of the sail. determines what step to move onto
     */
    private void sailsTutorial(boolean isOut){
        List<KeyCode> keycodes = new ArrayList<KeyCode>();
        GameClient.clearTutorialAction();
        keycodes.add(KeyCode.SHIFT);
        if(!isOut){
            controller.showTutorialOverlay("Sails In", "Press the SHIFT key to bring your sails in. \n\nThis should cause your boat to luff and lose all velocity.\n\n" +
                                                "If you have a touch capable device, you can swipe up and down (parallel to the boat's direction) to put your boat's sails up or down.");
            GameClient.setTutorialActions(keycodes, () -> tutorialStage = TutorialStage.SAILSOUT);
        } else{
            controller.showTutorialOverlay("Sails Out", "Press the SHIFT key to put your sails out. \n\nThis should cause your boat to start gaining velocity.");
            GameClient.setTutorialActions(keycodes, () -> tutorialStage = TutorialStage.END);
        }

    }
}
