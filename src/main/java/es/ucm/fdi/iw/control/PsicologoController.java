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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

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

	@Autowired
	private LocalData localData;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@RequestMapping("/horario")
	public String horarioPsicologo(HttpSession session, Model model, @RequestParam(required = false) Integer weeks) {
		User requester = (User) session.getAttribute("u"); // TODO podría usar directamente el requester?
		User stored = entityManager.find(User.class, requester.getId());
		if (weeks == null)
			weeks = 0;
		model.addAttribute("u", stored);
		model.addAttribute("group_appointments", stored.getAppointmentsOfTheWeek(weeks.intValue()));
		model.addAttribute("days", stored.getDaysOfTheWeek(weeks.intValue()));
		model.addAttribute("week", weeks);
		return "horarioPsicologo";
	}

	// Requester es el usuario que solicita la accion.
	// Edited son los datos que obtenemos en la interfaz y por lo tanto, lo que
	// queremos cambiar
	// Target es el resultado final que obtenemos despues de completar la accion y
	// lo que guardamos en la BBDD

	@PostMapping("/saveGroupAppointment")
	@Transactional
	public String saveGroupAppointment(Model model, HttpServletResponse response,
			@ModelAttribute @Valid GroupAppointment group_appointment, BindingResult result, HttpSession session)
			throws IOException {
		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());

		int fecha = group_appointment.getDate().compareTo(LocalDate.now());
		int hora = group_appointment.getFinish_hour().compareTo(group_appointment.getStart_hour());
		LocalTime ahora = LocalTime.now();
		int horaActual = group_appointment.getStart_hour().compareTo(ahora);

		if (fecha == 0 && horaActual > 0 && hora > 0 || fecha > 0 && hora > 0) {
			group_appointment.setPychologist(stored);
			stored.addGroupAppointment(group_appointment);
			entityManager.persist(group_appointment);
			entityManager.flush();
		}
		return "redirect:/psicologo/horario";

		// devolvemos el model (los datos modificados) y la session para saber
		// quien es el usuario en todo momento

		/*
		 * else { return "redirect:/errorFormulario"; }
		 */

	}


	@RequestMapping("/deleteGroupAppointment")
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

		return "redirect:/psicologo/horario"; // devolvemos el model (los datos modificados) y la session para saber
												// quien es el usuario en todo momento
	}

	@RequestMapping("/modifyGroupAppointment")
	@Transactional
	public String modifyGroupAppointment(Model model, HttpServletResponse response,
			@ModelAttribute @Valid GroupAppointment group_appointment, BindingResult result, HttpSession session)
			throws IOException {
		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());
		GroupAppointment ga = entityManager.find(GroupAppointment.class, group_appointment.getID());

		// int weeks = model.getAttribute("week"); //TODO podría usar directamente el
		// requester?

		for (GroupAppointment it : stored.getGroupAppointments()) {
			if (it.equals(ga)) {
				ga.setName(group_appointment.getName());
				ga.setDate(group_appointment.getDate());
				ga.setStart_hour(group_appointment.getStart_hour());
				ga.setFinish_hour(group_appointment.getFinish_hour());
				ga.setDescription(group_appointment.getDescription());
				break;
			}
		}

		return "redirect:/psicologo/horario"; // devolvemos el model (los datos modificados) y la session para saber
												// quien es el usuario en todo momento
	}
	
	@RequestMapping("/getUsersOfGroupAppointments")
	public String receiveArrayOfValues(@RequestParam String[] values) 
	{
		
		
		return "redirect:/psicologo/horario";
	}

	@GetMapping("/citas")
	public String citasPsicologo() {
		return "misPacientes";
	}

	@GetMapping("/{id}")
	public String getUser(@PathVariable long id, Model model, HttpSession session) throws JsonProcessingException {
		User u = entityManager.find(User.class, id);
		model.addAttribute("user", u);

		// construye y envía mensaje JSON
		User requester = (User) session.getAttribute("u");
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		rootNode.put("text", requester.getUsername() + " is looking up " + u.getUsername());
		String json = mapper.writeValueAsString(rootNode);

		messagingTemplate.convertAndSend("/topic/admin", json);

		return "user";
	}

	@PostMapping("/{id}")
	@Transactional
	public String postUser(HttpServletResponse response, @PathVariable long id, @ModelAttribute User edited,
			@RequestParam(required = false) String pass2, Model model, HttpSession session) throws IOException {
		User target = entityManager.find(User.class, id);
		model.addAttribute("user", target);

		User requester = (User) session.getAttribute("u");
		if (requester.getId() != target.getId() && // un usuario no puede modificar un perfil que no sea el suyo
				!requester.hasRole(Role.ADMIN)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "No eres administrador, y éste no es tu perfil");
		}

		if (edited.getPassword() != null && edited.getPassword().equals(pass2)) {
			// save encoded version of password
			target.setPassword(passwordEncoder.encode(edited.getPassword()));
		}
		target.setUsername(edited.getUsername());
		return "user";
	}

	@GetMapping(value = "/{id}/photo")
	public StreamingResponseBody getPhoto(@PathVariable long id, Model model) throws IOException {
		File f = localData.getFile("user", "" + id);
		InputStream in;
		if (f.exists()) {
			in = new BufferedInputStream(new FileInputStream(f));
		} else {
			in = new BufferedInputStream(
					getClass().getClassLoader().getResourceAsStream("static/img/unknown-user.jpg"));
		}
		return new StreamingResponseBody() {
			@Override
			public void writeTo(OutputStream os) throws IOException {
				FileCopyUtils.copy(in, os);
			}
		};
	}

	@PostMapping("/{id}/msg")
	@ResponseBody
	@Transactional
	public String postMsg(@PathVariable long id, @RequestBody JsonNode o, Model model, HttpSession session)
			throws JsonProcessingException {

		String text = o.get("message").asText();
		User u = entityManager.find(User.class, id);
		User sender = entityManager.find(User.class, ((User) session.getAttribute("u")).getId());
		model.addAttribute("user", u);

		// construye mensaje, lo guarda en BD
		Message m = new Message();
		m.setRecipient(u);
		m.setSender(sender);
		m.setDateSent(LocalDateTime.now());
		m.setText(text);
		entityManager.persist(m);
		entityManager.flush(); // to get Id before commit

		// construye json
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		rootNode.put("from", sender.getUsername());
		rootNode.put("to", u.getUsername());
		rootNode.put("text", text);
		rootNode.put("id", m.getId());
		String json = mapper.writeValueAsString(rootNode);

		log.info("Sending a message to {} with contents '{}'", id, json);

		messagingTemplate.convertAndSend("/user/" + u.getUsername() + "/queue/updates", json);
		return "{\"result\": \"message sent.\"}";
	}

	@PostMapping("/{id}/photo")
	public String postPhoto(HttpServletResponse response, @RequestParam("photo") MultipartFile photo,
			@PathVariable("id") String id, Model model, HttpSession session) throws IOException {
		User target = entityManager.find(User.class, Long.parseLong(id));
		model.addAttribute("user", target);

		// check permissions
		User requester = (User) session.getAttribute("u");
		if (requester.getId() != target.getId() && !requester.hasRole(Role.ADMIN)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "No eres administrador, y éste no es tu perfil");
			return "user";
		}

		log.info("Updating photo for user {}", id);
		File f = localData.getFile("user", id);
		if (photo.isEmpty()) {
			log.info("failed to upload photo: emtpy file?");
		} else {
			try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(f))) {
				byte[] bytes = photo.getBytes();
				stream.write(bytes);
			} catch (Exception e) {
				log.warn("Error uploading " + id + " ", e);
			}
			log.info("Successfully uploaded photo for {} into {}!", id, f.getAbsolutePath());
		}
		return "user";
	}

}
