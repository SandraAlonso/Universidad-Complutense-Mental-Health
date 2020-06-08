package es.ucm.fdi.iw.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;


@Entity
public class GroupAppointment{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long ID;
	
	@NotNull(message="Introduce la fecha de la cita")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate date;

	@NotNull(message="Introduce hora de inicio")
	private LocalTime start_hour;
	
	@NotNull(message="Introduce hora de fin")
	private LocalTime finish_hour;
	
	@ManyToOne
	private User psychologist;

	public long getID() {
		return ID;
	}
	
	public void setID(long iD) {
		ID = iD;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public LocalTime getStart_hour() {
		return start_hour;
	}

	public void setStart_hour(LocalTime start_hour) {
		this.start_hour = start_hour;
	}

	public LocalTime getFinish_hour() {
		return finish_hour;
	}

	public void setFinish_hour(LocalTime finish_hour) {
		this.finish_hour = finish_hour;
	}

	public User getPsychologist() {
		return psychologist;
	}

	public void setPsychologist(User pychologist) {
		this.psychologist = pychologist;
	}
	
	@NotEmpty(message="La cita debe tener nombre")
	@Pattern (regexp="[a-zA-Z0-9ñÑáéíóúÁÉÍÓÚ_-]+", message="El nombre solo puede contener caracteres alfanumericos")
	private String name;

	private String description;
	
	//@NotEmpty(message="La cita debe tener pacientes")
	//@Size(min=2, message="Debes introducir al menos dos usuarios")
	
	@ManyToMany(targetEntity=User.class)
	private List<User> patient;

	public List<User> getPatient() {
		return patient;
	}

	public void setPatient(List<User> patient) {
		this.patient = patient;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public void removeAllPatients() {
		patient.clear();
	}
	
	public void addPatient(User u) {
		patient.add(u);
	}
}
