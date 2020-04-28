
function createLinkButton(value, link){
    let button = document.createElement("input");
    button.type = "button";
    button.value = value;

    // create another function in order to put arguments in "func"
    button.onclick = function(){return window.location=link;};
    return button;
}


function generateRow(movieJson) {
    // create row
    let row = document.createElement("tr");

    // Add cells
    let saleCell = row.insertCell(-1);
    let titleCell = row.insertCell(-1);
    let priceCell = row.insertCell(-1);
    let quantityCell = row.insertCell(-1);

    // populate cells
    saleCell.innerHTML = movieJson["sailId"];
    titleCell.innerHTML =  '<a href="single-movie.html?id=' + movieJson['id'] + '">'
        + movieJson["title"] + '</a>';
    priceCell.innerHTML = "$" +  movieJson["price"];
    quantityCell.innerHTML = movieJson["quantity"];

    return row;
}


// Populate the header above the table (mostly buttons)
function populateHeader(resultDataJson){
    let divElement = document.getElementById("confirmation_header");

    // clear anything that might already be there
    divElement.innerHTML = "";

    let confirmationParams = new URLSearchParams(window.location.search);
    let transactionId = confirmationParams.get("transaction_id");

    // Create buttons
    divElement.append(createLinkButton("Back to Movies", "index.html"));
    divElement.append("Transaction ID = " + transactionId);
}


// Populate the table of cart.html from the data from CartServlet
function populateTable(resultDataJson){

    let confirmationTable = jQuery("#confirmation_table_body");
    let movieJson = resultDataJson["movies"];
    confirmationTable.empty();
    // put in the rows
    for(let i = 0; i < movieJson.length; ++i){
        confirmationTable.append(generateRow(movieJson[i]));
    }

    //$("#confirmation_error_message").text(resultDataJson["errorMessage"])
}


function handleGetConfirmation(resultDataJson){
    console.log("handle cart response");
    console.log(resultDataJson);
    console.log(resultDataJson["errorMessage"]);

    populateHeader(resultDataJson);
    populateTable(resultDataJson)
}


// get cart by sending GET request to CartServlet
function getConfirmation(){
    let paymentParams = new URLSearchParams(window.location.search);
    let transactionId = paymentParams.get("transaction_id");
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET",// Setting request method
        url: "api/confirmation?transaction_id=" + transactionId,
        success: (resultData) => handleGetConfirmation(resultData) // Setting callback function to handle data returned successfully by the CartServlet
    });
}

getConfirmation();

