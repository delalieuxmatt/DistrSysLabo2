import java.rmi.*;
import java.rmi.server.*;
import java.util.Scanner;

public class ChatClient extends UnicastRemoteObject implements ChatClientInterface {
    private final String name;
    private final ChatInterface server;

    protected ChatClient(String name, ChatInterface server) throws RemoteException {
        this.name = name;
        this.server = server;
    }

    public void receiveMessage(String message) throws RemoteException {
        System.out.println(message);
    }

    static void main(String[] args) {
        try {
            Scanner sc = new Scanner(System.in);
            ChatInterface server = (ChatInterface) Naming.lookup("rmi://localhost/ChatServer");

            System.out.print("Enter username: ");
            String name = sc.nextLine();

            ChatClient client = new ChatClient(name, server);

            if (!server.register(name, client)) {
                System.out.println("❌ Username already in use. Try another one.");
                System.exit(0);
            }

            System.out.println("✅ Connected to chat! Type messages below:");
            while (true) {
                String msg = sc.nextLine();
                if (msg.equalsIgnoreCase("/quit")) break;
                server.broadcast(name, msg);
            }

            System.out.println("👋 Disconnected.");
            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
