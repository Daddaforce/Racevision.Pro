package seng302.models;

import org.junit.Test;
import seng302.models.CompoundMark;

import static org.junit.Assert.*;

/**
 * Created by Michael Trotter on 3/17/2017.
 */
public class CompoundMarkTest {

    @Test
    public void initializeMarkTest(){
        Mark startLine1 = new Mark(0, "Start Line 1", new Coordinate(0, 0));
        Mark startLine2 = new Mark(1, "Start Line 2", new Coordinate(0, 1));
        RaceLine start = new RaceLine(0, "Start", startLine1, startLine2);
        start.setMarkAsStart();

        Mark finishLine1 = new Mark(2, "Finish Line 1", new Coordinate(0, 5));
        Mark finishLine2 = new Mark(3, "Finish Line 2", new Coordinate(0, 6));
        RaceLine finish = new RaceLine(1, "Finish", finishLine1, finishLine2);
        finish.setMarkAsFinish();

        Mark mark1 = new Mark(4, "Mark 1", new Coordinate(2, 2));
        CompoundMark compoundMark = new CompoundMark(2, "Mark", mark1);

        start.setMarkAsStart();
        finish.setMarkAsFinish();

        assertTrue(start.isStartLine());
        assertFalse(start.isFinishLine());

        assertTrue(finish.isFinishLine());
        assertFalse(finish.isStartLine());

        assertFalse(compoundMark.isFinishLine());
        assertFalse(compoundMark.isStartLine());
    }

    @Test
    public void hasTwoMarksTest(){
        Mark mark1 = new Mark(4, "Mark 1", new Coordinate(2, 2));
        CompoundMark compoundMark1 = new CompoundMark(2, "Mark 1", mark1);

        assertFalse(compoundMark1.hasTwoMarks());

        Mark mark2 = new Mark(5, "Mark 2", new Coordinate(3, 3));
        CompoundMark compoundMark2 = new CompoundMark(3, "Mark 2", mark1, mark2);
        assertTrue(compoundMark2.hasTwoMarks());
    }
}