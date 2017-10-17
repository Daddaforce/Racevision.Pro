package seng302.data;

import org.junit.Test;
import seng302.data.registration.RegistrationType;

import static org.junit.Assert.*;

/**
 * Created by mjt169 on 9/08/17.
 */
public class RegistrationTypeTest {

    @Test
    public void playerRegistration() {
        RegistrationType regoType = RegistrationType.getTypeFromByte((byte) 1);
        assertEquals(RegistrationType.PLAYER, regoType);
    }

    @Test
    public void spectatorRegistration() {
        RegistrationType regoType = RegistrationType.getTypeFromByte((byte) 0);
        assertEquals(RegistrationType.SPECTATOR, regoType);
    }

}