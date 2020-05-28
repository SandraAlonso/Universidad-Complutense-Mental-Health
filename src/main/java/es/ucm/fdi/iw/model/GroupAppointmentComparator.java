package es.ucm.fdi.iw.model;

import java.util.Comparator;

public class GroupAppointmentComparator implements Comparator<GroupAppointment>  {

	@Override
	public int compare(GroupAppointment arg0, GroupAppointment arg1) {
		return arg0.getDate().compareTo(arg1.getDate());
	}

}
