package es.ucm.fdi.iw.model;

public class Problema {
	
	String mensaje;
	
	public Problema(String string) {
		this.mensaje = string;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}	

}
