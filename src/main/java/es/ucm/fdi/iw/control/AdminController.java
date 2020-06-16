package es.ucm.fdi.iw.control;

import java.io.File;
import java.io.IOException;
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
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.ucm.fdi.iw.LocalData;
import es.ucm.fdi.iw.model.Problema;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.User.Role;

/**
 * Admin-only controller
 * 
 * @author mfreire
 */
@Controller()
@RequestMapping("admin")
public class AdminController {

	private static final Logger log = LogManager.getLogger(AdminController.class);

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private LocalData localData;

	@Autowired
	private Environment env;
	
	@Autowired
	private PasswordEncoder passwordEncoder;


	@GetMapping("/")
	public String index(Model model) {
		model.addAttribute("activeProfiles", env.getActiveProfiles());
		model.addAttribute("basePath", env.getProperty("es.ucm.fdi.base-path"));

		model.addAttribute("usuarios",
				entityManager.createQuery("SELECT u FROM User u WHERE roles LIKE '%USER%'").getResultList());
		model.addAttribute("psicologos",
				entityManager.createQuery("SELECT u FROM User u WHERE roles LIKE '%PSICOLOGO%'").getResultList());
		model.addAttribute("pacientes",
				entityManager.createQuery("SELECT u FROM User u WHERE roles LIKE '%PACIENTE%'").getResultList());
		model.addAttribute("administradores",
				entityManager.createQuery("SELECT u FROM User u WHERE roles LIKE '%ADMIN%'").getResultList());

		return "admin";
	}

	@RequestMapping("/toggleuser")
	@Transactional
	public String delUser(Model model, HttpSession session, @RequestParam long id) {
		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());
		User target = entityManager.find(User.class, id);
		if (stored.getId() != target.getId()) {
			if (target.getEnabled() == 1) {
				// disable
				File f = localData.getFile("user", "" + id);
				if (f.exists()) {
					f.delete();
				}
				// disable user
				target.setEnabled((byte) 0);
				log.info("Usuario {} ha inhabilitado al usuario {} y no podrá acceder al sistema hasta que lo vuelva a habilitar.", stored.getFirstName(), target.getFirstName());
			} else {
				// enable user
				target.setEnabled((byte) 1);
				log.info("Usuario {} ha habilitado de nuevo al usuario {}.", stored.getFirstName(), target.getFirstName());
			}
		}
		return index(model);
	}

	@PostMapping("/createUser")
	@Transactional
	public String createUser(Model model, HttpServletResponse response, @ModelAttribute @Valid User user,
			BindingResult result, HttpSession session) {
		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());
		
		TypedQuery<Long> query = entityManager.createNamedQuery("User.hasUsername", Long.class);
		Long hasUsername = query.setParameter("username", user.getUsername()).getSingleResult();
		
		TypedQuery<Long> query2 = entityManager.createNamedQuery("User.hasEmail", Long.class);
		Long hasEmail = query2.setParameter("mail", user.getMail()).getSingleResult();
		
		if (hasUsername == 0 && hasEmail == 0) {
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			entityManager.persist(user);
			entityManager.flush();
			log.info("Usuario {} ha añadido al usuario {} con rol de {}.", stored.getUsername(), user.getUsername(),
					user.getRoles());
			return "redirect:/admin/";
		}
		else {
			Problema p = new Problema("El usuario " + stored.getUsername() + " no ha sido añadido. Ya existe en el sistema.");
			model.addAttribute("problema", p);
			log.info("El usuario {} no ha sido añadido. Ya existe en el sistema.", stored.getUsername());
			return index(model);
		}
		

	}

	@RequestMapping("/deleteUser")
	@Transactional
	public String deleteUser(Model model, HttpServletResponse response, HttpSession session, @RequestParam long id)
			throws IOException {
		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());
		User delete = entityManager.find(User.class, id);
		if (delete != null && delete != stored) {
			entityManager.remove(delete);
			log.info("Usuario {} ha eliminado al usuario {} con rol de {}.", stored.getFirstName(),
					delete.getFirstName(), delete.getRoles());
			return "redirect:/admin/";
		}
		else {
			Problema p = new Problema("El usuario " + delete.getUsername() + " no ha podido eliminar al usuario porque es nulo o es el mismo.");
			model.addAttribute("problema", p);
			log.info("Usuario {} no ha podido eliminar al usuario porque es nulo o es el mismo.", delete.getUsername());
			return index(model);
		}
	}

	@RequestMapping("/changePsychologist")
	@Transactional
	public String changePsychologist(Model model, HttpServletResponse response, HttpSession session,
			@RequestParam long id, @RequestParam String psycho) throws IOException {
		TypedQuery<User> query = entityManager.createNamedQuery("User.byUsername", User.class);
		List<User> psychologist = query.setParameter("username", psycho).getResultList();
		if (!psychologist.isEmpty() && psychologist.get(0).hasRole(Role.PSICOLOGO)) {
			User u = entityManager.find(User.class, id);
			u.setPsychologist(psychologist.get(0));
			log.info("Usuario {} ha cambiado de psicologo al usuario {}. Ahora su psicologo es {}.",
					psychologist.get(0).getFirstName(), u.getFirstName(), psychologist.get(0).getFirstName());
			return "redirect:/admin/";
		}
		else {
			Problema p = new Problema("El usuario " + psycho + " no es un psicólogo o no existe en el sistema.");
			model.addAttribute("problema", p);
			log.info("El usuario {} no es un psicólogo o no existe en el sistema.", psycho);
			return index(model);
		}

	}

	@RequestMapping("/findUsers")
	@Transactional
	public String findUser(Model model, HttpServletResponse response, HttpSession session, @RequestParam String filter,
			@RequestParam String search) throws IOException {

		switch (filter) {
		case "nickname":
			TypedQuery<User> query2 = entityManager.createNamedQuery("User.findByUsername", User.class); 
			model.addAttribute("busqueda", query2.setParameter("username", "%" + search + "%").getResultList());
			break;
		case "email":
			TypedQuery<User> query = entityManager.createNamedQuery("User.findByEmail", User.class); 
			model.addAttribute("busqueda", query.setParameter("email", "%" + search + "%").getResultList());
			break;
		case "nombre":
			TypedQuery<User> query3 = entityManager.createNamedQuery("User.findByName", User.class); 
			model.addAttribute("busqueda", query3.setParameter("name", "%" + search + "%").getResultList());
			break;
		}
		log.info("Se ha devuelto el contenido de la busqueda.");
		return index(model);
	}

}
