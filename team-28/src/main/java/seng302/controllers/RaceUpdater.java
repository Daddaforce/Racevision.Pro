package seng302.controllers;

import seng302.data.BoatStatus;
import seng302.data.CourseName;
import seng302.data.RaceStatus;
import seng302.data.RaceVisionXMLParser;
import seng302.models.*;
import seng302.utilities.MathUtils;
import seng302.utilities.TimeUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import static seng302.data.RaceStatus.*;

/**
 * Created by Michael Trotter on 4/29/2017.
 * Creates and runs a mock race to be sent out over the MockStream
 */

public class RaceUpdater implements Runnable {

    private final double SECONDS_PER_UPDATE = 0.02;
    private double scaleFactor = 1;
    private final double WARNING_SIGNAL_TIME_IN_MS = (1000 * 60 * 3);
    private final double PREPARATORY_SIGNAL_TIME_IN_MS = (1000 * 60 * 2);
    private final double MIN_WIND_SPEED = 6.0;
    private final double MAX_WIND_SPEED = 24.0;
    private final double MAX_EXTRA_TIME = TimeUtils.secondsToMilliseconds(TimeUtils.convertMinutesToSeconds(10.0));
    private double initialWindSpeed;
    private final int MAX_BOATS_IN_RACE = 6;
    private Race race;
    private Collection<Boat> potentialCompetitors;
    private CollisionManager collisionManager;
    private Coordinate startingPosition;
    private boolean serverRunning;
    private boolean isPractice;
    private long secondsElapsed;
    private double timeOfFirstFinisher, millisBeforeStart, raceSecondsPassed;
    private boolean oneBoatHasFinished, atLeastOneBoatNotFinished;
    private double timer;

    public RaceUpdater(String selectedCourse){
        collisionManager = new CollisionManager();
        initialWindSpeedGenerator();
        RaceVisionXMLParser raceVisionXMLParser = new RaceVisionXMLParser();
        raceVisionXMLParser.setCourseFile(selectedCourse);
        potentialCompetitors = raceVisionXMLParser.importDefaultStarters();
        race = raceVisionXMLParser.importRace();
        race.setRegattaName(CourseName.courseNameFromXMLName(selectedCourse));
        Course course = race.getCourse();
        course.setTrueWindSpeed(initialWindSpeed);
        course.setWindDirection(course.getWindDirectionBasedOnGates());
        this.serverRunning = true;
        initialize();
    }

    public RaceUpdater(Race race) {
        this.race = race;
        initialize();
    }

    public void initialize(){
        //for now we assume all boats racing are AC35 class yachts such that we can use the polars we have for them
        race.updateRaceStatus(RaceStatus.PRESTART);
        long currentTime = Instant.now().toEpochMilli();
        race.setCurrentTimeInEpochMs(currentTime);
        race.setStartTimeInEpochMs(currentTime + (1000 * 60 * 3)); //3 minutes from now
    }


    /**
     * Adds a competitor from the potential competitors collection into the race
     * @return the id of the added competitor, or -1 if max number reached.
     */
    public int addCompetitor() {
        if (potentialCompetitors.iterator().hasNext()) {
            Boat newCompetitor = potentialCompetitors.iterator().next();
            potentialCompetitors.remove(newCompetitor);
            race.addCompetitor(newCompetitor);
            prepareBoatForRace(newCompetitor);
            return newCompetitor.getId();
        } else {
            return -1;
        }
    }

    /**
     * Adds a competitor from the potential competitors collection into the race, changes the competitor to AI
     * @return the id of the added competitor, or -1 if max number reached.
     */
    public int addAICompetitor(AIDifficulty AIDifficulty) {
        if (potentialCompetitors.iterator().hasNext()) {
            Boat newCompetitor = potentialCompetitors.iterator().next();
            potentialCompetitors.remove(newCompetitor);
            Boat aiCompetitor = new AIBoat(newCompetitor.getId(), newCompetitor.getName(), newCompetitor.getNickName(),
                    newCompetitor.getMaxSpeed(), race.getCourse(), AIDifficulty);
            race.addCompetitor(aiCompetitor);
            aiCompetitor.setSailsIn(true);
            prepareBoatForRace(aiCompetitor);
            return aiCompetitor.getId();
        } else {
            return -1;
        }
    }

    /**
     * Generates a race id from the current date and time
     * @return a string of formated date
     */
    private String generateRaceId() {
        DateFormat dateFormat = new SimpleDateFormat("yyMMddHH");
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * the main loop of the race server
     */
    @Override
    public void run() {
        raceSecondsPassed = SECONDS_PER_UPDATE * scaleFactor;
        isPractice = RaceVisionXMLParser.courseFile.equals("PracticeStart-course.xml");
        if (isPractice) race.updateRaceStatus(PREPARATORY);
        oneBoatHasFinished = false;
        timeOfFirstFinisher = 0;
        while (!race.getRaceStatus().isRaceEndedStatus() && serverRunning) {
            atLeastOneBoatNotFinished = false;
            updateRaceTimes();
            updateRaceStartStatus();
            generateWind();
            if (race.hasStarted() || race.getRaceStatus().equals(RaceStatus.PREPARATORY)) {
                collisionManager.checkForCollisions(race);
            }
            for (Boat boat : race.getCompetitors()) {
                updateBoat(boat);
            }
            checkRaceTermination();
            try {
                Thread.sleep((long) (SECONDS_PER_UPDATE * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Updates the variables for the race times
     */
    private void updateRaceTimes() {
        millisBeforeStart = race.getStartTimeInEpochMs() - race.getCurrentTimeInEpochMs();
        secondsElapsed = (race.getCurrentTimeInEpochMs() - race.getStartTimeInEpochMs()) / 1000;
        race.setCurrentTimeInEpochMs(race.getCurrentTimeInEpochMs() + (long)(raceSecondsPassed * 1000));
    }

    /**
     * Updates the status of the race from WARNING to STARTED based on the race times
     */
    private void updateRaceStartStatus() {
        if (race.getRaceStatus().equals(STARTED) || millisBeforeStart < 0 && race.getRaceStatus().equals(RaceStatus.PREPARATORY)){
            race.updateRaceStatus(RaceStatus.STARTED);
            for(Boat boat : race.getCompetitors()){
                if(!boat.getStatus().equals(BoatStatus.DNF) && !boat.isFinished()){
                    boat.setStatus(BoatStatus.RACING);
                }
            }
        }else if(millisBeforeStart < WARNING_SIGNAL_TIME_IN_MS && millisBeforeStart > PREPARATORY_SIGNAL_TIME_IN_MS) {
            race.updateRaceStatus(WARNING);
        }else if(millisBeforeStart < PREPARATORY_SIGNAL_TIME_IN_MS && millisBeforeStart > 0){
            race.updateRaceStatus(RaceStatus.PREPARATORY);
        }
    }

    /**
     * Checks the terminating conditions of the race
     */
    private void checkRaceTermination() {
        if(oneBoatHasFinished){
            if (race.getCurrentTimeInEpochMs() - timeOfFirstFinisher >= MAX_EXTRA_TIME){
                for (Boat boatInRace: race.getCompetitors()){
                    if (!boatInRace.isFinished()){
                        boatInRace.setStatus(BoatStatus.DNF);
                    }
                }
                race.updateRaceStatus(RaceStatus.TERMINATED);
            } else if(race.getCompetitors().size() == 1) {
                race.updateRaceStatus(RaceStatus.TERMINATED);
            }
        }
        if (race.getCompetitors().size() > 0 && !atLeastOneBoatNotFinished) {
            race.updateRaceStatus(RaceStatus.TERMINATED);
        } else if(isPractice){
            Boat boat = race.getCompetitors().get(0); // Should be only one boat in practice mode
            if(secondsElapsed > 60 || boat.getLastRoundedMarkIndex() == 0){
                race.updateRaceStatus(TERMINATED);
            }
        }
    }

    /**
     * Updates the boat's location, speed and heading in the race.
     * @param boat The boat to be updated.
     */
    private void updateBoat(Boat boat){
        if(race.hasStarted() || race.getRaceStatus().equals(RaceStatus.PREPARATORY)){
            if (collisionManager.boatIsInCollision(boat) && !boat.isFinished()) {
                //revert the last location update as it was a collision
                boat.updateLocation(-raceSecondsPassed, race.getCourse());
                boat.setCurrentSpeed(boat.getCurrentSpeed() - 0.8);
                if (boat instanceof AIBoat){
                    boat.setSailsIn(true);
                    boat.setCurrentSpeed(0);
                    timer = 0;
                }
            }
            if(boat.isFinished()) {
                boat.setSailsIn(true);
            }
            adjustSpeed(boat);
            if(boat instanceof AIBoat){
                if(millisBeforeStart < AIBoat.START_MOVING_TIME_MS){
                    AIBoat aiBoat = (AIBoat) boat;
                    if (!(collisionManager.boatIsInCollision(boat)) && timer > 5) {
                        aiBoat.setSailsIn(false);
                    }
                    timer += raceSecondsPassed;
                    aiBoat.move(raceSecondsPassed, race.getCourse());
                }
            } else {
                boat.move(raceSecondsPassed, race.getCourse());
                Course course = race.getCourse();
                if (race.getCourse().getCourseOrder().size() > 0 && race.getRaceStatus().equals(STARTED) && !boat.isFinished()) {
                    checkMarkRounding(boat, course);
                }
            }
            calculateTimeAtNextMark(boat);
        }
        checkBoatStatus(boat);
    }


    /**
     * Records and updates a boat's status to be racing or finished
     * @param boat The boat to be updated
     */
    private void checkBoatStatus(Boat boat){
        BoatStatus currBoatStatus = boat.getStatus();
        if (currBoatStatus.equals(BoatStatus.FINISHED) && !oneBoatHasFinished){
            oneBoatHasFinished = true;
            timeOfFirstFinisher = race.getCurrentTimeInEpochMs();
        }
        if (!currBoatStatus.equals(BoatStatus.FINISHED) && !currBoatStatus.equals(BoatStatus.DNF)) {
            atLeastOneBoatNotFinished = true;
        }
        if (currBoatStatus.equals(BoatStatus.DNF)) {
            boat.setCurrentSpeed(0);
        }
        if(currBoatStatus.equals(BoatStatus.PRERACE) && race.getRaceStatus().equals(RaceStatus.STARTED)){
            boat.setStatus(BoatStatus.RACING);
        }
    }


    /**
     * Adjusts the speed of the boat based on sails luffing or damage taken
     * @param boat the boat who's speed we are adjusting
     */
    private void adjustSpeed(Boat boat) {
        if(boat.isSailsIn() && boat.getCurrentSpeed() > 0){
            boat.setCurrentSpeed(boat.getCurrentSpeed() - 0.2);
            if(boat.getCurrentSpeed() < 0) boat.setCurrentSpeed(0);
        } else if(!boat.isSailsIn()){
            boat.setMaxSpeed(boat.updateBoatSpeed(race.getCourse())-boat.getDamageSpeed());
            if(boat.getCurrentSpeed() < boat.getMaxSpeed()){
                boat.setCurrentSpeed(boat.getCurrentSpeed() + 0.1);
            }
            if(boat.getCurrentSpeed() > boat.getMaxSpeed() + 1) {
                boat.setCurrentSpeed(boat.getMaxSpeed());
            }
        }
    }

    /**
     * Checks the course feature rounding of each boat given
     * @param boat current boat
     * @param course current course the boat is in
     */
    private void checkMarkRounding(Boat boat, Course course) {
        CompoundMark currentMark = course.getCourseOrder().get(boat.getLastRoundedMarkIndex() + 1);
        CompoundMark previousMark = null;
        CompoundMark nextMark = null;
        if (!currentMark.isStartLine()){
            previousMark = course.getCourseOrder().get(boat.getLastRoundedMarkIndex());
        }
        if(!currentMark.isFinishLine()){
            nextMark = course.getCourseOrder().get(boat.getLastRoundedMarkIndex() + 2);
        }
        if(currentMark.isStartLine()){
            if(RoundingMechanics.boatPassedThroughCompoundMark(boat, course.getStartLine(), startingPosition, true)){
                boat.setLastRoundedMarkIndex(boat.getLastRoundedMarkIndex() + 1);
            }
        } else if (currentMark.isFinishLine()) {
            if(RoundingMechanics.boatPassedThroughCompoundMark(boat, course.getFinishLine(), previousMark.getPosition(), true)) {
                boat.setLastRoundedMarkIndex(boat.getLastRoundedMarkIndex() + 1);
                boat.setSailsIn(true);
                boat.setStatus(BoatStatus.FINISHED);
                race.updateRaceOrder();
            }
        } else if (!currentMark.hasTwoMarks()){
            RoundingMechanics.boatHeadingToMark(boat, currentMark, previousMark, nextMark);
        } else if(currentMark.hasTwoMarks()) {
            RoundingMechanics.boatHeadingToGate(boat, currentMark, previousMark, nextMark);
        }
    }

    /**
     * Initializes the boat status and its starting position
     * @param boat the boat to be initialized for
     */
    private void prepareBoatForRace(Boat boat) {
        setStartingPosition(boat);
        boat.updateBoatSpeed(race.getCourse());
        boat.setLastRoundedMarkIndex(-1);
        boat.setStatus(BoatStatus.PRERACE);
    }

    /**
     * Spreads the starting positions of the boats behind the start line
     * @param boat the boat that needs to starting position to be set
     */
    public void setStartingPosition(Boat boat){
        RaceLine startingLine = race.getCourse().getStartLine();
        CompoundMark startingEnd2 = new CompoundMark(-2, "", new Mark(-2, "", startingLine.getMark1().getPosition()));
        CompoundMark startingEnd1 = new CompoundMark(-1, "", new Mark(-1, "", startingLine.getMark2().getPosition()));
        double heading1 = (MathUtils.calculateBearingBetweenTwoPoints(startingEnd1,startingEnd2));
        double heading2 = (MathUtils.calculateBearingBetweenTwoPoints(startingEnd2,startingEnd1));
        double heading3;
        if(heading1 < heading2) {
            heading3 = heading1;
        } else {
            heading3 = heading2;
        }

        Coordinate startPosition1 = startingEnd1.getPosition().coordAt(0.15, heading3 + 90);
        Coordinate startPosition2 = startingEnd2.getPosition().coordAt(0.15, heading3 + 90);

        if(!MathUtils.boatBeforeStartline(startPosition1, startingLine, race.getCourse().getCourseOrder().get(1))){
            startPosition1 = startingEnd1.getPosition().coordAt(0.15, heading3 + 270);
            startPosition2 = startingEnd2.getPosition().coordAt(0.15, heading3 + 270);
        }

        Double dLat = (startPosition2.getLat() - startPosition1.getLat()) / (MAX_BOATS_IN_RACE / 2);
        Double dLon = (startPosition2.getLon() - startPosition1.getLon()) / (MAX_BOATS_IN_RACE / 2);
        if (boat.getId() % 2 == 0){
            dLat *= -1;
            dLon *= -1;
        }
        Double curLat = startPosition1.getLat() + (dLat * race.getCompetitors().size());
        Double curLon = startPosition1.getLon() + (dLon * race.getCompetitors().size());
        boat.setPosition(curLat, curLon);
        boat.setHeading(boat.getCurrentPosition().headingToCoordinate(startingLine.getPosition()));
        boat.setTargetHeading(boat.getHeading());
        startingPosition = new Coordinate(curLat, curLon);
    }

    /**
     * Updates the boats time to the next mark
     * @param boat the current boat that is being updated.
     */
    private void calculateTimeAtNextMark(Boat boat){
        ArrayList<CompoundMark> order = race.getCourse().getCourseOrder();
        if (boat.getLastRoundedMarkIndex() + 1 < order.size()) {
            CompoundMark nextMark = order.get(boat.getLastRoundedMarkIndex() + 1);
            Coordinate boatLocation = boat.getCurrentPosition();
            Coordinate markLocation = nextMark.getPosition();
            double dist = TimeUtils.calcDistance(boatLocation.getLat(), markLocation.getLat(), boatLocation.getLon(), markLocation.getLon());
            double testTime = dist / boat.getCurrentVMG();
            double time = (TimeUtils.convertHoursToSeconds(testTime) * 1000) + race.getCurrentTimeInEpochMs(); //time at next mark in milliseconds
            try {
                if (nextMark.isFinishLine()){
                    boat.setTimeTillFinish((long) time);
                }
                boat.setTimeTillMark((long) time);
            } catch (NumberFormatException ignored){ // Throws error at start when trying to convert ∞ to a double
            }
        }
    }

    /**
     * generates a random windspeed and wind angle within a range of the current speed and angle
     */
    public void generateWind(){
        double range = 0.05;
        double maxSpeed = race.getCourse().getTrueWindSpeed() + range;
        double minSpeed = race.getCourse().getTrueWindSpeed() - range;
        double speed = ThreadLocalRandom.current().nextDouble(minSpeed, maxSpeed);

        if(speed > MIN_WIND_SPEED && speed < MAX_WIND_SPEED) {
            race.getCourse().setTrueWindSpeed(speed);
        }
    }

    public Race getRace() {
        return race;
    }

    /**
     * @return whether or not the MockRaceRunner has finished generating new data for a race
     */
    public boolean raceHasEnded(){
        return race.getRaceStatus().isRaceEndedStatus();
    }

    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public void setRace(Race race) {
        this.race = race;
    }

    /**
     * Sets the race status to started so we don't have to wait around for the prerace countdown
     * Currently used in tutorial mode.
     */
    public void skipPrerace() {race.updateRaceStatus(STARTED);
    }

    /**
     * Randomly generates an initial wind speed between race regulations of 6-24 knots
     */
    private void initialWindSpeedGenerator(){
        Random random = new Random();
        initialWindSpeed = MIN_WIND_SPEED + (MAX_WIND_SPEED - MIN_WIND_SPEED) * random.nextDouble();
    }

    public void stopRunning(){
        this.serverRunning = false;
    }

    public CollisionManager getCollisionManager() {
        return collisionManager;
    }


}
