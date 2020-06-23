var sender;
		
		function formatDate(d) {
			// 2020-03-23T10:48:11.074 => 23/3/2020@10:48:18
			return new Date(d).toLocaleString("es-ES").split(" ").join("@")
		}

		$.ajax({
					url : config.rootUrl + "message/received",
					type : 'GET',
					dataType : 'json',
					success : function(json) {
						for (var i = 0; i < json.length; ++i) {
							$('#mensajes')
									.prepend(
											'<div class="col s10 offset-s1 card horizontal"><div class="card-stacked"><div class="card-content"><p><b>'
													+ json[i].from
													+ '</b></p><p>'
													+ formatDate(json[i].sent)
													+ '</p><br><p>'
													+ json[i].text
													+ '</p></div></div></div>');
						}
					},
					error : function(xhr, status) {
						alert('Disculpe, existiÃ³ un problema');
					},
				});
		
		function getMessages(id, username) {
			
			
			sender = username;
			console.log(sender);
			$('#mensajes').empty();
			$('#form-send').removeClass('hide');
			$("#conver").html("Conversación con " + username);
			
			$('#send').attr("data-id-receiver", id);
			$('#send').attr("data-topic", '');
			
			$.ajax({
				url : config.rootUrl + "message/get/" + id,
				type : 'GET',
				dataType : 'json',
				success : function(json) {
					mensajes = json;
					for (var i = 0; i < json.length; ++i) {
						$('#mensajes')
								.prepend(
										'<div class="col s10 offset-s1 card horizontal"><div class="card-stacked"><div class="card-content"><p><b>'
												+ json[i].from
												+ '</b></p><p>'
												+ formatDate(json[i].sent)
												+ '</p><br><p>'
												+ json[i].text
												+ '</p></div></div></div>');
					}
				},
				error : function(xhr, status) {
					alert('Disculpe, existiÃ³ un problema');
				}
			});
			
			

		
		}
		
		function getMessagesTopic(name) {
			
			sender = name;
			$('#mensajes').empty();
			$('#form-send').removeClass('hide');
			$('#send').attr("data-topic", name);
			$('#send').attr("data-id-receiver", '');
			$("#conver").html("Conversación del grupo " + name);

			//TODO borrar el receiver y viceversa
			
			$.ajax({
				url : config.rootUrl + "message/get-topic/" + name,
				type : 'GET',
				dataType : 'json',
				success : function(json) {
					mensajes = json;
					for (var i = 0; i < json.length; ++i) {
						$('#mensajes')
								.prepend(
										'<div class="col s10 offset-s1 card horizontal"><div class="card-stacked"><div class="card-content"><p><b>'
												+ json[i].from
												+ '</b></p><p>'
												+ formatDate(json[i].sent)
												+ '</p><br><p>'
												+ json[i].text
												+ '</p></div></div></div>');
					}
				},
				error : function(xhr, status) {
					alert('Disculpe, existiÃ³ un problema');
				}
			});
			
			
			
		}
		
		document.addEventListener("DOMContentLoaded", () => {
			let b = document.getElementById("sendmsg");
			b.onclick = (e) => {
				console.log("hola");
				var today = new Date();
				var action;
				if($("#send").attr("data-id-receiver")) action = "/user/" + $("#send").attr("data-id-receiver") + "/msg?=" + $("#send").attr("data-id-sender");
				else action = "/user/" + $("#send").attr("data-topic") + "/group-msg?=" + $("#send").attr("data-id-sender");
				e.preventDefault();
				go(action, 'POST', {message: 
						document.getElementById("message").value})
					.then(d => console.log("happy", d))
					.catch(e => console.log("sad", e))
				if($("#send").attr("data-id-receiver")) {
				$('#mensajes')
				.prepend(
						'<div class="col s10 offset-s1 card horizontal"><div class="card-stacked"><div class="card-content"><p><b>'
								+ $("#send").attr("data-username-sender")
								+ '</b></p><p>'
								+ today.toLocaleString("es-ES").split(" ").join("@")
								+ '</p><br><p>'
								+ document.getElementById("message").value
								+ '</p></div></div></div>');
				}
			}
			
			$(document).ready(function() {
				  $(window).keydown(function(event){
				    if(event.keyCode == 13) {
				      event.preventDefault();
				      return false;
				    }
				  });
			});
		});
		