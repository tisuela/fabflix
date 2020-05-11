import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;


// Calls both parse classes and then also looks through casts
public class ParseAll extends DefaultHandler {
    private Connection dbcon;
    private int numOfSuccess;
    private int numOfFails;
    private int numTotal;

    String tempVal;
    String starName;
    String movieFid;

    private int batch = 100;
    private int numInserts = 0;

    private Map<String, Star> stars;
    private Map<String, Movie> movies;

    WriteData writeStarsInMoviesData;


    public ParseAll(){
        try {
            String loginUser = "mytestuser";
            String loginPasswd = "mypassword";
            String loginUrl = "jdbc:mysql://localhost:3306/test?AllowLoadLocalInfile=true";

            Class.forName("com.mysql.jdbc.Driver").newInstance();
            dbcon = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);


            // run the two other parsers
            stars = new HashMap<>(); movies = new HashMap<>();
            this.runActorsAndMovies();

            writeStarsInMoviesData = new WriteData("stars_in_movies.txt");

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void runActorsAndMovies(){
        ParseMovies parseMovies = new ParseMovies();
        ParseActors parseActors = new ParseActors();

        parseMovies.run();
        parseActors.run();

        movies = parseMovies.getMovies();
        stars = parseActors.getStars();
    }


    public void run() {
        parseDocument();
        String stats = String.format("Success = %d | Fails = %d | Total = %d",
                numOfSuccess, numOfFails, numTotal);
        System.out.println();

        writeStarsInMoviesData.close();

        this.loadData();



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
        Movie movie = movies.get(movieFid);
        Star star = stars.get(starName);

        if (movie != null && star != null){
            String movieId = movie.getId();
            String starId = star.getId();

            writeStarsInMoviesData.addField(starId);
            writeStarsInMoviesData.addField(movieId);
            writeStarsInMoviesData.newLine();


            System.out.println("added " + movieId + " " + starId + " for " + starName);
            ++numOfSuccess;
            return "success";
        }
        else {
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


    private void loadData(){
        try {
            String loadStr = "LOAD DATA LOCAL INFILE '" + writeStarsInMoviesData.getFileName() + "'" +
                    " INTO TABLE stars_in_movies" +
                    " FIELDS TERMINATED BY '" + writeStarsInMoviesData.getFieldTerminator() + "'" +
                    " LINES TERMINATED BY '\\n'" +
                    " (@column1, @column2) SET starId = @column1, movieId = @column2";

            System.out.println(loadStr);

            Statement loadStatement = dbcon.createStatement();
            loadStatement.execute(loadStr);
            loadStatement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        long startTime = System.nanoTime();
        ParseAll test = new ParseAll();
        test.run();
        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;
        System.out.println("Execution time: " + elapsedTime);
    }





}
