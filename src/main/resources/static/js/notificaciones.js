function formatDate(d) {
			// 2020-03-23T10:48:11.074 => 23/3/2020@10:48:18
			return new Date(d).toLocaleString("es-ES").split(" ").join("@")
		}

		$.ajax({
					url : config.rootUrl + "notificaciones/received",
					type : 'GET',
					dataType : 'json',
					success : function(json) {
						for (var i = 0; i < json.length; ++i) {
							$('#mensajes')
									.prepend(
											'<div class="col s10 offset-s1 card horizontal"><div class="card-stacked"><div class="card-content"><p><b>'
													+ "Notificación"
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
		