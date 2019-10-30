import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerManager implements Runnable {


    private JTextArea _serverLog;
    private ServerSocket serverSocket;
    private Thread thread;
    ClientHandlerServer clientHandlerServer;

    public ServerManager(String IP_SERWER, int PORT_SERWER, JTextArea serverLog) throws IOException {
        _serverLog = serverLog;
        serverSocket = new ServerSocket(PORT_SERWER,5, InetAddress.getByName(IP_SERWER));

    }



    @Override
    public void run() {
        while(true){
            try {
                Socket client = serverSocket.accept();
                _serverLog.append(String.valueOf(client.getPort()));

                clientHandlerServer = new ClientHandlerServer(client, _serverLog);
                new Thread(clientHandlerServer).start();






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
}
