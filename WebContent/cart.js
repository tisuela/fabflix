

function createButton(value, func){
    let button = document.createElement("input");
    button.type = "button";
    button.value = value;
    button.onclick = func;
    return button
}


// generates row for cart table
function generateRow(movieJson){
    let addQuantity = document.createElement("BUTTON")
    let rowHTML = "";
    rowHTML += "<tr>";
    rowHTML +=
        "<th>" +
        // Add a link to single-movie.html with id passed with GET url parameter
        '<a href="single-movie.html?id=' + movieJson['id'] + '">'
        + movieJson["title"] +     // display star_name for the link text
        '</a>' +
        "</th>";
    rowHTML += "<th>" + movieJson["price"] + "</th>";
    rowHTML += "<th>" + movieJson["quantity"] + "</th>";
    rowHTML += "<th>" + "button lol" + "</th>";
    rowHTML += "</tr>";

    return rowHTML;
}


// Populate the table of cart.html from the data from CartServlet
function populateTable(resultDataJson){

    console.log("handle login response");
    console.log(resultDataJson);
    console.log(resultDataJson["errorMessage"]);

    let cartTable = jQuery("#cart_table_body");
    let movieJson = resultDataJson["movies"];

    // put in the rows
    for(let i = 0; i < movieJson.length; ++i){
        cartTable.append(generateRow(movieJson[i]))
    }

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