package es.ucm.fdi.iw.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortComparator;
import org.hibernate.annotations.SortType;
import org.hibernate.collection.internal.PersistentSet;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A user; can be an Admin, a User, or a Moderator
 *
 * Users can log in and send each other messages.
 *
 * @author mfreire
 */

/*
 * EXPLICACION GENERAL: 1. Introduciomos los datos en la vista (edited) 2.
 * Ejecutamos la acción (por ejemplo editar) 3. El controlador comprueba el
 * perfil de la sesion y si es correcto lo guarda (target). 4. El controlador
 * devuelve el modelo (datos) y la plantilla (html) 5. La vista es actualizada.
 * 
 * Vista ------>Controller-------->Modelo------->Vista.
 */

@Entity
@NamedQueries({
		@NamedQuery(name = "User.byUsername", query = "SELECT u FROM User u "
				+ "WHERE u.username = :username AND u.enabled = 1"),
		@NamedQuery(name = "User.hasUsername", query = "SELECT COUNT(u) " + "FROM User u "
				+ "WHERE u.username = :username") })

public class User {

	private static Logger log = LogManager.getLogger(User.class);
	private static BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	public enum Role {// los usuarios pueden tener varios roles.
		USER, // usuario sin privilegios
		ADMIN, // usuario con privilegios
		PSICOLOGO, PACIENTE, MODERATOR, // remove or add roles as needed
	}

	// do not change these fields
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	@Column(nullable = false)
	private String username;
	@Column(nullable = false)
	private String password;
	@Column(nullable = false)
	private String roles; // split by ',' to separate roles
	private byte enabled;

	// application-specific fields
	private String firstName;
	private String lastName;

	@OneToMany(targetEntity = Message.class)
	@JoinColumn(name = "sender_id")
	@JsonIgnore
	private List<Message> sent = new ArrayList<>();
	@OneToMany(targetEntity = Message.class)
	@JoinColumn(name = "recipient_id")
	@JsonIgnore
	private List<Message> received = new ArrayList<>();
	
	@OneToMany(targetEntity = GroupAppointment.class)
	@JsonIgnore
	@OrderBy("date ASC, start_hour ASC")
	private List<GroupAppointment> groupAppointments = new ArrayList<GroupAppointment>();
	// utility methods
	/**
	 * Checks whether this user has a given role.
	 * 
	 * @param role to check
	 * @return true iff this user has that role.
	 */
	public boolean hasRole(Role role) {
		String roleName = role.name();
		return Arrays.stream(roles.split(",")).anyMatch(r -> r.equals(roleName));
	}

	/**
	 * Tests a raw (non-encoded) password against the stored one.
	 * 
	 * @param rawPassword to test against
	 * @return true if encoding rawPassword with correct salt (from old password)
	 *         matches old password. That is, true iff the password is correct
	 */
	public boolean passwordMatches(String rawPassword) {
		return encoder.matches(rawPassword, this.password);
	}

	/**
	 * Encodes a password, so that it can be saved for future checking. Notice that
	 * encoding the same password multiple times will yield different encodings,
	 * since encodings contain a randomly-generated salt.
	 * 
	 * @param rawPassword to encode
	 * @return the encoded password (typically a 60-character string) for example, a
	 *         possible encoding of "test" is
	 *         $2y$12$XCKz0zjXAP6hsFyVc8MucOzx6ER6IsC1qo5zQbclxhddR1t6SfrHm
	 */
	public static String encodePassword(String rawPassword) {
		return encoder.encode(rawPassword);
	}

	// auto-generated getters and setters (which could be avoided with Lombok)

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getPassword() {
		return password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Sets the password to an encoded value. You can generate encoded passwords
	 * using {@link #encodePassword}. call only with encoded passwords - NEVER STORE
	 * PLAINTEXT PASSWORDS
	 * 
	 * @param encodedPassword to set as user's password
	 */
	public void setPassword(String encodedPassword) {
		this.password = encodedPassword;
	}

	public String getRoles() {
		return roles;
	}

	public void setRoles(String roles) {
		this.roles = roles;
	}

	public byte getEnabled() {
		return enabled;
	}

	public void setEnabled(byte enabled) {
		this.enabled = enabled;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public List<Message> getSent() {
		return sent;
	}

	public void setSent(List<Message> sent) {
		this.sent = sent;
	}

	public List<Message> getReceived() {
		return received;
	}

	public void setReceived(List<Message> received) {
		this.received = received;
	}


	public void addGroupAppointment(GroupAppointment ap) {
		System.out.println(ap.getDate());
		groupAppointments.add(ap);
	}
	

	public List<GroupAppointment> getGroupAppointments() {
		return groupAppointments;
	}

	public void setGroupAppointments(List<GroupAppointment> groupAppointments) {
		this.groupAppointments = groupAppointments;
	}
	
	public List<Date> getDaysOfTheWeek(int week) {
		
		List<Date> dates = new ArrayList<>();

		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_WEEK, cal.getActualMinimum(Calendar.DAY_OF_WEEK) + 1);
		cal.add(Calendar.WEEK_OF_YEAR, week);
		for(int i = 0; i < 7; ++i) {
			dates.add(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
		}
		return dates;
	} //TODO


	public List<GroupAppointment> getAppointmentsOfTheWeek(int week) {
		
		List<GroupAppointment> ga = new ArrayList<>();
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_WEEK, cal.getActualMinimum(Calendar.DAY_OF_WEEK) + 1);
		cal.add(Calendar.WEEK_OF_YEAR, week);
		cal.add(Calendar.DAY_OF_YEAR, -1);
		Date firstDayOfTheWeek = cal.getTime();
		System.out.println(firstDayOfTheWeek);
		
		cal.add(Calendar.DATE, 8);
		Date lastDayOfTheWeek = cal.getTime();
		System.out.println(lastDayOfTheWeek);

		
		for(GroupAppointment g : groupAppointments) {
			System.out.println(firstDayOfTheWeek + " " + lastDayOfTheWeek + " " + g.getDate());
			if(g.getDate().after(firstDayOfTheWeek) && g.getDate().before(lastDayOfTheWeek)) ga.add(g);
			if(g.getDate().equals(lastDayOfTheWeek) || g.getDate().after(lastDayOfTheWeek)) break;
		}
		
		return ga;
		

		// ahora calendar tiene el dia del lunes.
		/*group_appointments.clear();// borramos las ultima consultas si las hubiera.
		int i = 0;
		while (i < 7) {
			addGroupAppointment(All_group_appointments.get(calendar));// buscamos por fecha y añadimos a la lista
			i++;
			calendar.add(Calendar.DATE, +1);// sumamos hasta llegar al domingo
		}
		return group_appointments;*/
	}
}
