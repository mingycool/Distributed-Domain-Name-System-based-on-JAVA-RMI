import java.io.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Client {
	private Client() {}
	public final static String outFileName="Output.txt"; // This string is used to store output file's name

	public static void main(String[] args) {
		
		    String ip = (args.length < 1) ? null : args[0];
			int port = Integer.valueOf(args[1]);
			String inputFileName = args[2];
		
			File inputFile = new File(inputFileName);
			String DNSName = "";
			String notFound = "Not Found!!!";

			try {
				Registry registry = LocateRegistry.getRegistry(ip, port);
				DNSlookup stub = (DNSlookup) registry.lookup("DNSlookup");
                                
                PrintWriter outputfile=new PrintWriter (outFileName);;
                  
				Scanner scanner = new Scanner(inputFile);
				while (scanner.hasNextLine()) 
				{
					DNSName = scanner.nextLine();
					DNSreply response = stub.lookup(DNSName);

					if (!response.address.equals("")&& !response.hostname.equals("")) 
					    {      // In this situation, it required to access Server 2.												
						Registry registry2 = LocateRegistry.getRegistry(
								response.address, port);
						DNSlookup stub2 = (DNSlookup) registry2
								.lookup("DNSlookup");
						DNSreply response2 = stub2
								.lookup(response.hostname);
						if (!response2.address.equals("")) {
							outputfile.printf("%-30s%s\n",DNSName,response2.address);
							System.out.printf("%-30s%s\n",DNSName,response2.address);
						} else {
							outputfile.printf("%-30s%s\n",DNSName,notFound);
							System.out.printf("%-30s%s\n",DNSName,notFound);
						}
					} else if (response.address.equals("")) {
						outputfile.printf("%-30s%s\n",DNSName,notFound);
						System.out.printf("%-30s%s\n",DNSName,notFound);
					} else {
						outputfile.printf("%-30s%s\n",DNSName,response.address);
						System.out.printf("%-30s%s\n",DNSName,response.address);
					}
				}
				scanner.close();
				outputfile.close();
				System.out.println("The result has been writen to Output.txt!");
				System.out.println("Please input 'cat Output.txt' to check the result!");
                System.exit(0);
			} catch (Exception e) {
				System.err.println("Client exception: " + e.toString());
				e.printStackTrace();
			}
		

	}
}
