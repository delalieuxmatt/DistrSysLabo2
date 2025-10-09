package Ex2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Server {
    private static final int PORT_NUMBER = 12345;
    private static final Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());
    private static final ConcurrentHashMap<String, ClientHandler> clientHandlerMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT_NUMBER)) {
            System.out.println("Server is listening on port " + PORT_NUMBER);
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
        }
    }

    static void broadcast(String message, ClientHandler exclude) {
        synchronized (clients) {
            clients.stream()
                    .filter(client -> client != exclude)
                    .forEach(client -> client.sendMessage(message));
        }
    }

    static String getUserListString() {
        return "USERLIST:" + String.join(",", clientHandlerMap.keySet());
    }

    static void broadcastUserList() {
        broadcast(getUserListString(), null);
    }

    static synchronized boolean addClient(String username, ClientHandler handler) {
        if (username == null || username.trim().isEmpty() || "Global".equalsIgnoreCase(username.trim()) || clientHandlerMap.containsKey(username)) {
            return false;
        }
        clients.add(handler);
        clientHandlerMap.put(username, handler);
        return true;
    }

    static void removeClient(ClientHandler client) {
        if (client.getUsername() != null && clientHandlerMap.remove(client.getUsername(), client)) {
            clients.remove(client);
            broadcast("User " + client.getUsername() + " has left.", null);
            broadcastUserList();
        }
    }

    static void privateMessage(String sender, String receiver, String message) {
        ClientHandler recipientHandler = clientHandlerMap.get(receiver);
        if (recipientHandler != null) {
            recipientHandler.sendMessage("PRIVATE:" + sender + ":" + message);
        }
    }
}

class ClientHandler extends Thread {
    private final Socket socket;
    private PrintWriter out;
    private String username;

    ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public String getUsername() {
        return username;
    }

    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            this.out = out;

            // Step 1: Username negotiation loop
            while (true) {
                out.println("SUBMITNAME");
                String name = in.readLine();
                if (name == null) return;

                if (Server.addClient(name, this)) {
                    this.username = name;
                    out.println("NAMEACCEPTED:" + this.username);
                    break;
                }
                out.println("NAMEINUSE");
            }

            // Step 2: Inform new client and existing clients in the correct order
            System.out.println("User " + username + " has joined.");

            // Send the full user list ONLY to the newly connected client
            out.println(Server.getUserListString());

            // Inform all OTHER clients that a new user has joined
            Server.broadcast("User " + username + " has joined.", this);

            // Broadcast the updated user list to all OTHER clients
            Server.broadcast(Server.getUserListString(), this);


            // Step 3: Main message processing loop
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("GLOBAL:")) {
                    Server.broadcast(username + ": " + message.substring(7), null);
                } else if (message.startsWith("PRIVATE:")) {
                    String[] parts = message.substring(8).split(":", 2);
                    if (parts.length == 2) {
                        Server.privateMessage(username, parts[0], parts[1]);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Client " + (username != null ? username : "[unknown]") + " disconnected.");
        } finally {
            Server.removeClient(this);
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}
