package es.ucm.fdi.iw.model;

import java.time.LocalTime;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;


import org.springframework.format.annotation.DateTimeFormat;


@Entity
public class GroupAppointment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer ID;
	
	//@NotEmpty(message="Introduce la fecha de la cita")
	@DateTimeFormat(pattern = "yyyy-mm-dd")
	private Date date;
	
	//@NotEmpty(message="Introduce hora de inicio")
	private LocalTime start_hour;
	
	//@NotEmpty(message="Introduce hora de fin")
	private LocalTime finish_hour;	
	
	//TODO mínimo dos
	//@NotEmpty(message="Introduce al menos dos usuarios")
	@ManyToMany
	private Collection<User> patient;
	
	//@NotEmpty(message="Tienes que estar registrado")
	@ManyToOne
	private User pychologist;
	
	public Integer getID() {
		return ID;
	}

	public void setID(Integer iD) {
		ID = iD;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
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

	public Collection<User> getPatient() {
		return patient;
	}

	public void setPatient(Collection<User> patient) {
		this.patient = patient;
	}

	public User getPychologist() {
		return pychologist;
	}

	public void setPychologist(User pychologist) {
		this.pychologist = pychologist;
	}

}
