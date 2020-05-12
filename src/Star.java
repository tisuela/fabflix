public class Star {
    // logging
    private static int numOfStars = 0;
    private static int numOfInvalidStars = 0;
    private static int numOfValidStars = 0;
    private int numOfThisStar;

    private boolean valid = false;
    private String invalidLog = "";

    // star info
    private String name;
    private int birthYear = -1;
    private String id;


    public Star(){
       numOfThisStar = ++numOfStars;
    }


    public boolean isValid(){
        if (name.length() < 1){
            invalidLog += "No name ";
            numOfInvalidStars++;
        }
        else{
            valid = true;
            numOfValidStars++;
        }
        return valid;
    }


    public String toString(){
        String result = "Name: %s | Birth year: %d | Number: %d | id %s";
        result = String.format(result, name, birthYear, numOfThisStar, id);
        return result;
    }


    // getters and setters


    public String getInvalidLog() {
        return "Invalid log: " + invalidLog;
    }

    public static int getNumOfStars() {
        return numOfStars;
    }

    public static int getNumOfInvalidStars() {
        return numOfInvalidStars;
    }

    public static int getNumOfValidStars() {
        return numOfValidStars;
    }

    public int getNumOfThisStar() {
        return numOfThisStar;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static void setNumOfStars(int numOfStars) {
        Star.numOfStars = numOfStars;
    }

    public String getName() {
        return name;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBirthYear(int birthYear) {
        this.birthYear = birthYear;
    }

    public void setBirthYear(String birthYear) {
        try {
            this.birthYear = Integer.parseInt(birthYear);
        } catch (NumberFormatException e){
            ;
        }
    }

}
