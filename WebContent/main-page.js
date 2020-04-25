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
        result += "<a href='index.html?genre=" + genre + "'>" + genre + "</a> ";
    }
    return result;
}

// Bind the submit action of the form to a handler function
search_form.submit(submitSearchForm);
let browseCategories = $("#browse-genres");
browseCategories.append(genreBrowse());

