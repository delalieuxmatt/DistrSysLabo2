package Ex2;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 12345;


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new ChatClientGUI(SERVER_ADDRESS, PORT);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Unable to connect to server.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}

class ChatClientGUI extends JFrame {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private List<String> onlineUsers;

    private JComboBox<String> userSelector;
    private String selectedRecipient = "Global";
    private HashMap<String, List<String>> messages = new HashMap<>() {{
        put("Global", new ArrayList<>());
    }};


    ChatClientGUI(String serverAddress, int port) throws IOException {
        setTitle("Chat Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 800);
        setLocationRelativeTo(null);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        inputField = new JTextField();
        sendButton = new JButton("Send");


        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // NEW: Add user selector
        userSelector = new JComboBox<>(new String[]{"Global"});
        userSelector.addActionListener(e -> {
            selectedRecipient = (String) userSelector.getSelectedItem();
            SwingUtilities.invokeLater(() -> {
                chatArea.setText(String.join("", messages.getOrDefault(selectedRecipient, new ArrayList<>())));
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            });
        });
        inputPanel.add(userSelector, BorderLayout.WEST);

        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        setVisible(true);

        socket = new Socket(serverAddress, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        new Thread(this::readMessages).start();
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        String receiver = selectedRecipient.equals("Global") ? "Global" : selectedRecipient;
        if (!text.isEmpty()) {
            out.println(receiver + ":" + text);
            inputField.setText("");
            appendMessage(receiver, "Me: " + text);
        }

    }

    private void readMessages() {
        try {
            String response, group, message;
            while ((response = in.readLine()) != null) {
                if (response.startsWith("USERLIST")) {
                    String[] users = response.substring(9).split(",");
                    onlineUsers = new ArrayList<>(Arrays.asList(users));
                    if (!onlineUsers.contains("Global")) {
                        onlineUsers.add("Global");
                    }

                    SwingUtilities.invokeLater(() -> {
                        // Remove users that are no longer present
                        for (int i = userSelector.getItemCount() - 1; i >= 0; i--) {
                            String user = userSelector.getItemAt(i);
                            if (!onlineUsers.contains(user)) {
                                userSelector.removeItemAt(i);
                            }
                        }
                        // Add new users that are not already in the selector
                        for (String user : onlineUsers) {
                            boolean found = false;
                            for (int i = 0; i < userSelector.getItemCount(); i++) {
                                if (user.equals(userSelector.getItemAt(i))) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                userSelector.addItem(user);
                            }
                        }
                    });
                    continue;
                }
                System.out.println(response);
                String[] parts = response.split(":", 2);
                group = parts.length > 1 ? parts[0] : "Global";
                message = parts.length > 1 ? parts[1] : response;
                appendMessage(group, message);
            }
        } catch (IOException e) {
            appendMessage("Global", "Disconnected from server.");
        }
    }

    private void appendMessage(String group, String message) {
        messages.computeIfAbsent(group, k -> new ArrayList<String>());
        messages.get(group).add(message + "\n");
        SwingUtilities.invokeLater(() -> {
            chatArea.setText(String.join("", messages.getOrDefault(group, new ArrayList<>())));
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
}
