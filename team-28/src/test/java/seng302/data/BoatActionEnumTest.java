package seng302.data;

import javafx.scene.input.KeyCode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by lga50 on 20/07/17.
 *
 */
public class BoatActionEnumTest {

    @Test
    public void getTypeFromKeyCodeTest(){
        int type = BoatAction.getTypeFromKeyCode(KeyCode.PAGE_UP);
        assertEquals(type, 5);
    }

    @Test
    public void getBoatActionFromIntTest(){
        BoatAction action = BoatAction.getBoatActionFromInt(1);
        assertEquals(action, BoatAction.BOAT_VMG);
    }

    @Test
    public void getTypeFromRandomKeyCodeTest(){
        int type = BoatAction.getTypeFromKeyCode(KeyCode.P);
        assertEquals(-1, type);
    }
}
