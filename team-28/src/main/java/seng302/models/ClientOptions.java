package seng302.models;

import seng302.utilities.ConnectionUtils;

/**
 * Created by mjt169 on 15/08/17.
 * Class to hold the various options for a Client so that we can pass them around easily
 */
public class ClientOptions {

    private final String DEFAULT_ADDRESS = "localhost";
    private final Integer DEFAULT_PORT = ConnectionUtils.DEFAULT_GAME_PORT;

    private String serverAddress;
    private Integer serverPort;
    private GameMode gameMode;
    private Boolean isParticipant;
    private Boolean isHost;

    /** Default constructor */
    public ClientOptions() {
        serverAddress = DEFAULT_ADDRESS;
        serverPort = DEFAULT_PORT;
        isParticipant = true;
        isHost = false;
        gameMode = GameMode.MULTIPLAYER;
    }

    /** Constructor for local only games, requires GameMode only */
    public ClientOptions(GameMode mode) {
        serverAddress = DEFAULT_ADDRESS;
        serverPort = DEFAULT_PORT;
        isParticipant = true;
        if(mode.equals(GameMode.PARTYGAME)){
            isParticipant = false;
        }
        isHost = true;
        gameMode = mode;
    }

    /** Constructor with full option setting */
    public ClientOptions(String address, Integer port, GameMode gameMode, Boolean isParticipant, Boolean isHost) {
        this.serverAddress = address;
        this.serverPort = port;
        this.gameMode = gameMode;
        this.isParticipant = isParticipant;
        this.isHost = isHost;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public Boolean isTutorial() {
        return gameMode.equals(GameMode.TUTORIAL);
    }

    public Boolean isPractice() {
        return gameMode.equals(GameMode.PRACTICE);
    }

    public Boolean isParticipant() {
        return isParticipant;
    }

    public Boolean isHost() {
        return isHost;
    }

    public Boolean requiresPlayerHUD(){
        if(gameMode.equals(GameMode.MULTIPLAYER) || gameMode.equals(GameMode.SINGLEPLAYER)){
            return true;
        }
        return false;
    }

    public GameMode getGameMode() {
        return gameMode;
    }
}
