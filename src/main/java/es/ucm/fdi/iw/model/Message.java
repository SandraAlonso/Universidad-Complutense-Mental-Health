package es.ucm.fdi.iw.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;



/**
 * A message that users can send each other.
 *
 * @author mfreire
 */
@Entity
@NamedQueries({
	@NamedQuery(name = "Message.getConversation", query = "SELECT m FROM Message m WHERE (RECIPIENT_ID=:id AND SENDER_ID=:userId) OR (RECIPIENT_ID=:userId AND SENDER_ID=:id)"),
	@NamedQuery(name = "Message.getByTopic", query = "SELECT m FROM Message m WHERE TOPIC=:id"),
	@NamedQuery(name = "Message.countTopic", query = "SELECT COUNT (m) FROM Message m WHERE topic=:t")
})
/*
@NamedQueries({
	@NamedQuery(name="Message.countUnread",
	query="SELECT COUNT(m) FROM Message m "
			+ "WHERE m.recipient.id = :userId AND m.dateRead = null")
			
})*/
public class Message {
		
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	@ManyToOne(targetEntity = User.class)
	private User sender;
	@ManyToOne(targetEntity = User.class)
	private User recipient;
	private String text;
	
	private String topic;

	private LocalDateTime dateSent;
	//private LocalDateTime dateRead;
	
	/**
	 * Convierte colecciones de mensajes a formato JSONificable
	 * @param messages
	 * @return
	 * @throws JsonProcessingException
	 */
	public static List<Transfer> asTransferObjects(Collection<Message> messages) {
		ArrayList<Transfer> all = new ArrayList<>();
		for (Message m : messages) {
			all.add(new Transfer(m));
		}
		return all;
	}
	
	/**
	 * Objeto para persistir a/de JSON
	 * @author mfreire
	 */
	
	public static class Transfer {
		private String from;
		private String to;
		private String sent;
		private String received;
		private String text;
		long id;
		private String topic;
		public Transfer(Message m) {
			if(m.getSender()!=null) 
				this.from = m.getSender().getUsername();
			if(m.getRecipient() != null) this.to = m.getRecipient().getUsername();
			this.sent = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(m.getDateSent());
			//this.received = m.getDateRead() == null ?
			//		null : DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(m.getDateRead());
			this.text = m.getText();
			this.id = m.getId();
			this.topic = m.getTopic();
		
		}
		public String getFrom() {
			return from;
		}
		public void setFrom(String from) {
			this.from = from;
		}
		public String getTo() {
			return to;
		}
		public void setTo(String to) {
			this.to = to;
		}
		public String getSent() {
			return sent;
		}
		public void setSent(String sent) {
			this.sent = sent;
		}
		public String getReceived() {
			return received;
		}
		public void setReceived(String received) {
			this.received = received;
		}
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}
		public String getTopic() {
			return topic;
		}
		public void setTopic(String topic) {
			this.topic = topic;
		}
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public User getSender() {
		return sender;
	}

	public void setSender(User sender) {
		this.sender = sender;
	}

	public User getRecipient() {
		return recipient;
	}

	public void setRecipient(User recipient) {
		this.recipient = recipient;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public LocalDateTime getDateSent() {
		return dateSent;
	}

	public void setDateSent(LocalDateTime dateSent) {
		this.dateSent = dateSent;
	}
	
	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}
}
