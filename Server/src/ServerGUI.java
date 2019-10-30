import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class ServerGUI {
    private static final String IPADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    private static final String PORT_PATTERN = "\\d+";
    private Pattern ipPattern;
    private Pattern portPattern;
    private Matcher matcherIP;
    private Matcher matcherPORT;
    private int intervalTime;
    private JFrame frame;
    private JPanel mainPanel;
    private JButton launch;
    private JTextField connServiceField;
    private JPanel launchedPanel;
    private JTextField connServerPort;
    private JTextArea callLog;
    private JTextField intervalField;
    private JButton disconnectBtn;
    //private JButton COS;


    private String connServiceIp;
    private String connServerPortLabel;

    ServerManager server ;


    public ServerGUI() {
        initLayout();
        initEvents();
        ipPattern = Pattern.compile(IPADDRESS_PATTERN);
        portPattern = Pattern.compile(PORT_PATTERN);

    }

    public void initEvents() {
        launch.addActionListener((new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                launch();
            }
              }));
        disconnectBtn.addActionListener((new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                disconnectServer();
            }
        }));

    }

    public void initLayout() {
        frame = new JFrame("Server");
        frame.setContentPane(mainPanel);

        int width = 450;
        int height = 520;
        frame.setSize(width, height);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        launchedPanel.setVisible(false);
        frame.setVisible(true);

        connServiceField.setText(getAddressThisMachine());
        connServiceField.setEnabled(false);
    }

    public void launch() {
        connServiceIp = connServiceField.getText();
        connServerPortLabel = connServerPort.getText();
        intervalTime = Integer.parseInt(intervalField.getText());
        if (!validateIp(connServiceIp, connServerPortLabel)) {
            wrongIpAddress();
            return;
        }

        try{
            server = new ServerManager(connServiceIp, Integer.parseInt(connServerPortLabel), callLog);
            server.start();
            log("Serwer dołączania gotowy..\nAdres serwera: " + connServiceIp + "\n");
            connServiceField.setEnabled(false);
            connServerPort.setEnabled(false);
            launch.setEnabled(false);
            launchedPanel.setVisible(true);
        } catch (Exception e) {
            System.out.println("Serwer nie wystartowal");
        }

    }


    public void disconnectServer(){
        server.disconnectServer();
    }



    public boolean validateIp(final String ip, final String port){

        matcherIP = ipPattern.matcher(ip);
        matcherPORT = portPattern.matcher(port);

        return matcherIP.matches() && matcherPORT.matches();
    }

    private void wrongIpAddress(){
        JOptionPane.showMessageDialog(frame, "Zły adres IP serwera dołączania.", "Błąd adresu IP",JOptionPane.ERROR_MESSAGE );
    }

    public void log(String text){
        callLog.append("\n" + text);
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
}
