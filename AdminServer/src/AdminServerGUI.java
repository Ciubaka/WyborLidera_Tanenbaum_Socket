import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class AdminServerGUI {
    private static final String IPADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    private static final String CLIENTS_PATTERN = "^[0-9]$";

    private Pattern numberOfClientsPattern;
    private Matcher numberOfClientsMatcher;
    private String hostName;
    private JPanel mainPanel;
    private JTextArea adminLog;
    private JTextField adminIpField;
    private JTextField adminPortField;
    private JButton connectBtn;
    private JTextField numberOfClientsField;
    private JFrame frame;
    private int ADMIN_PORT = 12345;
    private String adminIp;
    private String adminPort;
    private String numberOfClients;

    private AdminServerManager adminServerManager;


    public AdminServerGUI() {

        numberOfClientsPattern = Pattern.compile(CLIENTS_PATTERN);
        //Inicjalizacja okna członka
        initLayout();
        //Inicjalizacja zdarzeń dla przycisków
        initEvents();
    }


    private void initLayout() {
        frame = new JFrame("ClientServerGUI");
        frame.setContentPane(mainPanel);
        int width = 550;
        int height = 650;
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

       // memberArea.setLayout(new BoxLayout(memberArea, BoxLayout.Y_AXIS));


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
        numberOfClients = numberOfClientsField.getText();


        if (!validateIp(numberOfClients)) {
            wrongNumber();
            return;
        }


        try {
            adminServerManager = new AdminServerManager(adminIp, Integer.parseInt(adminPort), adminLog, Integer.parseInt(numberOfClients));
            adminServerManager.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

            //operacje na GUI
        adminIpField.setEnabled(false);
        adminPortField.setEnabled(false);
        connectBtn.setEnabled(false);
        numberOfClientsField.setEnabled(false);

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

    public boolean validateIp(final String ip){
        numberOfClientsMatcher = numberOfClientsPattern.matcher(ip);
        return numberOfClientsMatcher.matches();
    }
    private void wrongNumber(){
        JOptionPane.showMessageDialog(frame, "Zły znak w polu liczby klientów! Dozwolony pojedyńczy znak 0-9!", "Błąd znaku!",JOptionPane.ERROR_MESSAGE );
    }
}
