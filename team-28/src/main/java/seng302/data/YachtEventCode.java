package seng302.data;

/**
 * Created by mjt169 on 25/07/17.
 */
public enum YachtEventCode {
    COLLISION(33), COLLISION_PENALTY(34), COLLISION_MARK(35), OUT_OF_BOUNDS(36);

    private int code;

    YachtEventCode(int code) {
        this.code = code;
    }

    public int code(){
        return this.code;
    }

}
