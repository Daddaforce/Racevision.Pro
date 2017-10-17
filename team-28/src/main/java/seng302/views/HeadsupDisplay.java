package seng302.views;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import seng302.models.Race;
import seng302.utilities.AnimationUtils;

/** class to mange the heads up display
 *  shows when sidepanel toggled off
 */
public class HeadsupDisplay {

    private BoatDisplay boat;
    private VBox display;
    private Label positionLabel;
    private ProgressBar healthBar;
    private Race race;
    private String partyPin;


    /**
     * creates a HUD for the current player
     */
    public HeadsupDisplay(BoatDisplay boat, VBox display, Race race){
        this.boat = boat;
        this.display = display;
        this.race = race;
        display.setPickOnBounds(false);
        addInfoToDisplay();
        addListeners();
        display.getStyleClass().add("headsUpDisplay");
        AnimationUtils.toggleHiddenBoardNodes(display, false, 0.8);
    }

    /**
     * creates a hud containing the party code
     */
    public HeadsupDisplay(String partyPin, VBox display){
        this.display = display;
        this.partyPin = partyPin;
        display.setAlignment(Pos.CENTER);
        addPartyPin();
        display.getStyleClass().add("headsUpDisplayParty");
        ImageView imvQR = new ImageView(new Image("graphics/qrCode.png"));
        imvQR.setFitHeight(100);
        imvQR.setFitWidth(100);
        display.getChildren().add(imvQR);
        AnimationUtils.toggleHiddenBoardNodes(display, false, 1);
    }

    private void addPartyPin(){
        Label pinLabel = new Label("PartyCode");
        pinLabel.setId("partyCodeTitle");

        Label codeLabel = new Label();
        codeLabel.setId("codeLabel");
        codeLabel.setText(String.valueOf(partyPin));

        display.getChildren().add(pinLabel);
        display.getChildren().add(codeLabel);
    }

    private void addInfoToDisplay(){
        addSpeed();
        addPosition();
        addHealth();
    }

    /**
     * adds a label bound to the boats speed property to the display
     */
    private void addSpeed(){
        Label speedTitleLabel = new Label("Speed");
        speedTitleLabel.setId("titleLabel");

        Label speedLabel = new Label();
        speedLabel.setId("speedLabel");
        boat.getBoat().getSpeedProperty().addListener((obs, oldStatus, newStatus) ->
                Platform.runLater(() -> speedLabel.setText(String.format("%.2f kn", newStatus))));

        display.getChildren().add(speedTitleLabel);
        display.getChildren().add(speedLabel);
    }

    /**
     * adds a progress bar showing the boats health to the display
     */
    private void addHealth(){
        Label healthLabel = new Label("Health");
        healthLabel.setId("healthLabel");

        healthBar = new ProgressBar(boat.getBoat().getHealthProperty().doubleValue());
        healthBar.setMinWidth(140);
        boat.getBoat().getHealthProperty().addListener((obs, oldStatus, newStatus) ->
                Platform.runLater(() -> updateHealth((Double)newStatus)));
        healthBar.setId("boatHealth");

        display.getChildren().add(healthLabel);
        display.getChildren().add(healthBar);
    }

    /**
     * adds a label bound to the boats speed position property to the display
     */
    private void addPosition(){
        Label positionTitleLabel = new Label("Position");
        positionTitleLabel.setId("titleLabel");

        positionLabel = new Label("...");
        positionLabel.setId("positionLabel");
        updatePlacing(1);
        boat.getBoat().getCurrPlacingProperty().addListener((obs, oldStatus, newStatus) ->
                Platform.runLater(() -> updatePlacing((int)newStatus)));

        display.getChildren().add(positionTitleLabel);
        display.getChildren().add(positionLabel);
    }

    private void updatePlacing(int newPos){
        positionLabel.setText(String.format("%d / %d", newPos, race.getCompetitors().size()));
        AnimationUtils.drawAttentionToNode(positionLabel);
    }

    private void updateHealth(Double value){
        healthBar.setProgress(value);
    }

    /**
     * adds listeners to the vbox so it grows when mouse enters
     */
    private void addListeners(){
        display.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> AnimationUtils.scaleButtonHover(display));
        display.addEventHandler(MouseEvent.MOUSE_EXITED,
                e ->  AnimationUtils.scaleButtonHoverExit(display));
    }

    public void competitorAdded(){
        positionLabel.setText(String.format("%d / %d", boat.getBoat().getCurrPlacingProperty().get(), race.getCompetitors().size()));
    }
}
