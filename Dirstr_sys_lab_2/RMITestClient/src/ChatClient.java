import java.rmi.*;
import java.rmi.server.*;
import java.util.List;
import java.util.Scanner;

public class ChatClient extends UnicastRemoteObject implements ChatClientInterface {
    private final String name;
    private final ChatInterface server;

    protected ChatClient(String name, ChatInterface server) throws RemoteException {
        this.name = name;
        this.server = server;
    }

    // This method is called remotely by the server when a message is received
    public void receiveMessage(String message) throws RemoteException {
        System.out.println(message);
    }

    public static void main(String[] args) {
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
            System.out.println("💬 Use /msg <username> <message> for private messages.");
            System.out.println("💤 Type /quit to disconnect.");

            while (true) {
                String msg = sc.nextLine();
                if (msg.equalsIgnoreCase("/quit")) break;

                // Detect private message command
                if (msg.startsWith("/msg ")) {
                    String[] parts = msg.split(" ", 3);
                    if (parts.length < 3) {
                        System.out.println("⚠️ Usage: /msg <username> <message>");
                        continue;
                    }

                    String receiver = parts[1];
                    String message = parts[2];
                    server.sendPrivate(name, receiver, message);
                } else if (msg.equalsIgnoreCase("/users")) {
                    List<String> users = server.getUsers();
                    System.out.println("👥 Active users: " + String.join(", ", users));
                } else if (msg.toLowerCase().startsWith("/group ")) {
                    // /group GroupName (Tom, Jerry, Alice)
                    String[] parts = msg.split(" ", 3);
                    if (parts.length < 3) {
                        System.out.println("⚠️ Usage: /group <groupName> (<user1>,<user2>,...)");
                        continue;
                    }
                    String groupName = parts[1];
                    String members = parts[2];
                    server.createGroup(groupName, members);
                } else if (msg.toLowerCase().startsWith("/gmsg ")) {
                    // /gmsg GroupName Message
                    String[] parts = msg.split(" ", 3);
                    if (parts.length < 3) {
                        System.out.println("⚠️ Usage: /gmsg <groupName> <message>");
                        continue;
                    }
                    String groupName = parts[1];
                    String message = parts[2];
                    server.sendGroupMessage(name, groupName, message);
                } else {
                    // Normal broadcast message
                    server.broadcast(name, msg);
                }

            }

            System.out.println("👋 Disconnected.");
            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
