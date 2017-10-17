package seng302.models;

import seng302.utilities.DisplayUtils;

/**
 * Created on 7/03/17.
 * Class to specify the marks/gates on the course.
 */

public class CompoundMark {

    public enum MarkType {
        START, FINISH, NORMAL
    }
	protected Mark mark1;
    protected Mark mark2;
	private String name;
    private Integer compoundMarkID;
    private MarkType type;
    private Coordinate position;

    public CompoundMark(Integer compoundMarkID, String name, Mark mark1, Mark mark2){
        this.name = name;
        this.compoundMarkID = compoundMarkID;
        this.type = MarkType.NORMAL;
        this.mark1 = mark1;
        this.mark2 = mark2;
        if (hasTwoMarks()){
            position = DisplayUtils.midPointFromTwoCoords(mark1.getPosition(), mark2.getPosition());
        } else{
            position = mark1.getPosition();
        }
    }

    public CompoundMark(Integer compoundMarkID, String name, Mark mark1){
        this(compoundMarkID, name, mark1, null);
    }

    public boolean hasTwoMarks(){
        return mark2 != null;
    }

    public Integer getCompoundMarkID(){
        return compoundMarkID;
    }

    public String getName(){
        return this.name;
    }

    public void setMarkAsStart(){
        this.type = MarkType.START;
    }

    public void setMarkAsFinish(){
        this.type = MarkType.FINISH;
    }

    public boolean isStartLine(){
        return this.type == MarkType.START;
    }
    public boolean isFinishLine(){
        return this.type == MarkType.FINISH;
    }

    public void setPosition(Coordinate position) {
        this.position = position;
    }

    public Coordinate getPosition() {
        return position;
    }

    public Mark getMark1() {
        return mark1;
    }

    public Mark getMark2() {
        return mark2;
    }

    public void setMark2(Mark mark2) {
        this.mark2 = mark2;
    }

    /**
     * Converts a CompoundMark object to a RaceLine object
     * @param compoundMark the compound mark to be converted
     * @param type the type of the raceline (either start or finish)
     * @return the new converted raceline object
     */
    public static RaceLine convertToRaceLine(CompoundMark compoundMark, MarkType type){
        RaceLine raceLine = new RaceLine(compoundMark.getCompoundMarkID(), compoundMark.getName(),
                compoundMark.getMark1(), compoundMark.getMark2());
        if(type == MarkType.START){
            raceLine.setMarkAsStart();
        } else if(type == MarkType.FINISH){
            raceLine.setMarkAsFinish();
        }
        return raceLine;
    }
}
