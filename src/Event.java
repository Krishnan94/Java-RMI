
import java.util.ArrayList;

enum EventType{
	CONFERENCE, TRADESHOW, SEMINAR
}

public class Event {

	String eventID;
	String eventType;
	boolean availableForBooking;
	int capacity;
	int currentCapacity;
	ArrayList<String> bookedCustomers = new ArrayList<>();
	
	public Event(String ID, String type, int total_capacity) {
		eventID = ID;
		eventType = type;
		capacity =total_capacity;
		currentCapacity = 0;
		availableForBooking = true;
	}
	
	
	public Event(String ID, String type, String total_capacity, 
			String cur_capacity, String[] bookedCustomers) {
		
		eventID = ID;
		eventType = type;
		capacity = Integer.parseInt(total_capacity);
		currentCapacity = Integer.parseInt(cur_capacity);
		availableForBooking = true;
		
		for(String s : bookedCustomers)
			this.bookedCustomers.add(s);
	}
	
	public boolean checkAvailability() {
		
		if(currentCapacity+1 <= capacity)
		{
			availableForBooking = true;
		}
		else
		{
			availableForBooking = false;
		}
		
		return availableForBooking;
	}
	
	public boolean bookCustomer(String custID)
	{
		if(checkAvailability())
		{
			if(bookedCustomers.contains(custID))
				return false;
			currentCapacity++;
			bookedCustomers.add(custID);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean cancelBooking(String custID) {
		if(bookedCustomers.contains(custID))
		{
			currentCapacity--;
			bookedCustomers.remove(custID);
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		for(String s : bookedCustomers)
			sb.append(" "+s);
		return "\nEvent ID:"+eventID+"\tEvent Type: "+eventType+"\t Total Capacity:"+capacity+" \tCurrent Capacity:"+currentCapacity+"\tBooked Customers:"+sb.toString();
	}
	
}


