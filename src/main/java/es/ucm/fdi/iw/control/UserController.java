package es.ucm.fdi.iw.control;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.List;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
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

import es.ucm.fdi.iw.LocalData;
import es.ucm.fdi.iw.model.GroupAppointment;
import es.ucm.fdi.iw.model.Message;
import es.ucm.fdi.iw.model.Problema;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.User.Role;

/**
 * User-administration controller
 * 
 * @author mfreire
 */
@Controller()
@RequestMapping("user")
public class UserController {
	
	private static final Logger log = LogManager.getLogger(UserController.class);
	
	@Autowired 
	private EntityManager entityManager;
	
	@Autowired
	private LocalData localData;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@GetMapping("/{id}")
	public String getUser(@PathVariable long id, Model model, HttpSession session) 			
			throws JsonProcessingException {
		User u = entityManager.find(User.class, id);
		model.addAttribute("user", u);

		User requester = (User)session.getAttribute("u");
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		rootNode.put("text", requester.getUsername() + " está mirando el perfil de " + u.getUsername());
		rootNode.put("to", "admin");
		String json = mapper.writeValueAsString(rootNode);
		
		messagingTemplate.convertAndSend("/topic/admin", json);

		session.setAttribute("u", u);
		return "user";
	}	
	
	
	@PostMapping("/{id}")
	@Transactional
	public String postUser(
			HttpServletResponse response,
			@PathVariable long id, 
			@ModelAttribute User edited, 
			@RequestParam(required=false) String pass2,
			Model model, HttpSession session) throws IOException {
		User target = entityManager.find(User.class, id);
		User sender = entityManager.find(
				User.class, ((User)session.getAttribute("u")).getId());
		model.addAttribute("user", target);
		
		User requester = (User)session.getAttribute("u");
		if (requester.getId() != target.getId() &&
				! requester.hasRole(Role.ADMIN)) {			
			response.sendError(HttpServletResponse.SC_FORBIDDEN, 
					"No eres administrador, y éste no es tu perfil");
		}
		
		TypedQuery<User> query = entityManager.createNamedQuery("User.byUsername", User.class);
		List<User> u = query.setParameter("username", edited.getUsername()).getResultList();
		
		TypedQuery<User> query2 = entityManager.createNamedQuery("User.byMail", User.class);
		List<User> u2 = query2.setParameter("email", edited.getMail()).getResultList();
		
		if((u == null && u2 == null) 
				|| (u.isEmpty() && !u2.isEmpty() && target.getMail().equals(u2.get(0).getMail()))
				|| (!u.isEmpty() && u2.isEmpty() && target.getUsername().equals(u.get(0).getUsername()))
				|| (target.getMail().equals(u2.get(0).getMail()) && target.getUsername().equals(u.get(0).getUsername()))) {		
			if (edited.getPassword() != null && edited.getPassword().equals(pass2)) {
				// save encoded version of password
				target.setPassword(passwordEncoder.encode(edited.getPassword()));
			}		
			if(sender.hasRole(Role.ADMIN)) target.setRoles(edited.getRoles());
			target.setUsername(edited.getUsername());
			target.setFirstName(edited.getFirstName());
			target.setLastName(edited.getLastName());
			target.setMail(edited.getMail());
		}
		else {
			Problema p = new Problema("Error al modificar el usuario " + target.getUsername() + ". Ya hay un usuario existente el sistema.");
			model.addAttribute("problema", p);
			log.info("Error al modificar el usuario '{}'. Ya hay un usuario existente en el sistema.", target.getUsername());
		}
		return "user";
	}	
	
	@GetMapping(value="/{id}/photo")
	public StreamingResponseBody getPhoto(@PathVariable long id, Model model) throws IOException {		
		File f = localData.getFile("user", ""+id);
		InputStream in;
		if (f.exists()) {
			in = new BufferedInputStream(new FileInputStream(f));
		} else {
			in = new BufferedInputStream(getClass().getClassLoader()
					.getResourceAsStream("static/img/unknown-user.jpg"));
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
	public String postMsg(@PathVariable long id, 
			@RequestBody JsonNode o, Model model, HttpSession session) 
		throws JsonProcessingException {
		
		String text = o.get("message").asText();
		User u = entityManager.find(User.class, id);
		User sender = entityManager.find(
				User.class, ((User)session.getAttribute("u")).getId());
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
		
		TypedQuery<User> query = entityManager.createNamedQuery("User.byUsername", User.class);
		User to = query.setParameter("username", u.getUsername()).getSingleResult();
		to.setUnread(to.getUnread() + 1);
		
		log.info("Sending a message to {} with contents '{}'", id, json);

		messagingTemplate.convertAndSend("/user/"+u.getUsername()+"/queue/updates", json);
		return "{\"result\": \"message sent.\"}";
	}
	
	@PostMapping("/{topic}/group-msg")
	@ResponseBody
	@Transactional
	public String postMsg(@PathVariable String topic, 
			@RequestBody JsonNode o, Model model, HttpSession session) 
		throws JsonProcessingException {
		
		String text = o.get("message").asText();
		User sender = entityManager.find(
				User.class, ((User)session.getAttribute("u")).getId());
		
		// construye mensaje, lo guarda en BD
		Message m = new Message();
		m.setTopic(topic);
		m.setSender(sender);
		m.setDateSent(LocalDateTime.now());
		m.setText(text);
		entityManager.persist(m);
		entityManager.flush(); // to get Id before commit
		
		// construye json
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		rootNode.put("from", sender.getUsername());
		rootNode.put("to", topic);
		rootNode.put("text", text);
		rootNode.put("id", m.getId());
		String json = mapper.writeValueAsString(rootNode);
		
		if(topic.equals("admin") || topic.contentEquals("peticiones")) {
			List<User> users = entityManager.createQuery("SELECT u FROM User u WHERE roles LIKE '%ADMIN%'").getResultList();
			for (User u : users) u.setUnread(u.getUnread() + 1);
		}
		else {
			TypedQuery<GroupAppointment> query = entityManager.createNamedQuery("GroupAppointment.byName", GroupAppointment.class);
			List<GroupAppointment> to = query.setParameter("topic", topic).getResultList();
			for(User u : to.get(0).getPatient()) u.setUnread(u.getUnread() + 1);
			to.get(0).getCreator().setUnread(to.get(0).getCreator().getUnread() + 1);
		}
		
		log.info("Sending a message to {} with contents '{}'", topic, json);

		messagingTemplate.convertAndSend("/topic/" + topic, json);
		return "{\"result\": \"message sent.\"}";
	}
	
	@PostMapping("/{id}/photo")
	public String postPhoto(
			HttpServletResponse response,
			@RequestParam("photo") MultipartFile photo,
			@PathVariable("id") String id, Model model, HttpSession session) throws IOException {
		User target = entityManager.find(User.class, Long.parseLong(id));
		model.addAttribute("user", target);
		
		// check permissions
		User requester = (User)session.getAttribute("u");
		if (requester.getId() != target.getId() &&
				! requester.hasRole(Role.ADMIN)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, 
					"No eres administrador, y éste no es tu perfil");
			return "user";
		}
		
		log.info("Updating photo for user {}", id);
		File f = localData.getFile("user", id);
		if (photo.isEmpty()) {
			log.info("failed to upload photo: emtpy file?");
		} else {
			try (BufferedOutputStream stream =
					new BufferedOutputStream(new FileOutputStream(f))) {
				byte[] bytes = photo.getBytes();
				stream.write(bytes);
			} catch (Exception e) {
				log.warn("Error uploading " + id + " ", e);
			}
			BufferedImage resizeMe = ImageIO.read(f);
			Dimension newMaxSize = new Dimension(256, 256);
			BufferedImage resizedImg = Scalr.resize(resizeMe, Method.QUALITY,
			                                        newMaxSize.width, newMaxSize.height);
			BufferedImage cutImg = null;
			if(resizedImg.getHeight() < 256 || resizedImg.getHeight() < resizedImg.getWidth()) cutImg = Scalr.crop(resizedImg, resizedImg.getHeight(), resizedImg.getHeight());
			else if(resizedImg.getWidth() < 256 || resizedImg.getWidth() < resizedImg.getHeight()) cutImg = Scalr.crop(resizedImg, resizedImg.getWidth(), resizedImg.getWidth());
			else cutImg = resizedImg;
			ImageIO.write(cutImg, "jpg", f);
			log.info("Successfully uploaded photo for {} into {}!", id, f.getAbsolutePath());
		}
		return "user";
	}
	
	
	
}
