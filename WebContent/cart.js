

function createButton(value, func, movieId){
    let button = document.createElement("input");
    button.type = "button";
    button.value = value;
    button.onclick = function(){return func(movieId);};
    return button;
}


// generates row for cart table
function generateRow(movieJson){
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



    return rowHTML;
}


function addToCart(movieId){
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET",// Setting request method
        url: "api/cart?action=addtocart&id=" + movieId,
        success: (resultData) => populateTable(resultData) // Setting callback function to handle data returned successfully by the CartServlet
    });
}

// Populate the table of cart.html from the data from CartServlet
function populateTable(resultDataJson){

    console.log("handle login response");
    console.log(resultDataJson);
    console.log(resultDataJson["errorMessage"]);

    let cartTable = jQuery("#cart_table_body");
    let movieJson = resultDataJson["movies"];
    cartTable.empty();
    // put in the rows
    for(let i = 0; i < movieJson.length; ++i){
        cartTable.append(generateRow(movieJson[i]))
        cartTable.append("<th>")
        cartTable.append(createButton("add",addToCart,movieJson[i]["id"]))
        cartTable.append("</th></tr>")
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