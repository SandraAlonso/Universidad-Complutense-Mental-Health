package es.ucm.fdi.iw.control;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import es.ucm.fdi.iw.model.Animosity;
import es.ucm.fdi.iw.model.GroupAppointment;
import es.ucm.fdi.iw.LocalData;
import es.ucm.fdi.iw.model.Message;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.User.Role;

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

	@PostMapping("/saveAnimosity")
	@Transactional
	public String saveAnimosity(Model model, HttpServletResponse response, @ModelAttribute @Valid Animosity animosity,
			BindingResult result, HttpSession session) {

		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());

		animosity.setPatient(stored);
		stored.addAnimosity(animosity);

		entityManager.persist(animosity);
		entityManager.flush();

		return "redirect:/paciente/estadisticas";

	}

	@RequestMapping(method = RequestMethod.GET, value = "/getMonthAnimosity", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<Animosity> obtenerOferta(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime date, BindingResult result, HttpSession session) {
		
		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());

		//TODO cambiar para que sean las de solo un mes, esto solo es una prueba
		
		
		return stored.getAnimosity();
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
		return "redirect:/user/horario";

		// devolvemos el model (los datos modificados) y la session para saber
		// quien es el usuario en todo momento

		/*
		 * else { return "redirect:/errorFormulario"; }
		 */

	}

}
