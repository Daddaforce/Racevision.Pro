package seng302.controllers;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Callback;
import seng302.data.RaceStatus;
import seng302.models.*;
import seng302.utilities.AnimationUtils;
import seng302.utilities.ConnectionUtils;
import seng302.utilities.DisplayUtils;
import seng302.data.BoatStatus;
import seng302.utilities.*;
import seng302.utilities.TimeUtils;
import seng302.views.BoatDisplay;
import seng302.views.HeadsupDisplay;


import java.net.URL;
import java.util.*;

import static java.lang.Math.abs;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.input.KeyCode.*;
import static seng302.utilities.DisplayUtils.DRAG_TOLERANCE;
import static seng302.utilities.DisplayUtils.isOutsideBounds;
import static seng302.utilities.DisplayUtils.zoomLevel;

public class Controller implements Initializable, Observer {

    @FXML public Canvas canvas;
    @FXML private Group root;
    @FXML private AnchorPane canvasAnchor;
    @FXML private AnchorPane rightHandSide;
    @FXML private Label fpsLabel;
    @FXML private ListView<String> overViewList;
    @FXML private Label clockLabel;
    @FXML private Label lblNoBoardClock;
    @FXML public VBox startersOverlay;
    @FXML private Label startersOverlayTitle;
    @FXML public ImageView mapImageView;
    @FXML private Slider zoomSlider;
    @FXML public Label lblUserHelp;
    @FXML public Label lblWindSpeed;
    @FXML public Circle windCircle;
    @FXML public Circle nextMarkCircle;
    @FXML private Button btnHide;
    @FXML private Button btnQuickMenuTrack;
    @FXML private Button btnQuickMenuExit;
    @FXML private AnchorPane quickMenu;
    @FXML private ImageView imvSpeedScale;
    @FXML private TableView<Boat> tblPlacingsRV;
    @FXML private TableColumn<Boat, Integer> columnPosition;
    @FXML private TableColumn<Boat, String> columnName;
    @FXML private TableColumn<Boat, String> columnSpeed;
    @FXML private TableColumn<Boat, String> columnStatus;
    @FXML private Label lblExitRV;
    @FXML private Label lblTrackRV;
    @FXML private VBox headsUpDisplay;
    @FXML private GridPane partyModeBox;
    @FXML private VBox partyModeBoxWrapper;
    private HeadsupDisplay infoDisplay;

    @FXML public StackPane stackPane;
    @FXML private VBox tutorialOverlay;
    @FXML private Label tutorialOverlayTitle;
    @FXML private Label tutorialContent;
    @FXML public Label lblNextMark;
    @FXML private GridPane nextMarkGrid;
    @FXML private Label lblPartyCode;

    //FPS Counter
    private SimpleStringProperty fpsString = new SimpleStringProperty();
    private final long[] frameTimes = new long[100];
    private int frameTimeIndex = 0;
    private boolean arrayFilled = false;

    //Race Clock
    public SimpleStringProperty raceTimerString = new SimpleStringProperty();
    private SimpleStringProperty clockString = new SimpleStringProperty();

    private ObservableList<String> formattedDisplayOrder = observableArrayList();
    private static double canvasHeight;
    private static double canvasWidth;

    private static double anchorHeight;
    private static double anchorWidth;
    private final String BOAT_CSS = "/style/boatStyle.css";
    private final String COURSE_CSS = "/style/courseStyle.css";
    private final String STARTERS_CSS = "/style/startersOverlayStyle.css";
    private final String SETTINGSPANE_CSS = "/style/settingsPaneStyle.css";
    private final String DISTANCELINE_CSS = "/style/distanceLineStyle.css";
    private final String HEADSUP_DISPLAY_CSS = "/style/headsupDisplay.css";
    private final Color UNSELECTED_BOAT_COLOR = Color.WHITE;
    private final Color SELECTED_BOAT_COLOR = Color.rgb(77, 197, 138);

    // Controllers
    @FXML
    private RaceViewController raceViewController;
    @FXML
    private ScoreBoardController scoreBoardController = new ScoreBoardController();
    @FXML
    private SelectionController selectionController;
    @FXML
    private Pane touchPane;

    private boolean raceStatusChanged = true;
    private Race race;
    private ClientOptions options;
    private DisplaySwitcher displaySwitcher;
    private boolean scoreboardVisible = true;
    private boolean moveAnnotation = false;
    private boolean moveHUD = false;
    private boolean hasHUDXMoved = false;
    private boolean hasHUDYMoved = false;
    private boolean moveTutorial = false;
    private boolean hasTutorialXMoved = false;
    private boolean hasTutorialYMoved = false;
    private ArrayList<BoatDisplay> boatDisplayArrayList = new ArrayList<>();
    private BoatDisplay currentDisplayBoat;
    private boolean dragging = false;



    private final double FOCUSED_ZOOMSLIDER_OPACITY = 0.8;
    private final double IDLE_ZOOMSLIDER_OPACITY = 0.4;
    private final double TUTORIAL_OVERLAY_X_OFFSET = 105;
    private final double TUTORIAL_OVERLAY_Y_OFFSET = 67;

    private SoundController soundController;
    private Scene scene;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addRightHandSideListener();
        canvasAnchor.getStylesheets().addAll(COURSE_CSS, STARTERS_CSS, SETTINGSPANE_CSS, BOAT_CSS, DISTANCELINE_CSS, HEADSUP_DISPLAY_CSS);
        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();
        anchorWidth = canvasAnchor.getWidth();
        anchorHeight = canvasAnchor.getHeight();

        race = GameClient.getRace();
        race.addObserver(this);
        Course course = race.getCourse();
        course.initCourseLatLon();
        DisplayUtils.setMaxMinLatLon(course.getMinLat(), course.getMinLon(), course.getMaxLat(), course.getMaxLon());
        selectionController = new SelectionController(root, scoreBoardController, this);
        raceViewController = new RaceViewController(root, race, this, scoreBoardController, selectionController);
        selectionController.addObserver(raceViewController);

        createCanvasAnchorListeners();
        scoreBoardController.setControllers(this, raceViewController, race, selectionController, DisplaySwitcher.getScene());
        scoreBoardController.setUp(DisplaySwitcher.getGameSounds());
        fpsString.set("..."); //set to "..." while fps count loads
        fpsLabel.textProperty().bind(fpsString);
        lblNoBoardClock.textProperty().bind(raceTimerString);
        clockLabel.textProperty().bind(clockString);
        hideStarterOverlay();
        rightHandSide.setOpacity(0.7);
        lblNoBoardClock.setVisible(false);
        tblPlacingsRV.setVisible(false);
        headsUpDisplay.setVisible(false);
        lblTrackRV.setVisible(false);
        lblExitRV.setVisible(false);
        partyModeBox.setVisible(false);
        setPartyBoxPosition();
        setTutorialOverlayPosition();

        raceCompetitorOverview();
        startersOverlay.toFront();
        initZoom();
    }

    /**
     * Set app options and pass them on to the RaceViewController
     * @param options configured ClientOptions
     * @param displaySwitcher required to allow switching back to main menu
     * @param scene the scene in which we are being drawn
     */
    public void setApp(ClientOptions options, DisplaySwitcher displaySwitcher, Scene scene) {
        this.displaySwitcher = displaySwitcher;
        this.options = options;
        this.scene = scene;
        if (this.options.isHost()) {
            String ip = ConnectionUtils.getPublicIp();
            if (Objects.equals(ip, null)) {
                startersOverlayTitle.setText(race.getRegattaName());
            } else {
                startersOverlayTitle.setText("IP: " + ip);
            }
        } else {
            startersOverlayTitle.setText(race.getRegattaName());
        }
        if(options.getGameMode().equals(GameMode.PARTYGAME)){
            partyModeBoxWrapper.setVisible(true);
            partyModeBox.setVisible(true);
            String paddedPartyCode = String.format("%04d", GameClient.getRoomCode());
            lblPartyCode.setText(paddedPartyCode);
            partyModeBox.toFront();
        }

        initZoomEventListener();
        initKeyPressListener();
        initTouchDisplayDrag();
        resetVBoxPosition();
        raceViewController.setupRaceView(options);
        initHiddenScoreboard();
        raceViewController.updateWindArrow();
        raceViewController.start();

    }

    private void setPartyBoxPosition(){
        partyModeBoxWrapper.toFront();
        partyModeBox.toFront();
        partyModeBox.setLayoutX(canvasWidth / 2);
        partyModeBox.setLayoutY(canvasHeight / 2);
    }

    private void setTutorialOverlayPosition() {
        tutorialOverlay.toFront();
        tutorialOverlay.setLayoutX(canvasWidth - tutorialOverlay.getMaxWidth() - TUTORIAL_OVERLAY_X_OFFSET);
        tutorialOverlay.setLayoutY(TUTORIAL_OVERLAY_Y_OFFSET);
    }

    /**
     * adds listeners to content on the scorePanel
     */
    private void addRightHandSideListener(){
        btnQuickMenuTrack.setPickOnBounds(true);
        btnQuickMenuExit.setPickOnBounds(true);
        rightHandSide.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> AnimationUtils.focusNode(rightHandSide));
        rightHandSide.addEventHandler(MouseEvent.MOUSE_EXITED,
                e ->  AnimationUtils.dullNode(rightHandSide));
        btnHide.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> AnimationUtils.focusNode(btnHide));
        btnHide.addEventHandler(MouseEvent.MOUSE_EXITED,
                e ->  AnimationUtils.dullNode(btnHide));
        lblNoBoardClock.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> AnimationUtils.toggleHiddenBoardNodes(tblPlacingsRV, false, 0.8));
        lblNoBoardClock.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> AnimationUtils.toggleHiddenBoardNodes(tblPlacingsRV, true, 0.8));
        btnQuickMenuExit.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> quickMenuHighlight(true, lblExitRV, btnQuickMenuExit));
        btnQuickMenuExit.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> quickMenuHighlight(false, lblExitRV, btnQuickMenuExit));
        btnQuickMenuTrack.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e ->  quickMenuHighlight(true, lblTrackRV, btnQuickMenuTrack));
        btnQuickMenuTrack.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> quickMenuHighlight(false, lblTrackRV, btnQuickMenuTrack));
    }

    /**
     * shows a tutorial overlay on the screen
     * @param title the title shown in the overlay
     * @param content the tutorial content shown in the overlay
     */
    public void showTutorialOverlay(String title, String content){
        if(!tutorialContent.getText().equals(content) || !tutorialOverlayTitle.getText().equals(title)){
            tutorialOverlayTitle.setText(title);
            tutorialContent.setText(content);
            tutorialOverlay.setVisible(true);
            AnimationUtils.scalePop(tutorialOverlay);
        }
    }

    @FXML public void exitRunningRace() {
        ConnectionUtils.initiateDisconnect(options.isHost());
        DisplayUtils.resetZoom();
        displaySwitcher.loadMainMenu();
        soundController.setRunning(false);
        raceViewController.stop();
    }

    public void exitTerminatedRace() {
        displaySwitcher.loadMainMenu();
    }

    public void addDisplayBoat(BoatDisplay boatDisplay) {
        boatDisplayArrayList.add(boatDisplay);
    }

    public boolean hasHUDXMoved() {
        return hasHUDXMoved;
    }

    public void setHUDXMoved(boolean hasHUDXMoved) {
        this.hasHUDXMoved = hasHUDXMoved;
    }

    public boolean hasHUDYMoved() {
        return hasHUDYMoved;
    }

    public void setHUDYMoved(boolean hasHUDYMoved) {
        this.hasHUDYMoved = hasHUDYMoved;
    }

    public boolean hasTutorialXMoved() {
        return hasTutorialXMoved;
    }

    public void setTutorialXMoved(boolean hasTutorialXMoved) {
        this.hasTutorialXMoved = hasTutorialXMoved;
    }

    public boolean hasTutorialYMoved() {
        return hasTutorialYMoved;
    }

    public void setTutorialYMoved(boolean hasTutorialYMoved) {
        this.hasTutorialYMoved = hasTutorialYMoved;
    }

    private static class Delta {
        public static double x;
        public static double y;
    }

    /**
     * Takes the displayBoat and makes the annotation of that boat draggable.
     * As well as making the HUD draggable.
     * Via mouse or touch press.
     * Also sets up the click and drag panning of the course functionality
     */
    public void initCanvasAnchorListeners() {

        canvasAnchor.setOnMousePressed(event -> {
            for(BoatDisplay boatDisplay : boatDisplayArrayList) {

                if(checkAnnotationClick(event, boatDisplay)) {
                    moveAnnotation = true;
                    DisplayUtils.externalTouchEvent = true;
                    currentDisplayBoat = boatDisplay;
                }
            }
            if(checkHUDClick(event)) {
                dragging = true;
                headsUpDisplay.setCursor(Cursor.MOVE);
                moveHUD = true;
                DisplayUtils.externalTouchEvent = true;
            }
            if(checkTutorialClick(event)) {
                dragging = true;
                tutorialOverlay.setCursor(Cursor.MOVE);
                moveTutorial = true;
                DisplayUtils.externalTouchEvent = true;
            }
        });

        canvasAnchor.setOnMouseDragged(event -> {

            if(moveAnnotation && DisplayUtils.externalTouchEvent) {
                moveAnnotation(event);
                DisplayUtils.externalDragEvent = true;
            } else if(moveHUD) {
                moveHUD(event);
                DisplayUtils.externalDragEvent = true;
                hasHUDXMoved = false;
                hasHUDYMoved = false;
            } else if(moveTutorial) {
                moveTutorial(event);
                DisplayUtils.externalDragEvent = true;
                hasTutorialXMoved = false;
                hasTutorialYMoved = false;
            } else if (DisplayUtils.zoomLevel != 1 && !event.isSynthesized() && !DisplayUtils.externalDragEvent) {
                dragCourse(event);
            }

        });

        canvasAnchor.setOnMouseReleased(event -> {
            dragging = false;
            headsUpDisplay.setCursor(Cursor.HAND);
            moveAnnotation = false;
            moveHUD = false;
            moveTutorial = false;
            DisplayUtils.externalTouchEvent = false;
            DisplayUtils.externalDragEvent = false;
        });

        headsUpDisplay.setOnMouseMoved(event -> {
            if(!dragging) {
                headsUpDisplay.setCursor(Cursor.HAND);
            }
        });
    }

    /**
     * Checks to see if the mouse is clicked onto the annotation of a boat or not.
     * @param event Mouse event that is clicking on the annotation.
     * @param boatDisplay Boat that the annotation belongs to.
     * @return boolean that is true if the mouse click is on the annotation or false if it is not.
     */
    private boolean checkAnnotationClick(MouseEvent event, BoatDisplay boatDisplay) {
        VBox boatAnnotation = boatDisplay.getAnnotation();

        double annotationXPosition = event.getX() - boatAnnotation.getLayoutX();
        double annotationYPosition = event.getY() - boatAnnotation.getLayoutY();
        double annotationWidth = boatAnnotation.getWidth();
        double annotationHeight = boatAnnotation.getHeight();

        return (annotationXPosition <= annotationWidth && annotationXPosition >= 0 &&
                annotationYPosition <= annotationHeight && annotationYPosition >= 0);
    }

    /**
     * Checks to see if the mouse is clicked onto the HUD or not.
     * @param event Mouse event to check if the mouse is clicked on the HUD
     * @return boolean that is true if the mouse click is on the HUD or false if it is not.
     */
    private boolean checkHUDClick(MouseEvent event) {
        double HUDXPosition = event.getX() - headsUpDisplay.getLayoutX();
        double HUDYPosition = event.getY() - headsUpDisplay.getLayoutY();
        double HUDWidth = headsUpDisplay.getWidth();
        double HUDHeight = headsUpDisplay.getHeight();

        return (HUDXPosition <= HUDWidth && HUDXPosition >= 0 &&
                HUDYPosition <= HUDHeight && HUDYPosition >= 0);
    }

    /**
     * Checks to see if the mouse is clicked onto the tutorialOverlay or not.
     * @param event Mouse event to check if the mouse is clicked on the tutorialOverlay
     * @return boolean that is true if the mouse click is on the tutorialOverlay or false if it is not.
     */
    private boolean checkTutorialClick(MouseEvent event) {
        double tutorialXPosition = event.getX() - tutorialOverlay.getLayoutX();
        double tutorialYPosition = event.getY() - tutorialOverlay.getLayoutY();
        double tutorialWidth = tutorialOverlay.getWidth();
        double tutorialHeight = tutorialOverlay.getHeight();

        return (tutorialXPosition <= tutorialWidth && tutorialXPosition >= 0 &&
                tutorialYPosition <= tutorialHeight && tutorialYPosition >= 0);
    }

    /**
     * Takes the mouse event x and y coordinates if the mouse is clicked on an annotation and moves the annotation
     * correctly in accordance to the position of the mouse.
     * @param event Mouse event to get the x and y coordinates of the mouse to set correct placement.
     */
    private void moveAnnotation(MouseEvent event) {
        if (zoomLevel > 1 || (zoomLevel <= 1 && !isOutsideBounds(currentDisplayBoat.getAnnotation()))) {
            if (abs(event.getX() - Delta.x) < DRAG_TOLERANCE &&
                    abs(event.getY() - Delta.y) < DRAG_TOLERANCE) {
                double scaledChangeX = ((event.getX() - Delta.x) / zoomLevel);
                double scaledChangeY = ((event.getY() - Delta.y) / zoomLevel);
                currentDisplayBoat.setAnnoOffsetX(currentDisplayBoat.getAnnoOffsetX() + scaledChangeX);
                currentDisplayBoat.setAnnoOffsetY(currentDisplayBoat.getAnnoOffsetY() + scaledChangeY);
            }
        }
        Delta.x = event.getX();
        Delta.y = event.getY();
    }

    /**
     * Takes the mouse event x and y coordinates if the mouse is clicked on the HUD and moves the vbox
     * correctly in accordance to the position of the mouse.
     * @param event Mouse event to get the x and y coordinates of the mouse to set correct placement.
     */
    private void moveHUD(MouseEvent event) {
        if(zoomLevel > 1 || (zoomLevel <= 1 && !isOutsideBounds(headsUpDisplay))) {
            if (abs(event.getX() - Delta.x) < DRAG_TOLERANCE &&
                    abs(event.getY() - Delta.y) < DRAG_TOLERANCE) {
                double scaledChangeX = (event.getX() - Delta.x);
                double scaledChangeY = (event.getY() - Delta.y);
                headsUpDisplay.relocate(headsUpDisplay.getLayoutX() + scaledChangeX, headsUpDisplay.getLayoutY() + scaledChangeY);
            } else {
                dragCourse(event);
            }
        }
        Delta.x = event.getX();
        Delta.y = event.getY();
    }

    /**
     * Takes the mouse event x and y coordinates if the mouse is clicked on the tutorialOverlay and moves the vbox
     * correctly in accordance to the position of the mouse.
     * @param event Mouse event to get the x and y coordinates of the mouse to set correct placement.
     */
    private void moveTutorial(MouseEvent event) {
        if(zoomLevel > 1 || (zoomLevel <= 1 && !isOutsideBounds(tutorialOverlay))) {
            if (abs(event.getX() - Delta.x) < DRAG_TOLERANCE &&
                    abs(event.getY() - Delta.y) < DRAG_TOLERANCE) {
                double scaledChangeX = (event.getX() - Delta.x);
                double scaledChangeY = (event.getY() - Delta.y);
                tutorialOverlay.relocate(tutorialOverlay.getLayoutX() + scaledChangeX, tutorialOverlay.getLayoutY() + scaledChangeY);
            } else {
                dragCourse(event);
            }
        }
        Delta.x = event.getX();
        Delta.y = event.getY();
    }

    /**
     * Checks to see if the mouse is clicked on the HUD, and if so, if double click resets the HUD to its default
     * position in the top left corner.
     */
    private void resetVBoxPosition() {
        canvasAnchor.setOnMouseClicked(event -> {
            if(checkHUDClick(event)) {
                if (event.getClickCount() == 2) {
                    hasHUDXMoved = false;
                    hasHUDYMoved = false;
                    headsUpDisplay.relocate(150, 10);
                }
            }
            if(checkTutorialClick(event)) {
                if (event.getClickCount() == 2) {
                    hasTutorialXMoved = false;
                    hasTutorialYMoved = false;
                    tutorialOverlay.relocate(canvasWidth - tutorialOverlay.getMaxWidth() - 105, 67);
                }
            }
        });
    }

    /**
     * Calls DisplayUtils to move display and redraw course and paths as appropriate.
     */
    private void dragCourse(MouseEvent event) {
        if (DisplayUtils.zoomLevel != 1 && !event.isSynthesized() && !DisplayUtils.externalDragEvent) {
            DisplayUtils.dragDisplay((int) event.getX(), (int) event.getY());
            raceViewController.redrawCourse();
            raceViewController.redrawBoatPaths();
            selectionController.deselectBoat();
        }
    }

    /**
     * initilized two finger dragging of the course. allows for panning while zoomed.
     */
    private void initTouchDisplayDrag() {
        canvasAnchor.addEventFilter(TouchEvent.ANY, touch -> {
            if (touch.getTouchPoints().size() == 2 && DisplayUtils.zoomLevel != 1 && DisplayUtils.externalZoomEvent) {
                DisplayUtils.externalDragEvent = false;
                double touchX = (touch.getTouchPoints().get(0).getX() + touch.getTouchPoints().get(1).getX()) / 2;
                double touchY = (touch.getTouchPoints().get(0).getY() + touch.getTouchPoints().get(1).getY()) / 2;
                DisplayUtils.dragDisplay((int) touchX, (int) touchY);
                raceViewController.redrawCourse();
                raceViewController.redrawBoatPaths();
                selectionController.deselectBoat();
            }
        });
    }


    /**
     * adds a listener to the + and - keys to manage keyboard zooming
     */
    private void initKeyPressListener(){
        scene.addEventFilter(KeyEvent.KEY_PRESSED, key -> {
            if(key.getCode().equals(X) || key.getCode().equals(PLUS) || key.getCode().equals(EQUALS)){
                setZoomSliderValue(zoomSlider.getValue()+ 0.1);
            }
            if(key.getCode().equals(Z) || key.getCode().equals(MINUS) || key.getCode().equals(UNDERSCORE)){
                setZoomSliderValue(zoomSlider.getValue()- 0.1);
            }
        });
    }


    /**
     * initilises zoom listener
     */
    private void initZoomEventListener() {
        canvasAnchor.setOnZoom(zoom -> {
            DisplayUtils.externalZoomEvent = (zoom.getZoomFactor() > 0.96 && zoom.getZoomFactor() < 1.04);
            if(!DisplayUtils.externalZoomEvent) {
                zoomSlider.adjustValue(zoomSlider.getValue() * zoom.getZoomFactor());
            }
        });
    }

    /**
     * Initilizes zoom slider on display. Resets zoom on slide out
     */
    private void initZoom() {
        zoomSlider.valueProperty().addListener((arg0, arg1, arg2) -> {
            raceViewController.stopHighlightAnimation();
            zoomSlider.setOpacity(FOCUSED_ZOOMSLIDER_OPACITY);
            DisplayUtils.setZoomLevel(zoomSlider.getValue());
            if (DisplayUtils.zoomLevel != 1) {
                mapImageView.setVisible(false);
            } else {
                selectionController.setRotationOffset(0);
                root.getTransforms().clear();
                mapImageView.setVisible(true);
                nextMarkCircle.setVisible(false);
                DisplayUtils.resetOffsets();
                selectionController.setTrackingPoint(false);
            }
            raceViewController.redrawCourse();
            raceViewController.redrawBoatPaths();
        });
    }


    /**
     * Creates the change in width and height listeners to redraw course objects
     */
    private void createCanvasAnchorListeners() {

        final ChangeListener<Number> resizeListener = new ChangeListener<Number>() {
            final Timer timer = new Timer();
            TimerTask task = null;
            final long delayTime = 300;
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, final Number newValue) {
                if (task != null) {
                    task.cancel();
                    mapImageView.setEffect(new GaussianBlur(300));
                }
                task = new TimerTask() // create new task that calls your resize operation
                {
                    @Override
                    public void run() {
                        raceViewController.drawMap();
                        mapImageView.setEffect(null);
                    }
                };
                timer.schedule(task, delayTime);
            }
        };
        canvasAnchor.widthProperty().addListener(resizeListener);
        canvasAnchor.widthProperty().addListener((observable, oldValue, newValue) -> {
            canvasWidth = (double) newValue;
            anchorWidth = canvasAnchor.getWidth();
            raceViewController.redrawCourse();
            raceViewController.redrawBoatPaths();
            setPartyBoxPosition();
            setTutorialOverlayPosition();
            btnHide.setLayoutX(canvasWidth - 485.0);
        });
        canvasAnchor.heightProperty().addListener(resizeListener);
        canvasAnchor.heightProperty().addListener((observable, oldValue, newValue) -> {
            canvasHeight = (double) newValue;
            anchorHeight = canvasAnchor.getHeight();
            raceViewController.redrawCourse();
            setPartyBoxPosition();
            setTutorialOverlayPosition();
            raceViewController.redrawBoatPaths();

        });
    }

    /**
     * shows a popup informing user that the server has disconnected
     */
    public void showServerDisconnectError() {
        if(!options.isHost()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add("style/menuStyle.css");
            dialogPane.getStyleClass().add("myDialog");
            alert.setTitle("Server Disconnected");
            alert.setHeaderText("The Server has disconnected");
            alert.setContentText("The server appears to have " +
                    "disconnected. \nYou will be returned to " +
                    "the main menu");

            alert.setOnHidden(evt -> exitTerminatedRace());
            alert.show();
        }
    }

    /**
     * Display information about the success of the practice of crossing the start line. At the end of the practice time.
     */
    void displayFinishedPracticePopUp() {
        Boat boat = race.getCompetitors().get(0);
        if (boat.getLastRoundedMarkIndex() >= 0) {
            showTutorialOverlay("Finished", "You passed the start line! \nTime: " +
                    raceTimerString.get() + "\nSpeed: " + Math.round(boat.getCurrentSpeed()) + " knots");
        } else {
            showTutorialOverlay("Too late", "You failed to pass the start line at a reasonable time." +
                    "\nTry Again");
        }
        tutorialOverlay.setTranslateX(-450);
    }


    /**
     * Called from the RaceViewController handle if there is a change in race status
     * Handles the starters Overlay and timing for the boats objects to be created
     */
    public void updatePreRaceScreen() {
        switch (race.getRaceStatus()) {
            case WARNING:
                showStarterOverlay();
                if(!options.isParticipant() && options.getGameMode().equals(GameMode.PARTYGAME)){
                    this.infoDisplay = new HeadsupDisplay(String.format("%04d", GameClient.getRoomCode()), headsUpDisplay);
                }
                break;
            case PREPARATORY:
                hideStarterOverlay();
                if(!raceViewController.hasInitializedBoats()) {
                    raceViewController.initBoatHighlight();
                    raceViewController.initializeBoats();
                }
                break;
            case STARTED:
                if(options.isParticipant() && !options.isPractice()){
                    raceViewController.getCourseRouteArrows().removeRaceRoute();
                    scoreBoardController.getCoursePathToggle().setSelected(false);
                }
                if (startersOverlay.isVisible()) {
                    hideStarterOverlay();
                }
                if (!raceViewController.hasInitializedBoats()) {
                    raceViewController.initBoatHighlight();
                    raceViewController.initializeBoats();
                }
                raceViewController.initBoatPaths();
                break;
        }
    }

    /**
     * Populate the starters overlay list with boats that are competing
     */
    public void raceCompetitorOverview() {
        ObservableList<String> overViewBoatStrings = observableArrayList();
        overViewList.getItems().clear();
        for (Boat boat : race.getRaceOrder()) {
            if(race.isTerminated()){
                if (boat.getStatus().equals(BoatStatus.DNF)){
                    overViewBoatStrings.add(String.format("DNF: %s: %s", boat.getName(), boat.getFinalRaceTime()));
                } else {
                    overViewBoatStrings.add(String.format("%d: %s: %s", boat.getCurrPlacing(), boat.getName(), boat.getFinalRaceTime()));
                }
            } else {
                overViewBoatStrings.add(String.format("%s - %s", boat.getNickName(), boat.getName()));
            }
        }
        overViewList.setItems(overViewBoatStrings);
    }

    /**
     * Keep the placings list up to date based on last past marked of boats
     */
    public void updatePlacings() {
        List<Boat> raceOrder = race.getRaceOrder();
        formattedDisplayOrder.clear();
        for (int i = 0; i < raceOrder.size(); i++) {
            Boat boat = raceOrder.get(i);
            String displayString = "";
            if (boat.getLastRoundedMarkIndex() != -1) {
                displayString = String.format("%d : %s (%s) - ", i + 1, boat.getName(), boat.getNickName());
                if (raceOrder.get(i).isFinished()) {
                    displayString += "Finished!";
                } else if (raceOrder.get(i).getStatus() == BoatStatus.DNF) {
                    displayString += "DNF";
                } else {
                    displayString += String.format("%.3f knots", boat.getCurrentSpeed());
                }
            }
            formattedDisplayOrder.add(displayString);
        }
    }

    /**
     * Updates the fps counter to the current fps of the average of the last 100 frames of the Application.
     *
     * @param now Is the current time
     */
    public void updateFPSCounter(long now) {
        long oldFrameTime = frameTimes[frameTimeIndex];
        frameTimes[frameTimeIndex] = now;
        frameTimeIndex = (frameTimeIndex + 1) % frameTimes.length;
        if (frameTimeIndex == 0) {
            arrayFilled = true;
        }
        if (arrayFilled) {
            long elapsedNanos = now - oldFrameTime;
            long elapsedNanosPerFrame = elapsedNanos / frameTimes.length;
            double frameRate = 1_000_000_000.0 / elapsedNanosPerFrame;
            fpsString.set(String.format("%.1f", frameRate));
        }
    }

    /**
     * Updates the race clock to display the current time in race
     */
    public void updateRaceClock() {
        long secondsElapsed = (race.getCurrentTimeInEpochMs() - race.getStartTimeInEpochMs()) / 1000;
        int hours = (int) secondsElapsed / 3600;
        int minutes = ((int) secondsElapsed % 3600) / 60;
        int seconds = (int) secondsElapsed % 60;
        if (secondsElapsed < 0) {
            raceTimerString.set(String.format("-%02d:%02d:%02d", Math.abs(hours), Math.abs(minutes), Math.abs(seconds)));
        } else {
            raceTimerString.set(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        }
    }

    /**
     * displays the current time according to the UTC offset, in the GUI on the overlay
     * @param UTCOffset offset of the time zone you want to use
     */
    public void setTimeZone(double UTCOffset) {
        clockString.set(TimeUtils.setTimeZone(UTCOffset, race.getCurrentTimeInEpochMs()));
    }


    public void fpsLabel(Boolean visible) {
        fpsLabel.setVisible(visible);
    }

    /**
     * Causes the starters overlay to hide itself, enabling a proper view of the course and boats beneath
     */
    public void hideStarterOverlay() {
        AnimationUtils.fadeNode(startersOverlay, true);
    }

    public void showStarterOverlay() {
        startersOverlay.toFront();
        AnimationUtils.fadeNode(startersOverlay, false);
        AnimationUtils.fadeNode(partyModeBox, true);
    }

    public static void setCanvasHeight(double canvasHeight) {
        Controller.canvasHeight = canvasHeight;
    }

    public static void setCanvasWidth(double canvasWidth) {
        Controller.canvasWidth = canvasWidth;
    }

    public static double getCanvasHeight() {
        return canvasHeight;
    }

    public static double getCanvasWidth() {
        return canvasWidth;
    }

    public ObservableList<String> getFormattedDisplayOrder() {
        return formattedDisplayOrder;
    }

    public boolean hasRaceStatusChanged() {
        return raceStatusChanged;
    }

    public void setRaceStatusChanged(boolean raceStatusChanged) {
        this.raceStatusChanged = raceStatusChanged;
    }

    /**
     * Changes aspects of the race visualizer based on changes in the race object it observes
     * Updates the pre-race overlay when its informed race status has changed
     * Updates the race clock when the expected start time changes
     *
     * @param updatedRace the race that its race status changed
     * @param signal      determines which part of the race has changed
     */
    @Override
    public void update(Observable updatedRace, Object signal) {
        if (this.race == updatedRace && signal instanceof Integer) {
            Integer sig = (Integer) signal;
            switch (sig) {
                case Race.UPDATED_STATUS_SIGNAL:
                    raceStatusChanged = true;
                    break;
            }
        }
    }

    /**
     * sets up the user help label in the GUI
     * @param helper helper title
     */
    public void setUserHelpLabel(String helper, Color color) {
        lblUserHelp.setOpacity(0);
        lblUserHelp.setPrefWidth(canvasWidth);
        lblUserHelp.setMaxWidth(canvasWidth);
        lblUserHelp.setMinWidth(canvasWidth);
        lblUserHelp.setTextFill(color);
        lblUserHelp.setText(helper);
        DisplayUtils.fadeInFadeOutNodeTransition(lblUserHelp, 1, 4000);
    }

    /**
     * handles the toggling of screen elements when the side panel is toggled on and off
     */
    @FXML private void hideScoreboard(){
        if(scoreboardVisible){
            AnimationUtils.shiftPaneNodes(rightHandSide, 440, false);
            AnimationUtils.shiftPaneArrow(btnHide, 430, 1);
            AnimationUtils.shiftPaneNodes(imvSpeedScale, 430, true);
            AnimationUtils.shiftPaneNodes(lblWindSpeed, 430, true);
            AnimationUtils.shiftPaneNodes(nextMarkGrid, 430, true);
            AnimationUtils.shiftPaneNodes(quickMenu, -115, true);
            AnimationUtils.toggleHiddenBoardNodes(lblNoBoardClock, false, 0.8);
            if ((options.isParticipant() && infoDisplay != null) || (!options.isParticipant() && options.getGameMode().equals(GameMode.PARTYGAME))) {
                headsUpDisplay.setVisible(true);
            }
            scoreboardVisible = false;
            raceViewController.shiftArrow(false);
            setUpTable();
        } else {
            rightHandSide.setVisible(true);
            AnimationUtils.shiftPaneNodes(rightHandSide, -440, true);
            AnimationUtils.shiftPaneArrow(btnHide, -430, -1);
            AnimationUtils.shiftPaneNodes(imvSpeedScale, -430, true);
            AnimationUtils.shiftPaneNodes(lblWindSpeed, -430, true);
            AnimationUtils.shiftPaneNodes(nextMarkGrid, -430, true);
            AnimationUtils.shiftPaneNodes(quickMenu, 115, true);
            AnimationUtils.toggleHiddenBoardNodes(lblNoBoardClock, true, 0.8);
            if(infoDisplay != null){
                headsUpDisplay.setVisible(false);
            }
            scoreboardVisible = true;
            raceViewController.shiftArrow(true);
        }
    }

    public void setUpTouchInputController(TouchInputController touchInputController) {
        touchInputController.setUp(root, touchPane, this);
    }

    public void setUpKeyInputController(KeyInputController keyInputController) {
        keyInputController.setController(this);
    }


    public class ColoredTextListCell extends ListCell<String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setText(item);
            setTextFill(UNSELECTED_BOAT_COLOR);

            BoatDisplay userBoat = raceViewController.getCurrentUserBoatDisplay();
            if(userBoat != null && item != null){
                if(item.contains(userBoat.getBoat().getName())){
                    setTextFill(SELECTED_BOAT_COLOR);
                }
            }

        }
    }

    public static double getAnchorHeight() {
        return anchorHeight;
    }

    public static double getAnchorWidth() {
        return anchorWidth;
    }

    public void setZoomSliderValue(double level) {
        raceViewController.stopHighlightAnimation();
        zoomSlider.setValue(level);
    }


    public Circle getWindCircle() {
        return windCircle;
    }

    public Circle getNextMarkCircle() {
        return nextMarkCircle;
    }

    public AnchorPane getCanvasAnchor() {
        return canvasAnchor;
    }

    @FXML
    private void zoomCursorHover() {
        DisplayUtils.fadeNodeTransition(zoomSlider, FOCUSED_ZOOMSLIDER_OPACITY);
    }

    @FXML
    private void zoomCursorExitHover() {
        DisplayUtils.fadeNodeTransition(zoomSlider, IDLE_ZOOMSLIDER_OPACITY);
    }

    @FXML
    private void btnTrackPressed(){
        selectionController.trackBoat();
    }

    public ListView<String> getOverViewList() {
        return overViewList;
    }

    public boolean isScoreboardVisible() {
        return scoreboardVisible;
    }

    public void blurScreen(boolean blur) {
        blurNode(stackPane, blur);
        stackPane.setScaleX(1.08);
        stackPane.setScaleY(1.08);
    }

    /**
     * sets up the rv table when the board is toggled
     */
    private void setUpTable(){
        columnName.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
        columnPosition.setCellValueFactory(cellData -> cellData.getValue().getCurrPlacingProperty().asObject());
        columnSpeed.setCellValueFactory(cellData -> Bindings.format("%.2f kn", cellData.getValue().getSpeedProperty()));
        columnStatus.setCellValueFactory(cellData -> cellData.getValue().getStatusProperty());

        refreshTable();
        tblPlacingsRV.getSortOrder().add(columnPosition);
        columnPosition.setStyle( "-fx-alignment: CENTER;");
        columnName.setStyle( "-fx-alignment: CENTER;");
        columnSpeed.setStyle( "-fx-alignment: CENTER;");
        columnStatus.setStyle( "-fx-alignment: CENTER;");

        BoatDisplay userBoat = raceViewController.getCurrentUserBoatDisplay();
        if(userBoat != null){
            tblPlacingsRV.getSelectionModel().select(userBoat.getBoat());
        }
    }

    private void blurNode(Node node, boolean blur) {
        if (blur) {
            node.setEffect(new GaussianBlur(25));
        } else {
            node.setEffect(null);
        }
    }

    /**
     * refreshes the RV table when a competitor is added
     * order of boats is updated based on placings
     */
    public void refreshTable(){
        Callback<Boat, javafx.beans.Observable[]> cb =(Boat boat) -> new javafx.beans.Observable[]{boat.getCurrPlacingProperty()};
        ObservableList<Boat> observableList = FXCollections.observableArrayList(cb);
        observableList.addAll(race.getObservableCompetitors());
        SortedList<Boat> sortedList = new SortedList<>( observableList,
                (Boat boat1, Boat boat2) -> {
                    if( boat1.getCurrPlacingProperty().get() < boat2.getCurrPlacingProperty().get() ) {
                        return -1;
                    } else if( boat1.getCurrPlacingProperty().get() > boat2.getCurrPlacingProperty().get() ) {
                        return 1;
                    } else {
                        return 0;
                    }
                });

        tblPlacingsRV.setItems(sortedList);
    }

    public void addUserBoatHUD(){
        this.infoDisplay = new HeadsupDisplay(raceViewController.getCurrentUserBoatDisplay(), headsUpDisplay, race);
    }

    public void refreshHUD(){
        if (infoDisplay != null) {
            infoDisplay.competitorAdded();
        }
    }

    /**
     * sets up the tutorial mode by hiding the side panel and extra panes
     */
    public void setUpTutorialMode(){
        rightHandSide.setVisible(false);
        lblNoBoardClock.setVisible(false);
        btnHide.setVisible(false);
        AnimationUtils.shiftPaneNodes(imvSpeedScale, 430, true);
        AnimationUtils.shiftPaneNodes(lblWindSpeed, 430, true);
        AnimationUtils.shiftPaneNodes(quickMenu, -115, true);
    }

    private void quickMenuHighlight(boolean isEntered, Label label, Button button){
        if(isEntered){
            AnimationUtils.focusNode(button);
            AnimationUtils.toggleQuickMenuNodes(label, false);
        }else{
            AnimationUtils.dullNode(button);
            AnimationUtils.toggleQuickMenuNodes(label, true);
        }
    }

    private void initHiddenScoreboard(){
        rightHandSide.setTranslateX(rightHandSide.getTranslateX() + 440);
        rightHandSide.setVisible(false);
        btnHide.setRotate(180);
        lblNoBoardClock.setOpacity(0.8);
        if(options.isTutorial()){
            lblNoBoardClock.setVisible(false);
        }else{
            lblNoBoardClock.setVisible(true);
        }
        AnimationUtils.shiftPaneNodes(btnHide, 430, true);
        AnimationUtils.shiftPaneNodes(imvSpeedScale, 430, true);
        AnimationUtils.shiftPaneNodes(lblWindSpeed, 430, true);
        AnimationUtils.shiftPaneNodes(nextMarkGrid, 430, true);
        AnimationUtils.shiftPaneNodes(quickMenu, -115, true);
        scoreboardVisible = false;
        raceViewController.shiftArrow(false);
        setUpTable();
    }

    public void setSoundController(SoundController soundController) {
        this.soundController = soundController;
    }

    public VBox getHUD() {
        return headsUpDisplay;
    }

    public VBox getTutorialOverlay() {
        return tutorialOverlay;
    }


}