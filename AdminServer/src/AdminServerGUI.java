import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class AdminServerGUI {
    private String hostName;
    private JPanel mainPanel;
    private JTextArea adminLog;
    private JTextField adminIpField;
    private JTextField adminPortField;
    private JButton connectBtn;
    private JPanel memberArea;
    private JFrame frame;
    private int ADMIN_PORT = 12345;
    private String adminIp;
    private String adminPort;
    private AdminServerManager adminServerManager;


    public AdminServerGUI() {

        //Inicjalizacja okna członka
        initLayout();
        //Inicjalizacja zdarzeń dla przycisków
        initEvents();
    }


    private void initLayout() {
        frame = new JFrame("ClientServerGUI");
        frame.setContentPane(mainPanel);
        int width = 950;
        int height = 550;
        frame.setSize(width, height);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setVisible(true);
        adminIpField.setText(getAddressThisMachine());
        adminIpField.setEnabled(false);
        adminPortField.setText(String.valueOf(ADMIN_PORT));
        adminPortField.setEnabled(false);
        connectBtn.setEnabled(true);

        memberArea.setLayout(new BoxLayout(memberArea, BoxLayout.Y_AXIS));


    }

    private void initEvents() {
        connectBtn.addActionListener((new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                connect();
            }
        }));

    }

    private void connect(){
        adminIp = adminIpField.getText();
        adminPort = adminPortField.getText();

/**
 * ZROBIC WALIDACJE!!!!
 */
//        if (!validateIp(serverIP, serverPort)) {
//            wrongIpAddress();
//            return;
//        }


        try {
            adminServerManager = new AdminServerManager(adminIp, Integer.parseInt(adminPort), adminLog);
            adminServerManager.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

            //operacje na GUI
        adminIpField.setEnabled(false);
        adminPortField.setEnabled(false);
        connectBtn.setEnabled(false);

    }


    private String getAddressThisMachine() {
        String iplala = null;
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            iplala = socket.getLocalAddress().getHostAddress();
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
        return iplala;
    }

    public void logServer(String text){
        adminLog.append("\n" + text);
    }
    public void logServer(int text){
        adminLog.append("\n" + text);
    }


}
