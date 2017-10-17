package seng302.data;

/**
 * Created by atc60 on 15/08/17.
 */
public enum RoundingSide {
    PORT("Port"), STBD("STBD"), PORT_STBD("PS"), STBD_PORT("SP");

    private String roundingSideString;

    RoundingSide(String roundingSideString){
        this.roundingSideString = roundingSideString;
    }

    public static RoundingSide parseRoundingSide(String roundingSideString){
        roundingSideString = roundingSideString.toLowerCase();
        for(RoundingSide roundingSide : RoundingSide.values()){
            if(roundingSide.getRoundingSideString().toLowerCase().equals(roundingSideString)){
                return roundingSide;
            }
        }
        return null;
    }

    public String getRoundingSideString() {
        return roundingSideString;
    }

    /**
     * Returns the rounding side for the first mark in a compound mark
     * @return A rounding side (either port or stbd) for the first mark
     */
    public RoundingSide firstMarkRoundingSide(){
        if(this == PORT_STBD){
            return PORT;
        } else if(this == STBD_PORT){
            return STBD;
        } else{
            //Single mark rounding.
            return this;
        }
    }
}
