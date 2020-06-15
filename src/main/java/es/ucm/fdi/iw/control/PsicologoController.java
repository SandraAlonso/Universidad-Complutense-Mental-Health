package es.ucm.fdi.iw.control;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
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

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;

import es.ucm.fdi.iw.model.Appointment;
import es.ucm.fdi.iw.model.EntradasPsicologo;
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
		return (User) session.getAttribute("u");
	}

	private User refreshUser(User u) {
		return entityManager.find(User.class, u.getId());
	}

	/*
	 * @GetMapping(value = {"", "/pacientes"}) public String getUser(Model model) {
	 * User psy = refreshUser(userFromSession()); model.addAttribute("pacientes",
	 * entityManager.createNamedQuery( "User.findPatientsOf",
	 * User.class).setParameter("psychologistId", psy.getId()) .getResultList());
	 * 
	 * return "misPacientes"; }
	 * 
	 * @GetMapping(value = "/patient/{id}", produces = {
	 * MediaType.APPLICATION_JSON_VALUE})
	 * 
	 * @Transactional
	 * 
	 * @ResponseBody public UserTransferData getPatient(@PathVariable("id") long id)
	 * { User patient = entityManager.find(User.class, id); return new
	 * UserTransferData(patient); }
	 * 
	 * @PostMapping (value = "/modify/{id}", produces = {
	 * MediaType.APPLICATION_JSON_VALUE })
	 * 
	 * @Transactional
	 * 
	 * @ResponseBody public UserTransferData modifyUser(@ModelAttribute User
	 * user, @RequestParam(required=false) String disorder,
	 * 
	 * @RequestParam(required=false) String treatment,@PathVariable("id") long id) {
	 * User target = entityManager.find(User.class, id);
	 * target.setDisorder(disorder); target.setTreatment(treatment);
	 * entityManager.merge(target); return new UserTransferData(target); }
	 */

	// Requester es el usuario que solicita la accion.
	// Edited son los datos que obtenemos en la interfaz y por lo tanto, lo que
	// queremos cambiar
	// Target es el resultado final que obtenemos despues de completar la accion y
	// lo que guardamos en la BBDD

	@RequestMapping("/pacientes")
	public String getPacientes(Model model, HttpServletResponse response, HttpSession session,
			@RequestParam(required = false) Long id) {
		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());

		model.addAttribute("u", stored);

		if (id != null) {
			User patie = entityManager.find(User.class, id);
			model.addAttribute("patie", patie);
			log.info("Se están mostrando los datos del paciente {}", patie.getFirstName());
		}
		return "pacientes";
	}

	@RequestMapping("/horario")
	public String horarioPsicologo(HttpSession session, Model model, @RequestParam(required = false) Integer weeks) {
		User requester = (User) session.getAttribute("u"); // TODO podría usar directamente el requester?
		User stored = entityManager.find(User.class, requester.getId());
		System.out.println(stored.getCreatorAppointments());
		if (weeks == null)
			weeks = 0;
		model.addAttribute("u", stored);
		model.addAttribute("groupAppointments", stored.getAppointmentsOfTheWeekPsychologist(weeks.intValue()));
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
			groupAppointment.setCreator(stored);
			entityManager.persist(groupAppointment);
			entityManager.flush();
			log.info("El usuario {} ha creado una cita grupal el dia {} a las {}.", stored.getFirstName(),
					groupAppointment.getDate(), groupAppointment.getStart_hour());
		} else {
			log.info(
					"El usuario {} no ha podido crear una cita grupal porque la hora seleccionada es posterior a la actual ({}).",
					stored.getFirstName(), LocalDate.now());
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
		if (ga != null) {
			log.info("El usuario {} ha eliminado una cita grupal el dia {} a las {}.", stored.getFirstName(),
					ga.getDate(), ga.getStart_hour());
			entityManager.remove(ga);
		} else {
			log.info("El usuario {} no puede eliminar una cita inexistente.", stored.getFirstName());
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

		if (ga != null) {
			ga.setName(groupAppointment.getName());
			ga.setDate(groupAppointment.getDate());
			ga.setStart_hour(groupAppointment.getStart_hour());
			ga.setFinish_hour(groupAppointment.getFinish_hour());
			ga.setDescription(groupAppointment.getDescription());
			log.info("El usuario {} ha modificado la cita grupal {}, ahora es a las {} del dia {}.",
					stored.getFirstName(), groupAppointment.getName(), groupAppointment.getStart_hour(),
					groupAppointment.getDate());
		} else {
			log.info("El usuario {} no pude modificar una cita inexistente.", stored.getFirstName());
		}

		return "redirect:/psicologo/horario"; // devolvemos el model (los datos modificados) y la session para saber
												// quien es el usuario en todo momento
	}

	@RequestMapping("/addUsersOfGroupAppointments")
	@Transactional
	public String addUsersOfGroupAppointments(HttpServletResponse response, @RequestParam String[] values,
			@RequestParam long id, HttpSession session) throws IOException {
		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());
		GroupAppointment ga = entityManager.find(GroupAppointment.class, id);

		TypedQuery<User> query = entityManager.createNamedQuery("User.byUsername", User.class);
		List<User> lu = new ArrayList<User>();
		for (int i = 0; i < values.length; ++i) {
			User u = query.setParameter("username", values[i]).getSingleResult();
			if (u != null) {

				String[] roles = u.getRoles().split(",");
				boolean is_psycho = false;
				for (int j = 0; j < roles.length; ++j) {
					System.out.println(roles[j]);
					if (roles[j].equals("PSICOLOGO")) {
						is_psycho = true;
						break;
					}
				}
				if (is_psycho)
					break;
				lu.add(u);
			} else // ya se han añadido todos los pacientes
				break;
		}

		if (lu != null) {
			ga.setPatient(lu);
			log.info("El usuario {} ha añadido a la cita grupal {} a {} usuarios.", stored.getFirstName(),
					ga.getName(), lu.size());
		}
		return "redirect:/psicologo/horario";
	}

	@PostMapping("/addTreatment")
	@Transactional
	public String addTreatment(Model model, HttpServletResponse response, @ModelAttribute @Valid User user,
			BindingResult result, HttpSession session) throws IOException {
		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());
		User u = entityManager.find(User.class, user.getId());

		if (u != null) {
			if (user.getTreatment() != "") {
				u.setTreatment(user.getTreatment());
				log.info(
						"El usuario {} ha recetado {} al paciente {}.",
						stored.getFirstName(), user.getTreatment(), user.getFirstName());
			}
			if (user.getDisorder() != "") {
				u.setDisorder(user.getDisorder());
				log.info(
						"El usuario {} ha diagnosticado {} al paciente {}.",
						stored.getFirstName(), user.getDisorder(), user.getFirstName());
			}
		} else {
			log.info(
					"El usuario {} no puede añadir un tratamiento o hacer un diagnostico de un paciente inexistente, selecciona uno de la izquierda.",
					stored.getFirstName());

		}

		return "redirect:/psicologo/pacientes"; // devolvemos el model (los datos modificados) y la session para saber
												// quien es el usuario en todo momento
	}

	@PostMapping("/addPsycologistEntry")
	@Transactional
	public String addPsycologistEntry(Model model, HttpServletResponse response,
			@ModelAttribute @Valid EntradasPsicologo description, BindingResult result, HttpSession session,
			@RequestParam(required = false) Long id) throws IOException {
		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());
		if (id != null) {
			User pat = entityManager.find(User.class, id);
			LocalDate date = LocalDate.now();
			description.setPatient(pat);
			description.setDate(date);
			pat.addPsychologistEntry(description);

			entityManager.persist(description);
			entityManager.flush();
			log.info("El usuario {} ha añadido una nueva entrada al paciente {}, ya tiene {} entradas.",
					stored.getFirstName(), pat.getFirstName(), pat.getDescription().size());
		} else {
			log.info(
					"El usuario {} no puede escribir una entrada a un paciente inexistente, selecciona uno de la izquierda.",
					stored.getFirstName());
		}
		return "redirect:/psicologo/pacientes";
	}
}
