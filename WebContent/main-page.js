let search_form = $("#search_form");
let autocompleteCachePrefix = "autocompleteCache_";

// --- FORMS --- //


function submitSearchForm(formSubmitEvent) {
    console.log("submit search form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();
    let url = "index.html?";

    url += search_form.serialize();

    window.location.replace(url); // remove last "&" from URL

}

function generateGenreLink(genre){
    return "<a href='index.html?mode=genre&genre=" + genre + "'>" + genre + "</a> ";
}

function handleGenres(resultJSON){
    let browseGenres = $("#browse-genres");

    let genres = resultJSON["genres"];

    for(let i = 0; i < genres.length; i++){
        let genreName = genres[i]["name"];
        let genreLink = generateGenreLink(genreName);
        browseGenres.append(genreLink);
    }
}

function titleBrowse(){
    let result = "";
    let titles = "0123456789abcdefghijklmnopqrstuvwxyz*";
    for(var i = 0; i < titles.length; i++){
        let character = titles.charAt(i);
        result += "<a href='index.html?mode=title&title=" + character + "'>" + character + "</a> ";
    }
    return result;
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


// --- Autocomplete Search --- //


/*
 * This function is used to handle the ajax success callback function.
 * It is called by our own code upon the success of the AJAX request
 *
 * data is the JSON data string you get from your Java Servlet
 *
 */
function handleLookupAjaxSuccess(data, query, doneCallback, isCached) {
    console.log("lookup ajax successful")
    console.log(data)

    // Extract the data to be used by the autocomplete library
    let movieData = data["movies"];

    // IF this result is not cached already, cache it
    if (!isCached) {
        // Cache the result (JSON must be a string to be stored in localStorage)
        localStorage.setItem(autocompleteCachePrefix + query, JSON.stringify(data));
        console.log("Cache query result");
    }

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: movieData } );
}


/*
 * This function is the select suggestion handler function.
 * When a suggestion is selected, this function is called by the library.
 *
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
    console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["movie_id"])

    // Redirect to the movie page
    let url = "single-movie.html?id=";
    url += suggestion["data"]["movie_id"];
    window.location.replace(url);
}


/*
 * do normal full text search if no suggestion is selected
 */
function handleNormalSearch(query) {
    console.log("doing normal search with query: " + query);
    let url = "index.html?fulltext=true&title="; // use fulltext
    url += query;
    window.location.replace(url);
}



/*
 * This function is called by the library when it needs to lookup a query.
 *
 * The parameter query is the query string.
 * The doneCallback is a callback function provided by the library, after you get the
 *   suggestion list from AJAX, you need to call this function to let the library know.
 */
function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")
    console.log("sending AJAX request to backend Java Servlet")

    let cachedQuery = localStorage.getItem(autocompleteCachePrefix + query);
    if (cachedQuery == null) {
        // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
        // with the query data
        jQuery.ajax({
            "method": "GET",
            // generate the request url from the query.
            // escape the query string to avoid errors caused by special characters
            "url": "api/autocomplete?title=" + escape(query),
            "success": function (data) {
                // pass the data, query, and doneCallback function into the success handler
                handleLookupAjaxSuccess(data, query, doneCallback, false)
            },
            "error": function (errorData) {
                console.log("lookup ajax error")
                console.log(errorData)
            }
        });
    }
    else {
        console.log("Using cached query");
        handleLookupAjaxSuccess(JSON.parse(cachedQuery), query, doneCallback, true);
    }

}



// --- Binding --- //

let browseTitles = $("#browse-titles");
browseTitles.append(titleBrowse());

createCheckoutButton();
// Bind the submit action of the form to a handler function
search_form.submit(submitSearchForm);

// bind pressing enter key to a handler function
$('#autocomplete').keypress(function(event) {
    // keyCode 13 is the enter key
    if (event.keyCode == 13) {
        // pass the value of the input box to the handler function
        handleNormalSearch($('#autocomplete').val())
    }
})

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/genres", // Setting request url, which is mapped by GenreServlet
    success: (resultData) => handleGenres(resultData) // Setting callback function to handle data returned successfully by the GenreServlet
});



/*
 * This statement binds the autocomplete library with the input box element and
 *   sets necessary parameters of the library.
 *
 * The library documentation can be find here:
 *   https://github.com/devbridge/jQuery-Autocomplete
 *   https://www.devbridge.com/sourcery/components/jquery-autocomplete/
 *
 */
// $('#autocomplete') is to find element by the ID "autocomplete"
$('#autocomplete').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    // set minimum char to trigger autosuggest
    minChars: 3
});

