let payment_form = $("#payment_form");


function updateErrorMessage(resultDataJson){
    let errorDiv = document.getElementById("payment_error_message");
    errorDiv.innerHTML = "";
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
    let divElement = document.getElementById("payment_header");

    // clear anything that might already be there
    divElement.innerHTML = "";

    let paymentParams = new URLSearchParams(window.location.search);

    let totalPrice = document.createElement("p");
    totalPrice.innerText = "Total Price = $" + paymentParams.get("total_price");

    divElement.appendChild(createLinkButton("Back to Movies", "index.html"));
    divElement.appendChild(totalPrice);

}




function handlePaymentResult(ResultDataString){
    let resultDataJson = JSON.parse(ResultDataString);

    console.log("handle Payment response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    updateErrorMessage(resultDataJson);
    if (resultDataJson["status"] == "success"){
        window.location.replace("confirmation.html?transaction_id=" + resultDataJson["transactionId"]);
    }
}


function submitPaymentForm(formSubmitEvent) {
    console.log("submit payment form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    jQuery.ajax(
        "api/payment", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: payment_form.serialize(),
            success: handlePaymentResult
        }
    );
}

// Bind the submit action of the form to a handler function
populateHeader();
payment_form.submit(submitPaymentForm);