function change(arg) {

	var groupAppointment;
	var a = arg.getAttribute("data-id");
	clicked_id = a;
	$.ajax({
		url : config.rootUrl + "paciente/getAppointments/"+a,
		type : 'GET',
		dataType : 'json',
		success : function(json) {
			groupAppointment = json;

	    },
		async: false
	 })
					if(typeof groupAppointment === 'undefined') {
						//Es grupal
					} else {
						//Es individual	
						document.getElementById("ID").value = a;
						document.getElementById("date").value = arg
								.getAttribute("data-date");
						document.getElementById("start-hour").value = arg
								.getAttribute("data-start");
						document.getElementById("end-hour").value = arg
								.getAttribute("data-end");

						document.getElementById("add-button").classList
								.add('hide');
						document.getElementById("options-button").classList
								.remove('hide');

						var eliminate = document.getElementById("delete");
						eliminate.href = "/paciente/deleteAppointment?id=" + a;

					
				
			}

			
		}

		function changeBack() {
			document.getElementById("options-button").classList.add('hide');
			document.getElementById("add-button").classList.remove('hide')
		}