package seng302.controllers;

import org.junit.Test;
import seng302.models.Boat;
import seng302.models.CompoundMark;
import seng302.models.Coordinate;
import seng302.models.Mark;

/**
 * Created by lga50 on 14/08/17.
 *
 */
public class RoundingMechanicsTest {

    private Boat boat = new Boat(0, "TestBoat", "testNickname", 10);
    private Mark gate1 = new Mark(1, "Gate 1", new Coordinate(3, 2));
    private Mark gate2 = new Mark(2, "Gate 2", new Coordinate(3, 3));
    private CompoundMark currentMark = new CompoundMark(11, "gate", gate1, gate2);
    private Mark mark1 = new Mark(3, "Mark 1", new Coordinate(2, 2));
    private CompoundMark previousMark = new CompoundMark(10, "Mark", mark1);

    private Mark mark2 = new Mark(4, "Mark 4", new Coordinate(0, 0));
    private CompoundMark currMark = new CompoundMark(13, "gate", mark1, mark2);

    @Test
    public void boatPassedThroughCompoundMarkTest(){
        boolean throughCompoundMark = RoundingMechanics.boatPassedThroughCompoundMark(boat, currentMark, previousMark.getPosition(), true);
        assert(!throughCompoundMark);

        throughCompoundMark = RoundingMechanics.boatPassedThroughCompoundMark(boat, currentMark, currMark.getPosition(), true);
        assert(!throughCompoundMark);
    }

    @Test
    public void boatPassedMark(){
        boolean passed = RoundingMechanics.boatPassedMark(boat, currentMark, previousMark.getPosition(), mark2.getPosition());
        assert(!passed);
    }
}
