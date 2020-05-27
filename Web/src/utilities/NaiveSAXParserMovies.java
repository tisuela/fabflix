package utilities;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.sql.*;

public class NaiveSAXParserMovies extends DefaultHandler {

    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;
    private Connection dbcon = null;

    private String currentDirector;
    private String tempVal;
    private Movie movie;




    public NaiveSAXParserMovies() {
        try {
            String loginUser = "mytestuser";
            String loginPasswd = "mypassword";
            String loginUrl = "jdbc:mysql://localhost:3306/test";

            Class.forName("com.mysql.jdbc.Driver").newInstance();
            dbcon = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void run() {
        parseDocument();
        String stats = String.format("Valid: %d | Invalid: %d | Total %d",
                movie.getNumOfValidMovies(), movie.getNumOfInvalidMovies(), movie.getNumOfMovies());
        System.out.println(stats);
    }


    // Queries and inserts //


    public String getMovieId(Movie movie) throws SQLException{
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




    public void insertMovie(Movie movie){
        try {
            // Insert movie info to movies and movies_in_xml table. This is to link it with casts.xml
            String insertMovieStr = "CALL naive_add_movie_from_XML(?, ?, ?)";
            String insertMovieXmlStr = "INSERT INTO movies_in_xml (movieId, xmlId) VALUES (?,?)";

            PreparedStatement insertMovieStatement = dbcon.prepareStatement(insertMovieStr);
            PreparedStatement insertMovieXmlStatement = dbcon.prepareStatement(insertMovieXmlStr);

            insertMovieStatement.setString(1, movie.getTitle());
            insertMovieStatement.setInt(2, movie.getYear());
            insertMovieStatement.setString(3, movie.getDirector());
            insertMovieXmlStatement.setString(2, movie.getFid());

            insertMovieStatement.execute(); insertMovieStatement.close();

            // get movie id for inserting in the other tables
            String movieId = this.getMovieId(movie);

            insertMovieXmlStatement.setString(1, movieId);
            insertMovieXmlStatement.execute();
            insertMovieXmlStatement.close();



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

        NaiveSAXParserMovies test = new NaiveSAXParserMovies();
        test.run();
    }
}