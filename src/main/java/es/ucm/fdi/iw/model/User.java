package es.ucm.fdi.iw.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
				+ "WHERE u.username = :username"),
		@NamedQuery(name = "User.findPatientsOf", query = "SELECT u " + "FROM User u "
				+ "WHERE u.psychologist.id = :psychologistId ") })

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

	@ManyToOne
	private User psychologist;

	// application-specific fields
	private String firstName;
	private String lastName;
	private String mail;

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
	@JoinColumn(name = "psychologist_id")
	@OrderBy("date ASC, start_hour ASC")
	private List<GroupAppointment> groupAppointments = new ArrayList<GroupAppointment>();

	@OneToMany(targetEntity = IndividualAppointment.class)
	@JsonIgnore
	@JoinColumn(name = "patient_id")
	@OrderBy("date ASC, start_hour ASC")
	private List<IndividualAppointment> appointments = new ArrayList<IndividualAppointment>();

	@OneToMany(targetEntity = EmotionalState.class)
	@JsonIgnore
	@JoinColumn(name = "patient_id")
	@OrderBy("date ASC")
	private List<EmotionalState> emotionalState = new ArrayList<EmotionalState>();

	public void addEmotionalState(EmotionalState a) {
		emotionalState.add(a);
	}

	public List<EmotionalState> getEmotionalState() {
		return emotionalState;
	}

	public void setEmotionalState(List<EmotionalState> emotionalState) {
		this.emotionalState = emotionalState;
	}

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
		groupAppointments.add(ap);
	}

	public void removeGroupAppointment(GroupAppointment ap) {
		groupAppointments.remove(ap);
	}

	public List<IndividualAppointment> getAppointments() {
		return appointments;
	}

	public List<GroupAppointment> getGroupAppointments() {
		return groupAppointments;
	}

	public void setGroupAppointments(List<GroupAppointment> groupAppointments) {
		this.groupAppointments = groupAppointments;
	}

	public void setAppointments(List<IndividualAppointment> appointments) {
		this.appointments = appointments;
	}

	public void addAppointment(IndividualAppointment a) {
		appointments.add(a);
	}

	public List<LocalDate> getDaysOfTheWeek(int week) {

		List<LocalDate> dates = new ArrayList<>();
		LocalDate ahora = LocalDate.now().plusDays(week * 7);
		DayOfWeek firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
		LocalDate startOfCurrentWeek = ahora.with(TemporalAdjusters.previousOrSame(firstDayOfWeek));

		LocalDate printDate = startOfCurrentWeek;
		for (int i = 0; i < 5; i++) {
			dates.add(printDate);
			printDate = printDate.plusDays(1);
		}
		return dates;

	}

	public List<Appointment> getAppointmentsOfTheWeek(int week) {

		List<Appointment> ga = new ArrayList<>();

		LocalDate now = LocalDate.now().plusDays(week * 7);
		DayOfWeek startOfCurrentWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
		LocalDate firstDayOfTheWeek = now.with(TemporalAdjusters.previousOrSame(startOfCurrentWeek));
		System.out.println(firstDayOfTheWeek);

		LocalDate lastDayOfTheWeek = now.plusDays(4);

		System.out.println(lastDayOfTheWeek);

		for (GroupAppointment g : groupAppointments) {
			System.out.println(firstDayOfTheWeek + " " + lastDayOfTheWeek + " " + g.getDate());
			int comp = g.getDate().compareTo(firstDayOfTheWeek);
			int comp2 = g.getDate().compareTo(lastDayOfTheWeek);
			if (comp > 0 && comp2 < 0)
				ga.add(g);
			if (comp == 0 && comp2 > 0)
				break;
		}

		for (IndividualAppointment g : appointments) {
			System.out.println(firstDayOfTheWeek + " " + lastDayOfTheWeek + " " + g.getDate());
			int comp = g.getDate().compareTo(firstDayOfTheWeek);
			int comp2 = g.getDate().compareTo(lastDayOfTheWeek);
			if (comp > 0 && comp2 < 0)
				ga.add(g);
			if (comp == 0 && comp2 > 0)
				break;
		}

		return ga;

	}

	public void addGroupAppointments(GroupAppointment g) {
		groupAppointments.add(g);
	}

	public void addIndividualAppointment(IndividualAppointment g) {
		appointments.add(g);
	}

	public User getPsychologist() {
		return psychologist;
	}

	public void setPsychologist(User psychologist) {
		this.psychologist = psychologist;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

}
