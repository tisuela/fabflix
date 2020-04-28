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
let browseGenres = $("#browse-genres");
browseGenres.append(genreBrowse());
let browseTitles = $("#browse-titles");
browseTitles.append(titleBrowse());

