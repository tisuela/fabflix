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

function changedRPP() {
    let rpp = $("#Count :selected").val();
    let searchParams = new URLSearchParams(window.location.search);
    searchParams.set("results", rpp);
    searchParams.set("page", "1"); // reset our page to the first page
    // Rationale for resetting page to 1:
    // Prevent users from reaching a high page count with a low results per page
    // then swapping to a high results per page and getting an empty page with it
    window.location.replace("index.html?" + searchParams);
}


function createButton(value, func, argument1){
    let button = document.createElement("input");
    button.type = "button";
    button.value = value;

    // create another function in order to put arguments in "func"
    button.onclick = function(){return func(argument1);};
    return button;
}



function addToCart(movieId){
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET",// Setting request method
        url: "api/cart?action=add_to_cart&id=" + movieId,
        success: (resultData) => cartResult(resultData) // Setting callback function to handle data returned successfully by the CartServlet
    });
}


function cartResult(resultData){
    // Update cart result cell after CartServlet returns
    let movieId = resultData["cartMovieId"];
    let addToCartResultCell = document.getElementById("cart_result_" + movieId);
    addToCartResultCell.innerText = "Success";
}


function addGenres(cell, genresJson){
    let cellHTML = "<ul>";
    for(let i = 0; i < genresJson.length; i++){
        cellHTML += "<li>" + '<a href="index.html?mode=genre&genre=' + genresJson[i]["genre_name"] + '">';
        cellHTML += genresJson[i]["genre_name"] + "</a>" + "</li>";
    }
    cellHTML += "</ul>";
    cell.innerHTML = cellHTML;
    return cell;
}


function addStars(cell, starsJson){
    let cellHTML = "<ul>";
    for(let i = 0; i < starsJson.length; i++){
        cellHTML += "<li>" + '<a href="single-star.html?id=' + starsJson[i]["star_id"] + '">';
        cellHTML += starsJson[i]["star_name"] + "</a>" + "</li>";
    }
    cellHTML += "</ul>";
    cell.innerHTML = cellHTML;
    return cell;
}


function createRow(movieJson){
    let row = document.createElement("tr");
    let titleCell = row.insertCell(-1);
    let yearCell = row.insertCell(-1);
    let directorCell = row.insertCell(-1);
    let genresCell = row.insertCell(-1);
    let starsCell = row.insertCell(-1);
    let ratingCell = row.insertCell(-1);
    let addToCartCell = row.insertCell(-1);
    let addToCartResultCell = row.insertCell(-1);

    titleCell.innerHTML =  '<a href="single-movie.html?id=' + movieJson['movie_id'] + '">'
        + movieJson["movie_title"] + '</a>';
    yearCell.innerHTML = movieJson["movie_year"];
    directorCell.innerHTML = movieJson["movie_director"];

    // These are bulleted lists
    addGenres(genresCell, movieJson["movie_genres"]);
    addStars(starsCell, movieJson["movie_stars"]);

    ratingCell.innerHTML = movieJson["movie_rating"];

    // Add to cart button and the result
    addToCartCell.append(createButton("Add to cart", addToCart, movieJson["movie_id"]));

    // Add id to write result later by finding the cell's id
    addToCartResultCell.id = "cart_result_" + movieJson["movie_id"];

    return row;
}


function createCheckoutButton(){
    let checkoutDiv = document.getElementById("checkout-button");
    let button = document.createElement("input");
    button.type = "button";
    button.value = "Check out";

    // create another function in order to put arguments in "func"
    button.onclick = function(){return window.location='cart.html';};
    checkoutDiv.appendChild(button);
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultJson jsonObject
 */
function handleMovieResult(resultJson) {
    console.log("handleStarResult: populating star table from resultData");

    let resultData = resultJson["movies"];

    // Populate the Movie table
    // Find the empty table body by id "movie_table_body"
    let movieTableBody = jQuery("#movie_table_body");
    createCheckoutButton();

    // Iterate through resultData
    for (let i = 0; i < resultData.length; i++) {
        let row = createRow(resultData[i]);
        movieTableBody.append(row);
        }

    // also handles the prev and next buttons since it requires the resultJSON data
    let searchParams = new URLSearchParams(window.location.search);

    let pagination = $("#pagination");
    //pagination.append()
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

let query = getParameters();
setSortButtons();
setBrowseCategories();

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies?" + query, // Setting request url, which is mapped by MoviesServlet
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the MoviesServlet
});