package ClientGUI;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;



public class Nclient {

    String serveraddress;
    Scanner in;
    PrintWriter out;

    JFrame frame = new JFrame("Lan ChatRoom");
    
    JTextField textField = new JTextField(30);
    JTextArea messageArea = new JTextArea(40, 40);
    


    public Nclient(String serveraddress) {
        this.serveraddress = serveraddress;
        //The default textfield is not editable, it only becomes editable once it has been connected to the server.
        textField.setEditable(false);
        textField.setFont(new java.awt.Font("Times New Roman", 0, 14));
        textField.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        messageArea.setEditable(false);
        messageArea.setFont(new java.awt.Font("Times New Roman", 0, 15));

        ImageIcon img = new ImageIcon("icon.png");
        frame.setIconImage(img.getImage());
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.pack();

        //Clear the text holder for the next message.
        textField.addActionListener(e -> {
            out.println(textField.getText());
            textField.setText("Type here...");
        });
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Enter the server IP.");
        Scanner sc = new Scanner(System.in);
        String IP = sc.nextLine();
        var client = new Nclient(IP);
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }

    private String setName() {
        return JOptionPane.showInputDialog(frame, "choose a name:", "name selection", JOptionPane.PLAIN_MESSAGE);
    }

    private void run() throws IOException {
        try {
            var socket = new Socket(serveraddress, 59011);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);

            while (in.hasNextLine()) {
                var line = in.nextLine();
                if (line.startsWith("SEND_NAME")) {
                    out.println(setName());
                } else if (line.startsWith("ACCEPTED_NAME")) {
                    //Here you have to make the substring to cut the message from the server. If not, it would output MESSAGE "username".
                    this.frame.setTitle(line.substring(13));
                    textField.setEditable(true);
                } else if (line.startsWith("MESSAGE")) {
                    //Here you have to make the substring to cut the message from the server. If not, it would output MESSAGE "username".
                    messageArea.append(line.substring(7) + "\n");
                }
            }
        } finally {
            frame.setVisible(false);



            frame.dispose();
        }
    }
}