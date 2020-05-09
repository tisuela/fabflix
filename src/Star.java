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
        String result = "Name: %s | Birth year: %d | Number: %d";
        result = String.format(result, name, birthYear, numOfThisStar);
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
