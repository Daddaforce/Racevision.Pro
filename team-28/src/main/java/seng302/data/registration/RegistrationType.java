package seng302.data.registration;

/**
 * Created by mjt169 on 8/08/17.
 * Holds the different types of Registrations that exist and their byte values for messages
 */
public enum RegistrationType {
    SPECTATOR(0), PLAYER(1), TUTORIAL(2), GHOST(3), REQUEST_RUNNING_GAMES(4);

    private final byte requestValue;

    RegistrationType(int requestValue) {
        this.requestValue = (byte) requestValue;
    }

    public byte value() {
        return this.requestValue;
    }

    /**
     * Convert from byte requestValue into RegistrationType
     * @param regoByte the byte to convert
     * @return a RegistrationType represented by the given regoByte
     */
    public static RegistrationType getTypeFromByte(byte regoByte) {
        for (RegistrationType regoType : RegistrationType.values()) {
            if (regoType.value() == regoByte) {
                return regoType;
            }
        }
        return null;
    }
}
