package es.ucm.fdi.iw.model;


import javax.persistence.Entity;

import javax.persistence.ManyToOne;



@Entity
public class IndividualAppointment extends Appointment {

	@ManyToOne
	User psychologist;

	public User getPsychologist() {
		return psychologist;
	}

	public void setPsychologist(User psychologist) {
		this.psychologist = psychologist;
	}

}
