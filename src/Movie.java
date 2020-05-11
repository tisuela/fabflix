import java.util.ArrayList;
import java.util.List;


// Helper class for the SAX Parser
// Also takes care of logging inconsistencies
public class Movie {
    // logging
    private static int numOfMovies = 0;
    private static int numOfInvalidMovies = 0;
    private static int numOfValidMovies = 0;
    private int numOfThisMovie;

    private boolean valid = false;
    private String invalidLog = "";

    // movie info
    private String title = "";
    private int year = -1;
    private String director = "";
    private List<String> genres;
    private float rating  = -1;
    private String fid = ""; // XML film ID
    private String id = "";



    public Movie(){
        this.genres = new ArrayList<>();
        this.numOfThisMovie = ++this.numOfMovies;
    }


    public void addGenre(String genre){
        genres.add(genre);
    }


    public boolean isValid(){
        // Log inconsistencies
        if(year < 0){
            this.invalidLog += "Invalid year ";
        }
        if(title.length() < 1){
            this.invalidLog += "Invalid title ";
        }
        if(director.length() < 1){
            this.invalidLog += "Invalid director ";
        }
        if(genres.size() < 1){
            this.invalidLog += "No genres";
        }

        // Check if this movie is valid / consistent
        if(year > 0 && title.length() > 0 && director.length() > 0 && genres.size() > 0){
            this.valid = true;
            numOfValidMovies++;
        }
        else{
            this.numOfInvalidMovies++;
        }

        return this.valid;
    }


    public String toString(){
        String result = "Title = %s | Year = %d | Director %s | fid %s | id %s \nGenres:";
        result = String.format(result, this.title, this.year, this.director, this.fid, this.id);

        for (String genre: genres){
            result += "\n   " + genre;
        }
        return result;
    }


    // Setters and Getters //


    public void setTitle(String title) {
        this.title = title;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setYear(String year) {
        try {
            this.year = Integer.parseInt(year);
        }
        catch(NumberFormatException e){
        }
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public static int getNumOfMovies() {
        return numOfMovies;
    }

    public int getNumOfThisMovie() {
        return numOfThisMovie;
    }

    public int getNumOfInvalidMovies() {
        return numOfInvalidMovies;
    }

    public String getFid() {
        return fid;
    }

    public float getRating() {
        return rating;
    }

    public static int getNumOfValidMovies() {
        return numOfValidMovies;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public String getId() {
        return id;
    }

    public String getDirector() {
        return director;
    }

    public String getInvalidLog() {
        return "Movie #" + numOfThisMovie + " is invalid: " + invalidLog;
    }

    public List<String> getGenres() {
        return genres;
    }

    public static void main(String[] args){
        Movie test = new Movie();
        test.setTitle("poop");
        test.setDirector("poop");
        test.setYear(2000);
        test.addGenre("poop");
        System.out.println(test);
    }

}
