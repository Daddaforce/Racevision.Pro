package seng302.data;

import org.junit.Test;
import seng302.data.registration.RegistrationType;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static seng302.data.AC35StreamField.*;
import static seng302.data.AC35StreamMessage.BOAT_ACTION_MESSAGE;
import static seng302.controllers.listeners.Listener.byteArrayRangeToInt;

/**
 * Created by dhl25 on 20/07/17.
 */
public class ClientPacketTest {

    private ClientPacketBuilder clientPacketBuilder = new ClientPacketBuilder();
    private int CRC_LENGTH = 4;
    private final int HEADER_LENGTH = 15;

    @Test
    public void testClientActionPacket(){
        byte[] packet = clientPacketBuilder.createBoatCommandPacket(6, 101);
        byte[] body = new byte[BOAT_ACTION_MESSAGE.getLength()];

        System.arraycopy(packet, HEADER_LENGTH, body, 0, BOAT_ACTION_MESSAGE.getLength());

        int action = byteArrayRangeToInt(body, BOAT_ACTION_BODY.getStartIndex(), BOAT_ACTION_BODY.getEndIndex());
        assertEquals(6, action);

        int sourceId = byteArrayRangeToInt(body, BOAT_ACTION_SOURCE_ID.getStartIndex(), BOAT_ACTION_SOURCE_ID.getEndIndex());
        assertEquals(101, sourceId);
    }

    @Test
    public void testClientActionPacketIsIgnored(){
        byte[] packet = clientPacketBuilder.createBoatCommandPacket(12, 101);
        byte[] body = new byte[BOAT_ACTION_MESSAGE.getLength()];
        System.arraycopy(packet, HEADER_LENGTH, body, 0, BOAT_ACTION_MESSAGE.getLength());

        int action = byteArrayRangeToInt(body, BOAT_ACTION_BODY.getStartIndex(), BOAT_ACTION_BODY.getEndIndex());
        assertEquals(12, action);
    }

    @Test
    public void testRegistrationRequestAsPlayer() {
        byte[] packet = clientPacketBuilder.createRegistrationRequestPacket(RegistrationType.PLAYER);
        byte[] body = Arrays.copyOfRange(packet, HEADER_LENGTH, packet.length - CRC_LENGTH); //extract body

        byte requestType = (byte)byteArrayRangeToInt(body, REGISTRATION_REQUEST_TYPE.getStartIndex(), REGISTRATION_REQUEST_TYPE.getEndIndex());
        RegistrationType request = RegistrationType.getTypeFromByte(requestType);
        assertEquals(RegistrationType.PLAYER, request);
    }

    @Test
    public void testRegistrationRequestAsSpectator() {
        byte[] packet = clientPacketBuilder.createRegistrationRequestPacket(RegistrationType.SPECTATOR);
        byte[] body = Arrays.copyOfRange(packet, HEADER_LENGTH, packet.length - CRC_LENGTH); //extract body

        byte requestType = (byte)byteArrayRangeToInt(body, REGISTRATION_REQUEST_TYPE.getStartIndex(), REGISTRATION_REQUEST_TYPE.getEndIndex());
        RegistrationType request = RegistrationType.getTypeFromByte(requestType);
        assertEquals(RegistrationType.SPECTATOR, request);
    }
}
