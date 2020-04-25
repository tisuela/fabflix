let search_form = $("#search_form");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleSearchResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle search response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If login succeeds, it will redirect the user to main-page.html
    if (resultDataJson["status"] === "success") {
        $("#search_error_message").text(resultDataJson["message"]);
    } else {
        // If search fails, the web page will display
        // error messages on <div> with id "search_error_message"
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#search_error_message").text(resultDataJson["message"]);
    }
}

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
    let url = "index.html?search=true";

    url += search_form.serialize();

    window.location.replace(url); // remove last "&" from URL

}

// Bind the submit action of the form to a handler function
search_form.submit(submitSearchForm);

