package seng302.data;

/**
 * Created by Michael Trotter on 4/29/2017.
 *
 */
public enum AC35StreamMessage {
    REGISTRATION_REQUEST(55, 4), REGISTRATION_RESPONSE(56, 5), XML_MESSAGE(26), BOAT_LOCATION_MESSAGE(37, 56),
    MARK_ROUNDING_MESSAGE(38, 21), RACE_STATUS_MESSAGE(12), UNKNOWN(0),
    BOAT_ACTION_MESSAGE(100, 5), YACHT_EVENT_CODE(29, 22), BOAT_STATE_MESSAGE(103, 5), HOST_GAME_MESSAGE(108, 14),
    GAME_CANCEL(109, 8), REQUEST_AVAILABLE_RACES(114, 2), PARTY_MODE_CODE_MESSAGE(122, 2), WEB_CLIENT_INIT(120, 37), WEB_CLIENT_UPDATE(121, 9);

    private final int type, length;

    AC35StreamMessage(int type, int length){
        this.type = type;
        this.length = length;
    }

    AC35StreamMessage(int type){
        this.type = type;
        this.length = -1;
    }

    public int getValue(){
        return this.type;
    }

    public int getLength(){
        return this.length;
    }

    public static AC35StreamMessage fromInteger(int messageTypeValue) {
        for(AC35StreamMessage messageType: AC35StreamMessage.values()){
            if(messageType.getValue() == messageTypeValue){
                return messageType;
            }
        }
        return null;
    }
}
