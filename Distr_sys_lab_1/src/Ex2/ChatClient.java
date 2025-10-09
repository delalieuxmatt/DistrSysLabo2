package Ex2;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 12345;

     public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new ChatClientGUI(SERVER_ADDRESS, PORT);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Unable to connect to server.", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}

class ChatClientGUI extends JFrame {
    private final JTextArea chatArea;
    private final JTextField inputField;
    private final JComboBox<String> userSelector;
    private final ConcurrentHashMap<String, List<String>> messages = new ConcurrentHashMap<>();
    private final PrintWriter out;
    private String selectedRecipient = "Global";
    private String username;

    ChatClientGUI(String serverAddress, int port) throws IOException {
        super("Chat Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 800);
        setLocationRelativeTo(null);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        inputField = new JTextField();
        JButton sendButton = new JButton("Send");

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        userSelector = new JComboBox<>(new String[]{"Global"});
        messages.put("Global", new ArrayList<>());
        userSelector.addActionListener(e -> {
            selectedRecipient = (String) userSelector.getSelectedItem();
            updateChatArea();
        });
        inputPanel.add(userSelector, BorderLayout.WEST);

        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        Socket socket = new Socket(serverAddress, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        handleUsernameSelection(in);

        setVisible(true);
        new Thread(() -> readMessages(in)).start();
    }

    private void handleUsernameSelection(BufferedReader in) throws IOException {
        while (true) {
            String serverResponse = in.readLine();
            if (serverResponse == null) throw new IOException("Connection lost during username selection.");

            if (serverResponse.equals("SUBMITNAME")) {
                String name = JOptionPane.showInputDialog(this, "Enter your username:", "Username", JOptionPane.PLAIN_MESSAGE);
                if (name == null || name.trim().isEmpty()) {
                    System.exit(0);
                }
                out.println(name);
            } else if (serverResponse.startsWith("NAMEACCEPTED:")) {
                this.username = serverResponse.substring(13);
                setTitle("Chat Client - " + this.username);
                break;
            } else if (serverResponse.equals("NAMEINUSE")) {
                JOptionPane.showMessageDialog(this, "Username is already in use.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (!text.isEmpty()) {
            if ("Global".equals(selectedRecipient)) {
                out.println("GLOBAL:" + text);
            } else {
                out.println("PRIVATE:" + selectedRecipient + ":" + text);
                appendMessage(selectedRecipient, "Me: " + text + "\n");
                updateChatArea();
            }
            inputField.setText("");
        }
    }

    private void readMessages(BufferedReader in) {
        try {
            String response;
            while ((response = in.readLine()) != null) {
                final String res = response;
                if (res.startsWith("USERLIST:")) {
                    updateOnlineUsers(res.substring(9));
                } else if (res.startsWith("PRIVATE:")) {
                    String[] parts = res.substring(8).split(":", 2);
                    String sender = parts[0];
                    String message = parts[1];
                    appendMessage(sender, sender + ": " + message + "\n");
                } else {
                    appendMessage("Global", res + "\n");
                }
                if (shouldUpdate(res)) {
                    updateChatArea();
                }
            }
        } catch (IOException e) {
            appendMessage("Global", "Disconnected from server.\n");
        }
    }

    private boolean shouldUpdate(String response) {
        if (response.startsWith("USERLIST:")) return false;
        if (selectedRecipient.equals("Global") && !response.startsWith("PRIVATE:")) return true;
        if (response.startsWith("PRIVATE:")) {
            String sender = response.substring(8).split(":", 2)[0];
            return selectedRecipient.equals(sender);
        }
        return false;
    }

    private void appendMessage(String conversation, String message) {
        messages.computeIfAbsent(conversation, k -> new ArrayList<>()).add(message);
    }

    private void updateChatArea() {
        SwingUtilities.invokeLater(() -> {
            chatArea.setText(String.join("", messages.getOrDefault(selectedRecipient, new ArrayList<>())));
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    private void updateOnlineUsers(String userList) {
        SwingUtilities.invokeLater(() -> {
            String[] users = userList.split(",");
            String selected = (String) userSelector.getSelectedItem();

            userSelector.removeAllItems();
            userSelector.addItem("Global");
            Arrays.stream(users)
                    .filter(u -> !u.isEmpty() && !u.equals(this.username))
                    .forEach(userSelector::addItem);

            if (selected != null) {
                userSelector.setSelectedItem(selected);
            }
        });
    }
}
