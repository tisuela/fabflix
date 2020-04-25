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
            put("title", "movies.title");
            put("director", "movies.director");
            put("starName", "stars.name");
            put("genre", "genres.name");
        }};
    }


    private boolean notEmpty(String s){
        return (s != null && !s.equals(""));
    }


    public String addFromTables(String tables){
        return tables;
    }



    // adds the where conditions, returns the added portion (for debugging)
    public String addWhereConditions(String template, String columnName, String value){
        if (notEmpty(value)){

            // Check if this is the first argument
            if(numberOfConditions == 0) {
                // add "WHERE" to front
                template = " WHERE" + template;
            }
            else{
                template = " AND" + template;
            }
            numberOfConditions++;
            String filter = String.format(template, columnName, value);
            whereStr += filter;
            return filter;
        }
        else return "";
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
        query = selectStr + fromStr + whereStr;
        return query;
    }

}
