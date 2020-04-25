// import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;

public class BuildQuery {
    private int numberOfConditions = 0;
    private int numberOfTables = 0;

    // SELECT ...
    private String selectStr = "SELECT ";

    // FROM ...
    private String fromStr = "";

    // WHERE ...
    private String whereStr = "";

    private String appendStr = "";

    // The full query
    private String query = "";

    // maps url parameters to the MySQL table column names
    private HashMap<String, String> parametersToColumns;


    public BuildQuery(){
        initializeParametersToColumns();
    }


    public BuildQuery(String selectStr){
        this();
        this.selectStr = selectStr;
    }


    private void initializeParametersToColumns(){
        parametersToColumns = new HashMap<String, String>() {{
            put("title", "movies_with_rating.title");
            put("director", "movies_with_rating.director");
            put("starName", "stars.name");
            put("genre", "genres.name");
        }};
    }

/*
    public void addWhereConditionsFromRequest(HttpServletRequest request){
        Enumeration<String> parameterNames = request.getParameterNames();

        while(parameterNames.hasMoreElements()){
            String name = parameterNames.nextElement();
            String value = request.getParameter(name);

        }
    }
*/

    private boolean notEmpty(String s){
        return (s != null && !s.equals(""));
    }


    public void addFromTables(String tables){
        if (numberOfTables == 0){
            selectStr += " FROM";
        }
        selectStr += " " + tables;
        ++numberOfTables;
    }


    // adds the where conditions
    public void addWhereConditions(String template, String columnName, String value){
        if (notEmpty(value)){

            // Check if this is the first argument
            if(numberOfConditions == 0) {
                // add "WHERE" to front
                template = " WHERE " + template;
            }
            else{
                template = " AND " + template;
            }
            numberOfConditions++;
            whereStr += String.format(template, columnName, value);
        }
    }

    public void append(String s){
        appendStr += " " + s;
    }

    public void setSelectStr(String selectStr) {
        this.selectStr += selectStr;
    }

    public void setFromStr(String fromStr) {
        this.fromStr = fromStr;
    }

    public void setWhereStr(String whereStr) {
        this.whereStr = whereStr;
    }

    public String getSelectStr() {
        return selectStr;
    }

    public String getFromStr() {
        return fromStr;
    }

    public String getWhereStr() {
        return whereStr;
    }

    public String getQuery() {
        query = selectStr + fromStr + whereStr + appendStr;
        return query;
    }


    public static void main(String[] args){
        System.out.println("running BuildQuery Main");

        BuildQuery query = new BuildQuery();
        query.setSelectStr("*");
        query.addFromTables("movies_with_rating");
        query.addWhereConditions("%s LIKE \"%%%s%%\"","movies_with_ratings.title", "a");
        query.addWhereConditions("%s LIKE \"%%%s%%\"","stars.name", "a");
        query.addFromTables("JOIN (stars JOIN stars_in_movies ON id = starId) ON movies_with_rating.id = stars_in_movies.movieId");

        System.out.println(query.getQuery());
    }

}
