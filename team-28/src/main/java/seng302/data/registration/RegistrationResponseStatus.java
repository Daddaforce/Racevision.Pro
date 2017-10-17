package seng302.data.registration;

/**
 * Created by mjt169 on 9/08/17.
 */
public enum RegistrationResponseStatus {
    SPECTATOR_SUCCESS(0), PLAYER_SUCCESS(1), TUTORIAL_SUCCESS(2), GHOST_SUCCESS(3),

    GENERAL_FAILURE(10), OUT_OF_SLOTS(11), INCORRECT_CLIENT_TYPE(12), RACE_UNAVAILABLE(13);

    private byte value;

    RegistrationResponseStatus(int value) {
        this.value = (byte) value;
    }

    public byte value(){
        return this.value;
    }

    /**
     * Convert from byte value into RegistrationResponseStatus
     * @param statusByte the byte to convert
     * @return a RegistrationResponseType represented by the given statusByte
     */
    public static RegistrationResponseStatus getStatusFromByte(byte statusByte) {
        for (RegistrationResponseStatus status : RegistrationResponseStatus.values()) {
            if (status.value() == statusByte) {
                return status;
            }
        }
        return null;
    }
}
