


// Populate the table of cart.html from the data from CartServlet
function populateTable(resultDataJson){

    console.log("handle login response");
    console.log(resultDataJson);
    console.log(resultDataJson["errorMessage"]);

    $("#cart_error_message").text(resultDataJson["errorMessage"])
}


// get cart by sending GET request to CartServlet
function getCart(){
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET",// Setting request method
        url: "api/cart",
        success: (resultData) => populateTable(resultData) // Setting callback function to handle data returned successfully by the CartServlet
    });
}


getCart();