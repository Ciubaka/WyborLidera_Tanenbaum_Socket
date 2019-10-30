import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Time;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

public class ClientManager implements Runnable, Serializable{

    private static final String CONNECTION = "CONNECTION";
    private static final String DISCONNECT = "DISCONNECT";
    private static final String CONNECTION_CONFIRM = "CONNECTION_CONFIRM";
    private static final String PING = "PING";
    private static final String PING_CONFIRM = "PING_CONFIRM";

    private PrintWriter outSerwer;
    private BufferedReader inSerwer;


    private ObjectInputStream inObjClientAdmin;

    private Thread thread;
    private Socket clientAdmin;
    private Socket clientSerwer;

    PrintWriter outAdmin;
    public String info;




    protected LinkedHashSet<Clients> _clientsList = new LinkedHashSet<>();
    protected LinkedHashSet<Serwers> _serwersList = new LinkedHashSet<>();
    private boolean isAdmin;
    private JTextArea _loggerArea;

    public ClientManager(String IP_ADMIN, int PORT_ADMIN, String IP_SERWER, int PORT_SERWER, int PRIORITY, JTextArea loggerArea) throws IOException {
        _loggerArea = loggerArea;

        /**
         * DO SERWERA ADMINISTRUJACEGO
         */
        clientAdmin = new Socket(InetAddress.getByName(IP_ADMIN), PORT_ADMIN);
        outAdmin =
                new PrintWriter(clientAdmin.getOutputStream(), true);

        outAdmin.println(PRIORITY);
        outAdmin.println(PORT_SERWER);
        outAdmin.println(IP_SERWER);
        inObjClientAdmin = new ObjectInputStream(clientAdmin.getInputStream());

        /**
         * DO SERWERA=CLIENTA
         */
        clientSerwer = new Socket(InetAddress.getByName(IP_SERWER), PORT_SERWER);
        inSerwer =
                new BufferedReader(
                        new InputStreamReader(clientSerwer.getInputStream()));
        outSerwer =
                new PrintWriter(clientSerwer.getOutputStream(), true);


    }

    @Override
    public void run() {
        
        if(isAdmin) {

            while (true) {
            LinkedHashSet<Serwers> serwery = new LinkedHashSet<Serwers>();
            LinkedHashSet<Clients> klienci = new LinkedHashSet<Clients>();
            try {
                serwery = (LinkedHashSet<Serwers>) inObjClientAdmin.readObject();
                klienci = (LinkedHashSet<Clients>) inObjClientAdmin.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
                System.out.println("Liczba serweow: " + serwery.size() + " l. klientow: " + klienci.size());
            if(serwery.size() > 0 && klienci.size() > 0) {
                _serwersList.addAll(serwery);
                _clientsList.addAll(klienci);
            }
                System.out.println(_serwersList.size());
            System.out.println(_clientsList.size());

            outAdmin.println("OTRZYMALEM");

            }
        }
        else
        {
            outSerwer.println(CONNECTION);

            while (true){
                try {
                info = inSerwer.readLine();
                TimeUnit.SECONDS.sleep(4);
                    switch (info)
                    {
                        case CONNECTION_CONFIRM:
                            logClient(CONNECTION_CONFIRM);
                            TimeUnit.SECONDS.sleep(4);
                            outSerwer.println(PING);
                            break;
                        case PING_CONFIRM:
                            logClient(PING_CONFIRM);
                            TimeUnit.SECONDS.sleep(4);
                            outSerwer.println(PING);
                            //outSerwer.println(DISCONNECT);
                            break;
                        case DISCONNECT:
                            logClient(DISCONNECT);
                            TimeUnit.SECONDS.sleep(4);
                            //create method with tanenbaum algoritm
                            startElection();
                            break;

                        default:
                            logClient("ZLE");
                            break;
                    }


                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
            

    }

public void startAdmin(){
        isAdmin = true;
        thread = new Thread(this);
        thread.start();
    }
    
    public void startSerwer(){
        isAdmin = false;
        thread = new Thread(this);
        thread.start();
    }


    public void startElection(){
        for(Serwers serwers : _serwersList)
        {
            System.out.println(serwers.get_IP() + " " + serwers.get_PORT());
        }

    }
    
    


    public LinkedHashSet<Clients> getListOfClients() {
        return _clientsList;
    }

    public void logClient(String text){
        _loggerArea.append("\n" + text);
    }

    public void logClient(int text){
        _loggerArea.append("\n" + text);
    }

    public void disconnectClient(){
        info = DISCONNECT;
    }


}
