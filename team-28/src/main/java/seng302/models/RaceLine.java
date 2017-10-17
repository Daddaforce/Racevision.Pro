package seng302.models;

import javafx.scene.shape.Line;
import seng302.models.CompoundMark;
import seng302.utilities.DisplayUtils;

/**
 * Inheriting from compound mark it is used to determine a start or finish point of a race.
 * A line is drawn between the 2 lat lon.
 */
public class RaceLine extends CompoundMark {
    private Line line;

    public RaceLine(Integer compoundMarkID, String name, Mark mark1, Mark mark2){
        super(compoundMarkID, name, mark1, mark2);
        setPositionAsMidpoint();
    }

    private void setPositionAsMidpoint(){
        setPosition(DisplayUtils.midPointFromTwoCoords(mark1.getPosition(), mark2.getPosition()));
    }

    public Line getLine() {
        return line;
    }

    public void setLine(Line line) {
        this.line = line;
    }
}
