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



import es.ucm.fdi.iw.model.EmotionalState;
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
	public String saveEmotionalState(Model model, HttpServletResponse response, @ModelAttribute @Valid EmotionalState emotionalState,
			BindingResult result, HttpSession session) {

		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());

		emotionalState.setPatient(stored);
		stored.addEmotionalState(emotionalState);

		entityManager.persist(emotionalState);
		entityManager.flush();

		return "redirect:/paciente/estadisticas";

	}

	@RequestMapping(method = RequestMethod.GET, value = "/getMonthEmotionalState", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<EmotionalState> obtenerOferta(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime date, BindingResult result, HttpSession session) {
		
		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());

		//TODO cambiar para que sean las de solo un mes, esto solo es una prueba
		
		
		return stored.getEmotionalState();
	}
	
	
	@PostMapping("/saveAppointment")
	@Transactional
	public String saveAppointment(Model model, HttpServletResponse response,
			@ModelAttribute @Valid IndividualAppointment appointment, BindingResult result, HttpSession session)
			throws IOException {
		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());

		int fecha = appointment.getDate().compareTo(LocalDate.now());
		int hora = appointment.getFinish_hour().compareTo(appointment.getStart_hour());
		LocalTime ahora = LocalTime.now();
		int horaActual = appointment.getStart_hour().compareTo(ahora);

		if (fecha == 0 && horaActual > 0 && hora > 0 || fecha > 0 && hora > 0) {
			appointment.setPatient(stored);
			stored.addAppointment(appointment);
			entityManager.persist(appointment);
			entityManager.flush();
		}
		return "redirect:/user/horario";

		// devolvemos el model (los datos modificados) y la session para saber
		// quien es el usuario en todo momento

		/*
		 * else { return "redirect:/errorFormulario"; }
		 */

	}

}
