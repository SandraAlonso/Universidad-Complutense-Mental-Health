
	window.onload = iniciar;//lo inicia al cargar la pagina

function iniciar() {
		document.querySelectorAll('.borrar').forEach(item => {
			  item.addEventListener('click', event => {
					var a = item.getAttribute("data-id");
					var eliminate = document.getElementById("delete");
					eliminate.href = "/admin/deleteUser?id=" + a;
			  });
		});
		
		document.querySelectorAll('.deshabilitar').forEach(item => {
			  item.addEventListener('click', event => {
					var a = item.getAttribute("data-id");
					var eliminate = document.getElementById("toggle");
					eliminate.href = "/admin/toggleuser?id=" + a;
			  });
		});
		
		document.querySelectorAll('.renovar').forEach(item => {
			  item.addEventListener('click', event => {
					var a = item.getAttribute("data-id");
					document.getElementById("ID").value = a;
			  });
		});
	}
	
