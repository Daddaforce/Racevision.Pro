package seng302.models;

import javafx.scene.Group;
import javafx.scene.shape.Line;
import seng302.utilities.MathUtils;

/**
 * Created by swa158 on 15/05/17.
 * A class which holds angles and Line objects for a pair of laylines
 */
public class Laylines {

    private double rightOfTWDAngle;
    private double leftOfTWDAngle;
    private Line layline1;
    private Line layline2;
    private boolean shouldDraw;

    /**
     * Removes this objects laylines from touchPane
     * @param root the node to remove the laylines from
     */
    public void removeDrawnLines(Group root) {
        if (layline1 != null && layline2 != null) {
            root.getChildren().remove(layline1);
            root.getChildren().remove(layline2);
        }
        layline1 = null;
        layline2 = null;
    }

    public double getRightOfTWDAngle() { return rightOfTWDAngle; }

    public double getLeftOfTWDAngle() { return leftOfTWDAngle; }

    public void setDrawnlines(Line layline1, Line layline2) {
        this.layline1 = layline1;
        this.layline2 = layline2;
    }

    /**
     * Calculates and sets the angles for a pair of laylines. Sets 'shouldDraw' true if heading to a windward gate,
     * otherwise sets to false
     * @param TWD True Wind Direction
     * @param lastMark the last passed mark of the boat the laylines are for
     * @param nextMark the mark the boat is heading for
     * @param polarTable a polar table for the boat defining the optimum TWA
     */
    public void calculateLaylineAngle(double TWD, CompoundMark lastMark, CompoundMark nextMark, PolarTable polarTable) {
        Coordinate lastMarkPosition = lastMark.getPosition();
        Coordinate nextMarkPosition = nextMark.getPosition();
        double markBearing = lastMarkPosition.headingToCoordinate(nextMarkPosition);
        boolean upwind = MathUtils.pointBetweenTwoAngle(TWD, 90, markBearing);
        double TWA = polarTable.getOptimumTWA(upwind);
        this.rightOfTWDAngle = TWD + TWA;
        this.leftOfTWDAngle = TWD - TWA;
        shouldDraw = upwind;
    }

    public boolean shouldDraw() {
        return shouldDraw;
    }
}
