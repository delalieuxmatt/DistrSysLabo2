import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ChatInterface extends Remote {
    boolean register(String name, ChatClientInterface client) throws RemoteException;
    void broadcast(String sender, String message) throws RemoteException;
    List<String> getUsers() throws RemoteException;
}

