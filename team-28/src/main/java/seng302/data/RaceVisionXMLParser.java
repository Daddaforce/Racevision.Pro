package seng302.data;

import org.joda.time.DateTime;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import seng302.models.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created on 6/03/17.
 * Collection of methods for reading in data from files. Files must be located in the DEFAULT_FILE_PATH folder
 */

public class RaceVisionXMLParser {

    private static final String DEFAULT_COURSE_FILE = "AC35-course.xml";
    public static String courseFile = DEFAULT_COURSE_FILE;
    private static final String BOAT_FILE = "Boat.xml";

    private final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private Document dom;

    /**
     * Manages importing the course from the correct place
     * If a file path is specified, this will be used, otherwise a default is packaged with the jar.
     * Currently this an XML file at DEFAULT_FILE_PATH/COURSE_FILE
     * @param resourcePath String of the file path of the file to read in.
     * @return a Course object.
     */
    public Course importCourse(InputStream resourcePath) {
        try {
            parseXMLStream(resourcePath);
            return importCourseFromXML();
        }  catch (IOException ioe) {
            System.err.printf("Unable to read %s as a course definition file. " +
                    "Ensure it is correctly formatted.\n", resourcePath);
            ioe.printStackTrace();
            return null;
        }
    }

    /**
     * Manages importing the race from the correct place
     * If a file path is specified, this will be used, otherwise a default is packaged with the jar.
     * Currently this an XML file at DEFAULT_FILE_PATH/COURSE_FILE
     * @param resourcePath String of the file path of the file to read in.
     * @return a Race object.
     */
    public Race importRace(InputStream resourcePath){
        try {
            parseXMLStream(resourcePath);
            return importRaceFromXML();
        }  catch (IOException ioe) {
            System.err.printf("Unable to read %s as a course definition file. " +
                    "Ensure it is correctly formatted.\n", resourcePath);
            ioe.printStackTrace();
            return null;
        }
    }

    /**
     * Overload of importRace to simplify reading in the default course file.
     * @return a Race object
     */
    public Race importRace(){
        String resourcePath = "/defaultFiles/" + courseFile;
        return importRace(RaceVisionXMLParser.class.getResourceAsStream(resourcePath));
    }

    /**
     * Sets the race id in the course xml dom
     * @param root The root tag ("Race") of the dom
     * @param raceId The desired race id
     */
    private void setRaceId(Element root, String raceId){
        NodeList raceIdList = root.getElementsByTagName(XMLTags.Race.RACE_ID);
        raceIdList.item(0).setTextContent(raceId);
    }

    /**
     * Sets the message creation date and time in the course xml dom
     * @param root The root tag ("Race") of the dom
     */
    private void setCreationTime(Element root){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        NodeList creationTimeList = root.getElementsByTagName(XMLTags.Race.CREATION_TIME);

        LocalDateTime creationTime = LocalDateTime.now(ZoneId.of("UTC"));
        String formattedCreationTime = creationTime.format(formatter);

        creationTimeList.item(0).setTextContent(formattedCreationTime);
    }

    /**
     * Sets the race start time in the AC35-course.xml dom
     * @param root The root tag ("Race") of the dom
     * @param expectStartTimeEpochMs The expected race start time in epoch milliseconds
     */
    private void setStartTime(Element root, Long expectStartTimeEpochMs){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        NodeList startTimeList = root.getElementsByTagName(XMLTags.Race.START_TIME);

        LocalDateTime startTime = LocalDateTime.ofEpochSecond(expectStartTimeEpochMs / 1000, 0, ZoneOffset.UTC);
        String formattedStartTime = startTime.format(formatter);

        startTimeList.item(0).getAttributes().getNamedItem(XMLTags.Race.START).setTextContent(formattedStartTime);
    }

    /**
     * Injects the correct participants into the raceXML field
     * @param root The root tag ("Race") of the dom
     * @param participantIds participant Ids to inject
     */
    private void setParticipants(Element root, ArrayList<Integer> participantIds) {
        NodeList participants = root.getElementsByTagName(XMLTags.Course.PARTICIPANTS);
        for (Integer id : participantIds) {
            Element participant = dom.createElement(XMLTags.Boats.YACHT);
            participant.setAttribute(XMLTags.Boats.SOURCE_ID, id.toString());
            participants.item(0).appendChild(participant);
        }
    }

    /**
     * Updates the race.xml in race id, race creation time, race start time fields and participants fields.
     * @param raceXML The InputStream-ed race xml file
     * @param raceId The race id of the race
     * @param expectStartTimeEpochMs The expected start time of the race
     * @return A InputStream with the race xml containing the update fields
     */
    InputStream injectRaceXMLFields(InputStream raceXML, String raceId, Long expectStartTimeEpochMs, ArrayList<Integer> participantIds){
        try {
            parseXMLStream(raceXML);
            Element root = dom.getDocumentElement();

            setCreationTime(root);
            setStartTime(root, expectStartTimeEpochMs);
            setParticipants(root, participantIds);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(dom), new StreamResult(writer));

            String output = writer.getBuffer().toString();

            return new ByteArrayInputStream(output.getBytes());
        } catch (IOException | TransformerException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Attempts to read the desired XML file into the Document parser
     * @param inputStream - the location of the file to be read, must be XML
     * @throws IOException if the file is not found
     */
    private void parseXMLStream(InputStream inputStream) throws IOException{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(inputStream);
        } catch(ParserConfigurationException | SAXException pce) {
            pce.printStackTrace();
        }
    }

    /**
     * Decodes an XML file into a Race object
     * @return a Race Object
     */
    private Race importRaceFromXML(){
        Race race = new Race();

        race.setCourse(importCourseFromXML());

        Element root = dom.getDocumentElement();
        NodeList raceIdList = root.getElementsByTagName(XMLTags.Race.RACE_ID);
        String raceId = raceIdList.item(0).getTextContent();
        race.setId(raceId);

        NodeList startTimeList = root.getElementsByTagName(XMLTags.Race.START_TIME);
        String startTimeString = startTimeList.item(0).getAttributes().getNamedItem(XMLTags.Race.START).getTextContent();
        DateTime startTime = new DateTime( startTimeString ) ;

        race.setStartTimeInEpochMs(startTime.getMillis());
        race.setCompetitorIds(parseCompetitorIds());

        return race;
    }

    /**
     * Decodes an XML file into a Course object
     * @return a Course object
     */
    private Course importCourseFromXML() {
        Course course = new Course();

        try {
            Element root = dom.getDocumentElement();
            if (root.getTagName() != XMLTags.Course.RACE) {
                String message = String.format("The root tag must be <%s>.", XMLTags.Course.RACE);
                throw new XMLParseException(XMLTags.Course.RACE, message);
            }

            NodeList nodes = root.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    switch (element.getTagName()) {
                        case XMLTags.Course.COURSE:
                            NodeList compoundMarks = element.getElementsByTagName(XMLTags.Course.COMPOUND_MARK);
                            for (int j = 0; j < compoundMarks.getLength(); j++){
                                CompoundMark mark = parseCompoundMark((Element) compoundMarks.item(j), course);
                                course.addNewCompoundMark(mark);
                            }
                            break;
                        case XMLTags.Course.COMPOUND_MARK_SEQUENCE:
                            NodeList legs = element.getElementsByTagName(XMLTags.Course.CORNER);
                            Map<Integer, ArrayList<String>> markOrder = new TreeMap<>();
                            for (int k = 0; k < legs.getLength(); k++) {
                                Element corner = (Element) legs.item(k);
                                ArrayList<String> markRounding = new ArrayList<>();
                                Integer seqNumber = Integer.parseInt(corner.getAttribute(XMLTags.Course.SEQ_ID));
                                markRounding.add(corner.getAttribute(XMLTags.Course.COMPOUND_MARK_ID));
                                markRounding.add(corner.getAttribute(XMLTags.Course.ROUNDING));
                                markOrder.put(seqNumber, markRounding);
                            }
                            for(Integer seqNumber : markOrder.keySet()){
                                int markID = Integer.parseInt(markOrder.get(seqNumber).get(0));
                                RoundingSide roundingSide = RoundingSide.parseRoundingSide(markOrder.get(seqNumber).get(1));
                                if (seqNumber == 1) {
                                    CompoundMark mark = course.getCompoundMarkByID(markID);
                                    if (!mark.hasTwoMarks()){
                                        throw new RuntimeException("Race xml file has a incorrectly formatted Start Line");
                                    }
                                }
                                course.addMarkInOrder(markID, roundingSide);
                            }
                            break;
                        case XMLTags.Course.WIND:
                            course.setWindDirection(Double.parseDouble(element.getTextContent()));
                            break;
                        case XMLTags.Course.COURSE_LIMIT:
                            String nameAttr = element.getAttribute(XMLTags.Course.COURSE_LIMIT_NAME_ATTR);
                            if (nameAttr.isEmpty() || nameAttr.equals(XMLTags.Course.BOUNDARY)) {
                                NodeList courseLimits = element.getElementsByTagName(XMLTags.Course.LIMIT);
                                for (int k = 0; k < courseLimits.getLength(); k++) {
                                    Coordinate coord = parseCourseLimitCoord((Element) courseLimits.item(k));
                                    course.addToBoundary(coord);
                                }
                            }
                            break;
                    }
                }
            }
        } catch (XMLParseException e) {
            System.err.printf("Error reading course file around tag <%s>.\n", e.getTag());
            e.printStackTrace();
        }


        if(course.getCourseOrder().size() < 2){
            throw new InputMismatchException("There must be at least one leg in the course.");
        }

        setRaceLines(course);
        return course;
    }

    /**
     * Sets the first and final marks in course order to race lines.
     * @param course The course that the raceLines will be changed
     */
    private static void setRaceLines(Course course) {
        CompoundMark startLine;
        int startLinePos = 0;
        if(course.getCourseOrder().get(0).hasTwoMarks()){
            startLine = course.getCourseOrder().get(0);
        }else {
            startLine = course.getCourseOrder().get(1);
            course.setHasEntryMark(true);
            startLinePos = 1;
        }
        course.removeCompoundMark(startLine);
        RaceLine startRaceLine = CompoundMark.convertToRaceLine(startLine, CompoundMark.MarkType.START);
        course.setStartLine(startRaceLine);
        course.getCourseOrder().set(startLinePos, startRaceLine);
        course.addNewCompoundMark(startRaceLine);

        int lastMarkIndex = course.getCourseOrder().size() - 1;
        CompoundMark finishLine = course.getCourseOrder().get(lastMarkIndex);
        if(finishLine.hasTwoMarks()){
            course.removeCompoundMark(finishLine);
            RaceLine finishRaceLine = CompoundMark.convertToRaceLine(finishLine, CompoundMark.MarkType.FINISH);
            course.setFinishLine(finishRaceLine);
            course.getCourseOrder().set(lastMarkIndex, finishRaceLine);
            course.addNewCompoundMark(finishRaceLine);
        } else{
            throw new InputMismatchException("The finish line must have 2 marks.");
        }
    }

    /**
     * Parses a single Mark in a CompoundMark element
     * @param markElement A <Mark> element
     * @return a Mark representing the data given in the Mark element of the XML
     */
    private Mark parseMark(Element markElement, Course course){
        String markName = markElement.getAttribute(XMLTags.Course.NAME);
        Double lat1 = Double.parseDouble(markElement.getAttribute(XMLTags.Course.TARGET_LAT));
        Double lon1 = Double.parseDouble(markElement.getAttribute(XMLTags.Course.TARGET_LON));
        Integer sourceId = Integer.parseInt(markElement.getAttribute(XMLTags.Course.SOURCE_ID));
        if(course.getAllMarks().containsKey(sourceId)){
            return course.getAllMarks().get(sourceId);
        } else{
            return new Mark(sourceId, markName, new Coordinate(lat1, lon1));
        }
    }

    /**
     * Decodes a CompoundMark element into a CompoundMark object
     * @param compoundMarkElement - an XML <CompoundMark> element
     * @return a CompoundMark (potentially RaceLine) object
     * @throws XMLParseException when an expected tag is missing or unexpectedly formatted
     */
    private CompoundMark parseCompoundMark(Element compoundMarkElement, Course course) throws  XMLParseException{
        CompoundMark compoundMark;
        Integer compoundMarkID = Integer.parseInt(compoundMarkElement.getAttribute(XMLTags.Course.COMPOUND_MARK_ID));
        String compoundMarkName = compoundMarkElement.getAttribute(XMLTags.Course.NAME);
        NodeList markNodes = compoundMarkElement.getElementsByTagName(XMLTags.Course.MARK);

        if (markNodes.getLength() < 1) {
            throw new XMLParseException(XMLTags.Course.COMPOUND_MARK, "Required tag was not defined.");
        }
        int numMarks = markNodes.getLength();
        if(numMarks == 2){
            Element mark1Element = (Element) markNodes.item(0);
            Element mark2Element = (Element) markNodes.item(1);
            Mark mark1 = parseMark(mark1Element, course);
            Mark mark2 = parseMark(mark2Element, course);
            compoundMark = new CompoundMark(compoundMarkID, compoundMarkName, mark1, mark2);
        }else{
            Element markElement = (Element) markNodes.item(0);
            Mark mark = parseMark(markElement, course);
            compoundMark = new CompoundMark(compoundMarkID, compoundMarkName, mark);
        }
        return compoundMark;
    }

    /**
     * Parses a boundary limit element from Race xml to determine a single point of the boundary
     * @param limit - an XML <Limit> element
     * @return a Coordinate denoting where a point on boundary limit is.
     */
    private Coordinate parseCourseLimitCoord(Element limit){
        Double lat = Double.parseDouble(limit.getAttribute(XMLTags.Course.LAT));
        Double lon = Double.parseDouble(limit.getAttribute(XMLTags.Course.LON));
        Coordinate coord = new Coordinate(lat, lon);
        return coord;
    }

    /**
     * Manages importing the boats in the race from the correct place
     * If a file path is specified, this will be used, otherwise a default is packaged with the jar.
     * Currently this an XML file at DEFAULT_FILE_PATH/BOAT_FILE
     * @param inputStream String of the file path of the file to read in.
     * @return an ArrayList of Boats.
     */
    public List<Boat> importStarters(InputStream inputStream) {
        try {
            parseXMLStream(inputStream);
            return importStartersFromXML();

        }  catch (IOException ioe) {
            System.err.printf("Unable to read %s as a boat definition file. " +
                    "Ensure it is correctly formatted.\n", inputStream);
            ioe.printStackTrace();
            return null;
        }
    }


    /**
     * Imports file found at BOAT_FILE in DEFAULT_FILE_PATH in resources folder
     *
     * Boats defined as:
     *     BoatName, NickName and location
     *
     * @return starters - Set of Boat objects defined in file
     */
    private List<Boat> importStartersFromXML(){
        List<Boat> starters = new ArrayList<>();
        try {
            Element root = dom.getDocumentElement();
            if (!Objects.equals(root.getTagName(), XMLTags.Boats.BOAT_CONFIG)) {
                String message = String.format("The root tag must be <%s>.", XMLTags.Boats.BOAT_CONFIG);
                throw new XMLParseException(XMLTags.Boats.BOAT_CONFIG, message);
            }

            NodeList nodes = root.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    if (element.getTagName().equals(XMLTags.Boats.BOATS)) {
                        NodeList boats = element.getElementsByTagName(XMLTags.Boats.BOAT);
                        for (int j = 0; j < boats.getLength(); j++) {
                            Boat boat = parseBoat((Element) boats.item(j));
                            if (boat != null) {
                                starters.add(boat);
                            }
                        }
                    }
                }
            }
        } catch (XMLParseException e) {
            System.err.printf("Error reading course file around tag <%s>.\n", e.getTag());
            e.printStackTrace();
        }
        if(starters.size() < 2){
            throw new InputMismatchException("There must be at least two boats in the race.");
        }
        return starters;
    }

    /**
     * Takes BoatXML tag and returns it as a Boat object.
     *
     *  @return boat
     */
    private Boat parseBoat(Element boatXML) throws XMLParseException{
        Boat boat = null;
        String type = boatXML.getAttribute(XMLTags.Boats.TYPE);
        if(type.equals("Yacht")){
            String name = boatXML.getAttribute(XMLTags.Boats.BOAT_NAME);
            String nickname = boatXML.getAttribute(XMLTags.Boats.NICKNAME);
            Integer id = Integer.parseInt(boatXML.getAttribute(XMLTags.Boats.SOURCE_ID));
            boat = new Boat(id, name, nickname, 0);
        }
        return boat;
    }

    /**
     * Manages extracting information from the regatta xml
     * If a file path is specified, this will be used, otherwise a default is packaged with the jar.
     * Currently this an XML file at DEFAULT_FILE_PATH/REGATTA_FILE
     * @param inputStream String of the file path of the file to read in.
     *                 race Race object as initialized in the main method
     */
    public void importRegatta(InputStream inputStream, Race race) {
        try {
            parseXMLStream(inputStream);
            importRegattaFromXML(race);

        }  catch (IOException ioe) {
            System.err.printf("Unable to read %s as a regatta definition file. " +
                    "Ensure it is correctly formatted.\n", inputStream);
            ioe.printStackTrace();
        }
    }

    /**
     * Imports file found at DEFAULT_FILE_PATH/REGATTA_FILE and updates attributes in race
     */
    private void importRegattaFromXML(Race race) {
        try {
            Element root = dom.getDocumentElement();
            if (!Objects.equals(root.getTagName(), XMLTags.Regatta.REGATTA_CONFIG)) {
                String message = String.format("The root tag must be <%s>.", XMLTags.Regatta.REGATTA_CONFIG);
                throw new XMLParseException(XMLTags.Regatta.REGATTA_CONFIG, message);
            }

            NodeList nodes = root.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    switch (element.getTagName()) {
                        case XMLTags.Regatta.REGATTA_NAME:
                            race.setRegattaName(String.valueOf(element.getTextContent()));
                            break;
                        case XMLTags.Regatta.UTC_OFFSET:
                            double utcOffset = Double.parseDouble(String.valueOf(element.getTextContent()));
                            if (utcOffset <= 14 && utcOffset >= -12) {
                                race.setUTCOffset(utcOffset);
                            } else {
                                throw new InputMismatchException("The UTC offset must be greater than or equal to -12 and less than or equal to 14.");
                            }
                            break;
                    }
                }
            }
        } catch (XMLParseException e) {
            System.err.printf("Error reading course file around tag <%s>.\n", e.getTag());
            e.printStackTrace();
        }
    }

    /**
     * Takes a file from resources and puts it outside the jar to make it accessible to users
     * @param resourceName path to the resource
     * @param outputName path and name of where the file should be generated
     * @throws IOException when fails to read resource
     */
    public void exportResource(String resourceName, String outputName) throws IOException {
        InputStream inStream = null;
        OutputStream outStream = null;
        try {
            inStream = RaceVisionXMLParser.class.getResourceAsStream(resourceName);//note that each / is a directory down in the "jar tree" been the jar the root of the tree
            if(inStream == null) {
                throw new IOException("Cannot get resource \"" + resourceName + "\" from Jar file.");
            }

            int readBytes;
            byte[] buffer = new byte[4096];
            outStream = new FileOutputStream(outputName);
            while ((readBytes = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, readBytes);
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (inStream != null) {
                inStream.close();
            }
            if (outStream != null) {
                outStream.close();
            }
        }
    }

    public List<Boat> importDefaultStarters() {
        try {
            String resourcePath = "/defaultFiles/" + BOAT_FILE;
            parseXMLStream(RaceVisionXMLParser.class.getResourceAsStream(resourcePath));
            return importStartersFromXML();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    private Set<Integer> parseCompetitorIds() {
        Set<Integer> competitorIds = null;
        try {
            Element root = dom.getDocumentElement();
            if (root.getTagName() != XMLTags.Course.RACE) {
                String message = String.format("The root tag must be <%s>.", XMLTags.Course.RACE);
                throw new XMLParseException(XMLTags.Course.RACE, message);
            }
            competitorIds = new HashSet<>();
            NodeList nodes = root.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    if(element.getTagName().equals(XMLTags.Course.PARTICIPANTS)){
                        NodeList competitors = element.getChildNodes();
                        for (int j = 0; j < competitors.getLength(); j++) {
                            if (competitors.item(j).getNodeType() == Node.ELEMENT_NODE) {
                                Element boatNode = (Element) competitors.item(j);
                                competitorIds.add(Integer.parseInt(boatNode.getAttribute(XMLTags.Boats.SOURCE_ID)));
                            }
                        }
                    }
                }
            }
        } catch (XMLParseException e) {
            System.err.printf("Error reading course file around tag <%s>.\n", e.getTag());
            e.printStackTrace();
        }
        return competitorIds;
    }

    public void setCourseFile(String course) {
        courseFile = course;
    }

    /**
     * Checks to see if a file exists in the expected location
     * @param file the name of the file (which is expected to be inside /defaultFiles/) to test
     * @return true if the file exists, false otherwise
     */
    public boolean checkFileExists(String file) {
        String resourcePath = "/defaultFiles/" + file;
        return RaceVisionXMLParser.class.getResource(resourcePath) != null;
    }
}
