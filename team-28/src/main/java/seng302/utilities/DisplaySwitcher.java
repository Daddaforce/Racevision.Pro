package seng302.utilities;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import seng302.controllers.*;
import seng302.data.registration.ServerFullException;
import seng302.models.ClientOptions;

import javax.sound.sampled.LineUnavailableException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * class to manage the handling of switching FXML displays
 */
public class DisplaySwitcher {

    private static Scene scene;
    private Stage stage;
    private Main main;
    private static GameSounds gameSounds = new GameSounds();
    private Controller raceController;
    private MainMenuController mainMenu;
    private boolean mainMenuLoaded = false;

    public DisplaySwitcher(Main main, Stage stage){
        this.stage = stage;
        this.main = main;
    }

    /**
     * loads the main menu into the stage
     */
    public void loadMainMenu() {
        try {
            DisplayUtils.setIsRaceView(false);
            mainMenu = (MainMenuController) replaceSceneContent("main_menu.fxml");
            mainMenu.setApp(main, gameSounds);
            try {
                gameSounds.stopEndlessMusic();
            } catch (LineUnavailableException e) {
                System.out.println("Error with stopping endless music loop");
            }
            gameSounds.mainMenuMusic();
            gameSounds.playEndlessMusic();


        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServerFullException e) {
            e.printStackTrace();
        }
    }

    /**
     * loads the race view into the stage
     * @param options a set of configured ClientOptions
     */
    public void loadRaceView(ClientOptions options) {
        try {
            DisplayUtils.setIsRaceView(true);
            SoundController soundController = new SoundController(Main.getClient().getClientID());
            soundController.setRunning(true);
            Thread soundControllerThread = new Thread(soundController);
            soundControllerThread.start();

            if (mainMenu.getClient() != null) {
                mainMenu.getClient().stopPolling();
                mainMenu.getClient().getSender().closeConnection();
            }

            raceController = (Controller) replaceSceneContent("race_view.fxml");
            raceController.setApp(options, this, scene);
            raceController.setSoundController(soundController);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * takes an fxml file and replaces the current screen with it
     * @param fxml an FXML file
     * @return a display
     * @throws Exception if can't find FXML
     */
    public Initializable replaceSceneContent(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        URL fxmlLocation = getClass().getClassLoader().getResource(fxml);
        loader.setLocation(fxmlLocation);
        Parent root = loader.load();
        if(mainMenuLoaded) {
            scene.setRoot(root);
        } else {
            mainMenuLoaded = true;
            scene = new Scene(root);
            if (!System.getProperty("os.name").startsWith("Mac")) {
                stage.setMaximized(true);
                stage.setFullScreen(true);
            }
        }
        setScene(scene);
        AnimationUtils.transitionFXML(root);
        stage.setScene(scene);
        return (Initializable) loader.getController();
    }

    private void setScene(Scene newScene){
        scene = newScene;
    }

    public static Scene getScene(){return scene;}

    public static GameSounds getGameSounds() {
        return gameSounds;
    }

    public void setUpTouchInputController(TouchInputController touchInputController){
        raceController.setUpTouchInputController(touchInputController);
    }

    public void setUpKeyInputController(KeyInputController keyInputController){
        raceController.setUpKeyInputController(keyInputController);
    }
}
