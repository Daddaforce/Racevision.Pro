package seng302.models;

import static seng302.data.RaceStatus.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seng302.data.BoatStatus;
import seng302.data.RaceStatus;
import seng302.utilities.TimeUtils;

import java.util.*;


/**
 * Created on 7/03/17.
 * A Race encompasses the course and the competitors.
 */
public class Race extends Observable{

    public static final int UPDATED_STATUS_SIGNAL = 1;
    public static final int UPDATED_COURSE_SIGNAL = 2;
    public static final int UPDATED_COMPETITORS_SIGNAL = 3;
    private String id;
    private String regattaName;
    private Course course;
    private List<Boat> competitors = Collections.synchronizedList(new ArrayList<>());
    private ObservableList observableCompetitorsList  = FXCollections.observableArrayList();
    private List<Boat> raceOrder = new ArrayList<>();
    private Map<Integer, Boat> boatIdMap = new HashMap<>();
    private double totalRaceTime;
    private RaceStatus raceStatus = NOT_ACTIVE;
    private long startTimeInEpochMs, currentTimeInEpochMs;
    private double UTCOffset;
    private boolean firstMessage = true;
    private Set<Integer> competitorIds = new HashSet<>();
    private boolean abruptEnd;

    public Race(String name, Course course, List<Boat> competitors) {
        this.regattaName = name;
        this.course = course;
        this.competitors = competitors;
        raceOrder.addAll(competitors);
        for(Boat competitor : competitors){
            boatIdMap.put(competitor.getId(), competitor);
        }
        updateRaceOrder();
        raceStatus = NOT_ACTIVE;
    }

    public Race(){}

    /**
     * Updates the position, speed and heading of the a boat with a given source id
     * @param sourceID the source id of the boat
     * @param lat the new latitude of the boat
     * @param lon the new longitude of the boat
     * @param heading the new heading of the boat
     * @param speed the new speed of the boat
     */
    public void updateBoat(Integer sourceID, Double lat, Double lon, Double heading, Double speed, double twa){
        if(boatIdMap.containsKey(sourceID)){
            Boat boat = boatIdMap.get(sourceID);
            boat.setPosition(lat, lon);

            if(raceStatus.equals(STARTED) && !raceStatus.equals(TERMINATED)){
                if(boat.getPathCoords().size() == 0){
                    boat.addPathCoord(new Coordinate(lat, lon));
                } else if (boat.getHeading() != heading){
                    boat.addPathCoord(new Coordinate(boat.getCurrentLat(), boat.getCurrentLon()));
                }
            }

            boat.setHeading(heading);
            boat.setCurrentSpeed(speed);
            boat.setTWAofBoat(twa);
        }
    }

    public List<Boat> getCompetitors() {
        return new ArrayList<>(this.competitors);
    }

    public String getRegattaName() {
        return regattaName;
    }

    public void setRegattaName(String name) { this.regattaName = name; }

    public Course getCourse() {
        return course;
    }

    public List<Boat> getRaceOrder() {
        return raceOrder;
    }

    public double getTotalRaceTime() {
        return totalRaceTime;
    }

    /**
     * Calculates the total time it will take for the race to complete using the total course distance and the
     * slowest boat's speed.
     */
    public void setTotalRaceTime(){

        double slowestBoatSpeed = Double.MAX_VALUE;
        for (Boat competitor : competitors) {
            slowestBoatSpeed = Math.min(slowestBoatSpeed, competitor.getMaxSpeed());
        }
        double courseDistance = 0;
        ArrayList<CompoundMark> courseOrder = course.getCourseOrder();
        for (int i = 1; i < courseOrder.size(); i++){
            courseDistance += course.distanceBetweenMarks(i - 1, i);
        }

        this.totalRaceTime = TimeUtils.convertHoursToSeconds(courseDistance / slowestBoatSpeed);
    }

    /**
     * Updates the race status and notifies observers
     * @param newRaceStatus The new race status read in
     */
    public void updateRaceStatus(RaceStatus newRaceStatus) {
        if(raceStatus != newRaceStatus){
            raceStatus = newRaceStatus;
            setChanged();
            notifyObservers(UPDATED_STATUS_SIGNAL);
        }
    }

    /**
     * Updates a boat's last rounded mark based on the ids of boat and the rounded mark's id, and update the race
     * order accordingly.
     * If a mark occurs multiple times in the race order, the rounded mark index will be the one next occurrence
     * of the mark that the boat has not rounded yet.
     * @param sourceID the boat's id
     * @param roundedMarkIndex the mark's index in race order
     * @param time the time that the boat rounded the mark
     */
    public void updateMarkRounded(int sourceID, int roundedMarkIndex, long time) {
        Boat boat = boatIdMap.get(sourceID);
        if(boat != null){
            boat.setLastRoundedMarkIndex(roundedMarkIndex);
            boat.setLastRoundedMarkTime(time);
            updateRaceOrder();
        }
    }

    /**
     * Checks if race is terminated or not
     * @return true is race has terminated status, false otherwise
     */
    public boolean isTerminated(){
        return raceStatus == TERMINATED;
    }

    public long getStartTimeInEpochMs() {
        return startTimeInEpochMs;
    }

    public void setStartTimeInEpochMs(long startTimeInEpochMs) {
        this.startTimeInEpochMs = startTimeInEpochMs;
    }

    public long getCurrentTimeInEpochMs() {
        return currentTimeInEpochMs;
    }

    public void setCurrentTimeInEpochMs(long currentTimeInEpoch) {
        this.currentTimeInEpochMs = currentTimeInEpoch;
    }

    public void updateRaceOrder() {
        Collections.sort(raceOrder);
        for (int i = 0; i < raceOrder.size(); i++) {
            Boat boat = raceOrder.get(i);
            boat.setCurrPlacing(i + 1);
        }
    }

    public RaceStatus getRaceStatus() {
        return raceStatus;
    }

    public double getUTCOffset() { return UTCOffset; }

    public void setUTCOffset(double UTCOffset) { this.UTCOffset = UTCOffset; }

    public Boat getBoatById(Integer id){
        if(boatIdMap == null){
            return null;
        }
        if(boatIdMap.containsKey(id)){
            return boatIdMap.get(id);
        } else{
            return null;
        }
    }

    public boolean isInitialized(){
        return course != null && competitors != null;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isFirstMessage() {
        return firstMessage;
    }

    public void setFirstMessage(boolean firstMessage) {
        this.firstMessage = firstMessage;
    }

    private List<Boat> filterNonCompetitors(List<Boat> possibleCompetitors){
        List<Boat> participantsInRace = new ArrayList<>();
        for(Boat boat : possibleCompetitors){
            if(competitorIds.contains(boat.getId())){
                participantsInRace.add(boat);
            }
        }
        return participantsInRace;
    }

    public void setCompetitors(List<Boat> competitors) {
        List<Boat> actualCompetitors = filterNonCompetitors(competitors);
        this.competitors = actualCompetitors;

        raceOrder.addAll(actualCompetitors);
        boatIdMap = new HashMap<>();
        for (Boat competitor : actualCompetitors) {
            boatIdMap.put(competitor.getId(), competitor);
            if(!observableCompetitorsList.contains(competitor)){
                this.observableCompetitorsList.add(competitor);
            }
        }
    }

    public void setCompetitorIds(Set<Integer> competitorIds) {
        this.competitorIds = competitorIds;
    }

    public String getId() {
        return id;
    }

    public void addCompetitor(Boat newCompetitor) {
        this.competitorIds.add(newCompetitor.getId());
        this.boatIdMap.put(newCompetitor.getId(), newCompetitor);
        this.competitors.add(newCompetitor);
        this.observableCompetitorsList.add(newCompetitor);
        this.raceOrder.add(newCompetitor);
        newCompetitor.setSpawnTime(currentTimeInEpochMs);
        setChanged();
        notifyObservers(UPDATED_COMPETITORS_SIGNAL);
    }

    public void terminateRace(){
        raceStatus = RaceStatus.TERMINATED;
    }

    public Set<Integer> getCompetitorIds() {
        return competitorIds;
    }

    /**
     * @return true if the race status is has passed the initial stage
     */
    public boolean hasStarted() {
        return this.raceStatus.equals(RaceStatus.STARTED);
    }

    public ObservableList<Boat> getObservableCompetitors(){
        return observableCompetitorsList;
    }

    public void setAbruptEnd(boolean abruptEnd) {
        this.abruptEnd = abruptEnd;
    }

    public boolean getAbruptEnd() {
        return abruptEnd;
    }

    public void setBoatHealth(Integer boatID, Integer boatHealth) {
        boatIdMap.get(boatID).setBoatHealth(boatHealth);
    }
}

