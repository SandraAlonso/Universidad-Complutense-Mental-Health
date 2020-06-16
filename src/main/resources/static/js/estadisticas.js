function very_dissatisfied() {
			document.getElementById("emotionalState").value = 1;
			document.getElementById("emotion").innerHTML = 'Hoy te sientes: muy triste';
		}

		function dissatisfied() {
			document.getElementById("emotionalState").value = 2;
			document.getElementById("emotion").innerHTML = 'Hoy te sientes: triste';
		}

		function satisfied() {
			document.getElementById("emotionalState").value = 3;
			document.getElementById("emotion").innerHTML = 'Hoy te sientes: bien';
		}

		function satisfied_alt() {
			document.getElementById("emotionalState").value = 4;
			document.getElementById("emotion").innerHTML = 'Hoy te sientes: muy bien';
		}

		function very_satisfied() {
			document.getElementById("emotionalState").value = 5;
			document.getElementById("emotion").innerHTML = 'Hoy te sientes: genial';
		}
		