import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import utilities.MyQuery;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.sql.*;

public class NaiveSAXParserCasts extends DefaultHandler {
    private Connection dbcon;
    private int numOfSuccess;
    private int numOfFails;
    private int numTotal;

    String tempVal;
    String starName;
    String movieFid;


    public NaiveSAXParserCasts(){
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
        String stats = String.format("Success = %d | Fails = %d | Total = %d",
                numOfSuccess, numOfFails, numTotal);
        System.out.println();

    }


    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            javax.xml.parsers.SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("stanford-movies/casts124.xml", this);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }


    // Queries and inserts //


    // Get movie ID from movies_in_xml, by using the film FID
    private String getMovieId(String movieFid) throws SQLException {
        String movieId = "";

        MyQuery query = new MyQuery(dbcon, "SELECT * FROM movies_in_xml");
        query.append("WHERE xmlId = ?", movieFid);
        ResultSet rs = query.execute();

        if (rs.isBeforeFirst()){
            rs.first();
            movieId = rs.getString("movieId");
        }
        query.close();
        return movieId;
    }


    // get star ID using star name
    private String getStarId(String starName) throws SQLException {
        String starId = "";

        MyQuery query = new MyQuery(dbcon, "SELECT * FROM stars");
        query.append("WHERE stars.name = ?", starName);
        ResultSet rs = query.execute();

        if (rs.isBeforeFirst()){
            rs.first();
            starId = rs.getString("stars.id");
        }

        query.close();
        return starId;
    }


    public String insertStarsInMovies(String movieFid, String starName){
        try {
            String movieId = this.getMovieId(movieFid);
            String starId = this.getStarId(starName);

            if (movieId.length() > 0 && starId.length() > 0){
                String insertStr = "INSERT INTO stars_in_movies (starId, movieId) VALUES (?, ?)";
                PreparedStatement insertStatement = dbcon.prepareStatement(insertStr);
                insertStatement.setString(1, starId);
                insertStatement.setString(2, movieId);

                insertStatement.execute();
                insertStatement.close();

                System.out.println("added " + movieId + " " + starId + " for " + starName);
                ++numOfSuccess;
                return "success";
            }
            else {
                ++numOfFails;
                return "fail";
            }

        } catch (SQLException e){
            e.printStackTrace();
            ++numOfFails;
            return "fail";
        }

    }


    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
    }


    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }


    public void endElement(String uri, String localName, String qName) throws SAXException {


        if (qName.equalsIgnoreCase("f")) {
            movieFid = tempVal;
        }
        else if (qName.equalsIgnoreCase("a")) {
            starName = tempVal;
        }

        // end of element which contains fil & actor
        else if (qName.equalsIgnoreCase("m")) {
            // Thus, we do insertion
            String result = insertStarsInMovies(movieFid, starName);

            System.out.println(result);


            numTotal++;
        }

    }


    public static void main(String[] args) {
        NaiveSAXParserCasts test = new NaiveSAXParserCasts();
        test.run();
    }





}
