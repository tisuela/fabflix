package utilities;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ParseMovies extends DefaultHandler {
    private Connection dbcon;
    private int numOfSuccess;
    private int numOfFails;
    private int numTotal;
    private int batch = 100;
    private int numMovieInserts = 0;

    private String currentDirector;
    private String tempVal;
    private Movie movie;
    private Map<String, Movie> movies;
    private Map<String, String> genres;

    private WriteData writeMovieData;
    private WriteData writeMovieInXmlData;
    private WriteData writeGenreData;
    private WriteData writeGenresInMoviesData;

    String maxId;
    String currentId;

    int maxGenreId;
    int currentGenreId;

    private int[] newMovies = null;


    public ParseMovies() {
        try {
            dbcon = MyUtils.getConnection();

            this.setMaxId();
            this.currentId = maxId;
            this.currentGenreId = maxGenreId;

            movies = new HashMap<>();
            genres = new HashMap<>();

            writeMovieData = new WriteData("movies.txt");
            writeMovieInXmlData = new WriteData("movies_in_xml.txt");
            writeGenreData = new WriteData("genres.txt");
            writeGenresInMoviesData = new WriteData("genres_in_movies.txt");

        } catch (Exception e){
            e.printStackTrace();
        }
    }



    // --- generating new IDs --- //

    private void setMaxId(){
        try {
            MyQuery movieQuery = new MyQuery(dbcon, "SELECT max(id) FROM movies");
            MyQuery genreQuery = new MyQuery(dbcon, "SELECT max(id) FROM genres");
            ResultSet movieRs = movieQuery.execute();
            ResultSet genreRs = genreQuery.execute();

            movieRs.first();
            genreRs.first();

            this.maxId = movieRs.getString("max(id)");
            this.maxGenreId = genreRs.getInt("max(id)");

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


    private int getNextGenreId(){
        return ++this.currentGenreId;
    }


    public void run() {
        parseDocument();

        String stats = String.format("Valid: %d | Invalid: %d | Total %d",
                movie.getNumOfValidMovies(), movie.getNumOfInvalidMovies(), movie.getNumOfMovies());
        System.out.println(stats);

        writeMovieData.close();
        writeMovieInXmlData.close();
        writeGenreData.close();
        writeGenresInMoviesData.close();

        this.loadMovieData();
        this.loadMovieInXmlData();
        this.loadGenreData();
        this.loadGenreInMoviesData();

        try {
            dbcon.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }



    private void insertGenres(Movie movie){

        for (String genre : movie.getGenres()) {

            if (genres.get(genre) == null){
                genres.put(genre, String.valueOf(this.getNextGenreId()));
            }
            String genreId = genres.get(genre);
            writeGenreData.addField(genreId);
            writeGenreData.addField(genre);
            writeGenreData.newLine();

            writeGenresInMoviesData.addField(genreId);
            writeGenresInMoviesData.addField(movie.getId());
            writeGenresInMoviesData.newLine();
        }
    }


    private void insertMovie(Movie movie){
        movies.put(movie.getFid(), movie);

        writeMovieData.addField(movie.getId());
        writeMovieData.addField(movie.getTitle());
        writeMovieData.addField(String.valueOf(movie.getYear()));
        writeMovieData.addField(movie.getDirector());
        writeMovieData.newLine();

        writeMovieInXmlData.addField(movie.getId());
        writeMovieInXmlData.addField(movie.getFid());
        writeMovieInXmlData.newLine();

        this.insertGenres(movie);
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
            this.movie.setId(this.getNextId());
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
                this.insertMovie(this.movie);
                // System.out.println(this.movie + "\n");
            }
            else{
                // System.out.println(this.movie.getInvalidLog() + "\n");
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


    // --- loading the data from file --- //

    private void loadMovieData(){
        try {
            String loadStr = "LOAD DATA LOCAL INFILE '" + writeMovieData.getFileName() + "'" +
                    " INTO TABLE movies" +
                    " FIELDS TERMINATED BY '" + writeMovieData.getFieldTerminator() + "'" +
                    " LINES TERMINATED BY '\\n'" +
                    " (@column1, @column2, @column3, @column4) SET id = @column1, title = @column2, year = @column3, director = @column4" ;

            System.out.println(loadStr);

            Statement loadStatement = dbcon.createStatement();
            loadStatement.execute(loadStr);
            loadStatement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void loadMovieInXmlData(){
        try {
            String loadStr = "LOAD DATA LOCAL INFILE '" + writeMovieInXmlData.getFileName() + "'" +
                    " INTO TABLE movies_in_xml" +
                    " FIELDS TERMINATED BY '" + writeMovieInXmlData.getFieldTerminator() + "'" +
                    " LINES TERMINATED BY '\\n'" +
                    " (@column1, @column2) SET movieId = @column1, xmlId = @column2";

            System.out.println(loadStr);

            Statement loadStatement = dbcon.createStatement();
            loadStatement.execute(loadStr);
            loadStatement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadGenreData(){
        try {
            String loadStr = "LOAD DATA LOCAL INFILE '" + writeGenreData.getFileName() + "'" +
                    " INTO TABLE genres" +
                    " FIELDS TERMINATED BY '" + writeGenreData.getFieldTerminator() + "'" +
                    " LINES TERMINATED BY '\\n'" +
                    " (@column1, @column2) SET id = @column1, name = @column2";

            System.out.println(loadStr);

            Statement loadStatement = dbcon.createStatement();
            loadStatement.execute(loadStr);
            loadStatement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadGenreInMoviesData(){
        try {
            String loadStr = "LOAD DATA LOCAL INFILE '" + writeGenresInMoviesData.getFileName() + "'" +
                    " INTO TABLE genres_in_movies" +
                    " FIELDS TERMINATED BY '" + writeGenresInMoviesData.getFieldTerminator() + "'" +
                    " LINES TERMINATED BY '\\n'" +
                    " (@column1, @column2) SET genreId = @column1, movieId = @column2";

            System.out.println(loadStr);

            Statement loadStatement = dbcon.createStatement();
            loadStatement.execute(loadStr);
            loadStatement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Movie> getMovies() {
        return movies;
    }

    
    public static void main(String[] args) {
        long startTime = System.nanoTime();

        ParseMovies test = new ParseMovies();
        test.run();

        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;
        System.out.println("Execution time: " + elapsedTime);
    }
}