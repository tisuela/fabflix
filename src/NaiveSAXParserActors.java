import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;

public class NaiveSAXParserActors extends DefaultHandler {

    private Connection dbcon;

    private Star star;
    String tempVal;

    public NaiveSAXParserActors(){
        try {
            String loginUser = "mytestuser";
            String loginPasswd = "mypassword";
            String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

            Class.forName("com.mysql.jdbc.Driver").newInstance();
            dbcon = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public void run() {
        parseDocument();
        String stats = String.format("Valid: %d | Invalid: %d | Total %d",
                star.getNumOfValidStars(), star.getNumOfInvalidStars(), star.getNumOfStars());
        System.out.println(stats);
    }


    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            javax.xml.parsers.SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("stanford-movies/actors63.xml", this);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }


    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("actor")) {
            //create a new instance of movie
            this.star = new Star();
        }
    }


    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }


    public void endElement(String uri, String localName, String qName) throws SAXException {

        // input movie info
        if (qName.equalsIgnoreCase("actor")) {
            if (this.star.isValid()) {
                System.out.println(this.star + "\n");
            }
            else{
                System.out.println(this.star.getInvalidLog() + "\n");
            }
        }
        else if (qName.equalsIgnoreCase("stagename")) {
            this.star.setName(tempVal);
        }
        else if (qName.equalsIgnoreCase("dob")) {
            try {
                this.star.setBirthYear(tempVal);
            }
            catch (NumberFormatException e){
                // log inconsistency
                ;
            }
        }
    }


    public static void main(String[] args) {
        NaiveSAXParserActors test = new NaiveSAXParserActors();
        test.run();
    }



}
