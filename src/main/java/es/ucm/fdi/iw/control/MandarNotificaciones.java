package es.ucm.fdi.iw.control;

import java.time.LocalDateTime;

import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import es.ucm.fdi.iw.model.Message;
import es.ucm.fdi.iw.model.User;

public class MandarNotificaciones {

	@Autowired

	private static final Logger log = LogManager.getLogger(UserController.class);


	public static void postNotifications(User user, String body, EntityManager entityManager, SimpMessagingTemplate messagingTemplate) throws JsonProcessingException {

		// construye mensaje, lo guarda en BD
		Message m = new Message();
		m.setRecipient(user);
		m.setDateSent(LocalDateTime.now());
		m.setText(body);
		entityManager.persist(m);
		entityManager.flush(); // to get Id before commit

		// construye json
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		rootNode.put("to", user.getUsername());
		rootNode.put("text", body);
		rootNode.put("id", m.getId());
		String json = mapper.writeValueAsString(rootNode);

		log.info("Sending a message to {} with contents '{}'", user.getUsername(), json);

		messagingTemplate.convertAndSend("/user/" + user.getUsername() + "/queue/updates", json);

	}
}
