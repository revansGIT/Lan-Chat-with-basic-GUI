package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Executors;

public class Nserver {

    //HashSet to avoid duplicate names.
    private static final Set<String> listNAMES = new HashSet<>();

    //Printwriters for each client so it can be used for broadcast.
    private static final Set<PrintWriter> printWriterHashSet = new HashSet<>();

    public static void main(String[] args) throws Exception {

        System.out.println("Server Running.");
        var pool = Executors.newFixedThreadPool(500);
        try (var listener = new ServerSocket(59011)) {
            while (true) pool.execute(new Handler(listener.accept()));
        }
    }

    private static class Handler implements Runnable {
        private String username;
        private final Socket socket;
        private Scanner in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);

                //Repeatedly ask for the username.
                while (true) {
                    out.println("SEND_NAME");
                    username = in.nextLine();
                    if (username == null) {
                        return;
                    }
                    //We check that the name is not repeated and we add it to hashset.
                    synchronized (listNAMES) {
                        if (!username.isBlank() && !listNAMES.contains(username)) {
                            listNAMES.add(username);
                            break;
                        }
                    }
                }

                //Once we have the client's name accepted, we have to give it its own
                //PrintWriter but first you have to notify all users that someone
                //has joined.
                out.println("ACCEPTED_NAME" + username);
                printWriterHashSet.forEach(writer -> writer.println("\nMESSAGE" + username + " has joined the room.\n\n"));
                printWriterHashSet.add(out);

                //Accept messages from the client and broadcast the message.
                while (true) {
                    String input = in.nextLine();
                    //If the message begins with /leave the client is disconnected.
                    if (input.toLowerCase().startsWith("/leave")) return;
                    printWriterHashSet.forEach(writer -> writer.println("MESSAGE" + username + ">> " + input));
                }
            } catch (Exception e) {
                System.out.println();
            } finally {

                if (out != null) {
                    printWriterHashSet.remove(out);
                }
                if (username != null) {
                    System.out.println(username + " has left the room.");
                    listNAMES.remove(username);
                    printWriterHashSet.forEach(writer -> writer.println("\n\nMESSAGE" + username + " has gone.\n\n"));
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("The server could not be shut down properly.");
                }
            }
        }
    }
}