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
@RequestMapping("psicologo")
public class PsicologoController {

	private static final Logger log = LogManager.getLogger(PsicologoController.class);

	@Autowired
	private EntityManager entityManager;

	@GetMapping("/citas")
	public String citasPsicologo() {
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
		return "redirect:/user/horario";

		// devolvemos el model (los datos modificados) y la session para saber
		// quien es el usuario en todo momento

		/*
		 * else { return "redirect:/errorFormulario"; }
		 */

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
						response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No eres administrador, y éste no es tu perfil"); //TODO devuelve error
						return "redirect:/psicologo/horario";
					}
				}
				ga.removeAllPatients();
				ga.setPatient(ul);
				for (User u: ul) { u.addCita(ga); }
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
