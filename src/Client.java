import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

enum ClientType{
	CLIENT, EVENT_MANAGER, OTHER
}

enum ServerCities{
	TORONTO, MONTREAL, OTTAWA, OTHER
}

public class Client {

	String url = "rmi://localhost";
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	
	IMessage stub;
	
	String clientID;
	ClientType type;
	ServerCities serverToHit;
	IMessage msg;
	Calendar cal = Calendar.getInstance();
	String serverStatusMSG;
	
	public Client() throws IOException {
		
		System.out.println("Hello User!\n"
				+ "Please Enter your UserID:");
		clientID = br.readLine();
		clientID = clientID.toUpperCase().split(" ")[0];
		if(isWrongID(clientID))
		{
			System.out.println("THE CLIENT ID IS INVALID\nPROGRAM EXITING!");
			return;
		}
		log(clientID, "User :"+clientID+" logged in at "+cal.getTime());
		findClientType();
	}
	private boolean isWrongEventID(String eventId) {
		boolean flag= true;
//		System.out.println(eventId);
//		DateFormat df= new SimpleDateFormat ("ddMMyy");
			if(eventId.length()!=10)
			{
				return true;
			}
			if(!(eventId.substring(0, 3).compareToIgnoreCase("MTL")==0 || 
					eventId.substring(0, 3).compareToIgnoreCase("TOR")==0 ||
							eventId.substring(0, 3).compareToIgnoreCase("OTW")==0))
			{
				return true;
			}
			if(!(eventId.substring(3, 4).compareToIgnoreCase("A")==0 || 
					eventId.substring(3, 4).compareToIgnoreCase("E")==0 ||
							eventId.substring(3, 4).compareToIgnoreCase("M")==0))
			{
				return true;
			}
			String dates= eventId.substring(4,6);
			String Month= eventId.substring(6,8);
			String year= eventId.substring(8,10);
			if(!(Integer.parseInt(dates)<=31 && Integer.parseInt(dates)>0 && Integer.parseInt(Month)<=12))
			{
				return true;
			}
			return false;
	}
		
	
	
	private boolean isWrongID(String clientID) {
		boolean test= true;
		
		if(clientID.charAt(3)=='C'||clientID.charAt(3)=='M')
		{
			if(clientID.substring(0, 3).compareToIgnoreCase("MTL")==0 || 
					clientID.substring(0, 3).compareToIgnoreCase("TOR")==0 ||
					clientID.substring(0, 3).compareToIgnoreCase("OTW")==0)
			{
				test= false;
			}
			if(!(clientID.length()==8))
			{
				return true;
			}
			String testID=clientID.substring(clientID.length() - 4);
			for(int i=0;i<testID.length();i++)
			{
				if(!(Character.isDigit(testID.charAt(i))))
				{
					test= true;
				}
			}
		}
		return test;
	}

	private void displayOperations() throws IOException {
	
		char ch = 'y';
		do
		{
			if(type==ClientType.CLIENT)
				displayOperationClient();
			else if(type == ClientType.EVENT_MANAGER)
				displayOperationEventManager();
			log(clientID, "REQUEST SUCCESSFULLY PROCCESSED!");
			System.out.println("Do you want to continue in this login? \n(Press y/Y only to continue):");
			ch = br.readLine().charAt(0);
		}while(ch=='y'||ch=='Y');
		
	}

	private void displayOperationClient() throws IOException {
		
		System.out.println(
				 "\n1. Book Event"
				+ "\n2. Get Booking Schedule"
				+ "\n3. Cancel Event"
				+ "\n4. Swap Event"
				+ "\n5. Quit");	
		
		int choice=4;
		choice = Integer.parseInt(br.readLine());
		String temp[];
		switch(choice) {
		case 1: 
			System.out.println("Enter EventID, Event Type:");
			temp = br.readLine().toUpperCase().split(" ");			
			log(clientID,"Calling the server to book event with request parameters"+" "+ clientID+" " + temp[0]+" "+ temp[1]);
			serverStatusMSG=stub.bookEvent(clientID, temp[0], temp[1]);
			log(clientID,serverStatusMSG);			
			break;	
		case 2:
//			System.out.println("Enter Customer ID:");
//			temp = br.readLine().split(" ");			
			log(clientID,"Calling the server to get booking schedule"+" "+clientID);
			serverStatusMSG=stub.getBookingSchedule(clientID);
			log(clientID,serverStatusMSG);
			break;
		case 3:
			System.out.println("Enter EventID and Event Type:");
			temp = br.readLine().toUpperCase().split(" ");			
			log(clientID,"Calling server to cancel event with request parameters"+" "+clientID+" " + temp[0]+" "+ temp[1]);
			serverStatusMSG=stub.cancelEvent(clientID, temp[0], temp[1]);
			log(clientID,serverStatusMSG);
			break;
		case 4:
			System.out.println("Enter new EventID, Event Type, old EventID, Event Type:");
			temp = br.readLine().toUpperCase().split(" ");					
			log(clientID,"Calling server to swap event"+" "+temp[0].trim()+" "+temp[1].trim()+" "
					+temp[2].trim()+" "+temp[3].trim());
			serverStatusMSG=stub.swapEvent(clientID ,temp[0], temp[1], temp[2], temp[3]);
			log(clientID,serverStatusMSG);
			break;
		case 5:
			return;
		default:
			System.out.println("Enter a valid Option[1-4]:");
		}
		
		System.out.println(serverStatusMSG);
	}

	private void displayOperationEventManager() throws IOException {
	
		System.out.println(
				"\n1. Add Event"
				+ "\n2. Remove Event"
				+ "\n3. List Event Availability"
				+ "\n4. Book Event"
				+ "\n5. Get Booking Schedule"
				+ "\n6. Cancel Event"
				+ "\n7. Swap Event"
				+ "\n8. Quit");
		
		int choice=7;
		choice = Integer.parseInt(br.readLine());
		String temp[];
		switch(choice) {
		case 1: 
			System.out.println("Enter EventID, Event Type and Booking capacity:");
			temp = br.readLine().toUpperCase().split(" ");
			if(isWrongEventID(temp[0].trim()))
			{
				log(clientID, "THE EVENT ID IS INVALID\n");
				System.out.println("THE EVENT ID IS INVALID\nPLEASE ENTER PROPER ID!");				
				return;
			}
			else
			{	
			log(clientID,"Calling server to create event with request parameter"+" "+clientID+" " + temp[0]+" "+ temp[1]+" "+temp[2]);
			serverStatusMSG=stub.addEvent(temp[0], temp[1], Integer.parseInt(temp[2]));
			log(clientID,serverStatusMSG);
			}
			break;
		case 2:
			System.out.println("Enter EventID and Event Type:");
			temp = br.readLine().toUpperCase().split(" ");					
			log(clientID,"Calling server to remove event with request parameters"+" "+clientID+" " + temp[0]+" "+ temp[1]);
			serverStatusMSG=stub.removeEvent(temp[0], temp[1]);
			log(clientID,serverStatusMSG);
			break;
		case 3:
			System.out.println("Enter Event Type:");
			temp = br.readLine().toUpperCase().split(" ");					
			log(clientID,"Calling server to get list event availability with request parameters"+" "+temp[0]);
			serverStatusMSG=stub.listEventAvailability(temp[0]);
			log(clientID,serverStatusMSG);
			break;
		case 4:
			System.out.println("Enter Customer ID ,EventID, Event Type:");
			temp = br.readLine().toUpperCase().split(" ");				
			log(clientID,"Calling server to book event with request paramters"+" "+ temp[0]+" "+ temp[1]+" "+temp[2]);
			serverStatusMSG=stub.bookEvent(temp[0], temp[1], temp[2]);			
			log(clientID,serverStatusMSG);
			break;			
		case 5:
			System.out.println("Enter Customer ID:");
			temp = br.readLine().toUpperCase().split(" ");			
			log(clientID,"Calling server to get booking schedule with request paramters"+" "+ temp[0].trim());
			serverStatusMSG=stub.getBookingSchedule(temp[0].trim());
			log(clientID,serverStatusMSG);
			break;
		case 6:
			System.out.println("Enter Customer ID, EventID and Event Type:");
			temp = br.readLine().toUpperCase().split(" ");					
			log(clientID,"Calling server to cancel event"+" "+temp[0].trim()+" "+temp[1].trim()+" "+temp[2].trim());
			serverStatusMSG=stub.cancelEvent(temp[0], temp[1], temp[2]);
			log(clientID,serverStatusMSG);
			break;
		case 7:
			System.out.println("Enter Customer ID, old EventID, Event Type, new EventID, Event Type:");
			temp = br.readLine().toUpperCase().split(" ");					
			log(clientID,"Calling server to swap event"+" "+temp[0].trim()+" "+temp[1].trim()+" "
					+temp[2].trim()+" "+temp[3].trim()+" "+temp[4].trim());
			serverStatusMSG=stub.swapEvent(temp[0], temp[1], temp[2], temp[3], temp[4]);
			log(clientID,serverStatusMSG);
			break;
		case 8:
			return;
		default:
			System.out.println("Enter a valid Option[1-7]:");
		}
		
		System.out.println(serverStatusMSG);
//		br.readLine();
	}

	private void findClientType() {
		if(clientID.charAt(3)=='M')
			type = ClientType.EVENT_MANAGER;
		else if(clientID.charAt(3)=='C')
			type = ClientType.CLIENT;
		else
			type = ClientType.OTHER;
		if(clientID.substring(0, 3).compareToIgnoreCase("TOR")==0)
			serverToHit = ServerCities.TORONTO;
		else if(clientID.substring(0, 3).compareToIgnoreCase("MTL")==0)
			serverToHit = ServerCities.MONTREAL;
		else if(clientID.substring(0, 3).compareToIgnoreCase("OTW")==0)
			serverToHit = ServerCities.OTTAWA;
		else {
			serverToHit = ServerCities.OTHER;
			System.out.println("CITY CAN BE EITHER MTL, TOR OR OTW\nPROGRAM EXITING");
		}
	}

	public static void main(String[] args) throws IOException {

		System.out.println("New Client");
		Client c = new Client();
		c.hitServer();
	}

	private void hitServer() {
		
//		System.out.println(url);
		int portNo = 0;
		if(serverToHit==ServerCities.MONTREAL)
			portNo = 5555;
		else if(serverToHit==ServerCities.OTTAWA)
			portNo = 7777;
		else if(serverToHit==ServerCities.TORONTO)
			portNo = 6666;
		else
			return;

		url = url.concat(":"+portNo+"/"+serverToHit+"/");
		try {
			Registry registry = LocateRegistry.getRegistry(portNo);
			stub=(IMessage)registry.lookup(url);  
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}  
		
		try {
			displayOperations();
		} catch (IOException e) {
			e.printStackTrace();
			log(clientID, "REQUEST FAILED!");
		}
		
	}
	public static void log(String Id, String msg) {
		String filePaths  = "./src/Clientlogs/";
		Calendar cal = Calendar.getInstance();
		String file = filePaths+Id+".txt";		
		String strMessage = cal.getTime()+":"+msg+System.lineSeparator();
		
		try {
			Path filePath = Paths.get(file);
			if(! Files.exists(filePath)) {
			String welcome = "Hello "+Id+System.lineSeparator();
			Files.write(filePath, welcome.getBytes());				
			} 
			Files.write(filePath, strMessage.getBytes(), StandardOpenOption.APPEND);
		
		} catch(IOException iox) {
			System.out.println("Received an error "+iox.getMessage());
		}
		
	}
}
