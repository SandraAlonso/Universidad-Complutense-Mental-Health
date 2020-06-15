package es.ucm.fdi.iw.control;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import es.ucm.fdi.iw.model.Appointment;
import es.ucm.fdi.iw.model.EmotionalState;
import es.ucm.fdi.iw.model.GroupAppointment;
import es.ucm.fdi.iw.model.IndividualAppointment;
import es.ucm.fdi.iw.model.User;

/**
 * User-administration controller
 * 
 * @author mfreire
 */
@Controller()
@RequestMapping("paciente")
public class PacienteController {

	private static final Logger log = LogManager.getLogger(PacienteController.class);

	@Autowired
	private EntityManager entityManager;

	@GetMapping("/estadisticas")
	public String citasPsicologo() {
		return "estadisticas";
	}

	@PostMapping("/saveEmotionalState")
	@Transactional
	public String saveAnimosity(Model model, HttpServletResponse response,
			@ModelAttribute @Valid EmotionalState emotionalEstate, BindingResult result, HttpSession session) {

		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());

		int fecha = emotionalEstate.getDate().compareTo(LocalDate.now());// solo se pueden añadir las anteriores o
																			// iguales a hoy
		if (fecha <= 0) {
			emotionalEstate.setPatient(stored);
			stored.addEmotionalState(emotionalEstate);

			entityManager.persist(emotionalEstate);
			entityManager.flush();
			log.info("Usuario {} ha añadido un nuevo estado emocional y es {}", stored.getFirstName(),
					emotionalEstate.getEmotionalState());
		} else {
			log.info(
					"La fecha en que el usuario {} ha intentado introducir un estado emocional es posterior a hoy {} y por lo tanto imposible",
					stored.getFirstName(), LocalDate.now());
		}
		return "redirect:/paciente/estadisticas";

	}

	@GetMapping(path = "/getEmotionalState", produces = "application/json")
	@ResponseBody
	public List<EmotionalState> getEmotionalState(HttpSession session) {

		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());

		// TODO cambiar para que sean las de solo un mes, esto solo es una prueba
		return stored.getEmotionalState();
	}

	@RequestMapping("/horario")
	public String horarioPsicologo(HttpSession session, Model model, @RequestParam(required = false) Integer weeks) {
		User requester = (User) session.getAttribute("u"); // TODO podría usar directamente el requester?
		User stored = entityManager.find(User.class, requester.getId());
		if (weeks == null)
			weeks = 0;
		model.addAttribute("u", stored);
		model.addAttribute("groupAppointments", stored.getAppointmentsOfTheWeekPatient(weeks.intValue()));
		model.addAttribute("days", stored.getDaysOfTheWeek(weeks.intValue()));
		model.addAttribute("week", weeks);
		return "horarioPaciente";
	}

	@PostMapping("/saveAppointment")
	@Transactional
	public String saveAppointment(Model model, HttpServletResponse response,
			@ModelAttribute @Valid IndividualAppointment appointment, BindingResult result, HttpSession session)
			throws IOException {
		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());
		User psychologist = stored.getPsychologist();
		if (psychologist != null) {
			int fecha = appointment.getDate().compareTo(LocalDate.now());
			int hora = appointment.getFinish_hour().compareTo(appointment.getStart_hour());
			LocalTime ahora = LocalTime.now();
			int horaActual = appointment.getStart_hour().compareTo(ahora);

			if (fecha == 0 && horaActual > 0 && hora > 0 || fecha > 0 && hora > 0) {
				appointment.setCreator(stored);
				appointment.setPsychologist(psychologist);
				entityManager.persist(appointment);
				entityManager.flush();
				log.info("El usuario {} ha creado una cita individual con el psicologo {} el dia {} a las {}.",
						stored.getFirstName(), stored.getPsychologist(), appointment.getDate(),
						appointment.getStart_hour());
			} else {
				log.info(
						"El usuario {} no ha podido crear una cita individual porque la hora seleccionada es posterior a la actual ({}).",
						stored.getFirstName(), LocalDate.now());
			}
		} else {
			log.info("El usuario {} no puede solicitar una cita por que no tiene un psicologo asociado.",
					stored.getFirstName());
		}
		// TODO sino error, debe tener un psicologo
		return "redirect:/paciente/horario";
	}

	@RequestMapping("/deleteAppointment")
	@Transactional
	public String deleteGroupAppointment(Model model, HttpServletResponse response, HttpSession session,
			@RequestParam long id) throws IOException {
		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());
		IndividualAppointment ga = entityManager.find(IndividualAppointment.class, id);
		if (ga != null) {
			log.info("El usuario {} ha eliminado una cita individual con el psicologo {} el dia {} a las {}.",
					stored.getFirstName(), stored.getPsychologist(), ga.getDate(), ga.getStart_hour());
			entityManager.remove(ga);

		} else {
			log.info("El usuario {} no puede eliminar una cita inexistente.", stored.getFirstName());
		}
		return "redirect:/paciente/horario";
	}

	@RequestMapping("/modifyAppointment")
	@Transactional
	public String modifyGroupAppointment(Model model, HttpServletResponse response,
			@ModelAttribute @Valid IndividualAppointment appointment, BindingResult result, HttpSession session)
			throws IOException {
		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());
		IndividualAppointment a = entityManager.find(IndividualAppointment.class, appointment.getID());

		if (a != null) {
			a.setDate(appointment.getDate());
			a.setStart_hour(appointment.getStart_hour());
			a.setFinish_hour(appointment.getFinish_hour());
			log.info("El usuario {} ha modificado una cita individual, ahora es a las {} del dia {}", stored.getFirstName(),
					appointment.getStart_hour(), appointment.getDate());
		} else {
			log.info("El usuario {} no pude modificar una cita inexistente.", stored.getFirstName());
		}

		return "redirect:/paciente/horario"; // devolvemos el model (los datos modificados) y la session para saber
												// quien es el usuario en todo momento
	}
	
	

}
