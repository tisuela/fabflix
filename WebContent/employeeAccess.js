let employee_access_form = $("#employee_access_form");

function handleLoginResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle login response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If login succeeds, it will redirect the user to main-page.html
    if (resultDataJson["status"] === "success") {
        window.location.replace("_dashboard.html");
    }else {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#employee_login_error_message").text(resultDataJson["message"]);
    }
}

function submitLoginForm(formSubmitEvent) {
    console.log("submit login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();
    let loginData  = employee_access_form.serialize();

    $.ajax(
        "api/employeeAccess", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: loginData,
            success: handleLoginResult
        }
    );
}

employee_access_form.submit(submitLoginForm);