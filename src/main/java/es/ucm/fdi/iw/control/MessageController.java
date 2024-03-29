package es.ucm.fdi.iw.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import es.ucm.fdi.iw.model.Message;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.User.Role;

/**
 * User-administration controller
 * 
 * @author mfreire
 */
@Controller()
@RequestMapping("message")
public class MessageController {
	
	private static final Logger log = LogManager.getLogger(MessageController.class);
	
	@Autowired 
	private EntityManager entityManager;
		
	@Transactional
	@GetMapping("/")
	public String getMessages(Model model, HttpSession session) {
		User user = (User)session.getAttribute("u");
		User u = entityManager.find(User.class, user.getId());
		session.setAttribute("u", u);
		List<String> topics_list = new ArrayList<String>();
		List<String> topics_list1 = null;
		if(u.hasRole(User.Role.PSICOLOGO)) topics_list1 = u.getCeratorAppointmentsTopic();
		if(u.hasRole(User.Role.PACIENTE)) topics_list1 = u.getGroupAppointmentsPatientTopic();
		if(topics_list1 != null) topics_list = topics_list1;
		String joined = String.join(",", topics_list);
		HashMap<String, Long> unreadMap = new HashMap<String, Long>();
		TypedQuery<Long> query1 = entityManager.createNamedQuery("Message.countTopic", Long.class); 
		for(String e : topics_list) {
			Long count = query1.setParameter("t", e).getSingleResult(); 
			unreadMap.put(e, count);
		}
		if(u.hasRole(Role.ADMIN)) {
			Long count1 = query1.setParameter("t", "admin").getSingleResult();
			unreadMap.put("admin", count1);
			count1 = query1.setParameter("t", "peticiones").getSingleResult();
			unreadMap.put("peticiones", count1);
		}
		session.setAttribute("topics", joined);
		model.addAttribute("topicsList", unreadMap);
		TypedQuery<User> query = entityManager.createNamedQuery("User.getAllActive", User.class); 
		model.addAttribute("usuarios", query.getResultList());
		return "messages";
	}

	@GetMapping(path = "/received", produces = "application/json")
	@Transactional // para no recibir resultados inconsistentes
	@ResponseBody // para indicar que no devuelve vista, sino un objeto (jsonizado)
	public List<Message.Transfer> retrieveMessages(HttpSession session) {
		long userId = ((User)session.getAttribute("u")).getId();		
		User u = entityManager.find(User.class, userId);
		
		List<Message> lm = new ArrayList<Message>();
		for(Message m: u.getReceived()) {
			if(m.getSender()!=null)
				lm.add(m);
		}
		log.info("Generating message list for user {} ({} messages)", 
				u.getUsername(), lm.size());
		return Message.asTransferObjects(lm);
	}	
	
	@GetMapping(path = "/get/{id}", produces = "application/json")
	@Transactional // para no recibir resultados inconsistentes
	@ResponseBody // para indicar que no devuelve vista, sino un objeto (jsonizado)
	public List<Message.Transfer> getById(HttpSession session, @PathVariable long id) {
		long userId = ((User)session.getAttribute("u")).getId();
		User u = entityManager.find(User.class, userId);
		TypedQuery<Message> query = entityManager.createNamedQuery("Message.getConversation", Message.class); 
		List<Message> lm = query.setParameter("id", id).setParameter("userId", userId).getResultList(); 
		log.info("Generating message list for user {} ({} messages)", 
				u.getUsername(),lm.size());
		return Message.asTransferObjects(lm);
	}	
	
	@GetMapping(path = "/get-topic/{id}", produces = "application/json")
	@Transactional // para no recibir resultados inconsistentes
	@ResponseBody // para indicar que no devuelve vista, sino un objeto (jsonizado)
	public List<Message.Transfer> getByTopic(HttpSession session, @PathVariable String id) {
		long userId = ((User)session.getAttribute("u")).getId();
		User u = entityManager.find(User.class, userId);
		TypedQuery<Message> query = entityManager.createNamedQuery("Message.getByTopic", Message.class); 
		List<Message> lm = query.setParameter("id", id).getResultList();
		log.info("Generating message list for user {} ({} messages)", 
				u.getUsername(),lm.size());
		return Message.asTransferObjects(lm);
	}
	
	@GetMapping(path = "/unread", produces = "application/json")
	@ResponseBody
	public String checkUnread(HttpSession session) {
		long userId = ((User)session.getAttribute("u")).getId();		
		long unread = entityManager.createNamedQuery("Message.countUnread", Long.class)
			.setParameter("userId", userId)
			.getSingleResult();
		session.setAttribute("unread", unread);
		return "{\"unread\": " + unread + "}";
	}
	
	
}