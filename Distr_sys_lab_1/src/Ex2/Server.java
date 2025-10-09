package Ex2;

import java.io.*;
import java.util.*;
import java.net.*;

public class Server {

    private static final Set<String> usernames = new HashSet<>();
    private static final Set<ClientHandler> clients = new HashSet<>();
    private static final Set<PrintWriter> writers = new HashSet<>();
    private static final int portNumber = 12345;
    private static final HashMap<String, PrintWriter> onlineWriters = new HashMap<>();
    private static final HashMap<String, String> publicChat = new HashMap<>();


    public static void main(String[] args) throws IOException {

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Server is listening on port " + portNumber);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                ClientHandler handler = new ClientHandler(socket);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Exception caught when trying to listen on port " + portNumber);
            System.err.println(e.getMessage());
        }
    }

    static void broadcast(String message, ClientHandler exclude) {
        for (ClientHandler client : clients) {
            if (client != exclude) {
                client.sendMessage(message);
            }
        }
    }

    static synchronized boolean addUsername(String username) {
        return usernames.add(username);

    }

    public static synchronized Set<String > getUsernamesSet() {
        return usernames;
    }

    static void printUserList() {
        StringBuilder sb = new StringBuilder("USERLIST ");
        for (String username : usernames) {
            sb.append(username).append(",");
        }
        System.out.println(sb.toString());
        broadcast(sb.toString(), null);
    }

    static synchronized void removeClient(ClientHandler client) {
        usernames.remove(client.getUsername());
        clients.remove(client);
        broadcast("User left: " + client.getUsername(), null);
        printUserList();
    }

    static synchronized void addClient(ClientHandler client) {
        clients.add(client);
        printUserList();
    }
    static synchronized void addWriter(PrintWriter writer) {
        writers.add(writer);
    }


    static synchronized Set<String> getUsernames() {
        return usernames;
    }
    public static synchronized HashMap<String, PrintWriter> getOnlineWriters(){
        return onlineWriters;
    }

    public synchronized static void addOnlineWriter(String userName, PrintWriter writer){
        onlineWriters.put(userName, writer);
        System.out.println(onlineWriters);
    }

    public synchronized static void removeOnlineWriter(PrintWriter writer, String userName){
        onlineWriters.remove(userName);

    }

    public synchronized static void privateMessage(String sender, String receiver, String message) {
        String fullMessage = sender + ": " + message;
        for (ClientHandler rec : clients) {
            if (rec.getUsername().equals(receiver)){
                rec.sendMessage(fullMessage);
            }
        }

    }
}

class ClientHandler implements Runnable {
    private final Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private PrintWriter currentConvo;

    ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public String getUsername() {
        return username;
    }

    public void run() {
        String receiver;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Ask for username
            while (true) {
                out.println("Submit your username:");
                username = in.readLine();
                if (username == null) return;
                username = username.replace("Global:", "");
                synchronized (Server.class) {
                    if (Server.addUsername(username)) {
                        break;
                    }
                }
                out.println("The name you chose is already taken. Please choose another one.");
            }
            Server.addOnlineWriter(username, out);
            Server.addWriter(out);
            Server.addClient(this);
            out.println("You joined the global chat " + username);
            Server.broadcast("User joined: " + username, this);

            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("USERLIST")) {
                    Server.printUserList();
                    continue;
                }
                if (message.startsWith("Global:")) {
                    message = message.replace("Global:", "");
                    Server.broadcast(username + ": " + message, this);
                    continue;
                }
                String parts[] = message.split(":", 2);
                receiver = parts[0];
                message = parts[1];
                Server.privateMessage(getUsername(), receiver, message);
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            Server.removeClient(this);
            try { socket.close(); } catch (IOException e) {}
        }
    }

    void sendMessage(String message) {
        out.println(message);
    }



}
