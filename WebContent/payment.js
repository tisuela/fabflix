let payment_form = $("#payment_form");


function updateErrorMessage(resultDataJson){
    let errorDiv = document.getElementById("payment_error_message");
    errorDiv.innerHTML = "";
    errorDiv.innerText = resultDataJson["message"];
}


function handlePaymentResult(ResultDataString){
    let resultDataJson = JSON.parse(ResultDataString);

    console.log("handle Payment response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    updateErrorMessage(resultDataJson);
}


function submitLoginForm(formSubmitEvent) {
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
payment_form.submit(submitLoginForm);