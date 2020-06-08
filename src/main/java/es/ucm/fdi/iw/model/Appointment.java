package es.ucm.fdi.iw.model;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
public abstract class Appointment {
	
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

}
