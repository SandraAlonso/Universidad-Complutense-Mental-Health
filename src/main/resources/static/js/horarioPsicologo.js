var clicked_id;

		function change(arg) {

			var a = arg.getAttribute("data-id");
			clicked_id = a;

			/* <![CDATA[ */

			    var appointments = /* [[${groupAppointments}]] */ 'default';
				for(var i = 0; i < appointments.length; ++i) {
					if(appointments[i].id == a) {
						if("name" in appointments[i]) {
							// Es grupal
							document.getElementById("modify-appointment").classList.remove('hide');
							document.getElementById("add-people").classList.remove('hide');
							document.getElementById("ID").value = a;
							document.getElementById("ID-users").value = a;
							document.getElementById("name").value = arg
									.getAttribute("data-name");
							document.getElementById("date").value = arg
									.getAttribute("data-date");
							document.getElementById("start-hour").value = arg
									.getAttribute("data-start");
							document.getElementById("end-hour").value = arg
									.getAttribute("data-end");
							document.getElementById("description").value = arg
									.getAttribute("data-content");
			
						}
						else {
							// Es individual
							document.getElementById("modify-appointment").classList.add('hide');
							document.getElementById("add-people").classList.add('hide');
						}
					}
				}

			/* ]]> */
			
			document.getElementById("add-button").classList.add('hide');
			document.getElementById("options-button").classList.remove('hide');

			var eliminate = document.getElementById("delete");
			eliminate.href = "/psicologo/deleteAppointment?id=" + a;

		}

		function changeBack() {
			document.getElementById("options-button").classList.add('hide');
			document.getElementById("add-button").classList.remove('hide')
		}

		function addUsers() {
			var instance = M.Chips.getInstance($('.chips'));
			var value = null;
			for (var i = 0; i < instance.chipsData.length; i++) {
				if (value == null)
					value = instance.chipsData[i].tag;
				else {
					value += ',' + instance.chipsData[i].tag;
				}
			}

			var id = document.getElementById("ID-users").value;

			window.location = "/psicologo/addUsersOfGroupAppointments?values="
					+ value + "&id=" + id;
			
		}
		
		function getUsers() {
			
			/* <![CDATA[ */
				

		    var appointments = /* [[${groupAppointments}]] */ 'default';
			for(var i = 0; i < appointments.length; ++i) {
				if(appointments[i].id == clicked_id) {
					var patients = appointments[i].patient;
					var instance = M.Chips.getInstance($('.chips'));
					if(patients != null) {
						patients.forEach(element => {
							var photo = '/user/' + element.id + '/photo?=' + element.id;
							instance.addChip({
							    tag: element.username,
							    image: photo, // optional
							  });
						})
					}
					
					
				}
			}
			
			/* ]]> */
		}