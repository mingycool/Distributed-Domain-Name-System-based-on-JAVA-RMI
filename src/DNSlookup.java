import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DNSlookup extends Remote {
	
    DNSreply lookup (String hostname) throws RemoteException;

}
