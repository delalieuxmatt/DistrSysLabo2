import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class ChatClientGUI extends UnicastRemoteObject implements ChatClientInterface {
    private final ChatInterface server;
    private String name;
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;

    // Constructor
    protected ChatClientGUI(ChatInterface server) throws RemoteException {
        this.server = server;
        createUI();
    }

    // Create the GUI
    private void createUI() {
        frame = new JFrame("RMI Global Chat");
        frame.setSize(550, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        inputField = new JTextField();
        sendButton = new JButton("Send");

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        // Send on Enter or button click
        ActionListener sendAction = e -> sendMessage();
        sendButton.addActionListener(sendAction);
        inputField.addActionListener(sendAction);

        frame.setVisible(true);
    }

    // Handle sending messages
    private void sendMessage() {
        try {
            String msg = inputField.getText().trim();
            if (msg.isEmpty()) return;
            inputField.setText("");

            if (msg.equalsIgnoreCase("/quit")) {
                frame.dispose();
                System.exit(0);
            }
            else if (msg.equalsIgnoreCase("/users")) {
                List<String> users = server.getUsers();
                appendMessage("👥 Active users: " + String.join(", ", users));
            }
            else if (msg.startsWith("/msg ")) {
                String[] parts = msg.split(" ", 3);
                if (parts.length < 3) {
                    appendMessage("⚠️ Usage: /msg <username> <message>");
                    return;
                }
                server.sendPrivate(name, parts[1], parts[2]);
            }
            else if (msg.toLowerCase().startsWith("/group ")) {
                // /group GroupName (Tom, Jerry, Alice)
                String[] parts = msg.split(" ", 3);
                if (parts.length < 3) {
                    appendMessage("⚠️ Usage: /group <groupName> (<user1>,<user2>,...)");
                    return;
                }
                server.createGroup(parts[1], parts[2]);
                appendMessage("✅ Group '" + parts[1] + "' created with: " + parts[2]);
            }
            else if (msg.toLowerCase().startsWith("/gmsg ")) {
                // /gmsg GroupName Message
                String[] parts = msg.split(" ", 3);
                if (parts.length < 3) {
                    appendMessage("⚠️ Usage: /gmsg <groupName> <message>");
                    return;
                }
                server.sendGroupMessage(name, parts[1], parts[2]);
            }
            else {
                server.broadcast(name, msg);
            }

        } catch (Exception e) {
            appendMessage("⚠️ Error sending message: " + e.getMessage());
        }
    }

    // Server calls this remotely
    public void receiveMessage(String message) throws RemoteException {
        appendMessage(message);
    }

    // Add text to chat area safely
    private void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    // Startup entry point
    public static void main(String[] args) {
        try {
            ChatInterface server = (ChatInterface) Naming.lookup("rmi://localhost/ChatServer");

            String name = JOptionPane.showInputDialog(null, "Enter username:", "Connect to Chat", JOptionPane.QUESTION_MESSAGE);
            if (name == null || name.isBlank()) {
                System.out.println("Cancelled.");
                System.exit(0);
            }

            ChatClientGUI client = new ChatClientGUI(server);
            client.name = name;

            if (!server.register(name, client)) {
                JOptionPane.showMessageDialog(null, "❌ Username already in use. Try another one.", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }

            client.appendMessage("✅ Connected as " + name);
            client.appendMessage("💬 Commands:");
            client.appendMessage("   /users → list users");
            client.appendMessage("   /msg <username> <message> → private message");
            client.appendMessage("   /group <groupName> (<user1>,<user2>,...) → create group");
            client.appendMessage("   /gmsg <groupName> <message> → group message");
            client.appendMessage("   /quit → leave chat");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "❌ Could not connect to server:\n" + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
