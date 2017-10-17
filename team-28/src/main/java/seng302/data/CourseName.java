package seng302.data;

import java.util.Objects;

public enum CourseName {

    AC35(0, "AC35"), AC33(1, "AC33"), LAKE_TEKAPO(2, "Lake Tekapo"), LAKE_TAUPO(3, "Lake Taupo"), MALMO(4, "Malmo"), ATHENS(5, "Athens"), UNKNOWN(-1, "UNKNOWN");

    private final int value;
    private final String name;

    CourseName(int value, String text){
        this.value = value;
        this.name = text;
    }

    public int getValue() {return value;}

    public String getText(){return name;}

    /**
     * gets a course name from a given int
     * @param value the integer value of the course name
     * @return
     */
    public static CourseName getCourseNameFromInt(int value){
        for (CourseName course : CourseName.values()){
            if (course.getValue() == value){
                return course;
            }
        }
        return UNKNOWN;
    }

    /**
     * gets a course integer value from a given string
     * @param name
     * @return
     */
    public static int getCourseIntFromName(String name){
        String strippedGivenName = name.replace(" ", "");
        for (CourseName courseName : CourseName.values()){
            String strippedCourseName = courseName.getText().replace(" ", "");
            if (Objects.equals(strippedGivenName, strippedCourseName)){
                return courseName.getValue();
            }
        }
        return -1;
    }

    /**
     * Converts a course xml name to a normal course name (e.g. Malmo-course.xml to Malmo)
     * @param xmlName A String that ends in "-course.xml"
     * @return A String with the "-course.xml" stripped out
     */
    public static String courseNameFromXMLName(String xmlName){
        Integer courseNameEndIndex = xmlName.length() - "-course.xml".length();
        return xmlName.substring(0, courseNameEndIndex);
    }
}
