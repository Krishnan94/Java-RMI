import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerT  {
	
	ServerCities city;
	String url ;
	MessageImpl stub;
	int udpPort;
	int rmiPort;
	
	public ServerT(ServerCities c, String u, int p, int udpP) {
		
		city = c;
		
		url = u;
		
		rmiPort = p;
		
		udpPort = udpP;
		
		Runnable rmiComm = ()-> {
			startRMI();
		};
		
		Runnable udpConnectionDual = ()-> {
			startUDPConnectionDual();
		};
		
		Thread thread1 = new Thread(rmiComm);
		Thread thread2 = new Thread(udpConnectionDual);
		
		thread1.start();
		thread2.start();
	}

	private void startUDPConnectionDual() {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(udpPort);
			// to stored the received data from
			byte[] buffer = new byte[1000];						// the client.
			System.out.println("Server Started............");
			while (true) {// non-terminating loop as the server is always in listening mode.
				
				buffer = new byte[1000];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				
				// Server waits for the request to come
				aSocket.receive(request);// request received
				
				String req = new String(request.getData());
				
				String replyStr = replyGenerator(req.concat(" "));
				
				if(stub.portUDP!=request.getPort())
				{
					System.out.println("Request received from client: " + new String(request.getData()));
					stub.log("Request received from client: " + new String(request.getData()));
					
				}
				
				byte[] rep = new byte[1000];
				rep = replyStr.getBytes();
				DatagramPacket reply = new DatagramPacket(rep, replyStr.length(), request.getAddress(),
						request.getPort());// reply packet ready
				
				aSocket.send(reply);// reply sent
				if(stub.portUDP!=request.getPort())
				{
					System.out.println("Request sent to client: " + replyStr);
					stub.log("Request sent to client: " + replyStr);
				}
				
			}
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
	}

	private synchronized String replyGenerator(String string) {
		
		String sp[] = string.trim().split(" ");
		System.out.println(sp[0]);
		if(sp[0].compareToIgnoreCase("LIST")==0)
		{
			EventList el = stub.EventRecords.get(sp[2].trim());
			StringBuilder sb = new StringBuilder("");
			sb.append("\nList Event Availability: "+sp[2].trim()+" in Server: "+city+"\n");	
			int cntRec=0;
			if(el!=null)
			{
				for(Event e: el.events.values())
				{
					sb.append("\n"+e.eventID+" "+(e.capacity-e.currentCapacity)+"\n");
					cntRec++;
				}
			}
			if(cntRec==0)
				sb.append("\nNO EVENTS FOUND!");
			System.out.println(sb.toString());
			return sb.toString();
		}
		
		else if(sp[0].compareToIgnoreCase("CANCEL")==0)
		{
			boolean flag = false;
			StringBuilder sb = new StringBuilder("");
			if(sp[3].substring(0, 3).compareToIgnoreCase(stub.cityCode)!=0)
				return "blank";
			EventList el = stub.EventRecords.get(sp[4].trim());
				if(el.events.containsKey(sp[3].trim()))
				{
					Event e = el.events.get(sp[3].trim());
					flag = true;
					if(e.cancelBooking(sp[2].trim()))
					{
						sb.append("\nCANCELLED BOOKING FOR "+sp[3]);
					}
					else
						sb.append("\nNO CLIENT FOUND BOOKED IN THE EVENT");
				}
				
				else
					return "\nNO EVENT TO BE CANCELLED";
			
			if(flag == false)
				return "blank";
			return sb.toString();
		}
		
		else if(sp[0].compareToIgnoreCase("SCHEDULE")==0)
		{
			boolean flag = false;
			StringBuilder sb = new StringBuilder("\nEVENTS SCHEDULED IN "+city);
			for(EventList el : stub.EventRecords.values())
			{
				for(Event e: el.events.values())
				{
					for(String s: e.bookedCustomers)
						if(s.compareToIgnoreCase(sp[2].trim())==0)
						{
							sb.append("\n"+e.toString());
							flag = true;
						}
				}
			}
			if(flag == false)
				sb.append("\nNO BOOKING FOUND FOR "+sp[2]+" in "+city);
			return sb.toString();
		}
		
		else if(sp[0].compareToIgnoreCase("BOOK")==0)
		{
			boolean flag = false;
			StringBuilder sb = new StringBuilder("");
			if(sp[3].substring(0, 3).compareTo(stub.cityCode)!=0)
				return "blank";
			EventList el = stub.EventRecords.get(sp[4].trim());
			// el = stub.EventRecords.get("SEMINAR");
				if(el.events.containsKey(sp[3].trim()))
				{
					flag = true;
					Event e = el.events.get(sp[3].trim());
					if(e.bookCustomer(sp[2].trim()))
						sb.append("BOOKED EVENT FOR "+sp[2]);
					else
						sb.append("CANNOT BOOK EVENT AS THE EVENT IS FULL OR THE CUSTOMER HAS ALREADY BOOKED THE EVENT!");
				}
			if(flag==false)
				return "NO EVENT FOUND!";
				
			return sb.toString();
		}
		
		else if(sp[0].compareToIgnoreCase("REMOVE_BOOK")==0)
		{
			if(sp[1].trim().substring(0, 3).compareToIgnoreCase(stub.cityCode)!=0)
				return "blank";
			else
			{
				for(UserDetails u : stub.nonCityUsers)
				{
					if(u.custID.compareToIgnoreCase(sp[1].trim())==0)
					{
						int c = u.count.get(Integer.parseInt(sp[2].substring(6)));
						c--;
						u.count.put(Integer.parseInt(sp[2].substring(6)), c);
						return "DECREMENTED COUNT FOR "+u.custID;
					}
				}
			}
		}
		
		else if(sp[0].compareToIgnoreCase("CAN_BOOK?")==0)
		{
			if(sp[1].trim().substring(0, 3).compareToIgnoreCase(stub.cityCode)==0)
			{
				boolean flag = false;
				for(UserDetails u : stub.nonCityUsers)
				{
					if(u.custID.compareToIgnoreCase(sp[1].trim())==0)
					{
						flag = true;
						int c=0;
						if(u.count.get(Integer.parseInt(sp[2].substring(6)))!=null)
							c = u.count.get(Integer.parseInt(sp[2].substring(6)));
						if(c<=2)
						{
							flag = true;
							u.count.put(Integer.parseInt(sp[2].substring(6)), ++c);
							return "CAN";
						}
						else
						{
							return "NO";
						}
					}
				}
				
				if(flag==false)
				{
					stub.nonCityUsers.add(new UserDetails(sp[1], sp[2], 1));
					return "CAN";
				}
			}
			
			else
				return "blank";
		}
			
		return null;
	}

	private void startRMI() {
		try {
			stub = new MessageImpl(city);
			Registry registry = LocateRegistry.createRegistry(rmiPort);
			registry.rebind(url,stub);
		} catch (RemoteException e) {
			e.printStackTrace();
		}	
	}

	public static void main(String args[])
	{
		Server serverTOR = new Server(ServerCities.TORONTO,
				"rmi://localhost:6666/TORONTO/", 6666, 2222 );
	}

}
