import java.util.ArrayList;
import java.util.List;

public class Movie {
    private String title = "";
    private int year = 0;
    private String director = "";
    private List<String> genres;

    public Movie(){
        genres = new ArrayList<>();
    }


    public void addGenre(String genre){
        genres.add(genre);
    }


    public String toString(){
        String result = "Title = %s | Year = %d | Director %s \nGenres:";
        result = String.format(result, this.title, this.year, this.director);

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

    public void setDirector(String director) {
        this.director = director;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public String getDirector() {
        return director;
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
