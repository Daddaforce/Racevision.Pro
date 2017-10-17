package seng302.models;

import seng302.data.RaceVisionXMLParser;
import seng302.utilities.ConnectionUtils;

/**
 * Created by mjt169 on 3/08/17.
 *
 */
public class ServerOptions {

    private final String DEFAULT_COURSE = "AC35-course.xml";
    private final Double DEFAULT_SPEED = 15.0;
    private final Integer DEFAULT_PORT = ConnectionUtils.DEFAULT_GAME_PORT;
    private Integer MAX_PARTICIPANTS = 6;

    private Double speedScale;
    private Integer minParticipants;
    private Integer port;
    private String raceXML;
    private boolean isTutorial;
    private Integer numRacesToRun;
    private AIDifficulty aiDifficulty = AIDifficulty.NO_AI;
    private GameMode gameMode = GameMode.MULTIPLAYER;

    /**
     * Constructor with default options
     */
    public ServerOptions(GameMode gameMode){
        speedScale = DEFAULT_SPEED;
        minParticipants = 1;
        port = DEFAULT_PORT;
        raceXML = DEFAULT_COURSE;
        isTutorial = false;
        numRacesToRun = 1;
        this.gameMode = gameMode;
    }

    /**
     * Constructor for custom host game
     */
    public ServerOptions(Double speedScale, int minParticipants){
        this.speedScale = speedScale;
        this.minParticipants = minParticipants;
        port = DEFAULT_PORT;
        raceXML = DEFAULT_COURSE;
        isTutorial = false;
        numRacesToRun = 1;
    }

    /**
     * Constructor for custom host game
     */
    public ServerOptions(Double speedScale, int minParticipants, GameMode mode){
        this.speedScale = speedScale;
        this.minParticipants = minParticipants;
        port = DEFAULT_PORT;
        raceXML = DEFAULT_COURSE;
        isTutorial = false;
        numRacesToRun = 1;
        gameMode = mode;
    }

    public Double getSpeedScale() {
        return speedScale;
    }

    public String getRaceXML() {
        return raceXML;
    }

    public Integer getPort() {
        return port;
    }

    public Integer getMinParticipants() {
        return minParticipants;
    }

    public void setSpeedScale(Double speedScale) throws IllegalArgumentException {
        if (speedScale > 0) {
            this.speedScale = speedScale;
        } else {
            throw new IllegalArgumentException("Speed scale must be a positive value");
        }
    }

    public void setPort(int port) {
        if (ConnectionUtils.validatePort(port)) {
            this.port = port;
        } else {
             throw new IllegalArgumentException("Invalid port number");
        }
    }

    public void setMinParticipants(int minParticipants) throws IllegalArgumentException {
        if (minParticipants > 0 && minParticipants <= MAX_PARTICIPANTS) {
            this.minParticipants = minParticipants;
        } else {
            throw new IllegalArgumentException(
                    String.format("Minimum number of participants must be between 1 and %d", MAX_PARTICIPANTS));
        }
    }

    public void setRaceXML(String raceXML) {
        RaceVisionXMLParser parser = new RaceVisionXMLParser();
        if (parser.checkFileExists(raceXML)) {
            this.raceXML = raceXML;
        } else {
            throw new IllegalArgumentException("Could not find a map that matches that name");
        }
    }

    public void setTutorial(boolean tutorial) {
        this.isTutorial = tutorial;
    }

    public boolean isTutorial() {
        return this.isTutorial;
    }

    public Integer getNumRacesToRun() {
        return numRacesToRun;
    }

    public void setNumRacesToRun(Integer numRacesToRun) {
        this.numRacesToRun = numRacesToRun;
    }

    /**
     * @return true if the server has been configured to always restart at the end of a race
     */
    public Boolean alwaysRerun() {
        return numRacesToRun == -1;
    }

    public void setAiDifficulty(AIDifficulty AIDifficulty) {
        this.aiDifficulty = AIDifficulty;
    }

    public AIDifficulty getAIDifficulty() {
        return this.aiDifficulty;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public Boolean isOnline() {
        return gameMode == GameMode.MULTIPLAYER || gameMode == GameMode.PARTYGAME;
    }

    public Boolean isPartyMode() {
        return gameMode == GameMode.PARTYGAME;
    }

    public Boolean isMultiplayer() {
        return gameMode == GameMode.MULTIPLAYER;
    }

}
