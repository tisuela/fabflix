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

    //to maintain context


    public NaiveSAXParserMovies() {
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
    }


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


    public void insertGenres(Movie movie){

        for (String genre : movie.getGenres()) {
            try {
                MyQuery genreQuery = new MyQuery(dbcon, "SELECT * FROM genres");
                genreQuery.append("WHERE genres.name = ?", genre);
                ResultSet rs = genreQuery.execute();

                // if it doesn't exist, add it and get ID
                if (!rs.isBeforeFirst()) {
                    // close, since we're making new query
                    genreQuery.close();

                    String insertGenreStr = "INSERT INTO genres (name) VALUES (?)";
                    PreparedStatement insertGenreStatement = dbcon.prepareStatement(insertGenreStr);
                    insertGenreStatement.setString(1, genre);

                    insertGenreStatement.execute();
                    insertGenreStatement.close();

                    genreQuery = new MyQuery(dbcon, "SELECT * FROM genres");
                    genreQuery.append("WHERE genres.name = ?", genre);
                    rs = genreQuery.execute();
                }

                // Now we know, for sure, this genre exists
                rs.first();

                // Get IDs
                String genreId = rs.getString("id");
                String movieId = this.getMovieId(movie);
                String insertGenresInMoviesStr = "INSERT INTO genres_in_movies VALUES (?, ?)";

                PreparedStatement insertGenreInMoviesStatement = dbcon.prepareStatement(insertGenresInMoviesStr);
                insertGenreInMoviesStatement.setString(1, genreId);
                insertGenreInMoviesStatement.setString(2, movieId);

                insertGenreInMoviesStatement.execute();
                insertGenreInMoviesStatement.close();
                genreQuery.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }


    public void insertMovie(Movie movie){
        try {
            String insertMovieStr = "CALL naive_add_movie_from_XML(?, ?, ?)";
            PreparedStatement insertMovieStatement = dbcon.prepareStatement(insertMovieStr);

            insertMovieStatement.setString(1, movie.getTitle());
            insertMovieStatement.setInt(2, movie.getYear());
            insertMovieStatement.setString(3, movie.getDirector());

            insertMovieStatement.execute();
            insertMovieStatement.close();

            this.insertGenres(movie);

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


    //Event Handlers
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
        else if (qName.equalsIgnoreCase("t")) {
            // create a new instance of employee
            this.movie.setTitle(tempVal);
        }
        else if (qName.equalsIgnoreCase("year")) {
            // create a new instance of employee
            try {
                this.movie.setYear(tempVal);
            }
            catch (NumberFormatException e){
                // log inconsistency
                ;
            }
        }
        else if (qName.equalsIgnoreCase("cat")) {
            //create a new instance of employee
            this.movie.addGenre(tempVal);
        }
    }

    public static void main(String[] args) {
        NaiveSAXParserMovies test = new NaiveSAXParserMovies();
        test.run();
    }

}