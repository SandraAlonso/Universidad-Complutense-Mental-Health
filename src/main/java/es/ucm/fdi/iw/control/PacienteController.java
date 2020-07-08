package es.ucm.fdi.iw.control;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import es.ucm.fdi.iw.model.Appointment;
import es.ucm.fdi.iw.model.EmotionalState;
import es.ucm.fdi.iw.model.IndividualAppointment;
import es.ucm.fdi.iw.model.Problema;
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
	public String citasPsicologo(HttpSession session) {
		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());
		session.setAttribute("u", stored);
		return "estadisticas";
	}

	@PostMapping("/saveEmotionalState")
	@Transactional
	public String saveEmotionalState(Model model, HttpServletResponse response,
			@ModelAttribute @Valid EmotionalState emotionalEstate, BindingResult result, HttpSession session) {

		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());

		int fecha = emotionalEstate.getDate().compareTo(LocalDate.now());// solo se pueden añadir las anteriores o
																			// iguales a hoy
		if (fecha <= 0) {
			TypedQuery<EmotionalState> query = entityManager.createNamedQuery("EmotionalState.statesOfTheDay",
					EmotionalState.class);
			List<EmotionalState> lm = query.setParameter("dat", emotionalEstate.getDate()).setParameter("id", stored)
					.getResultList();
			if (lm.size() == 1) {
				EmotionalState emo = entityManager.find(EmotionalState.class, lm.get(0).getID());
				emo.setDescription(emotionalEstate.getDescription());
				emo.setEmotionalState(emotionalEstate.getEmotionalState());
				//emo.setDate(emotionalEstate.getDate());
				//emo.setPatient(emotionalEstate.getPatient());
				//entityManager.persist(emo);
				//entityManager.flush();
			} else {
				emotionalEstate.setPatient(stored);
				entityManager.persist(emotionalEstate);
				entityManager.flush();
			}
				log.info("Usuario {} ha añadido un nuevo estado emocional y es {}", stored.getFirstName(),
						emotionalEstate.getEmotionalState());
				return "redirect:/paciente/estadisticas";
			
		} else {
			Problema p = new Problema("La fecha en que el usuario " + stored.getUsername()
					+ " ha intentado introducir un estado emocional es posterior a hoy");
			model.addAttribute("problema", p);
			log.info(
					"La fecha en que el usuario {} ha intentado introducir un estado emocional es posterior a hoy {} y por lo tanto imposible",
					stored.getUsername(), LocalDate.now());
			return citasPsicologo(session);
		}

	}

	@GetMapping(path = "/getEmotionalState", produces = "application/json")
	@ResponseBody
	public List<EmotionalState> getEmotionalState(HttpSession session) {
		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());
		return stored.getEmotionalState();
	}

	@RequestMapping("/horario")
	public String horarioPsicologo(HttpSession session, Model model, @RequestParam(required = false) Integer weeks) {
		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());
		session.setAttribute("u",stored);
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
				appointment.setStart_hour(appointment.getStart_hour().plusMinutes(1));
				TypedQuery<Appointment> query = entityManager.createNamedQuery("Appointment.allAppointmentsOfSameDate",
						Appointment.class);
				List<Appointment> lm = query.setParameter("username", psychologist.getId())
						.setParameter("date", appointment.getDate()).setParameter("sth", appointment.getStart_hour())
						.setParameter("fnh", appointment.getFinish_hour()).getResultList();
				if (lm.size() == 0) {
					appointment.setCreator(stored);
					appointment.setStart_hour(appointment.getStart_hour().minusMinutes(1));
					appointment.setPsychologist(psychologist);
					entityManager.persist(appointment);
					entityManager.flush();
					log.info("El usuario {} ha creado una cita individual con el psicologo {} el dia {} a las {}.",
							stored.getFirstName(), stored.getPsychologist(), appointment.getDate(),
							appointment.getStart_hour());
					return "redirect:/paciente/horario";
				} else {
					Problema p = new Problema(
							"El usuario " + stored.getUsername() + " no puede añadir cita porque el psicologo "
									+ psychologist.getFirstName() + " ya tiene una cita a esa hora.");
					model.addAttribute("problema", p);
					log.info("El usuario {} no ha podido crear una cita individual porque ya hay una cita a esa hora.",
							stored.getFirstName());
					return horarioPsicologo(session, model, null);

				}
			} else {
				Problema p = new Problema("El usuario " + stored.getUsername()
						+ " no ha podido crear una cita individual porque la hora seleccionada es posterior a la actual");
				model.addAttribute("problema", p);
				log.info(
						"El usuario {} no ha podido crear una cita individual porque la hora seleccionada es posterior a la actual ({}).",
						stored.getUsername(), LocalDate.now());
				return horarioPsicologo(session, model, null);
			}
		} else {
			Problema p = new Problema("El usuario " + stored.getUsername()
					+ " no ha podido crear una cita individual porque la hora seleccionada es posterior a la actual");
			model.addAttribute("problema", p);
			log.info("El usuario {} no puede solicitar una cita por que no tiene un psicologo asociado.",
					stored.getUsername());
			return horarioPsicologo(session, model, null);
		}
	}

	@RequestMapping("/deleteAppointment")
	@Transactional
	public String deleteGroupAppointment(Model model, HttpServletResponse response, HttpSession session,
			@RequestParam long id) throws IOException {
		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());
		IndividualAppointment ga = entityManager.find(IndividualAppointment.class, id);
		if (ga != null && ga.getCreator().getId() == stored.getId()) {
			log.info("El usuario {} ha eliminado una cita individual con el psicologo {} el dia {} a las {}.",
					stored.getFirstName(), stored.getPsychologist(), ga.getDate(), ga.getStart_hour());
			entityManager.remove(ga);
			return "redirect:/paciente/horario";
		} else {
			Problema p = new Problema(
					"El usuario " + stored.getUsername() + " no puede eliminar una cita inexistente o que no es suya.");
			model.addAttribute("problema", p);
			log.info("El usuario {} no puede eliminar una cita inexistente.", stored.getUsername());
			return horarioPsicologo(session, model, null);
		}
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
			int fecha = appointment.getDate().compareTo(LocalDate.now());
			int hora = appointment.getFinish_hour().compareTo(appointment.getStart_hour());
			LocalTime ahora = LocalTime.now();
			int horaActual = appointment.getStart_hour().compareTo(ahora);

			if (fecha == 0 && horaActual > 0 && hora > 0 || fecha > 0 && hora > 0) {
				appointment.setStart_hour(appointment.getStart_hour().plusMinutes(1));

				TypedQuery<Appointment> query = entityManager.createNamedQuery("Appointment.allAppointmentsOfSameDate",
						Appointment.class);
				List<Appointment> lm = query.setParameter("username", stored.getPsychologist().getId())
						.setParameter("date", appointment.getDate()).setParameter("sth", appointment.getStart_hour())
						.setParameter("fnh", appointment.getFinish_hour()).getResultList();

				if (lm.size() == 1 && lm.get(0).getID() == appointment.getID()
						&& appointment.getCreator().getId() == stored.getId()) {
					a.setStart_hour(appointment.getStart_hour().minusMinutes(1));
					a.setDate(appointment.getDate());
					a.setFinish_hour(appointment.getFinish_hour());
					log.info("El usuario {} ha modificado una cita individual, ahora es a las {} del dia {}",
							stored.getFirstName(), appointment.getStart_hour(), appointment.getDate());
					return "redirect:/paciente/horario";
				} else {
					Problema p = new Problema("El usuario " + stored.getUsername() + " no puede modificar esta cita.");
					model.addAttribute("problema", p);
					log.info("El usuario {} no puede modificar una cita inexistente.", stored.getUsername());
					return horarioPsicologo(session, model, null);
				}
			} else {
				Problema p = new Problema(
						"El usuario " + stored.getUsername() + " no puede modificar una cita a un día anterior.");
				model.addAttribute("problema", p);
				log.info("El usuario {} no puede modificar una cita a un día anterior.", stored.getUsername());
				return horarioPsicologo(session, model, null);
			}

		} else {
			Problema p = new Problema(
					"El usuario " + stored.getUsername() + " no puede modificar una cita inexistente.");
			model.addAttribute("problema", p);
			log.info("El usuario {} no puede modificar una cita inexistente.", stored.getUsername());
			return horarioPsicologo(session, model, null);
		}
	}

	@GetMapping(path = "/getAppointments/{id}", produces = "application/json")
	@ResponseBody
	public Appointment getAppointments(HttpSession session, @PathVariable long id) {

		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());
		Appointment ga = entityManager.find(Appointment.class, id);
		boolean esSuya = false;
		for (int i = 0; i < stored.getCreatorAppointments().size(); i++) {
			if (stored.getCreatorAppointments().get(i).getID() == id) {
				esSuya = true;
				break;
			}

		}
		if (!esSuya) {
			for (int i = 0; i < stored.getAppointments().size(); i++) {
				if (stored.getGroupAppointmentsPatient().get(i).getID() == id) {
					esSuya = true;
					break;
				}
			}
		}
		if (esSuya)
			return ga;
		else
			return null;
	}
}
