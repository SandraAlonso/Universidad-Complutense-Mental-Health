document.addEventListener("DOMContentLoaded", function(e){	
$( ".pacientBTN" ).click(function(e) {
	  let id = $(this).attr("user-id");
	  console.log(id);
	  $("#sent-confirmation").css("visibility", "hidden");
	  $.ajax({
		  headers: {'X-CSRF-TOKEN' : config.csrf.value},
		  method: "GET",
		  url: `${config.rootUrl}psicologo/patient/${id}`,
		  data: {}
	  })
	  .done(function( msg ) {
		  console.log(msg.firstName);
		  $("#pacientName").text(msg.firstName + " " + msg.lastName);
		  $("#pacientDisorder").val(msg.disorder);
		  $("#pacientTreatment").val(msg.treatment);
		  $(".savePacient").attr("data-clicked-id", msg.id); 
		  //$(".sendmsg").attr("data-clicked-mail", mail);
		  //$("#get-user-history").css("visibility", "visible");
		  //$("#get-user-history").attr("data-clicked-mail", mail);
		  //TODO: fix user domain problem
		  //$("#add-new-entry").attr("data-clicked-mail", "");
		  console.log(msg);
	      });
	});
	
	$( ".savePacient" ).click(function(e) {
	  let id = $(this).attr("data-clicked-id");
	  let disorderB = $("#pacientDisorder").val();
	  let treatmentB = $("#pacientTreatment").val();
	  
	  console.log("click");
	  
	  $.ajax({
		  headers: {'X-CSRF-TOKEN' : config.csrf.value},
		  method: "POST",
		  url: `${config.rootUrl}psicologo/modify/${id}`,
		  data: {disorder : disorderB, treatment : treatmentB}
	  })
	  .done(function( msg ) {
		  alert("Cambios guardados");
		  });
	  });
});