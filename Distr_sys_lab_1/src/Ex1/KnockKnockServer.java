package Ex1;

import java.net.*;
import java.io.*;

public class KnockKnockServer {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: java Ex1.KnockKnockServer <port number>");
            System.exit(1);
        }
        int portNumber = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(portNumber);
             Socket clientSocket = serverSocket.accept();
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(clientSocket.getInputStream()))) {

            String inputLine, outputLine;

            KnockKnockProtocol kkp = new KnockKnockProtocol();
            outputLine = kkp.processInput(null);  // start the conversation
            out.println(outputLine);

            while ((inputLine = in.readLine()) != null) {
                outputLine = kkp.processInput(inputLine);
                out.println(outputLine);
                if (outputLine.equals("Bye."))
                    break;
            }
        } catch (IOException e) {
            System.err.println("Exception caught when trying to listen on port " + portNumber);
            System.err.println(e.getMessage());
        }
    }
}
