import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;


public class AdminServerManager implements Runnable, Serializable{
    private ServerSocket _serverSocket;
    private JTextArea _adminlog;
    private InetAddress _IP_ADMIN;
    private int _initialNumberOfClients;
    private LinkedHashSet<Clients> _clientsList = new LinkedHashSet<>();
    private LinkedHashSet<Serwers> _serwersList = new LinkedHashSet<>();

    private LinkedList<Clients> _clientsList1 = new LinkedList<>();
    private LinkedList<Serwers> _serwersList1 = new LinkedList<>();


    private Vector<Socket> _clients = new Vector<Socket>();
    private Vector<ClientHandler> _clientHandlers = new Vector<ClientHandler>();
    private Thread thread;


    public AdminServerManager(String IP_ADMIN, int PORT_ADMIN, JTextArea adminlog, int initialNumberOfClients) throws IOException {
        _IP_ADMIN = InetAddress.getByName(IP_ADMIN);
        _serverSocket = new ServerSocket(PORT_ADMIN, 5, _IP_ADMIN);
        _adminlog = adminlog;
        logServer("Odpalony serwer, adres: " + _serverSocket.getInetAddress().getHostAddress() + ", na porcie: "+ _serverSocket.getLocalPort());
        _initialNumberOfClients = initialNumberOfClients;
    }



    @Override
    public void run() {

        while (true)
        {
            try {

                Socket client = _serverSocket.accept();
                BufferedReader in =
                        new BufferedReader(
                                new InputStreamReader(client.getInputStream()));

                /**
                 * OBIÓR DANYCH OD KLIENTA
                 */
                int priority = Integer.parseInt(in.readLine());
                int portServer = Integer.parseInt(in.readLine());
                String ipServer = in.readLine();


                /**
                 * ZAPIS DANYCH O KLIENTACH, SERWERACH I SOCKETACH DO LIST
                 */
                //_serwersList.add(new Serwers(ipServer, portServer));
                _serwersList1.add(new Serwers(ipServer, portServer));
               ///////_clientsList.add(new Clients(client.getInetAddress().getHostAddress(), client.getPort(), priority));
               _clientsList1.add(new Clients(client.getInetAddress().getHostAddress(), client.getPort(), priority));
               _clients.add(client);
                logServer("Liczba serwerów: " + _serwersList1.size() + "\nLiczba klientów: " + _clientsList1.size() + "\nLiczba socketów: " + _clients.size() );

                ClientHandler clientHandler = new ClientHandler(client);
                _clientHandlers.add(clientHandler);
                //new Thread(clientHandler).start();


                /**
                 * OGARNAC TUTAJ ZEBY DO KAZDEGO SIE WYSYLALO A NIE RAZ PO OKRESLONEJ LICZBIE DOLACZONYCH KLIENTOW!!!!
                 */
                int numberInList = 0;
                if(_clientHandlers.size() == _initialNumberOfClients) {
                    for (ClientHandler cl : _clientHandlers) {
                        cl.outObjClient.writeObject(_serwersList1);
                        cl.outObjClient.writeObject(_clientsList1);
                        cl.outObjClient.writeObject(numberInList);
                        numberInList++;
                        cl.outObjClient.flush();
                        Thread.currentThread();
                        Thread.sleep(1 * 1000);
                    }
                }

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        }

    }
    
    
    public void start(){
        thread = new Thread(this);
        thread.start();
    }

    public void logServer(String text){
        _adminlog.append("\n" + text);
    }
    public void logServer(int text){
        _adminlog.append("\n" + text);
    }

}
