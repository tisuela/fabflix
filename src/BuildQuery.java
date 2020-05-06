// import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/* Purpose of this class is to:
 *  > Reduce duplicate code
 *  > Have control over all queries made
 *  > Break up the building of queries into small parts
 *  > Make it easier to implement future changes
 */

public class BuildQuery {
    protected int numberOfConditions = 0;
    protected int numberOfTables = 0;

    // SELECT ...
    protected String selectStr = "SELECT DISTINCT ";

    // FROM ...
    protected String fromStr = "";

    // WHERE ...
    protected String whereStr = "";

    // ORDER BY ... LIMIT ... etc.
    protected String appendStr = "";

    // maps url parameters to the MySQL table column names
    protected HashMap<String, String> parametersToColumns;

    // templates for creating queries
    // string formatting for column names and parameter values
    // Sets up the query to be used as a Prepared Statement
    // Map of Maps: {"parameter": {"value": String, "template1": String}}
    protected Map<String, Map<String, String>> parameterTemplates;

    // values, used to fill in the "?" for PreparedStatements
    // Sets up the query to be executed
    // Array of map, where map = {"value": String, "type": String}
    protected ArrayList<Map<String, String>> selectValues;
    protected ArrayList<Map<String, String>> fromValues;
    protected ArrayList<Map<String, String>> whereValues;
    protected ArrayList<Map<String, String>> appendValues;


    public BuildQuery(){
        initializeParametersToColumns();
        initializeParametersToTemplates();
        initializeQueryValues();
    }


    public BuildQuery(String selectStr){
        this();
        this.selectStr = selectStr;
    }


    // --- initializing data structures --- //


    private void initializeQueryValues(){
        selectValues = new ArrayList<Map<String,String>>();
        fromValues = new ArrayList<Map<String,String>>();
        whereValues = new ArrayList<Map<String,String>>();
        appendValues = new ArrayList<Map<String,String>>();
    }


    private void initializeParametersToColumns(){
        parametersToColumns = new HashMap<String, String>() {{
            put("title", "movies_with_rating.title");
            put("director", "movies_with_rating.director");
            put("starName", "stars.name");
            put("genre", "genres.name");
            put("year", "movies_with_rating.year");
        }};
    }


    private void initializeParametersToTemplates(){
        // SQL columns
        String like = "%s LIKE ?";
        String equals = "%s = ?";

        // SQL values with matching
        String contains = "%%%s%%";
        String beginsWith = "%%%s";
        String match = "%s";


        parameterTemplates = new HashMap<String, Map<String, String>>(){{
           put("title", new HashMap<String, String>(){{
               put("search_column", like);
               put("search_value", contains);
               put("browse_column", like);
               put("browse_value", beginsWith);
               put("type", "String");
           }});
            put("director", new HashMap<String, String>(){{
                put("search_column", like);
                put("search_value", contains);
                put("browse_column", like);
                put("browse_value", contains);
                put("type", "String");
            }});
            put("starName", new HashMap<String, String>(){{
                put("search_column", like);
                put("search_value", contains);
                put("browse_column", like);
                put("browse_value", contains);
                put("type", "String");
            }});
            put("genre", new HashMap<String, String>(){{
                put("search_column", equals);
                put("search_value", match);
                put("browse_column", equals);
                put("browse_value", match);
                put("type", "String");
            }});
            put("year", new HashMap<String, String>(){{
                put("search_column", equals);
                put("search_value", match);
                put("browse_column", equals);
                put("browse_value", match);
                put("type", "int");
            }});
        }};
    }


    // --- Utility methods --- //


    protected boolean notEmpty(String s){
        return (s != null && !s.equals(""));
    }


    // --- Building Query --- //


    public void addFromTables(String tables){
        if (numberOfTables == 0){
            selectStr += " FROM";
        }
        selectStr += " " + tables;
        ++numberOfTables;
    }


    // for specifying parameters
    public void addFromTables(String tables, String value){
        if (numberOfTables == 0){
            selectStr += " FROM";
        }
        selectStr += " " + tables;

        // add parameter value to map
        this.fromValues.add(new HashMap<String, String>(){{
            put("value", value);
            put("type", "String");
        }});
        ++numberOfTables;
    }

    public void addFromTables(String tables, int value){
        if (numberOfTables == 0){
            selectStr += " FROM";
        }
        selectStr += " " + tables;

        // add parameter value to map
        this.fromValues.add(new HashMap<String, String>(){{
            put("value", String.valueOf(value));
            put("type", "int");
        }});
        ++numberOfTables;
    }


    // adds the where conditions. Will always have to add parameter values
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
            whereStr += String.format(template, columnName);
            this.whereValues.add(new HashMap<String, String>(){{
                put("value", value);
                put("type", "String");
            }});
            numberOfConditions++;
        }
    }


    public void append(String s){
        appendStr += " " + s;
    }


    // Append while adding parameter value
    public void append(String template, String value, String type){
        this.append(template);

        // add parameter value to map
        this.appendValues.add(new HashMap<String, String>(){{
            put("value", value);
            put("type", type);
        }});
    }

    public void append(String template, String value){
        this.append(template);

        // add parameter value to map
        this.appendValues.add(new HashMap<String, String>(){{
            put("value", value);
            put("type", "String");
        }});
    }

    public void append(String template, int value){
        this.append(template);

        // add parameter value to map
        this.appendValues.add(new HashMap<String, String>(){{
            put("value", String.valueOf(value));
            put("type", "int");
        }});
    }


    // --- Building query from parameter map --- //


    // adds ordering/sorting parameters to query
    private void addOrdering(Map<String,String[]> parameters){
        // By default, we will have things in order of Rating Descending, Title Ascending
        String ordering1 = "rating";
        String sorting1  = "DESC";
        String ordering2 = "title";
        String sorting2  = "ASC";
        String results = "25";
        String offset = "0";
        String statement = "ORDER BY %1$s %2$s, %3$s %4$s";

        // check for order and sort to append the appropriate conditions
        if ( parameters.get("order") != null && this.notEmpty(parameters.get("order")[0]) && parameters.get("order")[0].equals("title")){
            ordering1 = "title";
            sorting1  = "ASC";
            ordering2 = "rating";
            sorting2  = "DESC";
        }
        if(parameters.get("sort1") != null && this.notEmpty(parameters.get("sort1")[0])) {
            if (parameters.get("sort1")[0].equals("asc")) {
                sorting1 = "ASC";
            } else {
                sorting1 = "DESC";
            }
        }
        if(parameters.get("sort2") != null && this.notEmpty(parameters.get("sort2")[0])){
            if(parameters.get("sort2")[0].equals("asc")) {
                sorting2 = "ASC";
            } else {
                sorting2 = "DESC";
            }
        }
        if(parameters.get("results") != null && this.notEmpty(parameters.get("results")[0])){
            results = parameters.get("results")[0];
        }
        if(parameters.get("pageNum") != null && this.notEmpty(parameters.get("pageNum")[0])){
            String page = parameters.get("pageNum")[0];
            offset = String.valueOf( (Integer.parseInt(page) - 1) * Integer.parseInt(results) );
        }

        this.append(String.format(statement, ordering1, sorting1, ordering2, sorting2));
        this.append("LIMIT ?", results, "int");
        this.append("OFFSET ?", offset, "int");
    }



    // adds parameters to WHERE conditions
    public void addParameters(Map<String, String[]> parameters){
        this.addOrdering(parameters);

        // check if browsing or searching to determine templates
        // default is search
        String templateName = "search_";;
        if (parameters.get("mode") != null && this.notEmpty(parameters.get("mode")[0]) &&
                (parameters.get("mode")[0].equals("title") || parameters.get("mode")[0].equals("genre") )) {
            templateName = "browse_";
            if(parameters.get("title") != null && this.notEmpty(parameters.get("title")[0]) &&
                    parameters.get("title")[0].equals("*")){
                this.addWhereConditions("%s REGEXP ?", "title", "^[^a-z0-9]+");
                numberOfConditions++;
            }
        }

        // add where conditions
        for (String name: parameters.keySet()) {
            try {
                String value = parameters.get(name)[0];

                // ensure that parameter value is not an empty String
                if (notEmpty(value)) {

                    String column = parametersToColumns.get(name);
                    String columnTemplate = this.parameterTemplates.get(name).get(templateName + "column");
                    String valueTemplate = this.parameterTemplates.get(name).get(templateName + "value");

                    // idk what this is for???
                    if (value.equals("*") && parameters.get("mode") != null) {
                        continue;
                    }

                    String formattedValue = String.format(valueTemplate, value);
                    System.out.println(columnTemplate + column + formattedValue);
                    addWhereConditions(columnTemplate, column, formattedValue);
                }
            }
            catch (NullPointerException e){
                // Skip invalid parameters
                System.out.println(e.getMessage());
            }
        }
    }


    // --- Getters and Setters --- //


    public void setSelectStr(String selectStr) {
        this.selectStr += selectStr;
        // is it correct to += here or is just equal?
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
        return selectStr + fromStr + whereStr + appendStr;

    }


    // Aggregates all values into one list
    protected ArrayList<Map<String, String>> getValues(){
        ArrayList<Map<String, String>> values = new ArrayList<Map<String, String>>();
        values.addAll(selectValues);
        values.addAll(fromValues);
        values.addAll(whereValues);
        values.addAll(appendValues);

        return values;
    }


    // --- Testing --- //


    // For testing
    public static void main(String[] args){
        System.out.println("running BuildQuery Main");

        BuildQuery query = new BuildQuery();
        query.setSelectStr("*");
        query.addFromTables("movies_with_rating");
        query.addWhereConditions("%s LIKE \"%%%s%%\"","movies_with_ratings.title", "a");
        query.addWhereConditions("%s LIKE \"%%%s%%\"","stars.name", "a");
        query.addFromTables("JOIN (stars JOIN stars_in_movies ON id = stars_in_movies.starId) ON movies_with_rating.id = stars_in_movies.movieId");

        System.out.println(query.getQuery());
    }

}
