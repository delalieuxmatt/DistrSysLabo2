import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ChatInterface extends Remote {
    boolean register(String name, ChatClientInterface client) throws RemoteException;
    void broadcast(String sender, String message) throws RemoteException;
    List<String> getUsers() throws RemoteException;
    void sendPrivate(String sender, String receiver, String message) throws RemoteException;
    void createGroup(String groupName, String names) throws RemoteException;
    void sendGroupMessage(String sender, String groupName, String message) throws RemoteException;


}

