package es.ucm.fdi.iw.model;

import java.time.LocalDate;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;


@Entity
public class Animosity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long ID;
	
	@NotNull(message="Introduce la fecha de la cita")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate date;

	//TODO comprobar entre 0 y 5
	private Integer animosity;
	
	private String description;
	
	@ManyToOne
	private User patient;

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
	
	public Integer getAnimosity() {
		return animosity;
	}

	public void setAnimosity(Integer animosity) {
		this.animosity = animosity;
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
