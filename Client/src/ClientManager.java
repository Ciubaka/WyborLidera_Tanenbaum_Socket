import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Time;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

public class ClientManager implements Runnable, Serializable{

    private static final String CONNECTION = "CONNECTION";
    private static final String DISCONNECT_FROM_CLIENT = "DISCONNECT_FROM_CLIENT";
    private static final String DISCONNECT_FROM_SERWER = "DISCONNECT_FROM_SERWER";
    private static final String CONNECTION_CONFIRM = "CONNECTION_CONFIRM";
    private static final String PING = "PING";
    private static final String PING_CONFIRM = "PING_CONFIRM";
    private static final String ELECTION = "ELECTION";
    private static int _PRIORITY = 0;
    private boolean disconnectFlag = false;

    private PrintWriter outSerwer;
    private BufferedReader inSerwer;


    private ObjectInputStream inObjClientAdmin;

    private Thread thread;
    private Socket clientAdmin;
    private Socket clientSerwer;

    PrintWriter outAdmin;
    public String info;

    public boolean running = true;




    protected LinkedHashSet<Clients> _clientsList = new LinkedHashSet<>();
    protected LinkedHashSet<Serwers> _serwersList = new LinkedHashSet<>();

    protected LinkedList<Serwers> lista = new LinkedList<>();



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


        _PRIORITY = PRIORITY;

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
            outSerwer.println(_PRIORITY);
            while (running){
                try {
                info = inSerwer.readLine();
                TimeUnit.SECONDS.sleep(4);
                    if(disconnectFlag)
                        info = DISCONNECT_FROM_CLIENT;

                        switch (info) {
                            case CONNECTION_CONFIRM:
                                logClient(CONNECTION_CONFIRM);
                                TimeUnit.SECONDS.sleep(4);
                                outSerwer.println(PING);
                                break;
                            case PING_CONFIRM:
                                logClient(PING_CONFIRM);
                                TimeUnit.SECONDS.sleep(4);

                                String tmp = inSerwer.readLine();
                                if (tmp.equals("NIC"))
                                    outSerwer.println(PING);
                                else {
                                    outSerwer.println(PING);
                                    command(tmp);
                                }
                                //outSerwer.println(DISCONNECT);
                                break;
                            case DISCONNECT_FROM_CLIENT:
                                logClient(DISCONNECT_FROM_CLIENT);
                                outSerwer.println(DISCONNECT_FROM_CLIENT);
                                inSerwer.close();
                                outSerwer.close();
                                clientSerwer.close();
                                running = false;
                                break;


                            case DISCONNECT_FROM_SERWER:
                                logClient(DISCONNECT_FROM_SERWER);
                                throw new IOException();


                            default:
                                logClient("ZLE");
                                break;
                        }



                } catch (IOException | InterruptedException e) {
                    running = false;
                    logClient(clientSerwer.isClosed());
                    try {
                            inSerwer.close();
                            outSerwer.close();
                            clientSerwer.close();

                            logClient(clientSerwer.isClosed());

                        createNewConnection();
                        //startElection();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }





                }

            }
            logClient("za whilem");



        }

        logClient("koniec");
        //metodaElction() -> w srodku nowy watek na kolejne polaczenie, wyslanie komunikatu!!
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


    public void startElection() throws IOException {
        for(Serwers serwers : _serwersList) //poporawivcna liste
        {
            Socket clientSerwer1 = new Socket(InetAddress.getByName(serwers.get_IP()), serwers.get_PORT());
            PrintWriter outSerwer1 =
                    new PrintWriter(clientSerwer1.getOutputStream(), true);
            outSerwer1.write(ELECTION);
            outSerwer1.write(_PRIORITY);

            outSerwer1.close();
            clientSerwer1.close();
        }



    }

    public void createNewConnection() {

//        for(int i = 0; i<lista.size(); i++){
//
//            try{
//            clientSerwer = new Socket(InetAddress.getByName(lista.get(i).get_IP()), lista.get(i).get_PORT());
//            inSerwer =
//                        new BufferedReader(
//                                new InputStreamReader(clientSerwer.getInputStream()));
//            outSerwer =
//                        new PrintWriter(clientSerwer.getOutputStream(), true);
//            }
//            catch (IOException e) {
//                continue;
//            }
//
//
//
//            outSerwer.println(CONNECTION);
//            outSerwer.println(_PRIORITY);
//            running = true;
//            break;
//
//        }


        for(Serwers s :_serwersList) {
            try {
                ///albo sprobowac strorzyc nowe obiekty

                clientSerwer = new Socket(InetAddress.getByName(s.get_IP()), s.get_PORT());
                inSerwer =
                        new BufferedReader(
                                new InputStreamReader(clientSerwer.getInputStream()));
                outSerwer =
                        new PrintWriter(clientSerwer.getOutputStream(), true);

                outSerwer.println(CONNECTION);
                outSerwer.println(_PRIORITY);
                running = true;
                break;

            } catch (IOException e) {
                e.printStackTrace();
            }

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
    public void logClient(boolean text){
        _loggerArea.append("\n" + text);
    }

    public void disconnectClient(){
        disconnectFlag = true;
    }

    private void command(String tmp){
        for(Serwers s :_serwersList){


            try {
                Socket clientSerwer2 = new Socket(InetAddress.getByName(s.get_IP()), s.get_PORT());
                PrintWriter outSerwer1 =
                        new PrintWriter(clientSerwer2.getOutputStream(), true);
                //dodawanie swojego numeru
                tmp += _PRIORITY;

                outSerwer1.println(tmp);

                outSerwer1.close();
                clientSerwer2.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            break;
        }
    }


}
