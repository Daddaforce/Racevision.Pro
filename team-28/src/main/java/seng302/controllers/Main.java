package seng302.controllers;

/**
 * Main class. Sets up server and client and starts GUI.
 */
import javafx.application.Application;
import javafx.application.Preloader;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.geometry.Rectangle2D;
import javafx.application.Platform;
import seng302.data.registration.RaceUnavailableException;
import seng302.data.registration.ServerFullException;
import seng302.data.registration.ServerRegistrationException;
import seng302.models.AIDifficulty;
import seng302.models.ClientOptions;
import seng302.models.GameMode;
import seng302.models.ServerOptions;
import seng302.utilities.ConnectionUtils;
import seng302.utilities.DisplaySwitcher;
import seng302.utilities.NoConnectionToServerException;
import java.io.IOException;
import java.net.BindException;


public class Main extends Application {
    private static GameClient client;
    private GameServer server;
    private GameRecorder gameRecorder;
    private Stage primaryStage;
    private DisplaySwitcher displaySwitcher;


    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Objective Sea");
        this.primaryStage.getIcons().add(new Image("graphics/icon.png"));
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        this.primaryStage.setHeight(primaryScreenBounds.getHeight());
        this.primaryStage.setWidth(primaryScreenBounds.getWidth());
        displaySwitcher = new DisplaySwitcher(this, primaryStage);
        loadMainMenu();
        notifyPreloader(new Preloader.StateChangeNotification(Preloader.StateChangeNotification.Type.BEFORE_START));
        this.primaryStage.show();
        this.primaryStage.setOnCloseRequest(e -> {
            if(client != null){
                client.initiateClientDisconnect();
            }
            if(server != null){
                server.initiateServerDisconnect();
            }

            Platform.exit();
            System.exit(0);
        });
    }

    public static void main( String[] args ) {
        if (args.length >= 1) {
            Main main = new Main();
            main.launchWithArguments(args);
        } else {
            launch(args);
        }
    }

    /**
     * Parse command line arguments and use them to launch the application
     * @param args arguments that application was started with
     */
    private void launchWithArguments(String[] args) {
        if (args[0].equals("server")){
            try {
                if (args.length > 1 && args[1].equals("-g")) {
                    gameRecorder = new GameRecorder();
                    System.out.println("Game recorder started");
                } else {
                    ServerOptions serverOptions = new ServerOptions(GameMode.MULTIPLAYER);
                    serverOptions.setNumRacesToRun(-1);
                    for (int i = 1; i < args.length; i += 2) {
                        switch (args[i]) {
                            case "-p":
                                serverOptions.setPort(Integer.parseInt(args[i + 1]));
                                break;
                            case "-n":
                                serverOptions.setMinParticipants(Integer.parseInt(args[i + 1]));
                                break;
                            case "-m":
                                serverOptions.setRaceXML(args[i + 1]);
                                break;
                            case "-s":
                                serverOptions.setSpeedScale(Double.parseDouble(args[i + 1]));
                                break;
                            case "-r":
                                serverOptions.setNumRacesToRun(Integer.parseInt(args[i + 1]));
                                break;
                            default:
                                throw new IllegalArgumentException(String.format("Unknown argument \"%s\"", args[i]));
                        }
                    }
                    setupServer(serverOptions);
                    System.out.println("Headless server started.");
                }
            } catch (IllegalArgumentException iae) {
                System.out.print("Invalid server arguments. ");
                System.out.println(iae.getMessage());
                Platform.exit();
            } catch (IOException e) {
                System.out.println("Failed to start headless server.");
                e.printStackTrace();
                Platform.exit();
            }
        }
    }

    /**
     * Creates a Server object, puts it in it's own thread and starts the thread
     */
    private void setupServer(ServerOptions serverOptions) throws IOException {
        server = new GameServer(serverOptions);
        ConnectionUtils.setServer(server);
        Thread serverThread = new Thread(server);
        serverThread.setName("Server");
        serverThread.start();
    }

    public static GameClient getClient() {
        return client;
    }

    private void loadMainMenu() {
        displaySwitcher.loadMainMenu();
    }


    /**
     * Loads the visualiser and attaches a KeyInputController to the client and the JavaFX scene
     * @param options ClientOptions for the RaceView
     */
    public void loadRaceView(ClientOptions options) {
        displaySwitcher.loadRaceView(options);
        if (options.isParticipant()) {
            KeyInputController keyInputController = new KeyInputController(DisplaySwitcher.getScene(), GameClient.getRace(), GameClient.getRace().getBoatById(getClient().getClientID()));
            TouchInputController touchInputController = new TouchInputController(GameClient.getRace(), GameClient.getRace().getBoatById(getClient().getClientID()));
            client.setInputControllers(keyInputController, touchInputController);
            keyInputController.addObserver(client);
            touchInputController.addObserver(client);
            displaySwitcher.setUpTouchInputController(touchInputController);
            displaySwitcher.setUpKeyInputController(keyInputController);
        }
    }

    /**
     * starts a local race with the given params
     * @param course the course name
     * @param port the host port of the game
     * @param isTutorial boolean, true if it is a tutorial
     * @param clientOptions client options for game
     * @return boolean, true if the client starts successfully
     * @throws Exception throws this
     */
    public boolean startLocalRace(String course, Integer port, Boolean isTutorial, ClientOptions clientOptions, AIDifficulty aiDifficulty, GameMode gameMode) throws Exception {
        ServerOptions serverOptions = new ServerOptions(gameMode);
        serverOptions.setAiDifficulty(aiDifficulty);
        serverOptions.setPort(port);
        serverOptions.setRaceXML(course);
        serverOptions.setTutorial(isTutorial);
        try{
            setupServer(serverOptions);
        } catch(BindException e){
            return false;
        }
        startClient(clientOptions);
        return true;
    }

    /**
     * initilises a hosted race with the provided parameters
     * @param course course name
     * @param clientOptions client options
     * @return whether starting hosted race was successful or not
     * @throws Exception uncaught error
     */
    public boolean startHostedRace(String course, Double speedScale, int numParticipants, ClientOptions clientOptions, int currentCourseIndex) throws Exception {
        ServerOptions serverOptions = new ServerOptions(speedScale, numParticipants);
        if(clientOptions.getGameMode().equals(GameMode.PARTYGAME)){
            serverOptions = new ServerOptions(speedScale, numParticipants, GameMode.PARTYGAME);
        }
        serverOptions.setRaceXML(course);

        try{
            setupServer(serverOptions);
        } catch(BindException e){
            showPortInUseError(serverOptions.getPort());
            return false;
        }
        startClient(clientOptions);
        return true;
    }

    /**
     * starts the client at the desired ip and port number
     * ensures that the client connects
     * throws error if connection fails
     * @param options ClientOptions to initialize with
     * @return whether the connection was successful or times out
     */
    public boolean startClient(ClientOptions options){
        try {
            client = new GameClient(options);
            ConnectionUtils.setClient(client);
            Thread clientThread = new Thread(client);
            clientThread.setName("Client");
            clientThread.start();
        } catch (NoConnectionToServerException e) {
            showServerConnectionError(e);
            return false;
        } catch (ServerRegistrationException e) {
            showServerJoinError(options.isParticipant(), e);
            return false;
        }
        return true;
    }

    /**
     * Shows a popup informing user that they were unable to connect to the server
     */
    private void showServerConnectionError(NoConnectionToServerException err){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add("style/menuStyle.css");
        dialogPane.getStyleClass().add("myDialog");
        alert.setTitle("Cannot Connect to Server");
        alert.setHeaderText("Cannot Connect to Server");
        if(err.isLocalError()){
            alert.setContentText("No connection to local server.\n\n" +
                    "Please ensure that the Port number \n" +
                    "you have entered is correct.");
        } else {
            alert.setContentText("This server may not be running.\n\n" +
                    "Please ensure that the IP and Port numbers \n" +
                    "you have entered are correct.");
        }
        alert.showAndWait();
    }

    /**
     * Shows a popup informing user that they were not allowed to join the server
     * If they attempted to join as a participant, suggests they try joining as a spectator
     * @param isParticipant whether or not an attempt was made to participate in the race
     */
    private void showServerJoinError(boolean isParticipant, ServerRegistrationException ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add("style/menuStyle.css");
        dialogPane.getStyleClass().add("myDialog");
        alert.setTitle("Failed to Join Server");
        alert.setHeaderText("Failed to Join Server");
        String message = "";
        if (ex instanceof ServerFullException) {
            message = "There was not a free slot for you to join the server.\n\n";
            if (isParticipant) message += "You may be able to join as a spectator instead.";
        } else if (ex instanceof RaceUnavailableException) {
            message = "The race has not started yet.\n\n";
        }
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * shows a popup saying that port is in use.
     * @param port port number to display
     */
    private void showPortInUseError(int port) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add("style/menuStyle.css");
        dialogPane.getStyleClass().add("myDialog");
        alert.setTitle("Failed to Host this Race");
        alert.setHeaderText("Failed to Host this Race");
        String message = "You already have a game running\n" +
                "Please close that game to continue.";
        alert.setContentText(message);
        alert.showAndWait();
    }
}

