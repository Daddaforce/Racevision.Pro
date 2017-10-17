package seng302.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import javafx.scene.input.KeyCode;
import seng302.data.BoatAction;
import seng302.data.ClientPacketBuilder;


import static org.junit.Assert.assertEquals;
import static seng302.data.AC35StreamField.BOAT_ACTION_BODY;
import static seng302.data.AC35StreamField.BOAT_ACTION_SOURCE_ID;
import static seng302.data.AC35StreamMessage.BOAT_ACTION_MESSAGE;
import static seng302.data.BoatAction.DOWNWIND;
import static seng302.controllers.listeners.Listener.byteArrayRangeToInt;

/**
 * Created by gla42 on 21/07/17.
 *
 */
public class ReceivingCorrectButtonPressTest {


    private ClientPacketBuilder clientPacketBuilder = new ClientPacketBuilder();
    private int commandInt;
    private byte[] header;
    private byte[] body;
    private int action;
    private int sourceId;
    private int HEADER_LENGTH = 15;


    @Given("^the user has pressed a button$")
    public void the_user_has_pressed_a_button() throws Throwable {
        commandInt = BoatAction.getTypeFromKeyCode(KeyCode.PAGE_DOWN);
    }

    @Given("^this has been sent to the receiver$")
    public void this_has_been_sent_to_the_receiver() throws Throwable {
        byte[] packet = clientPacketBuilder.createBoatCommandPacket(commandInt, 101);
        header = new byte[HEADER_LENGTH];
        body = new byte[BOAT_ACTION_MESSAGE.getLength()];

        int packetIndex = 0;
        for (int i = 0; i < HEADER_LENGTH; i++){
            header[packetIndex] = packet[i];
            packetIndex ++;
        }
        packetIndex = 0;
        for (int i = HEADER_LENGTH; i < HEADER_LENGTH + BOAT_ACTION_MESSAGE.getLength(); i++){
            body[packetIndex] = packet[i];
            packetIndex ++;
        }
    }

    @When("^the receiver decodes this packet$")
    public void the_receiver_decodes_this_packet() throws Throwable {
        action = byteArrayRangeToInt(body, BOAT_ACTION_BODY.getStartIndex(), BOAT_ACTION_BODY.getEndIndex());
        sourceId = byteArrayRangeToInt(body, BOAT_ACTION_SOURCE_ID.getStartIndex(), BOAT_ACTION_SOURCE_ID.getEndIndex());

    }
    @Then("^the receiver will know what button was pressed$")
    public void the_receiver_will_know_what_button_was_pressed() throws Throwable {
        BoatAction boatAction = BoatAction.getBoatActionFromInt(action);
        assertEquals(DOWNWIND, boatAction);
    }

    @Then("^the receiver will know which boat that maps too$")
    public void theReceiverWillKnowWhichBoatThatMapsToo() throws Throwable {
        assertEquals(101, sourceId);
    }


    @Given("^the user has pressed an incorrect button$")
    public void theUserHasPressedAnIncorrectButton() throws Throwable {

        commandInt = BoatAction.getTypeFromKeyCode(KeyCode.H);
    }

    @Then("^the receiver will know that it is an incorrect button$")
    public void theReceiverWillKnowThatItIsAnIncorrectButton() throws Throwable {
        BoatAction boatAction = BoatAction.getBoatActionFromInt(action);
        assertEquals(null, boatAction);
    }
}
