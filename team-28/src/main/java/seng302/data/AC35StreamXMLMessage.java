package seng302.data;

/**
 * Created by Michael Trotter on 4/29/2017.
 */
public enum AC35StreamXMLMessage {

    RACE_XML_MESSAGE(6), BOAT_XML_MESSAGE(7), REGATTA_XML_MESSAGE(5);

    private final int type;

    AC35StreamXMLMessage(int type){
        this.type = type;
    }

    public int getType(){
        return this.type;
    }

    public static AC35StreamXMLMessage fromInteger(int value) {
        switch (value) {
            case 5:
                return REGATTA_XML_MESSAGE;
            case 6:
                return RACE_XML_MESSAGE;
            case 7:
                return BOAT_XML_MESSAGE;
            default:
                return null;
        }
    }

}
