package seng302.data;

/**
 * Created by Michael Trotter on 4/29/2017.
 */
public enum BoatStatus {
    UNDEFINED(0, "Undefined"), PRERACE(1, "Prerace"), RACING(2, "Racing"), FINISHED(3, "Finished"), DNS(4, "DNS"), DNF(5, "DNF"), DISQUALIFIED(6, "DQ"), OCS(7, "OCS"); //OCS = On Course Side, i.e. crossed start line early

    private final int value;
    private final String text;

    BoatStatus(int value, String text){
        this.value = value;
        this.text = text;
    }

    public int getValue() {return value;}

    public String getText(){return text;}
}
