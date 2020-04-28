/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating movie info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let movieInfoElement = jQuery("#movie_info");

    // append one html <p> created to the h3 body, which will refresh the page
    movieInfoElement.append("<p>Movie Name: " + resultData["movie_name"] + "</p>" +
        "<p>Movie Year: "  + resultData["movie_year"] + "</p>" +
        "<p>Movie Director: "  + resultData["movie_director"] + "</p>" +
        "<p>" + '<a href="' + resultData["movieState"] +'"> Back to Movies </a>' + "</p>");

    console.log("handleResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows

    // get list of movie Stars
    let rowHTML = "";
    rowHTML += "<tr><th><ul>";
    let movieStars = resultData["movie_stars"];
    for(let starsIndex = 0; starsIndex < movieStars.length; starsIndex++){
        rowHTML += "<li>" + '<a href="single-star.html?id=' + movieStars[starsIndex]["star_id"] + '">';
        rowHTML += movieStars[starsIndex]["star_name"] + "</a>" + "</li>";
    }
    rowHTML += "</ul></th>";


    rowHTML += "<th><ul>";
    let movieGenres = resultData["movie_genres"];
    for(let genresIndex = 0; genresIndex < movieGenres.length; genresIndex++){
        rowHTML += "<li>" + movieGenres[genresIndex]["genre_name"] + "</li>";
    }
    rowHTML += "</ul></th>";


    rowHTML += "</tr>";



    // Append the row created to the table body, which will refresh the page
    movieTableBodyElement.append(rowHTML);
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});