import javax.swing.*;
import java.awt.color.ICC_ColorSpace;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class ServerManager implements Runnable {


    private JTextArea _serverLog;
    private ServerSocket serverSocket;
    private Thread thread;
    public boolean running = true;


    private SharedCommand sharedCommand = new SharedCommand();

    ClientHandlerServer clientHandlerServer;
    private ArrayList<Integer> listOfPriorityFromClients = new ArrayList<>();

    public Vector<ClientHandlerServer> get_clientHandlers() {
        return _clientHandlers;
    }

    private Vector<ClientHandlerServer> _clientHandlers = new Vector<ClientHandlerServer>();

    public ServerManager(String IP_SERWER, int PORT_SERWER, JTextArea serverLog) throws IOException {
        _serverLog = serverLog;
        serverSocket = new ServerSocket(PORT_SERWER,5, InetAddress.getByName(IP_SERWER));

    }



    @Override
    public void run() {
        while(running){
            try {
                Socket client = serverSocket.accept();


                set_serverLog("Liczba klintow: "+ _clientHandlers.size());
                set_serverLog("Is client exist: "+ sharedCommand.isClientExist());

                if(!sharedCommand.isClientExist()) {
                    _clientHandlers.removeElementAt(_clientHandlers.size()-1);
                    sharedCommand.setClientExist(true);
                }

                set_serverLog("Liczba klintow: "+ _clientHandlers.size());
                set_serverLog("Is client exist: "+ sharedCommand.isClientExist());


                clientHandlerServer = new ClientHandlerServer(client, _serverLog, sharedCommand,this);
                new Thread(clientHandlerServer).start();
                _clientHandlers.add(clientHandlerServer);

                set_serverLog("Liczba klintow: "+ _clientHandlers.size());



            } catch (IOException e) {
                running = false;
                //e.printStackTrace();
            }
        }

    }


    public void start() {
        thread = new Thread(this);
        thread.start();
//        thread.setName("DUUUUUUUUUUUUUUUUUUUUUUUUUUUPA");
//        set_serverLog(thread.isAlive());
//        set_serverLog(thread.isInterrupted());
//        Thread.getAllStackTraces().keySet().forEach((t) -> System.out.println(t.getName() + "\nIs Daemon " + t.isDaemon() + "\nIs Alive " + t.isAlive()));

    }

    public void disconnectServer(){
        if(_clientHandlers.size() > 0)
        clientHandlerServer.disconnectServer();
        running = false;

        try {
            TimeUnit.SECONDS.sleep(5);
            serverSocket.close();
            set_serverLog("Serwer wyłączony");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }



    public void set_serverLog(String text) {
        _serverLog.append("\n" + text);
    }
    public void set_serverLog(boolean text) {
        _serverLog.append("\n" + text);
    }
}
