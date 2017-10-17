package seng302.data;

/**
 * Created  on 3/16/2017.
 * An exception thrown by the reader when it encounters issues parsing an XML file
 */
public class XMLParseException extends Exception{

    private String tag;
    private String message;

    XMLParseException(String tag, String message) {
        this.tag = tag;
        this.message = message;
    }

    public String getTag(){
        if (this.tag != null) {
            return this.tag;
        }
        return "";
    }

    public String getMessage(){
        return this.message;
    }
}
