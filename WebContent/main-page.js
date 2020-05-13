let search_form = $("#search_form");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */


/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
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

createCheckoutButton();
// Bind the submit action of the form to a handler function
search_form.submit(submitSearchForm);

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/genres", // Setting request url, which is mapped by GenreServlet
    success: (resultData) => handleGenres(resultData) // Setting callback function to handle data returned successfully by the GenreServlet
});
let browseTitles = $("#browse-titles");
browseTitles.append(titleBrowse());

