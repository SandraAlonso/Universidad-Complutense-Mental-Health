//Today's date
var dt = new Date();
var emotional_states;


function formatDate(d) {
	// 2020-03-23T10:48:11.074 => 23/3/2020@10:48:18
	return new Date(d).toLocaleString("es-ES").split("/")
}
$.ajax({
	url : config.rootUrl + "paciente/getEmotionalState",
	type : 'GET',
	dataType : 'json',
	success : function(json) {
    	emotional_states = json;

    },
	async: false
 })
function renderDate() {
	// Set the date relative to the current month
    dt.setDate(1);
    var dayOne = dt.getDay();
    var today = new Date();
    // Takes a new Date object with the last day of the current month
    // Outside Spain, the week starts on Sunday
    var endDate = new Date(
        dt.getFullYear(),
        dt.getMonth() + 1,
        0
    ).getDate();
    
    // Takes the last day of the previous month
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
    
    // Change month and year number inside the calendar
    document.getElementById("month").innerHTML = months[dt.getMonth()];
    document.getElementById("date_str").innerHTML = dt.getFullYear();
    
    var cells = "";
  
    for (x = dayOne; x > 0; x--) {
        cells += "<div class='prev_date'>" + (prevDate - x + 1) + "</div>";
    }
    
  
    		j=0;
    		for (i = 1; i <= endDate; i++) {

    	        if (i == today.getDate() && dt.getMonth() == today.getMonth()){ 

    	        	cells += "<div class='today'>" +
    	        				"<a style='color: #000000; display:block' class='modal-trigger' href=#modal>" + 
    	        					i +
    	        				"</a>" +
    	        			 "</div>";
    	        }
    	        

    	        else if (emotional_states.length>j){
    	         if(i==formatDate(emotional_states[j].date)[0] && dt.getMonth()==formatDate(emotional_states[j].date)[1]-1){
    	        	if(emotional_states[j].emotionalState ==1){

    	        		cells += "<div class='muy_mal'>" +
    					"<a style='color: #000000; display:block' class='modal-trigger' href=#modal>" + 
    						i +
    					"</a>" +
    				 "</div>";
    	        		j++;
    	        	}
    	        	else if(emotional_states[j].emotionalState ==2 && dt.getMonth()==formatDate(emotional_states[j].date)[1]-1){

    	        		cells += "<div class='mal'>" +
    					"<a style='color: #000000; display:block' class='modal-trigger' href=#modal>" + 
    						i +
    					"</a>" +
    				 "</div>";
    	        		j++;
    	        	}
    	        	else if(emotional_states[j].emotionalState ==3 && dt.getMonth()==formatDate(emotional_states[j].date)[1]-1){

    	        		cells += "<div class='bien'>" +
    					"<a style='color: #000000; display:block' class='modal-trigger' href=#modal>" + 
    						i +
    					"</a>" +
    				 "</div>";
    	        		j++;
    	        	}else if(emotional_states[j].emotionalState ==4 && dt.getMonth()==formatDate(emotional_states[j].date)[1]-1){

    	        		cells += "<div class='muy_bien'>" +
    					"<a style='color: #000000; display:block' class='modal-trigger' href=#modal>" + 
    						i +
    					"</a>" +
    				 "</div>";
    	        		j++;
    	        	}
    	        	else if(emotional_states[j].emotionalState ==5 && dt.getMonth()==formatDate(emotional_states[j].date)[1]-1){

    	        		cells += "<div class='genial'>" +
    					"<a style='color: #000000; display:block' class='modal-trigger' href=#modal>" + 
    						i +
    					"</a>" +
    				 "</div>";
    	        		j++;
    	        	}  
    	         }
    	        	 else{
    	    	        	cells += "<div>" + 
    	        			"<a style='color: #000000; display:block' class='modal-trigger' href=#modal>" +
    	        				i +
    	        			"</a>" +
    	        		  "</div>";
    	    	        }
    	        	
    	        
    	        
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
	 * We create a custom click listener where we will store the day marked by
	 * our user.
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