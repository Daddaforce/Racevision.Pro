package seng302.views;

import javafx.scene.Parent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import seng302.data.RaceVisionXMLParser;
import seng302.models.Course;
import seng302.models.Mark;
import seng302.utilities.DisplayUtils;

import java.util.Map;

/**
 * class to handle a map displayed in the menu selection
 */
public class CourseMap {

    private String mapName;
    private Integer numberOfMarks;
    private String estTimeToRace;
    private Polygon mapBoundary;
    private Course course;
    private CourseRouteArrows arrowedRoute;
    private Line startLine;
    private Line finishLine;
    private RaceView raceView = new RaceView();
    private final int X_OFFSET = 155;
    private final int Y_OFFSET = 30;

    public CourseMap(String mapName, String estTimeToRace) {
        this.mapName = mapName;
        this.estTimeToRace = estTimeToRace;
        loadCourse();
        setUpBoundary();
        setUpMarks();
        setUpLines();
        this.numberOfMarks = course.getCourseOrder().size();
    }

    private void loadCourse(){
        RaceVisionXMLParser parser = new RaceVisionXMLParser();
        this.course = parser.importCourse(CourseMap.class.getResourceAsStream("/defaultFiles/" + getXML()));
        course.initCourseLatLon();
    }

    private void setUpBoundary(){
        DisplayUtils.setMaxMinLatLon(course.getMinLat(), course.getMinLon(), course.getMaxLat(), course.getMaxLon());
        this.mapBoundary = raceView.createCourseBoundary(course.getBoundary());
    }

    /**
     * sets up the start and finish lines in the main menu
     */
    private void setUpLines(){
        this.startLine = raceView.createRaceLine(course.getStartLine().getMark1().getPosition(), course.getStartLine().getMark2().getPosition());
        this.finishLine = raceView.createRaceLine(course.getFinishLine().getMark1().getPosition(), course.getFinishLine().getMark2().getPosition());
        finishLine.setTranslateX(finishLine.getTranslateX() + X_OFFSET);
        finishLine.setTranslateY(finishLine.getTranslateY() + Y_OFFSET);
        startLine.setTranslateX(startLine.getTranslateX() + X_OFFSET);
        startLine.setTranslateY(startLine.getTranslateY() + Y_OFFSET);
    }

    /**
     * creates circles for all of the marks in the course
     */
    private void setUpMarks(){
        for (Mark mark : course.getAllMarks().values()) {
            Circle circle = raceView.createMark(mark.getPosition());
            circle.setCenterX(circle.getCenterX() + X_OFFSET);
            circle.setCenterY(circle.getCenterY() + Y_OFFSET);
            circle.setRadius(3f);
            mark.setIcon(circle);
            mark.getIcon().setScaleX(1);
            mark.getIcon().setScaleY(1);
        }
    }

    /**
     * sets up the arrowed route in the main menu
     * @param root
     */
    public void setUpArrowRoute(Parent root){
        DisplayUtils.setMaxMinLatLon(course.getMinLat(), course.getMinLon(), course.getMaxLat(), course.getMaxLon());
        this.arrowedRoute = new CourseRouteArrows(course, root);
        arrowedRoute.drawRaceRoute();
    }

    public void removeArrowedRoute(){ arrowedRoute.removeRaceRoute(); }

    public Map<Integer, Mark> getMarks(){
        return course.getAllMarks();
    }

    public void updateArrowRoute(){ arrowedRoute.updateCourseArrows(); }

    public String getXML(){
        return mapName.replace(" ", "") + "-course.xml";
    }

    public String getMapName() {
        return mapName;
    }

    public Integer getNumberOfMarks() {
        return numberOfMarks;
    }

    public String getEstTimeToRace() {
        return estTimeToRace;
    }

    public Course getCourse(){
        return course;
    }

    public Polygon getMapBoundary(){
        return mapBoundary;
    }

    public Line getStartLine() { return startLine; }

    public Line getFinishLine() { return finishLine; }
}
