/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */




/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameters(){
    let url = window.location.href;
    let query = url.split("?").pop();
    console.log("query is " + query);
    return query;
}

function sortByGenerator(orderBy, sortBy){
    let searchParams = new URLSearchParams(window.location.search);
    let order = searchParams.get("order");
    let sort1 = searchParams.get("sort1")
    let sort2 = searchParams.get("sort2");
    if(order !== orderBy) { // if order is not set or the incorrect one
        order = orderBy;
        if (sort1 && sort2) { // neither sort is null, we save sort1 into sort2 to preserve it
                              // since we are swapping from title to rating or vice versa
            sort2 = sort1;
        }
    }
    sort1 = sortBy;
    if(!sort2){ // sort 2 is null here so we set it to our default value based
        console.log("sort2 is null");
        if(order == "rating"){ sort2 = "asc";} // sort2 will be title , which is defaulted to ascending
        else{sort2 = "desc"}                   // sort2 will be rating, which is defaulted to descending
    }

    searchParams.set("order", order);
    searchParams.set("sort1", sort1);
    searchParams.set("sort2", sort2);
    return searchParams;
}

function setSortButtons(){
    const buttonPairs = [ ["title", "asc"], ["title", "desc"], ["rating", "asc"], ["rating", "desc"] ];
    let sortingButtons = $("#sorting-buttons");
    let result = "";

    const encoder = {"asc": "↑", "desc": "↓"}

    for(i = 0; i< buttonPairs.length; i++){
        var order = buttonPairs[i][0];
        var sort  = buttonPairs[i][1];
        console.log("new url for " + order + sort + " : " + sortByGenerator(order, sort));
        result += "<a href='index.html?" + sortByGenerator(order, sort) + "'>" + order + encoder[sort] + "</a> ";
    }
    sortingButtons.append(result);
}

function genreBrowse(){
    let result = '';
    let genres = ["Action", "Adult", "Adventure", "Animation", "Biography", "Comedy", "Crime",
        "Documentary", "Drama", "Family", "Fantasy", "History", "Horror", "Music", "Musical",
        "Mystery", "Reality-TV", "Romance", "Sci-Fi", "Sport", "Thriller", "War", "Western"]
    for(const genre of genres){
        result += "<a href='index.html?mode=genre&genre=" + genre + "'>" + genre + "</a> ";
    }
    return result;
}

function titleBrowse(){
    let result = "";
    let titles = "0123456789abcdefghijklmnopqrstuvwxyz*";
    for(let i = 0; i < titles.length; i++){
        let character = titles.charAt(i);
        result += "<a href='index.html?mode=title&title=" + character + "'>" + character + "</a> ";
    }
    return result;
}

function setBrowseCategories() {
    let searchParams = new URLSearchParams(window.location.search);
    let mode = searchParams.get("mode");
    if(mode){
        let banner = $("#browse-categories");
        if(mode === "title"){
            banner.append(titleBrowse());
        }
        if(mode === "genre"){
            banner.append(genreBrowse());
        }
    }
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleMovieResult(resultData) {
    console.log("handleStarResult: populating star table from resultData");

    // Populate the Movie table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < Math.min(20, resultData.length); i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            // Add a link to single-movie.html with id passed with GET url parameter
            '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]["movie_title"] +     // display star_name for the link text
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";

        // get list of genres
        rowHTML += "<th><ul>";
        let movieGenres = resultData[i]["movie_genres"];
        for(let genreIndex = 0; genreIndex < movieGenres.length; genreIndex++){
            rowHTML += "<li>" + movieGenres[genreIndex]["genre_name"] + "</li>";
        }
        rowHTML += "</ul></th>";

        // get list of movie Stars
        rowHTML += "<th><ul>";
        let movieStars = resultData[i]["movie_stars"];
        for(let starsIndex = 0; starsIndex < movieStars.length; starsIndex++){
            rowHTML += "<li>" + '<a href="single-star.html?id=' + movieStars[starsIndex]["star_id"] + '">';
            rowHTML += movieStars[starsIndex]["star_name"] + "</a>" + "</li>";
        }
        rowHTML += "</ul></th>"

        rowHTML += "<th>" + resultData[i]["movie_rating"] + "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
        }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

let query = getParameters();
setSortButtons();
setBrowseCategories();

//console.log("new url    : " + sortByTitleFirstAscending());
console.log("current url: " + new URLSearchParams(window.location.search));

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies?" + query, // Setting request url, which is mapped by MoviesServlet
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the MoviesServlet
});