package es.ucm.fdi.iw.control;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import es.ucm.fdi.iw.model.Message;
import es.ucm.fdi.iw.model.User;

@Controller()
@RequestMapping("notificaciones")
public class NotificacionesController {

	private static final Logger log = LogManager.getLogger(MessageController.class);

	@Autowired
	private EntityManager entityManager;

	@GetMapping("/")
	public String geNotificaciones(Model model, HttpSession session) {
		return "notificaciones";
	}

	@GetMapping(path = "/received", produces = "application/json")
	@Transactional // para no recibir resultados inconsistentes
	@ResponseBody // para indicar que no devuelve vista, sino un objeto (jsonizado)
	public List<Message.Transfer> retrieveMessages(HttpSession session) {
		long userId = ((User) session.getAttribute("u")).getId();
		User u = entityManager.find(User.class, userId);

		List<Message> lm = new ArrayList<Message>();
		for (Message m : u.getReceived()) {
			if (m.getSender() == null)
				lm.add(m);
		}
		log.info("Generating message list for user {} ({} messages)", u.getUsername(), lm.size());
		return Message.asTransferObjects(lm);
	}
}
