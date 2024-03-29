/**
 * WebSocket API, which only works once initialized
 */
const ws = {		

	/**
	 * Number of retries if connection fails
	 */
	retries: 3,
		
	/**
	 * Default action when message is received. 
	 */
	receive: (text) => {
			var count = parseInt($(".counter").first().text());
			$(".counter").html("");
			$(".counter").html(count + 1);
			if(typeof text.from === 'undefined' || sender === text.from || typeof sender === 'undefined' || sender === text.to) {
			var today = new Date();
			if(typeof text.from != 'undefined'){
				$('#mensajes')
				.prepend( 
						'<div class="col s10 offset-s1 card horizontal"><div class="card-stacked"><div class="card-content"><p><b>'
								+ text.from
								+ '</b></p><p>'
								+ today.toLocaleString("es-ES").split(" ").join("@")
								+ '</p><br><p>'
								+ text.text
								+ '</p></div></div></div>');
			}
			else{
				$('#notificaciones')
				.prepend(
						'<div class="col s10 offset-s1 card horizontal"><div class="card-stacked"><div class="card-content"><p><b>'
								+ 'Notificación'
								+ '</b></p><p>'
								+ today.toLocaleString("es-ES").split(" ").join("@")
								+ '</p><br><p>'
								+ text.text
								+ '</p></div></div></div>');
			}
			}
	},
	
	headers: {'X-CSRF-TOKEN' : config.csrf.value},
	
	/**
	 * Attempts to establish communication with the specified
	 * web-socket endpoint. If successfull, will call 
	 */
	initialize: (endpoint, subs = []) => {
		try {
			ws.stompClient = Stomp.client(endpoint);
			ws.stompClient.reconnect_delay = 2000;
			// only works on modified stomp.js, not on original from mantainer's site
			ws.stompClient.reconnect_callback = () => ws.retries -- > 0;
			ws.stompClient.connect(ws.headers, () => {
		        ws.connected = true;
		        console.log('Connected to ', endpoint, ' - subscribing...');		        
		        while (subs.length != 0) {
		        	ws.subscribe(subs.pop())
		        }
		    });			
			console.log("Connected to WS '" + endpoint + "'")
		} catch (e) {
			console.log("Error, connection to WS '" + endpoint + "' FAILED: ", e);
		}
	},
	
	subscribe: (sub) => {
        try {
	        ws.stompClient.subscribe(sub, 
	        		(m) => ws.receive(JSON.parse(m.body))); 	// falla si no recibe JSON!
        	console.log("Hopefully subscribed to " + sub);
        } catch (e) {
        	console.log("Error, could not subscribe to " + sub);
        }
	}
} 

/**
 * Sends an ajax request using fetch
 */
//envía json, espera json de vuelta; lanza error si status != 200
function go(url, method, data = {}) {
  let params = {
    method: method, // POST, GET, POST, PUT, DELETE, etc.
    headers: {
      "Content-Type": "application/json; charset=utf-8",
    },
    body: JSON.stringify(data)
  };
  if (method === "GET") {
	  delete params.body;
  } else {
      params.headers["X-CSRF-TOKEN"] = config.csrf.value; 
  }  
  console.log("sending", url, params)
  return fetch(url, params)
  	.then(response => {
	    if (response.ok) {
	        return response.json(); // esto lo recibes con then(d => ...)
	    } else {
	    	throw response.text();  // esto lo recibes con catch(d => ...)
	    }
  	})
}

/**
 * Actions to perform once the page is fully loaded
 */
document.addEventListener("DOMContentLoaded", () => {
	console.log(config);
	if (config.socketUrl) {
		let subs = [];
		if(config.admin) {
			subs.push("/topic/admin");
			subs.push("/topic/peticiones");
		}
		else {
			var array_topics = config.topics.split(",");
			if(array_topics[0] != "") {
				for(var i = 0; i < array_topics.length; ++i) {
					console.log("hola");
					subs.push("/topic/" + array_topics[i]);
				}
			}	
		}
		subs.push("/user/queue/updates");
		
		ws.initialize(config.socketUrl, subs);
	}
	
	// add your after-page-loaded JS code here; or even better, call 
	// 	 document.addEventListener("DOMContentLoaded", () => { /* your-code-here */ });
	//   (assuming you do not care about order-of-execution, all such handlers will be called correctly)
});
