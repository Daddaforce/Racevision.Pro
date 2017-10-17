package seng302.utilities;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import seng302.data.RoundingSide;
import seng302.models.*;
import seng302.views.BoatDisplay;
import java.time.Instant;
import java.util.Objects;

/**
 * Created by lga50 on 6/04/17.
 *
 */
public class DisplayTimeUtils {
    private String regex = "^(\\d\\d:\\d\\d)";
    private BoatDisplay boatDisplay;
    private static Course defaultCourse;


    @Before
    public void setUp(){
        Boat boat = new Boat(-1, "New Zealand", "NZ",  20.00);
        boatDisplay = new BoatDisplay(boat, null);

        defaultCourse = new Course();

        Mark startLine1 = new Mark(0, "Start Line 1", new Coordinate(32.296577, -64.854304));
        Mark startLine2 = new Mark(1, "Start Line 2", new Coordinate(32.293771, -64.855242));
        RaceLine start = new RaceLine(1, "Start Line", startLine1, startLine2);

        Mark mark1 = new Mark(2, "Mark 1", new Coordinate(32.293039, -64.843983));
        CompoundMark mark = new CompoundMark(2, "Mark", mark1);

        Mark gate1 = new Mark(3, "Gate 1", new Coordinate(32.284680, -64.850045));
        Mark gate2 = new Mark(4, "Gate 2", new Coordinate(32.280164, -64.847591));
        CompoundMark gate = new CompoundMark(3, "Gate", gate1, gate2);

        Mark finishLine1 = new Mark(5, "Finish Line 1", new Coordinate(32.317379, -64.839291));
        Mark finishLine2 = new Mark(6, "Finish Line 2", new Coordinate(32.317257, -64.836260));
        RaceLine finish = new RaceLine(4, "Finish", finishLine1, finishLine2);

        defaultCourse.addNewCompoundMark(start);
        defaultCourse.addNewCompoundMark(mark);
        defaultCourse.addNewCompoundMark(gate);
        defaultCourse.addNewCompoundMark(finish);

        defaultCourse.addMarkInOrder(1, RoundingSide.PORT);
        defaultCourse.addMarkInOrder(2, RoundingSide.PORT);
        defaultCourse.addMarkInOrder(3, RoundingSide.PORT);
        defaultCourse.addMarkInOrder(4, RoundingSide.PORT);

        defaultCourse.addToBoundary(new Coordinate(32.318879, -64.840291));
        defaultCourse.addToBoundary(new Coordinate(32.328879, -64.840291));
        defaultCourse.addToBoundary(new Coordinate(32.328879, -64.865304));
        defaultCourse.addToBoundary(new Coordinate(32.318879, -64.865304));
    }

    @Test
    public void TestTimeTill(){
        long currentTime = Instant.now().toEpochMilli();
        long timeAtMark = Instant.now().toEpochMilli() + 1000000;
        boatDisplay.getBoat().setHeading(304);
        String formattedTime = DisplayUtils.getTimeToNextMark(timeAtMark, currentTime, boatDisplay.getBoat(), defaultCourse);
        assert(formattedTime.matches(regex));

        String time = DisplayUtils.getTimeToNextMark(4000000,0, boatDisplay.getBoat(), defaultCourse);
        String time1 = DisplayUtils.getTimeToNextMark(2000,0, boatDisplay.getBoat(), defaultCourse);

        Assert.assertTrue(time.equals("..."));
        Assert.assertTrue(!time1.equals("..."));
    }

    @Test
    public void TestTimeAfter(){
        long currentTime = Instant.now().toEpochMilli();
        String formattedTime = DisplayUtils.getTimeSinceLastMark(currentTime, boatDisplay.getBoat());
        assert(Objects.equals(formattedTime, "..."));
    }
}