package es.ucm.fdi.iw.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Entity
public class GroupAppointment extends Appointment {

	@NotEmpty(message = "La cita debe tener nombre")
	@Pattern(regexp = "[a-zA-Z0-9ñÑáéíóúÁÉÍÓÚ_-]+", message = "El nombre solo puede contener caracteres alfanumericos")
	private String name;

	private String description;

	// @NotEmpty(message="La cita debe tener pacientes")
	// @Size(min=2, message="Debes introducir al menos dos usuarios")
	@ManyToMany
	@JoinTable(
			  name = "patient_group_appointment", 
			  joinColumns = @JoinColumn(name = "group_appointment_id"), 
			  inverseJoinColumns = @JoinColumn(name = "patient_id"))
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
