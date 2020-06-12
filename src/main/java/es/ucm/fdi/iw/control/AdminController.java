package es.ucm.fdi.iw.control;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.ucm.fdi.iw.LocalData;
import es.ucm.fdi.iw.model.GroupAppointment;
import es.ucm.fdi.iw.model.User;

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

	@GetMapping("/")
	public String index(Model model) {
		model.addAttribute("activeProfiles", env.getActiveProfiles());
		model.addAttribute("basePath", env.getProperty("es.ucm.fdi.base-path"));

		model.addAttribute("usuarios", entityManager.createQuery("SELECT u FROM User u WHERE roles LIKE '%USER%'").getResultList());
		model.addAttribute("psicologos", entityManager.createQuery("SELECT u FROM User u WHERE roles LIKE '%PSICOLOGO%'").getResultList());
		model.addAttribute("pacientes", entityManager.createQuery("SELECT u FROM User u WHERE roles LIKE '%PACIENTE%'").getResultList());
		model.addAttribute("administradores", entityManager.createQuery("SELECT u FROM User u WHERE roles LIKE '%ADMIN%'").getResultList());

		return "admin";
	}

	@RequestMapping("/toggleuser")
	@Transactional
	public String delUser(Model model, HttpSession session, @RequestParam long id) {
		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());
		User target = entityManager.find(User.class, id);
		if(stored.getId() != target.getId()) {
			if (target.getEnabled() == 1) {
				// disable
				File f = localData.getFile("user", "" + id);
				if (f.exists()) {
					f.delete();
				}
				// disable user
				target.setEnabled((byte) 0);
			} else {
				// enable user
				target.setEnabled((byte) 1);
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
		//TODO comprobar que stored tiene admin
		entityManager.persist(user);
		entityManager.flush();
		return "redirect:/admin/";
	}

	@RequestMapping("/deleteUser")
	@Transactional
	public String deleteUser(Model model, HttpServletResponse response, HttpSession session,
			@RequestParam long id) throws IOException {
		User requester = (User) session.getAttribute("u");
		User stored = entityManager.find(User.class, requester.getId());
		//TODO comprobar que stored tiene admin
		User delete = entityManager.find(User.class, id);
		if(delete != null && delete != stored ) entityManager.remove(delete);
		return "redirect:/admin/";
	}
	
	@RequestMapping("/changePsychologist")
	@Transactional
	public String changePsychologist(Model model, HttpServletResponse response, HttpSession session,
			@RequestParam long id, @RequestParam String psycho) throws IOException {
		TypedQuery<User> query = entityManager.createNamedQuery("User.byUsername", User.class);	
		//Este user contiene el id del paciente y el nickname del usuario
		User psychologist = query.setParameter("username", psycho).getSingleResult();
		
		//Comprobar si el usuario es un psicólgo
		String[] roles = psychologist.getRoles().split(",");
		boolean is_psycho = false;
		for(int j = 0; j < roles.length; ++j) {
			if(roles[j].equals("PSICOLOGO")) {
				is_psycho = true;
				break;
			}
		}
		if(is_psycho) {
			User u = entityManager.find(User.class, id);
			u.setPsychologist(psychologist);
		};

		return "redirect:/admin/";
	}
	
	@RequestMapping("/findUsers")
	@Transactional
	public String findUser(Model model, HttpServletResponse response, HttpSession session,
			@RequestParam String filter, @RequestParam String search) throws IOException {

		switch(filter) {
		case "nickname":
			model.addAttribute("busqueda", entityManager.createQuery("SELECT u FROM User u WHERE u.username LIKE '%" + search + "%'").getResultList());
			break;
		case "email":
			model.addAttribute("busqueda", entityManager.createQuery("SELECT u FROM User u WHERE u.mail LIKE '%" + search + "%'").getResultList());
			break;
		case "nombre":
			model.addAttribute("busqueda", entityManager.createQuery("SELECT u FROM User u WHERE u.firstName LIKE '%" + search + "%'").getResultList());
			break;
		}
		return index(model);

	}
	
}
