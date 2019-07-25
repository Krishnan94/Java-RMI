import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

class EventList{
	HashMap<String , Event> events = new HashMap<>();
}

class UserDetails{
	
	String custID;
	HashMap<Integer, Integer> count = new HashMap<>();
//	int count = 0;
	String eventID;
	public UserDetails(String custID, String eventID, int count) {
		
		this.count.put(Integer.parseInt(eventID.substring(6)), 1);
		this.custID = custID;
		this.eventID = eventID;
	}
}

public class MessageImpl extends UnicastRemoteObject implements IMessage{

	private static final long serialVersionUID = 1L;
	
	HashMap<String, EventList> EventRecords = new HashMap<>();

	EventList conferenceList = new EventList();
	EventList tradeShowList = new EventList();
	EventList seminarList = new EventList();
	
	ArrayList<UserDetails> nonCityUsers = new ArrayList<>();
	
	ServerCities city ;
	String cityCode;
	int portUDP;
	Thread t1;
	
	String s = new String("");
	
	protected MessageImpl(ServerCities c) throws RemoteException {
		super();
		city = c;
		if(city == ServerCities.MONTREAL)
		{
			cityCode = "MTL";
			portUDP = 1111;
		}
		else if(city == ServerCities.OTTAWA)
		{
			cityCode = "OTW";
			portUDP = 3333;
		}
			
		else if(city == ServerCities.TORONTO)
		{
			cityCode = "TOR";
			portUDP = 2222;	
		}
		
		EventRecords.put("CONFERENCE", conferenceList);
		EventRecords.put("SEMINAR", seminarList);
		EventRecords.put("TRADESHOW", tradeShowList);
	}

	
	@Override
	public synchronized String addEvent(String eventID, String eventType, int capacity) {
		
		if(eventID.substring(0, 3).compareToIgnoreCase(cityCode+"")!=0)
			return "EVENT CAN ONLY BE CREATED BY THE EVENT MANAGER OF THE PARTICULAR CITY!";
		
//		System.out.println(eventID+" "+cityCode);
		EventList el = EventRecords.get(eventType.trim());	
		if(el.events.containsKey(eventID))
		{
			Event existingEvent = el.events.get(eventID);
			if(capacity<existingEvent.currentCapacity)
			{
				log("EVENT CAPACITY CAN'T BE UPDATED AS THE CURRENT CAPACITY OF THE EVENT IS GREATER THAN THE DESIRED VALUE");
				return "EVENT CAPACITY CAN'T BE UPDATED AS THE CURRENT CAPACITY OF THE EVENT IS GREATER THAN THE DESIRED VALUE";
			}
			existingEvent.capacity = capacity;
			log("EVENT UPDATED! "+eventID);
			return "EVENT UPDATED! "+eventID;
		}
		else
		{
			Event newEvent = new Event(eventID, eventType, capacity);
			el.events.put(eventID, newEvent);
		}
		log("EVENT ADDED "+eventID);
		return "EVENT ADDED "+eventID;
	}

	@Override
	public synchronized String removeEvent(String eventID, String eventType) {
		
		if(eventID.substring(0, 3).compareToIgnoreCase(cityCode)!=0)
		{
			log("USER ACCESS DENIED FOR THIS EVENT IS NOT AUTHORISED");
			return "EVENT CAN ONLY BE REMOVED BY THE EVENT MANAGER OF THE PARTICULAR CITY!";
		}
//		System.out.println("Remove event");
		EventList el = EventRecords.get(eventType.trim());
//		if(eventID.substring(0, 3).compareToIgnoreCase(cityCode)!=0)
//			return interserverComm("REMOVE EVENT"+eventID+" "+eventType);
		if(el.events.containsKey(eventID))
		{
			for(String s: el.events.get(eventID).bookedCustomers)
				interserverComm("REMOVE_BOOK "+s+" "+eventID);
			el.events.remove(eventID);
			log("EVENT REMOVED "+eventID);
		}
		else
		{
			log("NO EVENT TO REMOVE "+eventID);
			return "NO EVENT TO REMOVE!";
		}
		return "Removed Event".toUpperCase()+eventID;
	}

	@Override
	public synchronized String cancelEvent(String custID, String eventID, String eventType) {
		System.out.println("Cancel Event");
		String s = interserverComm("CANCEL EVENT "+custID+" "+eventID+" "+eventType);
		if(s.charAt(1)=='C')
			interserverComm("REMOVE_BOOK "+custID+" "+eventID);
		return s;
	}

	@Override
	public synchronized String getBookingSchedule(String custID) {
		System.out.println("Booking Schedule");
//		StringBuilder sb = new StringBuilder("BOOKING SCHEDULE FOR "+custID+" IN "+city);
//		EventList el =EventRecords.get(eventType);
//		int cntRec=0;
//		if(el!=null&&el.events.size()>0)
//		{
//			for(Event e: el.events.values())
//			{
//				sb.append("\n"+e.eventID+" "+(e.capacity-e.currentCapacity)+"\n");
//				cntRec++;
//			}
//		}
//		if(cntRec==0)
//			sb.append("\nNO EVENTS FOUND!\n");
		return interserverComm("SCHEDULE EVENT "+custID);
	}

	@Override
	public synchronized String listEventAvailability(String eventType) {
		
		StringBuilder sb = new StringBuilder("");
//		sb.append("\nList Event Availability: "+eventType+" in Server: "+city);	
//		EventList el =EventRecords.get(eventType);
//		int cntRec=0;
//		if(el!=null&&el.events.size()>0)
//		{
//			for(Event e: el.events.values())
//			{
//				sb.append("\n"+e.eventID+" "+(e.capacity-e.currentCapacity)+"\n");
//				cntRec++;
//			}
//		}
//		if(cntRec==0)
//			sb.append("\nNO EVENTS FOUND!\n");
		sb.append(interserverComm("LIST EVENT "+eventType));
		return sb.toString();
		
	}

	public synchronized String interserverComm(String str) {
		DatagramSocket aSocket = null; 	
		s= "";
		for(int i=1111; i<=3333; i=i+1111)
		{
//			if(i==portUDP)
//				continue;
			try{
				aSocket = new DatagramSocket(); //reference of the original socket
				byte [] message = str.getBytes(); //message to be passed is stored in byte array
				InetAddress aHost = InetAddress.getByName("localhost"); //Host name is specified and the IP address of server host is calculated using DNS. 
				int serverPort = i;//agreed upon port	
				DatagramPacket request = new DatagramPacket(message, str.length(), aHost, serverPort);//request packet ready
				if(portUDP!=i)
				{
					System.out.println(city+" Started Inter server communication WITH "+i);
					log(city+" Started Inter server communication WITH "+i);
					System.out.println("\nRequest sent to "+i+" "+str);		
					log("\nRequest sent to "+i+" "+str);
				}
				
				aSocket.send(request);//request sent out
				
				byte [] buffer = new byte[1000];//to store the received data, it will be populated by what receive method returns
				DatagramPacket reply = new DatagramPacket(buffer, buffer.length);//reply packet ready but not populated.
				
				//Client waits until the reply is received-----------------------------------------------------------------------
				aSocket.receive(reply);//reply received and will populate reply packet now.
				String repMSG = new String(reply.getData());
				if(portUDP!=i)
					System.out.println("\nReply received from port "+i+" : "+repMSG);//print reply message after converting it to a string from bytes
				if(repMSG.charAt(0)!='b')
					s = s.concat(repMSG);
				System.out.println(s.concat(repMSG));
				System.out.println(s);
			}
			catch(SocketException e){
				System.out.println("Socket: "+e.getMessage());
			}
			catch(IOException e){
				e.printStackTrace();
				System.out.println("IO: "+e.getMessage());
			}
			finally{
				if(aSocket != null) aSocket.close();//now all resources used by the socket are returned to the OS, so that there is no
													//resource leakage, therefore, close the socket after it's use is completed to release resources.
			}
		}
		return s.toString();
	}

	@Override
	public synchronized String bookEvent(String custID, String eventID, String eventType) {
		System.out.println("Book event");
		String s="";
		
		if(custID.substring(0, 3).compareToIgnoreCase(cityCode)==0 
				&& eventID.substring(0, 3).compareToIgnoreCase(cityCode)==0)
		{
			boolean flag = false;
			EventList el = EventRecords.get(eventType);
			for(Event e: el.events.values())
			{
				if(e.eventID.compareToIgnoreCase(eventID)==0)
				{
					flag = true;
					if(e.bookCustomer(custID))
					{
						log("BOOKED "+eventID+" FOR "+custID);
						return "BOOKED "+eventID+" FOR "+custID;
					}
					else
					{
						log("CANNOT BOOK AS EVENT CAPACITY IS FULL OR THE CUSTOMER HAS ALREADY BOOKED THE EVENT FOR CUSTOMER:"+custID+" FOR EVENT:"+eventID);
						return "CANNOT BOOK AS EVENT CAPACITY IS FULL OR THE CUSTOMER HAS ALREADY BOOKED THE EVENT";	
					}
				}
			}
			
			if(flag==false)
			{
				log("EVENT NOT PRESENT TO BOOK!");
				return "EVENT NOT PRESENT TO BOOK!";
			}
		}
		if(custID.substring(0, 3).compareToIgnoreCase(eventID.substring(0, 3))==0)
		{
			s = interserverComm("BOOK EVENT "+custID+" "+eventID+" "+eventType);
		}
		
		else {
			String s2 = interserverComm("CAN_BOOK? "+custID+" "+eventID);
			if(s2.substring(0, 3).compareToIgnoreCase("CAN")==0)
				s= interserverComm("BOOK EVENT "+custID+" "+eventID+" "+eventType);
			else
			{
				log("CANNOT BOOK AS THE CLIENT HAS ALREADY BOOKED 3 INTERCITY EVENTS "+custID);
				return "CANNOT BOOK AS THE CLIENT HAS ALREADY BOOKED 3 INTERCITY EVENTS";
			}
		}
		return s;
	}
	
	public void log(String msg) {
		String Id = cityCode;
		String filePaths  = "./src/Serverlogs/";
		Calendar ca = Calendar.getInstance();
		String file = filePaths+Id+".txt";		
		String strMessage = ca.getTime()+":"+msg+System.lineSeparator();
		
		try {
			Path filePathObj = Paths.get(file);
			if(! Files.exists(filePathObj)) {
			String welcome = "Hello "+Id+System.lineSeparator();
			Files.write(filePathObj, welcome.getBytes());
//				
			} 
			Files.write(filePathObj, strMessage.getBytes(), StandardOpenOption.APPEND);
//			
		
		} catch(IOException iox) {
			System.out.println("Received an error "+iox.getMessage());
		}
		
	}


	@Override
	public String swapEvent(String custID, String newEventID, String newEventType, String oldEventID,
			String oldEventType) {
		String s =cancelEvent(custID, oldEventID, oldEventType);
		if(s.charAt(1)!='N')
		{
			s = bookEvent(custID, newEventID, newEventType);
			if(s.charAt(0)!='C')
				return "\nEVENT SUCCESSFULLY SWAPPED";
			else
			{
				s = bookEvent(custID, oldEventID, oldEventType);
				return "\nNEW EVENT UNAVAILABLE TO SWAP";
			}
		}
		else
			s = "\nNO EVENT PRESENT TO REMOVE FROM OLD";
		return s;
	}

}
