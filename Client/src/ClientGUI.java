import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.*;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class ClientGUI {
    private final String hostName;
    private static final String IPADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    private static final String PORT_PATTERN = "\\d+";
    private static final int ADMIN_PORT = 12345;
    private Pattern ipPattern;
    private Pattern ipPatternServer;
    private Pattern portPattern;
    private Matcher matcher;
    private Matcher matcherIP;
    private Matcher matcherPORT;


    private JPanel mainPanel;
    private JLabel hostLabel;
    private JTextField memberIpField;
    private JTextField serverIpFromClientField;
    private JTextField serverPortFromClientField;
    private JTextField clientPriorityField;
    private JButton connectBtn;
    private JButton disconnectBtn;
    private JButton checkMembersListBtn;
    private JPanel membersPanel;
    private JButton firstElectionBtn;
    private JPanel leaderPanel;
    private JScrollPane scrollPane;
    private JTextArea loggerArea;
    private JTextField adminPortBtn;
    private JTextField adminAddressBtn;
    private JButton connToAdminBtn;
    private JFrame frame;
    private ClientManager adminClient;

    private LinkedHashSet<Clients> _clientsListGUI = new LinkedHashSet<>();
    private LinkedHashSet<Serwers> _serwersList = new LinkedHashSet<>();


    public ClientGUI() throws UnknownHostException {
        hostName = InetAddress.getLocalHost().getHostName();
        ipPattern = Pattern.compile(IPADDRESS_PATTERN);
        ipPatternServer = Pattern.compile(IPADDRESS_PATTERN);
        portPattern = Pattern.compile(PORT_PATTERN);

        //Inicjalizacja okna członka
        initLayout();

        //Inicjalizacja zdarzeń dla przycisków
        initEvents();
    }

    private void initLayout() {
        frame = new JFrame("ClientGUI");
        frame.setContentPane(mainPanel);
        int width = 650;
        int height = 750;
        frame.setSize(width, height);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setVisible(true);
        memberIpField.setText(getAddressThisMachine());
        memberIpField.setEnabled(false);


        adminPortBtn.setText(String.valueOf(ADMIN_PORT));
        adminPortBtn.setEnabled(false);

        hostLabel.setText(hostName);
        leaderPanel.setVisible(false);

        membersPanel.setLayout(new BoxLayout(membersPanel, BoxLayout.Y_AXIS));

        disconnectBtn.setEnabled(false);
        checkMembersListBtn.setEnabled(false);

        firstElectionBtn.setEnabled(false);
    }


    private void initEvents() {

        connectBtn.addActionListener((new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                connect();
            }
        }));

        disconnectBtn.addActionListener((new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                disconnect();
            }
        }));

        checkMembersListBtn.addActionListener((new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setListOfMembers(null);
            }
        }));

        firstElectionBtn.addActionListener((new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                firstElection();
            }
        }));

    }

    private void connect() {
        String adminAddress = adminAddressBtn.getText();
        int adminPort = Integer.parseInt(adminPortBtn.getText());
        String serwerAddress = serverIpFromClientField.getText();
        int serwerPort = Integer.parseInt(serverPortFromClientField.getText());
        int clientPriority = Integer.parseInt(clientPriorityField.getText());

        /**
         * walidacja wprowadzanych danych
         */
//        if (!validateIp(adminAddress, String.valueOf(adminPort))) {
//            wrongIpAddress();
//            return;
//        }
//
//        if (!validateIp(serwerAddress, String.valueOf(serwerPort))) {
//            wrongIpAddress();
//            return;
//        }



        try {
            adminClient = new ClientManager(adminAddress, adminPort, serwerAddress, serwerPort, clientPriority, loggerArea);
            adminClient.startAdmin();
            TimeUnit.SECONDS.sleep(1);
            adminClient.startSerwer();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        //operacje na GUI
        hostLabel.setText(hostName + "(" + getAddressThisMachine() + ")");

        connectBtn.setEnabled(false);
        disconnectBtn.setEnabled(true);
        checkMembersListBtn.setEnabled(true);
        firstElectionBtn.setEnabled(true);

        adminAddressBtn.setEnabled(false);
        adminPortBtn.setEnabled(false);
        serverIpFromClientField.setEnabled(false);
        memberIpField.setEnabled(false);
        serverPortFromClientField.setEnabled(false);
        clientPriorityField.setEnabled(false);


    }


    private void disconnect() {
        adminClient.disconnectClient();
    }


    private void setListOfMembers(Clients leader) {


        _clientsListGUI.addAll(adminClient._clientsList);

        int poRRRT;
        String adRRRes;
        int prioRRIty;

        membersPanel.removeAll();
        JLabel label = new JLabel();
        JPanel ipsPanel = new JPanel();
        ipsPanel.setLayout(new GridLayout(((_clientsListGUI.size())), 3));


//        Iterator<Clients> iterator = _clientsList.iterator();
//        if(iterator.hasNext())
//            iterator.next().get_ip();
//        iterator.next().get_port();
//        iterator.next().get_priority();

        for (Clients ck : _clientsListGUI) {
            adRRRes = ck.get_ip();
            poRRRT = ck.get_port();
            prioRRIty = ck.get_priority();
            if (leader != null) {
                if (adRRRes.equals(leader.get_ip()) && poRRRT == leader.get_port() && prioRRIty == leader.get_priority()) {
                    label = new JLabel("Adres ip: " + adRRRes);
                    label.setForeground(Color.RED);
                    ipsPanel.add(label);

                    label = new JLabel("Priorytet: " + prioRRIty);
                    label.setForeground(Color.RED);
                    ipsPanel.add(label);

                    label = new JLabel("Port: " + poRRRT);
                    label.setForeground(Color.RED);
                    ipsPanel.add(label);
                } else {
                    label = new JLabel("Adres ip: " + adRRRes);
                    ipsPanel.add(label);

                    label = new JLabel("Priorytet: " + prioRRIty);
                    ipsPanel.add(label);

                    label = new JLabel("Port: " + poRRRT);
                    ipsPanel.add(label);
                }

            } else {
                label = new JLabel("Adres ip: " + adRRRes);
                ipsPanel.add(label);

                label = new JLabel("Priorytet: " + prioRRIty);
                ipsPanel.add(label);

                label = new JLabel("Port: " + poRRRT);
                ipsPanel.add(label);
            }

            }
            membersPanel.add(ipsPanel);
            membersPanel.getParent().invalidate();
            membersPanel.getParent().validate();

        }


        private void firstElection () {
            LinkedHashSet<Clients> _clientsListGUI222 = new LinkedHashSet<>();
            _clientsListGUI222.addAll(adminClient.getListOfClients());

            String adresLidera = "";
            int portLidera = 0;
            int prioryrtetLidera = 0;

            if (_clientsListGUI222.size() > 0) {
                if (_clientsListGUI222.size() == 1) {
                    portLidera = _clientsListGUI222.iterator().next().get_port();
                    adresLidera = _clientsListGUI222.iterator().next().get_ip();
                    prioryrtetLidera = _clientsListGUI222.iterator().next().get_priority();
                } else {
                    for (Clients lista : _clientsListGUI222) {
                        if (lista.get_priority() > prioryrtetLidera) {
                            prioryrtetLidera = lista.get_priority();
                            portLidera = lista.get_port();
                            adresLidera = lista.get_ip();
                            continue;
                        }
                        System.out.println("Mniejsze");
                    }
                }
            }
            Clients leader = new Clients(adresLidera, portLidera, prioryrtetLidera);


            String iplala = getAddressThisMachine();
            if (leader.get_priority() == Integer.parseInt(clientPriorityField.getText()) && leader.get_ip().equals(iplala)) {
                iAmLeader();
            } else {
                iAmNotLeader();
                //wyslij dalej do innych membersow
                //        //Map<Integer, String> daneDoPolaczenGUI = new TreeMap<>();
                //
            }
            setListOfMembers(leader);

        }


        private String getAddressThisMachine () {
            String iplala = null;
            try (final DatagramSocket socket = new DatagramSocket()) {
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                iplala = socket.getLocalAddress().getHostAddress();
            } catch (SocketException | UnknownHostException e) {
                e.printStackTrace();
            }
            return iplala;

        }

        public void iAmLeader () {
            leaderPanel.setVisible(true);
        }

        public void iAmNotLeader () {
            leaderPanel.setVisible(false);
        }

    public boolean validateIp(final String ip) {
        matcher = ipPattern.matcher(ip);
        return matcher.matches();
    }

    public boolean validateIp(final String ip, final String port){

        matcherIP = ipPattern.matcher(ip);
        matcherPORT = portPattern.matcher(port);

        return (matcherIP.matches() && matcherPORT.matches());
    }


    public void priorityExists(){
        JOptionPane.showMessageDialog(frame, "Wskazany priorytet jest już wykorzystywany!", "Błąd wartości priorytetu",JOptionPane.ERROR_MESSAGE );
    }

    public void wrongPriorityValue(){
        JOptionPane.showMessageDialog(frame, "Błędna wartość priorytetu!", "Błąd wartości priorytetu",JOptionPane.ERROR_MESSAGE );
    }

    public void wrongIpAddress() {
        JOptionPane.showMessageDialog(frame, "Błędny adres IP!", "Błąd adresu IP", JOptionPane.ERROR_MESSAGE);
    }




    }
