import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ParseActors extends DefaultHandler {

    private Connection dbcon;

    private Star star;
    String tempVal;

    String maxId;
    String currentId;

    private WriteData writeData;

    private Map<String, Star> stars;

    public ParseActors(){
        try {
            String loginUser = "mytestuser";
            String loginPasswd = "mypassword";
            String loginUrl = "jdbc:mysql://localhost:3306/moviedb?AllowLoadLocalInfile=true";

            Class.forName("com.mysql.jdbc.Driver").newInstance();
            dbcon = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
            //dbcon.setAllowLoadLocalInfile(true);
            this.setMaxId();
            this.currentId = maxId;
            writeData = new WriteData("stars.txt");

            stars = new HashMap<>();


        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // --- generating new IDs --- //

    private void setMaxId(){
        try {
            MyQuery query = new MyQuery(dbcon, "SELECT max(id) FROM stars");
            ResultSet rs = query.execute();
            rs.first();
            this.maxId = rs.getString("max(id)");

        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private String getNextId(){
        String prefix = this.currentId.substring(0,2);
        String suffix = this.currentId.substring(2);
        int suffixInt = Integer.parseInt(suffix);
        String newSuffix = String.valueOf(++suffixInt);
        this.currentId = prefix + newSuffix;
        return this.currentId;
    }


    // --- running the parser --- //


    public void run() {
        parseDocument();
        String stats = String.format("Valid: %d | Invalid: %d | Total %d",
                star.getNumOfValidStars(), star.getNumOfInvalidStars(), star.getNumOfStars());
        System.out.println(stats);
        writeData.close();

        // after everything is done, load data
        this.loadData();
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


    private void insertStar(Star star){
        try {
            this.getNextId();

            star.setId(this.currentId);
            writeData.addField(star.getId());
            writeData.addField(star.getName());
            writeData.addField(String.valueOf(star.getBirthYear()));
            writeData.newLine();

            stars.put(star.getName(), star);


        } catch (Exception e){
            e.printStackTrace();
        }
    }


    // Event Handlers //


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
                insertStar(star);
                //System.out.println(this.star + "\n");
            }
            else{
                //System.out.println(this.star.getInvalidLog() + "\n");
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


    // --- loading the data from file --- //

    private void loadData(){
        try {
            String loadStr = "LOAD DATA LOCAL INFILE '" + writeData.getFileName() + "'" +
                    " INTO TABLE stars" +
                    " FIELDS TERMINATED BY '" + writeData.getFieldTerminator() + "'" +
                    " LINES TERMINATED BY '\\n'";

            System.out.println(loadStr);

            Statement loadStatement = dbcon.createStatement();
            loadStatement.execute(loadStr);
            loadStatement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Star> getStars() {
        return stars;
    }







    public static void main(String[] args) {
        ParseActors test = new ParseActors();
        test.run();
    }

}
