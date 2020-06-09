package es.ucm.fdi.iw.control;


import java.io.IOException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;



import es.ucm.fdi.iw.model.GroupAppointment;

import es.ucm.fdi.iw.model.User;

/**
 * User-administration controller
 * 
 * @author mfreire
 */
@Controller()
@RequestMapping("psicologo")
public class PsicologoController {

	private static final Logger log = LogManager.getLogger(PsicologoController.class);
	
	@Autowired
	EntityManager entityManager;
	
	@Autowired
	private SimpMessagingTemplate messagingTemplate;
	
	@Autowired // this makes httpSession always available in each method
	private HttpSession session;
	
	private User userFromSession() {
		return (User)session.getAttribute("u");
	}
	
	private User refreshUser(User u) {
		return entityManager.find(User.class, u.getId());
	}
	
	@GetMapping(value = {"", "/pacientes"})
	public String getUser(Model model) {
		User psy = refreshUser(userFromSession());
		model.addAttribute("pacientes", entityManager.createNamedQuery(
			"User.findPatientsOf", User.class).setParameter("psychologistId", psy.getId())
			.getResultList());
		
		return "misPacientes";
	}
	
	


	// Requester es el usuario que solicita la accion.
	// Edited son los datos que obtenemos en la interfaz y por lo tanto, lo que
	// queremos cambiar
	// Target es el resultado final que obtenemos despues de completar la accion y
	// lo que guardamos en la BBDD

	@PostMapping("/saveAppointment")
	@Transactional
	public String saveAppointment(Model model, HttpServletResponse response,
			@ModelAttribute @Valid GroupAppointment groupAppointment, BindingResult result, HttpSession session)
			throws IOException {
		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());

		int fecha = groupAppointment.getDate().compareTo(LocalDate.now());
		int hora = groupAppointment.getFinish_hour().compareTo(groupAppointment.getStart_hour());
		LocalTime ahora = LocalTime.now();
		int horaActual = groupAppointment.getStart_hour().compareTo(ahora);

		if (fecha == 0 && horaActual > 0 && hora > 0 || fecha > 0 && hora > 0) {
			groupAppointment.setPsychologist(stored);
			stored.addGroupAppointment(groupAppointment);
			entityManager.persist(groupAppointment);
			entityManager.flush();
		}

		// devolvemos el model (los datos modificados) y la session para saber
		// quien es el usuario en todo momento

		else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Los datos de la cita son incorrectos"); //TODO devuelve error
		}
		return "redirect:/user/horario";

	}


	@RequestMapping("/deleteAppointment")
	@Transactional
	public String deleteGroupAppointment(Model model, HttpServletResponse response, HttpSession session,
			@RequestParam long id) throws IOException {
		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());
		GroupAppointment ga = entityManager.find(GroupAppointment.class, id);

		for (GroupAppointment it : stored.getGroupAppointments()) {
			if (it.equals(ga)) {
				stored.removeGroupAppointment(ga);
				entityManager.remove(ga);
				break;
			}
		}

		return "redirect:/user/horario"; // devolvemos el model (los datos modificados) y la session para saber
												// quien es el usuario en todo momento
	}

	@RequestMapping("/modifyAppointment")
	@Transactional
	public String modifyGroupAppointment(Model model, HttpServletResponse response,
			@ModelAttribute @Valid GroupAppointment groupAppointment, BindingResult result, HttpSession session)
			throws IOException {
		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());
		GroupAppointment ga = entityManager.find(GroupAppointment.class, groupAppointment.getID());

		// int weeks = model.getAttribute("week"); //TODO podría usar directamente el
		// requester?

		for (GroupAppointment it : stored.getGroupAppointments()) {
			if (it.equals(ga)) {
				ga.setName(groupAppointment.getName());
				ga.setDate(groupAppointment.getDate());
				ga.setStart_hour(groupAppointment.getStart_hour());
				ga.setFinish_hour(groupAppointment.getFinish_hour());
				ga.setDescription(groupAppointment.getDescription());
				break;
			}
		}

		return "redirect:/user/horario"; // devolvemos el model (los datos modificados) y la session para saber
												// quien es el usuario en todo momento
	}
	
	@RequestMapping("/addUsersOfGroupAppointments")
	@Transactional
	public String addUsersOfGroupAppointments(HttpServletResponse response, @RequestParam long[] values, @RequestParam long id,  HttpSession session) throws IOException 
	{
		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());
		GroupAppointment ga = entityManager.find(GroupAppointment.class, id);

		for (GroupAppointment it : stored.getGroupAppointments()) {
			if (it.equals(ga)) {
				List<User> ul = new ArrayList<>();
				for(int i = 0; i < values.length; ++i) {
					User u = entityManager.find(User.class, values[i]);
					if(u != null) { ul.add(u); }
					else {
						response.sendError(HttpServletResponse.SC_BAD_REQUEST, "El paciente al que intentas agregar no existe"); //TODO devuelve error
						return "redirect:/user/horario";
					}
				}
				ga.removeAllPatients();
				ga.setPatient(ul);
				for (User u: ul) { u.addGroupAppointment(ga); }
				break;
			}
		}
		return "redirect:/user/horario";
	}
	
	@RequestMapping(value = "/getUsersOfGroupAppointments", method = RequestMethod.POST,  consumes=MediaType.APPLICATION_JSON_VALUE)
	@Transactional
	public List<User> getUsersOfGroupAppointments(HttpServletResponse response, @RequestParam long id,  HttpSession session) throws IOException 
	{
		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());
		GroupAppointment ga = entityManager.find(GroupAppointment.class, id);

		List<User> ul = null;
		for (GroupAppointment it : stored.getGroupAppointments()) {
			if (it.equals(ga)) {
				ul = it.getPatient();
				break;
			}
		}
		return ul;
	}
}
