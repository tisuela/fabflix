let add_star_form = $("#add_star_form");


function updateErrorMessage(resultDataJson){
    let errorDiv = document.getElementById("add_star_error_message");
    errorDiv.innerText = "";
    errorDiv.innerText = resultDataJson["message"];
}

function createLinkButton(value, link){
    let button = document.createElement("input");
    button.type = "button";
    button.value = value;

    // create another function in order to put arguments in "func"
    button.onclick = function(){return window.location=link;};
    return button;
}

// Populate the header above the table (mostly buttons)
function populateHeader(resultDataJson){
    let divElement = document.getElementById("dashboard_header");

    // clear anything that might already be there
    divElement.innerHTML = "";

    divElement.appendChild(createLinkButton("Back to Movies", "index.html"));

}




function handleAddStarResult(ResultDataString){
    let resultDataJson = JSON.parse(ResultDataString);

    console.log("handle add star response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    updateErrorMessage(resultDataJson);
}


function submitAddStarForm(formSubmitEvent) {
    console.log("submit star form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    let starData = add_star_form.serialize();
    starData += "&form=add_star";

    jQuery.ajax(
        "api/dashboard", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: starData,
            success: handleAddStarResult
        }
    );
}

// Bind the submit action of the form to a handler function
populateHeader();
add_star_form.submit(submitAddStarForm);