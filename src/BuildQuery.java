// import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BuildQuery {
    protected int numberOfConditions = 0;
    protected int numberOfTables = 0;

    // SELECT ...
    protected String selectStr = "SELECT DISTINCT ";

    // FROM ...
    protected String fromStr = "";

    // WHERE ...
    protected String whereStr = "";

    protected String appendStr = "";

    // maps url parameters to the MySQL table column names
    protected HashMap<String, String> parametersToColumns;

    // templates
    protected HashMap<String, String> parametersToSearchTemplates;
    protected HashMap<String, String> parametersToBrowseTemplates;

    // values, used to fill in the "?" for PreparedStatements
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


    protected void initializeQueryValues(){
        selectValues = new ArrayList<Map<String,String>>();
        fromValues = new ArrayList<Map<String,String>>();
        whereValues = new ArrayList<Map<String,String>>();
        appendValues = new ArrayList<Map<String,String>>();
    }


    protected void initializeParametersToColumns(){
        parametersToColumns = new HashMap<String, String>() {{
            put("title", "movies_with_rating.title");
            put("director", "movies_with_rating.director");
            put("starName", "stars.name");
            put("genre", "genres.name");
            put("year", "movies_with_rating.year");
        }};
    }


    protected void initializeParametersToTemplates(){
        String like = "%s LIKE ?";
        String equalsStr = "%s = ?";
        String equalsInt = "%s = ?";
        String beginsStr = "%s LIKE ?";

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
        if (parameters.get("mode") != null && this.notEmpty(parameters.get("mode")[0]) &&
                (parameters.get("mode")[0].equals("title") || parameters.get("mode")[0].equals("genre") )) {
            templates = parametersToBrowseTemplates;
            if(parameters.get("title") != null && this.notEmpty(parameters.get("title")[0]) &&
                    parameters.get("title")[0].equals("*")){
                this.addWhereConditions("%s REGEXP ?", "title", "^[^a-z0-9]+");
                numberOfConditions++;
            }
        }
        // otherwise, by default we use search page
        else {
            templates = parametersToSearchTemplates;
        }

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

        for (String name: parameters.keySet()){
            String column = parametersToColumns.get(name);
            String template = templates.get(name);
            if(notEmpty(template)) {
                String value = parameters.get(name)[0];
                if(value.equals("*") && parameters.get("mode") != null) { continue; }
                System.out.println(template + " " +column + " " + value);
                addWhereConditions(template, column, value);
            }
        }
    }


    protected boolean notEmpty(String s){
        return (s != null && !s.equals(""));
    }


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
