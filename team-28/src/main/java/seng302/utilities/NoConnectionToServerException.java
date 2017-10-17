package seng302.utilities;

public class NoConnectionToServerException extends Exception {
    private boolean isLocalError;

    public NoConnectionToServerException(Boolean isLocalError, String message) {
        super(message);
        this.isLocalError = isLocalError;
    }

    public boolean isLocalError(){return isLocalError;}
}
