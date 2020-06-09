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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import es.ucm.fdi.iw.model.Appointment;
import es.ucm.fdi.iw.model.GroupAppointment;
import es.ucm.fdi.iw.model.IndividualAppointment;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.User.Role;
import es.ucm.fdi.iw.transfer.UserTransferData;

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
	
	@GetMapping(value = "/patient/{id}", produces = { MediaType.APPLICATION_JSON_VALUE})
	@Transactional
	@ResponseBody
	public UserTransferData getPatient(@PathVariable("id") long id)
	{
		User patient = entityManager.find(User.class, id);
		return new UserTransferData(patient);
	}
	
	@PostMapping (value = "/modify/{id}", produces = { MediaType.APPLICATION_JSON_VALUE })
	@Transactional
	@ResponseBody
	public UserTransferData modifyUser(@ModelAttribute User user, @RequestParam(required=false) String disorder,
			@RequestParam(required=false) String treatment,@PathVariable("id") long id)
	{
		User target = entityManager.find(User.class, id);
		target.setDisorder(disorder);
		target.setTreatment(treatment);
		entityManager.merge(target);
		return new UserTransferData(target); 
	}


	// Requester es el usuario que solicita la accion.
	// Edited son los datos que obtenemos en la interfaz y por lo tanto, lo que
	// queremos cambiar
	// Target es el resultado final que obtenemos despues de completar la accion y
	// lo que guardamos en la BBDD

	@RequestMapping("/horario")
	public String horarioPsicologo(HttpSession session, Model model, @RequestParam(required = false) Integer weeks) {
		User requester = (User) session.getAttribute("u"); // TODO podría usar directamente el requester?
		User stored = entityManager.find(User.class, requester.getId());
		if (weeks == null)
			weeks = 0;
		model.addAttribute("u", stored);
		model.addAttribute("groupAppointments", stored.getAppointmentsOfTheWeek(weeks.intValue()));
		model.addAttribute("days", stored.getDaysOfTheWeek(weeks.intValue()));
		model.addAttribute("week", weeks);
		return "horarioPsicologo";
	}

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
		return "redirect:/psicologo/horario";
	}


	@RequestMapping("/deleteAppointment")
	@Transactional
	public String deleteAppointment(Model model, HttpServletResponse response, HttpSession session,
			@RequestParam long id) throws IOException {
		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());
		GroupAppointment ga = entityManager.find(GroupAppointment.class, id);
		if(ga != null) {
			for (GroupAppointment it : stored.getGroupAppointments()) {
				if (it.equals(ga)) {
					stored.removeGroupAppointment(ga);
					entityManager.remove(ga);
					break;
				}
			}
		}
		else {
			IndividualAppointment ia = entityManager.find(IndividualAppointment.class, id);
			for (Appointment it : stored.getAppointments()) {
				if (it.equals(ia)) {
					stored.removeAppointment(ia);
					entityManager.remove(ia);
					break;
				}
			}
		}

		return "redirect:/psicologo/horario";
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

		
		return "redirect:/psicologo/horario"; // devolvemos el model (los datos modificados) y la session para saber
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
						response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No eres administrador, y éste no es tu perfil"); //TODO devuelve error
						return "redirect:/psicologo/horario";
					}
				}
				ga.removeAllPatients();
				ga.setPatient(ul);
				for (User u: ul) { u.addAppointment(ga); }
				break;
			}
		}
		return "redirect:/psicologo/horario";
	}
	
}
