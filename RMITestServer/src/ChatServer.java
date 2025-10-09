import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.*;

public class ChatServer extends UnicastRemoteObject implements ChatInterface {
    private final Map<String, ChatClientInterface> clients = new HashMap<>();

    protected ChatServer() throws RemoteException {
        super();
    }

    public synchronized boolean register(String name, ChatClientInterface client) throws RemoteException {
        if (clients.containsKey(name)) {
            return false;
        }
        clients.put(name, client);
        broadcast("Server", name + " joined the chat.");
        return true;
    }

    public synchronized void broadcast(String sender, String message) throws RemoteException {
        System.out.println(sender + ": " + message);
        for (ChatClientInterface c : clients.values()) {
            try {
                c.receiveMessage(sender + ": " + message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized List<String> getUsers() throws RemoteException {
        return new ArrayList<>(clients.keySet());
    }

    public synchronized void sendPrivate(String sender, String receiver, String message) throws RemoteException {
        ChatClientInterface receiverClient = clients.get(receiver);
        ChatClientInterface senderClient = clients.get(sender);

        if (receiverClient != null) {
            receiverClient.receiveMessage("[Private] " + sender + ": " + message);
            senderClient.receiveMessage("[Private to " + receiver + "] " + message);
        } else {
            senderClient.receiveMessage("❌ User '" + receiver + "' not found.");
        }
    }

    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
            ChatServer server = new ChatServer();
            Naming.rebind("rmi://localhost/ChatServer", server);
            System.out.println("Chat server is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
