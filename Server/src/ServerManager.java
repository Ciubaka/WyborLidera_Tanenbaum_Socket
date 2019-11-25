import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;

public class ServerManager implements Runnable {


    private JTextArea _serverLog;
    private ServerSocket serverSocket;
    private Thread thread;


    private SharedCommand sharedCommand = new SharedCommand();

    ClientHandlerServer clientHandlerServer;
    private ArrayList<Integer> listOfPriorityFromClients = new ArrayList<>();
    private Vector<ClientHandlerServer> _clientHandlers = new Vector<ClientHandlerServer>();

    public ServerManager(String IP_SERWER, int PORT_SERWER, JTextArea serverLog) throws IOException {
        _serverLog = serverLog;
        serverSocket = new ServerSocket(PORT_SERWER,5, InetAddress.getByName(IP_SERWER));

    }



    @Override
    public void run() {
        while(true){
            try {
                Socket client = serverSocket.accept();


                set_serverLog("Liczba klintow: "+ _clientHandlers.size());
                set_serverLog("Is client exist: "+ sharedCommand.isClientExist());

                if(!sharedCommand.isClientExist()) {
                    _clientHandlers.removeAllElements();
                    sharedCommand.setClientExist(true);
                }

                set_serverLog("Liczba klintow: "+ _clientHandlers.size());
                set_serverLog("Is client exist: "+ sharedCommand.isClientExist());


                clientHandlerServer = new ClientHandlerServer(client, _serverLog, sharedCommand);
                new Thread(clientHandlerServer).start();
                _clientHandlers.add(clientHandlerServer);

                set_serverLog("Liczba klintow: "+ _clientHandlers.size());

//                for (ClientHandlerServer cl : _clientHandlers) {
//                    if(cl.getPRIORITY() != 0)
//                    listOfPriorityFromClients.add(cl.getPRIORITY());
//                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    public void start() {
        thread = new Thread(this);
        thread.start();
    }

    public void disconnectServer(){
        clientHandlerServer.disconnectServer();

    }


    public void set_serverLog(String text) {
        _serverLog.append("\n" + text);
    }
}
