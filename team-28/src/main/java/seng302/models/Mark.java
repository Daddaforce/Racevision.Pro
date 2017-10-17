package seng302.models;

import javafx.scene.shape.Circle;

/**
 * Created by raych on 20/04/2017.
 */
public class Mark {
    private Integer sourceID;
    private Coordinate position;
    private Circle icon;
    private String name;

    public Mark(Integer sourceID, String name, Coordinate position) {
        this.position = position;
        this.sourceID = sourceID;
        this.name = name;
    }

    public Coordinate getPosition() {
        return position;
    }

    public void setPosition(Coordinate position) { this.position = position; }

    public Integer getSourceID() {
        return sourceID;
    }

    public Circle getIcon() {
        return icon;
    }

    public void setIcon(Circle icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }
}
