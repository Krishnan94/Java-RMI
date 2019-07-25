import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientInit {
	
	public static void main(String args[]) {
		
			
			Runnable task1= ()-> {
				runMontrealInit();
			};
			
			Runnable task2= ()-> {
				runTorontoInit();
			};
				
			Runnable task3= ()-> {
				runOttawaInit();
			};	
		
			Thread t1 = new Thread(task1);
			Thread t2 = new Thread(task2);
			Thread t3 = new Thread(task3);
			
			t1.start();
			t2.start();
			t3.start();
	}

	private static void runOttawaInit() {
		IMessage stub = null;
		try {
			Registry registry = LocateRegistry.getRegistry(7777);
			stub=(IMessage)registry.lookup("rmi://localhost:7777/OTTAWA/");  
			System.out.println(stub.addEvent("OTWA101010", "SEMINAR", 3));;
			stub.addEvent("OTWA111010", "SEMINAR", 2);
			stub.addEvent("OTWA121010", "SEMINAR", 1);
			stub.addEvent("OTWA131010", "TRADESHOW", 3);
			stub.addEvent("OTWA141010", "TRADESHOW", 2);
			stub.addEvent("OTWA151010", "TRADESHOW", 1);
			stub.addEvent("OTWA161010", "CONFERENCE", 3);
			stub.addEvent("OTWA171010", "CONFERENCE", 2);
			stub.addEvent("OTWA181010", "CONFERENCE", 1);
			stub.bookEvent("MTLC1234", "OTWA101010", "SEMINAR");
			stub.bookEvent("TORC1234", "OTWA101010", "SEMINAR");
			stub.bookEvent("OTWC1234", "OTWA101010", "SEMINAR");
			stub.bookEvent("TORC1234", "OTWA111010", "SEMINAR");
			stub.bookEvent("TORC1234", "OTWA121010", "SEMINAR");
			stub.bookEvent("OTWC1234", "OTWA131010", "TRADESHOW");
			stub.bookEvent("TORC1234", "OTWA151010", "TRADESHOW");
					
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void runTorontoInit() {
		IMessage stub = null;
		try {
			Registry registry = LocateRegistry.getRegistry(6666);
			stub=(IMessage)registry.lookup("rmi://localhost:6666/TORONTO/");  
			System.out.println(stub.addEvent("TORA101010", "SEMINAR", 3));;
			stub.addEvent("TORA111010", "SEMINAR", 2);
			stub.addEvent("TORA121010", "SEMINAR", 1);
			stub.addEvent("TORA131010", "TRADESHOW", 3);
			stub.addEvent("TORA141010", "TRADESHOW", 2);
			stub.addEvent("TORA151010", "TRADESHOW", 1);
			stub.addEvent("TORA161010", "CONFERENCE", 3);
			stub.addEvent("TORA171010", "CONFERENCE", 2);
			stub.addEvent("TORA181010", "CONFERENCE", 1);
			stub.bookEvent("MTLC1234", "TORA101010", "SEMINAR");
			stub.bookEvent("TORC1234", "TORA101010", "SEMINAR");
			stub.bookEvent("OTWC1234", "TORA101010", "SEMINAR");
			stub.bookEvent("TORC1234", "TORA111010", "SEMINAR");
			stub.bookEvent("TORC1234", "TORA121010", "SEMINAR");
			stub.bookEvent("OTWC1234", "TORA131010", "TRADESHOW");
			stub.bookEvent("TORC1234", "TORA151010", "TRADESHOW");
					
			
		} catch (RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void runMontrealInit() {
		IMessage stub =null;
		try {
			Registry registry = LocateRegistry.getRegistry(5555);
			stub=(IMessage)registry.lookup("rmi://localhost:5555/MONTREAL/");  
			System.out.println(stub.addEvent("MTLA101010", "SEMINAR", 3));;
			stub.addEvent("MTLA111010", "SEMINAR", 2);
			stub.addEvent("MTLA121010", "SEMINAR", 1);
			stub.addEvent("MTLA131010", "TRADESHOW", 3);
			stub.addEvent("MTLA141010", "TRADESHOW", 2);
			stub.addEvent("MTLA151010", "TRADESHOW", 1);
			stub.addEvent("MTLA161010", "CONFERENCE", 3);
			stub.addEvent("MTLA171010", "CONFERENCE", 2);
			stub.addEvent("MTLA181010", "CONFERENCE", 1);
			stub.bookEvent("MTLC1234", "MTLA101010", "SEMINAR");
			stub.bookEvent("TORC1234", "MTLA101010", "SEMINAR");
			stub.bookEvent("OTWC1234", "MTLA101010", "SEMINAR");
			stub.bookEvent("TORC1234", "MTLA111010", "SEMINAR");
//			stub.bookEvent("TORC1234", "MTLA101010", "SEMINAR");
			stub.bookEvent("OTWC1234", "MTLA131010", "TRADESHOW");
//			stub.bookEvent("TORC1234", "MTLA101015", "TRADESHOW");
			stub.swapEvent("TORC1234", "MTLA171010", "CONFERENCE", "MTLA101010", "SEMINAR");
					
			
		} catch (RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
