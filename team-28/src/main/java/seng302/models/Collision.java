package seng302.models;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by mjt169 on 25/07/17.
 */
public class Collision {

    private static Integer lastUsedIncidentId = 0;

    private Set<Integer> involvedBoats = new HashSet<>();
    private Set<Integer> atFaultBoats = new HashSet<>();
    private Integer incidentId;
    private boolean isOutOfBounds = false;

    public Collision(){
        incidentId = lastUsedIncidentId++;
    }

    /** Add a boat to keep track of that was involved in the collision.
     * @param boatId the boat involved in the collision
     */
    public void addBoat(Integer boatId) {
        involvedBoats.add(boatId);
    }

    /**
     *
     * @param boatId a boat in the collision who is at fault
     */
    public void addAtFaultBoat(Integer boatId) {
        if (!involvedBoats.contains(boatId)) {
            involvedBoats.add(boatId);
        }
        atFaultBoats.add(boatId);
    }

    public Boolean boatIsInCollision(Integer boatId){
        return involvedBoats.contains(boatId);
    }

    public Set<Integer> getInvolvedBoats() {
        return Collections.unmodifiableSet(involvedBoats);
    }

    public Integer getIncidentId() {
        return incidentId;
    }

    public boolean boatIsAtFault(Integer boatId) {
        return atFaultBoats.contains(boatId);
    }

    public boolean isSingleBoatCollision() {
        return involvedBoats.size() == 1;
    }

    public boolean isOutOfBounds() {
        return isOutOfBounds;
    }

    public void setOutOfBounds(boolean outOfBounds) {
        isOutOfBounds = outOfBounds;
    }
}
