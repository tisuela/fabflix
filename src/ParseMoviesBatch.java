import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import utilities.Movie;
import utilities.MyQuery;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.sql.*;

public class ParseMoviesBatch extends DefaultHandler {
    private Connection dbcon;
    private int numOfSuccess;
    private int numOfFails;
    private int numTotal;
    private int batch = 100;
    private int numMovieInserts = 0;

    private String currentDirector;
    private String tempVal;
    private Movie movie;

    private PreparedStatement insertMovieStatement;
    private  PreparedStatement insertGenreStatement;
    private int[] newMovies = null;


    public ParseMoviesBatch() {
        try {
            String loginUser = "mytestuser";
            String loginPasswd = "mypassword";
            String loginUrl = "jdbc:mysql://localhost:3306/test";

            Class.forName("com.mysql.jdbc.Driver").newInstance();
            dbcon = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

            // prepare batch insert statements
            dbcon.setAutoCommit(false);

            String insertMovieStr = "CALL add_movie_from_XML(?, ?, ?, ?)";
            String insertGenreStr = "CALL add_genre_from_XML_by_movie_id(?,?)";

            insertMovieStatement = dbcon.prepareStatement(insertMovieStr);
            insertGenreStatement = dbcon.prepareStatement(insertGenreStr);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void run() {
        parseDocument();

        // execute batches
        this.doBatches();

        String stats = String.format("Valid: %d | Invalid: %d | Total %d",
                movie.getNumOfValidMovies(), movie.getNumOfInvalidMovies(), movie.getNumOfMovies());
        System.out.println(stats);
    }


    // Queries and inserts //

    private void doBatches(){
        try {
            System.out.println("Executing batches");
            newMovies = insertMovieStatement.executeBatch();
            dbcon.commit();
            //insertGenreStatement.executeBatch();
            //dbcon.commit();
            insertMovieStatement.close();
           insertGenreStatement.close();
            dbcon.close();
            System.out.println("Successfully executed batches, new movies = " + newMovies.length);

        } catch (Exception e){
            e.printStackTrace();
        }
    }


    private String getMovieId(Movie movie) throws SQLException {
        String id = "";
        MyQuery query = new MyQuery(dbcon, "SELECT * FROM movies");
        query.addWhereConditions(" %s = ?", "title", movie.getTitle());
        query.addWhereConditions(" %s = ?", "year", movie.getYear());
        query.addWhereConditions(" %s = ?", "director", movie.getDirector());

        ResultSet rs = query.execute();
        if (rs.isBeforeFirst()){
            rs.first();
            id = rs.getString("id");
        }

        return id;
    }


    private void insertGenres(Movie movie){

        for (String genre : movie.getGenres()) {
            try {
                insertGenreStatement.setString(1, movie.getFid());
                insertGenreStatement.setString(2, genre);
                insertGenreStatement.addBatch();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    private void insertMovie(Movie movie){
        try {
            // Insert movie info to movies and movies_in_xml table. This is to link it with casts.xml

            insertMovieStatement.setString(1, movie.getTitle());
            insertMovieStatement.setInt(2, movie.getYear());
            insertMovieStatement.setString(3, movie.getDirector());
            insertMovieStatement.setString(4, movie.getFid());
            insertMovieStatement.addBatch();

            //this.insertGenres(movie);
            numMovieInserts++;
            if (numMovieInserts % batch == 0){
                System.out.println("Executing movie batch");
                insertMovieStatement.executeBatch();
            }

        } catch(SQLException e){
            e.printStackTrace();
        }

    }


    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            javax.xml.parsers.SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("stanford-movies/mains243.xml", this);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }


    // Event Handlers //


    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            //create a new instance of movie
            this.movie = new Movie();
            this.movie.setDirector(currentDirector);
        }
    }


    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }


    public void endElement(String uri, String localName, String qName) throws SAXException {

        // input movie info
        if (qName.equalsIgnoreCase("dirname")) {
            // set current director
            currentDirector = tempVal;
        }
        else if (qName.equalsIgnoreCase("film")) {
            // add it to the list
            if (this.movie.isValid()) {
                System.out.println(this.movie + "\n");
                this.insertMovie(this.movie);
            }
            else{
                System.out.println(this.movie.getInvalidLog() + "\n");
            }
        }
        else if (qName.equalsIgnoreCase("fid")) {
            this.movie.setFid(tempVal);
        }
        else if (qName.equalsIgnoreCase("t")) {
            this.movie.setTitle(tempVal);
        }
        else if (qName.equalsIgnoreCase("year")) {
            try {
                this.movie.setYear(tempVal);
            }
            catch (NumberFormatException e){
                // log inconsistency
                ;
            }
        }
        else if (qName.equalsIgnoreCase("cat")) {
            this.movie.addGenre(tempVal);
        }
    }


    public static void main(String[] args) {
        long startTime = System.nanoTime();

        ParseMoviesBatch test = new ParseMoviesBatch();
        test.run();

        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;
        System.out.println("Execution time: " + elapsedTime);
    }
}
