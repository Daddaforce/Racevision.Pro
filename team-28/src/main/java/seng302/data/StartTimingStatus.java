package seng302.data;

/**
 * Created by gla42 on 17/05/17.
 */
public enum StartTimingStatus {
    EARLY(0), ONTIME(1), LATE(2), INRACE(3); //If the boat is going to be more than 5 seconds late the boat is late

    private final int value;

    StartTimingStatus(int value){ this.value = value; }

    public int getValue() {return value;}
}