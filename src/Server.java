import java.io.*;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.util.Scanner;

public class Server implements DNSlookup {
    public static int port=0;
	public static char method;
	public static String filename;
	public static int id=0;
	Hashtable<String, String> IP_table=new Hashtable<String, String>(100,(float)0.8);
    public Server() {}
	public DNSreply lookup(String hostname) {           
		DNSreply response;
		if (id == 1) 
		{              //if id==1 then we use server1 and Layer1.txt
            String[] tokens=hostname.split(".");
            if (tokens.length<3)
                throw new IllegalArgumentException();
            String l1=(tokens[0]+"."+tokens[1]).trim();
            String l2=tokens[2].trim();  // split the Input string into two segments, first half and second half.
            System.out.println(l1+"   "+l2);
            
            if (method=='I') 
                response=iterativeReponse( "Layer1.txt", l2, l1);
            else if (method=='R')
            {       // recursive way
                if (IP_table.containsKey(hostname)) 
                    response = new DNSreply(IP_table.get(hostname), "");
                else 
                {
                    response=iterativeReponse( "Layer1.txt", l2, l1);
                    try
                    {
                        Registry registry2 = LocateRegistry.getRegistry(response.address, port);
                        DNSlookup stub2 = (DNSlookup) registry2.lookup("DNSlookup");
                        DNSreply response2 = stub2.lookup(l1);
                        response = new DNSreply(response2.address, "");
                        IP_table.put(hostname, response.address);
                    }
                    catch (Exception e) 
                    {
                        e.printStackTrace();     
                    }
                }
			}
            else //input method is wrong
            {
                System.err.println("Input method is incorrect!");
                return null;
            }
		} else if (id == 2) {
		    response=iterativeReponse( "Layer2.txt", hostname, "");
		} else {
			System.err.println("The Layer id is incorrect");
			return null;
		}
		return response;
	}
	
	public DNSreply iterativeReponse( String filename, String startString,String returnHostName)
    {
            // iterative way
            File file = new File(filename);
            try {
                if (!file.isFile() || !file.exists())  //use BufferReader and Scanner to read string in input file
                    throw new IllegalArgumentException("The file can not be located! ");     
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String IPAddress="";
                for (String lineTxt = br.readLine();lineTxt != null;lineTxt = br.readLine()) 
                {
                    if (lineTxt.startsWith(startString))
                    {
                        Scanner s = new Scanner(lineTxt);
                        s.next();
                        IPAddress = s.next();
                        System.out.println(IPAddress);
                        s.close();
                    }
                }
                DNSreply response = new DNSreply(IPAddress,returnHostName);                   
                br.close();
                return response;
               }
            
             catch (Exception e) 
             {
                 System.err.println(e.toString());
                 return null;
             }
   
    }

	public static void main(String args[]) {

		try {
		    if (args.length != 4) 
		        throw new IllegalArgumentException("Instruction incorrect!!");
			method=args[0].charAt(0);
			if ((method!='I')&&(method!='R'))
			    throw new IllegalArgumentException("This method is not defined,please choose method between iterative and recursive!! ");
			filename = args[1];
			if(!filename.equals("Layer1.txt") && !filename.equals("Layer2.txt"))
			    throw new IllegalArgumentException("The input filename is illegal. Please choose a right filename.");
			id = Integer.valueOf(args[2]);
			if(id!=1 && id!=2)
			    throw new IllegalArgumentException("This id is incorrrect, please choose id between 1 and 2!! ");
			port = Integer.valueOf(args[3]);
		    }// check input instruction, if it is wrong , exit.
		catch (Exception e)
		{
		    System.err.println(e.toString());
		    System.exit(0);
		}

		try {
			// open a socket
			Socket s = new Socket("google.com", 80);
			System.setProperty("java.rmi.server.hostname", s.getLocalAddress()
					.getHostAddress());//use socket to get local IP, and call setProperty function to set the property of rmi
			s.close();
			Server obj = new Server();// initialize a new server object
			DNSlookup stub = (DNSlookup)UnicastRemoteObject.exportObject(obj,0);// create a stub for the object

			LocateRegistry.createRegistry(port);// create a registry
			Registry registry = LocateRegistry.getRegistry(port); // get this registry
			registry.bind("DNSlookup", stub);  // bind "DNSreply" with stub

			System.out.println("Server is ready!!!");// server is ready!
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}

}
