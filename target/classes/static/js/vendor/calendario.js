//Today's date
var dt = new Date();

function renderDate() {
	//Set the date relative to the current month
    dt.setDate(1);
    var dayOne = dt.getDay();
    var today = new Date();
    //Takes a new Date object with the last day of the current month
    //Outside Spain, the week starts on Sunday
    var endDate = new Date(
        dt.getFullYear(),
        dt.getMonth() + 1,
        0
    ).getDate();
    
    //Takes the last day of the previous month
    var prevDate = new Date(
        dt.getFullYear(),
        dt.getMonth(),
        0
    ).getDate();
    
    var months = [
        "Enero",
        "Febrero",
        "Marzo",
        "Abril",
        "Mayo",
        "Junio",
        "Julio",
        "Agosto",
        "Septiembre",
        "Octubre",
        "Noviembre",
        "Diciembre"
    ]
    
    //Change month and year number inside the calendar
    document.getElementById("month").innerHTML = months[dt.getMonth()];
    document.getElementById("date_str").innerHTML = dt.getFullYear();
    
    var cells = "";
    //Help us to change color of dates before the current month
    //TODO: CONNECT THIS PART WITH OUR MODEL
    for (x = dayOne; x > 0; x--) {
        cells += "<div class='prev_date'>" + (prevDate - x + 1) + "</div>";
    }
    
    console.log(dayOne);
    
    var day_to_send = ("0" + dt.getDate()).slice(-2);
	var month_to_send = ("0" + (dt.getMonth() + 1)).slice(-2);
	var date_to_send = dt.toISOString().split("T")[0];
    
	
    $.ajax({
        url: "/paciente/getMonthAnimosity",
        data: {
        	"date" : date_to_send
        },
        type: "GET",
        success: function(result) {
        	console.log(result);
            // Do something with the response.
            // Might want to check for errors here.
        }, error: function(error) {
            // Here you can handle exceptions thrown by the server or your controller.
        }
     })
    
    
    //TODO: CONNECT THIS PART WITH OUR MODEL
    for (i = 1; i <= endDate; i++) {
        if (i == today.getDate() && dt.getMonth() == today.getMonth()){ 
        	cells += "<div class='today'>" +
        				"<a style='color: #000000; display:block' class='modal-trigger' href=#modal>" + 
        					i +
        				"</a>" +
        			 "</div>";
        }
        else{
            cells += "<div>" + 
            			"<a style='color: #000000; display:block' class='modal-trigger' href=#modal>" +
            				i +
            			"</a>" +
            		  "</div>";
        }
        
    }
    
    let inner_days = document.getElementsByClassName("days");
    
    inner_days[0].innerHTML = cells;
    
    /*
     * We create a custom click listener where we will store the day
     * marked by our user.
    */
    for(let element of inner_days[0].childNodes){
    	let key_string = "marked-day-" + element.innerText;
    	let value_string = element.innerText;
    	
    	
    	element.addEventListener("click", function(e){
        	var selected_date = new Date(dt.getFullYear(), dt.getMonth(), value_string);
    		document.getElementsByClassName("day-click-by-user")[0].innerHTML = value_string;
    		console.log("!!!"+ document.getElementsByClassName("day-click-by-user")[0]);
    		
    		var day = ("0" + selected_date.getDate()).slice(-2);
    		var month = ("0" + (selected_date.getMonth() + 1)).slice(-2);
    		
    	    var today = selected_date.getFullYear() + '-' + (month) + '-' + (day);

    	    document.getElementById("selectedDate").value = today;
    	})
    	
    	console.log(key_string+ ": "+ value_string);
    }
}

function moveDate(para) {
    if(para == "prev") {
        dt.setMonth(dt.getMonth() - 1);
    } else if(para == 'next') {
        dt.setMonth(dt.getMonth() + 1);
    }
    renderDate();
}