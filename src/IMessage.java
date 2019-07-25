
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IMessage extends Remote {
	String addEvent(String eventID, String eventType, int capacity) throws RemoteException;
	String removeEvent(String eventID, String eventType) throws RemoteException;
	String cancelEvent(String custID, String eventID, String eventtype) throws RemoteException;
	String getBookingSchedule(String custID) throws RemoteException;
	String listEventAvailability(String eventType) throws RemoteException;
	String bookEvent(String custID, String eventID, String eventType) throws RemoteException;
	String swapEvent(String custID, String newEventID, String newEventType,
			String oldEventID, String oldEventType) throws RemoteException;
}
