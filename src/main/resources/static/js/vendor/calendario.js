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
    		document.getElementsByClassName("day-click-by-user")[0].innerHTML = value_string;
    		console.log("!!!"+ document.getElementsByClassName("day-click-by-user")[0]);
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