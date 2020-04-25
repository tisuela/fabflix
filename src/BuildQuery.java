// import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class BuildQuery {
    private int numberOfConditions = 0;
    private int numberOfTables = 0;

    // SELECT ...
    private String selectStr = "SELECT DISTINCT ";

    // FROM ...
    private String fromStr = "";

    // WHERE ...
    private String whereStr = "";

    private String appendStr = "";

    // The full query
    private String query = "";

    // maps url parameters to the MySQL table column names
    private HashMap<String, String> parametersToColumns;

    private HashMap<String, String> parametersToSearchTemplates;
    private HashMap<String, String> parametersToBrowseTemplates;


    public BuildQuery(){
        initializeParametersToColumns();
        initializeParametersToTemplates();
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
            put("year", "movies_with_ratings.year");
        }};
    }


    private void initializeParametersToTemplates(){
        String like = "%s LIKE \"%%%s%%\"";
        String equalsStr = "%s = \"%s\"";
        String equalsInt = "%s = %s";
        String beginsStr = "%s LIKE \"%s%%\"";

        parametersToSearchTemplates = new HashMap<String, String>() {{
            put("title", like);
            put("director", like);
            put("starName", like);
            put("genre", equalsStr);
            put("year", equalsInt);
        }};

        parametersToBrowseTemplates = new HashMap<String, String>() {{
            put("title", beginsStr);
            put("director", like);
            put("starName", like);
            put("genre", equalsStr);
            put("year", equalsInt);
        }};
    }

    // adds parameters to WHERE conditions
    public void addParameters(Map<String, String[]> parameters){
        // check if browsing
        Map<String, String> templates = new HashMap<String, String>();
        if (parameters.get("browse") != null && notEmpty(parameters.get("browse")[0]) && parameters.get("browse")[0].equals("true")) {
            templates = parametersToBrowseTemplates;
        }
        // else use search
        else {
            templates = parametersToSearchTemplates;
        }

        for (String name: parameters.keySet()){
            String column = parametersToColumns.get(name);
            String template = templates.get(name);
            if(notEmpty(template)) {
                String value = parameters.get(name)[0];

                addWhereConditions(template, column, value);
            }
        }
    }


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
