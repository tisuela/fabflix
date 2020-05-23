package edu.uci.ics.fabflixmobile;

public class Star {
    private String id;
    private String name;
    private String birthYear;

    public Star(String id, String name, String birthYear){
        this.id = id;
        this.name = name;
        this.birthYear = birthYear;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBirthYear() {
        return birthYear;
    }
}
