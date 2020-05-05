

function createButton(value, func, movieId){

    let button = document.createElement("input");
    button.type = "button";
    button.value = value;

    // create another function in order to put argument "movieId" in "func"
    button.onclick = function(){return func(movieId);};
    return button;
}


// generates row for cart table
function generateRow(movieJson){
    // create row
    let row = document.createElement("tr");

    // Add cells
    let titleCell = row.insertCell(-1);
    let priceCell = row.insertCell(-1);
    let quantityCell = row.insertCell(-1);
    let buttonsCell = row.insertCell(-1)

    // populate cells
    titleCell.innerHTML =  '<a href="single-movie.html?id=' + movieJson['id'] + '">'
        + movieJson["title"] + '</a>';
    priceCell.innerHTML = "$" +  movieJson["price"];
    quantityCell.innerHTML = movieJson["quantity"];

    buttonsCell.appendChild(createButton("+", addToCart, movieJson["id"]));
    buttonsCell.appendChild(createButton("-", decreaseFromCart, movieJson["id"]));
    buttonsCell.appendChild(createButton("remove", removeFromCart, movieJson["id"]));

    return row;
}


function addToCart(movieId){
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET",// Setting request method
        url: "api/cart?action=add_to_cart&id=" + movieId,
        success: (resultData) => handleGetCart(resultData) // Setting callback function to handle data returned successfully by the CartServlet
    });
}


// decrease movie's quantity
function decreaseFromCart(movieId){
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET",// Setting request method
        url: "api/cart?action=decrease_from_cart&id=" + movieId,
        success: (resultData) => handleGetCart(resultData) // Setting callback function to handle data returned successfully by the CartServlet
    });
}


function removeFromCart(movieId){
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET",// Setting request method
        url: "api/cart?action=remove_from_cart&id=" + movieId,
        success: (resultData) => handleGetCart(resultData) // Setting callback function to handle data returned successfully by the CartServlet
    });
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
    let divElement = document.getElementById("cart_header");

    // clear anything that might already be there
    divElement.innerHTML = "";

    let totalPrice = document.createElement("p");
    totalPrice.innerText = "Total Price = $" +  resultDataJson["totalPrice"];

    // Create buttons
    divElement.append(createLinkButton("Back to Movies", "index.html"));
    divElement.append(createLinkButton("Proceed to Payment", "payment.html?total_price=" + resultDataJson["totalPrice"]));
    divElement.appendChild(totalPrice);
}


// Populate the table of cart.html from the data from CartServlet
function populateTable(resultDataJson){

    let cartTable = jQuery("#cart_table_body");
    let movieJson = resultDataJson["movies"];
    cartTable.empty();
    // put in the rows
    for(let i = 0; i < movieJson.length; ++i){
        cartTable.append(generateRow(movieJson[i]));
    }

    $("#cart_error_message").text(resultDataJson["errorMessage"])
}


function handleGetCart(resultDataJson){
    console.log("handle cart response");
    console.log(resultDataJson);
    console.log(resultDataJson["errorMessage"]);

    populateHeader(resultDataJson);
    populateTable(resultDataJson);
}


// get cart by sending GET request to CartServlet
function getCart(){
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET",// Setting request method
        url: "api/cart",
        success: (resultData) => handleGetCart(resultData) // Setting callback function to handle data returned successfully by the CartServlet
    });
}


getCart();