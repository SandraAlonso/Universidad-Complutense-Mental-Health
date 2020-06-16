package es.ucm.fdi.iw.model;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;


@Entity
public class EmotionalState {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long ID;
	
	@NotNull(message="Introduce la fecha de la cita")
	@Column(name="date", unique=true)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate date;

	private Integer emotionalState;
	
	private String description;
	
	@ManyToOne
	private User patient;

	public void setPatient(User patient) {
		this.patient = patient;
	}

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
	
	public Integer getEmotionalState() {
		return emotionalState;
	}

	public void setEmotionalState(Integer emotionalState) {
		this.emotionalState = emotionalState;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public User getPatient() {
		return patient;
	}


}
