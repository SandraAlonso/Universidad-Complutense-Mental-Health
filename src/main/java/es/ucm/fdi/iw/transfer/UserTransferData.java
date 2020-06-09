package es.ucm.fdi.iw.transfer;

import es.ucm.fdi.iw.model.User;

public class UserTransferData {

	public long id;
	public String firstName;
	public String lastName;
	public String mail;
	public String phoneNumber;
	public String psychologistName;  // empty for non-patients
	public long psychologistId; 	 // -1 for non-patients

	public String password;			 // only used when registering a new user
	public String type;
	
	private String disorder;
	private String treatment;
	
	public UserTransferData(long id, String firstName, String lastName, 
			String mail, String phoneNumber, String psychologistName, String type, String disorder, String treatment) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.mail = mail;
		this.phoneNumber = phoneNumber;
		this.psychologistName = psychologistName;
		this.type = type;
		this.disorder = disorder;
		this.treatment = treatment;
	}
	
	public UserTransferData(User u) {
		this.id = u.getId();
		this.firstName = u.getFirstName();
		this.lastName = u.getLastName();
		this.mail = u.getMail();
		this.psychologistId = -1;
		this.disorder = u.getDisorder();
		this.treatment = u.getTreatment();
		if (u.getPsychologist() != null) {
			this.psychologistName = 
				u.getPsychologist().getFirstName() + ", " + 
				u.getPsychologist().getLastName();
			this.psychologistId = u.getPsychologist().getId();
		}
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}


	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @return the mail
	 */
	public String getMail() {
		return mail;
	}

	/**
	 * @param mail the mail to set
	 */
	public void setMail(String mail) {
		this.mail = mail;
	}

	/**
	 * @return the psychologistName
	 */
	public String getPsychologistName() {
		return psychologistName;
	}

	/**
	 * @param psychologistName the psychologistName to set
	 */
	public void setPsychologistName(String psychologistName) {
		this.psychologistName = psychologistName;
	}


	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}


	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "UserTransferData [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", mail=" + mail
				+ ", phoneNumber=" + phoneNumber + ", psychologistName=" + psychologistName + ", type=" + type + "]";
	}

	public String getDisorder() {
		return disorder;
	}

	public void setDisorder(String disorder) {
		this.disorder = disorder;
	}

	public String getTreatment() {
		return treatment;
	}

	public void setTreatment(String treatment) {
		this.treatment = treatment;
	}
}
