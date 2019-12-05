import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.security.PublicKey;
import java.sql.Time;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class ClientHandlerServer implements Runnable {
    private PrintWriter outP;
    private BufferedReader in;
    private final Socket _socket;
    private JTextArea _serverLog;
    private static final String CONNECTION = "CONNECTION";
    //private static final String DISCONNECT = "DISCONNECT";

    private static final String DISCONNECT_FROM_CLIENT = "DISCONNECT_FROM_CLIENT";
    private static final String DISCONNECT_FROM_SERWER = "DISCONNECT_FROM_SERWER";
    private static final String CONNECTION_CONFIRM = "CONNECTION_CONFIRM";
    private static final String PING = "PING";
    private static final String PING_CONFIRM = "PING_CONFIRM";
    private static final String ELECTION = "ELECTION";
    private static final String COORDINATOR = "COORDINATOR";
    private static final String NEW_CONNECTION = "NEW_CONNECTION";
    private static final String ALTERNATIVE_CONNECTION = "ALTERNATIVE_CONNECTION";
    public String info;
    private int PRIORITY;
    public boolean running = true;
    private boolean disconnectFlag = false;
    private ArrayList<Integer> arrayList = new ArrayList<>();

    private String kindOfConnection;
    private SharedCommand _sharedCommand;
    private ServerManager _serverManager;


    public ClientHandlerServer(Socket socket, JTextArea serverLog, SharedCommand sharedCommand, ServerManager serverManager) throws IOException {
        _socket = socket;
        _serverLog = serverLog;
        in =
                new BufferedReader(
                        new InputStreamReader(_socket.getInputStream()));
        outP =
                new PrintWriter(_socket.getOutputStream(), true);

        _sharedCommand = sharedCommand;
        _serverManager = serverManager;
    }

    public int getPRIORITY() {
        return PRIORITY;
    }

    @Override
    public void run() {
        while (running) {
            try {
                info = in.readLine();

                TimeUnit.SECONDS.sleep(4);

                if(disconnectFlag)
                    info = DISCONNECT_FROM_SERWER;

                switch (info) {
                    case CONNECTION:
                        PRIORITY = Integer.parseInt(in.readLine());
                        kindOfConnection = in.readLine();
                        if(kindOfConnection.equals(ALTERNATIVE_CONNECTION) && _serverManager.get_clientHandlers().size() > 0){
                            set_serverLog(ALTERNATIVE_CONNECTION);
                            _sharedCommand.setCzyNowy(true);
                            TimeUnit.SECONDS.sleep(4);
                        }
                        set_serverLog(CONNECTION + PRIORITY);
                        TimeUnit.SECONDS.sleep(4);
                        outP.println(CONNECTION_CONFIRM);
                        break;
                    case PING:
                        set_serverLog(PING);
                        TimeUnit.SECONDS.sleep(4);

                        if(_sharedCommand.isCzyNowy())
                        {
                            outP.println(DISCONNECT_FROM_CLIENT);
                            throw new IOException();
                        }
                        outP.println(PING_CONFIRM);
                        if(_sharedCommand.ready())
                            outP.println(_sharedCommand.take());

                        else
                            outP.println("NIC");

                        break;
                    case DISCONNECT_FROM_SERWER:
                        set_serverLog(DISCONNECT_FROM_SERWER);
                        outP.println(DISCONNECT_FROM_SERWER);
                        running = false;
                        _sharedCommand.setClientExist(false);
                        try {
                            in.close();
                            outP.close();
                            _socket.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        break;

                    case DISCONNECT_FROM_CLIENT:
                        //set_serverLog(DISCONNECT_FROM_CLIENT);
                        throw new IOException();

                    case ELECTION:
                    case COORDINATOR:
                        String msg = in.readLine();
                        set_serverLog(msg);
                       _sharedCommand.set(msg);
                        _sharedCommand.setClientExist(false); //////sprawdzic czy kasuje z listy rzeczywiscie tego klienta!!
                        in.close();
                        outP.close();
                        _socket.close();
                        running = false;
                        break;


                    default:
                        set_serverLog("Zle:" + info);
                        break;
                }
            } catch (IOException | InterruptedException e) {
                try {
                    set_serverLog(DISCONNECT_FROM_CLIENT);
                    in.close();
                    outP.close();
                    _socket.close();
                    running = false;
                    _sharedCommand.setClientExist(false);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        }



    }




    public void set_serverLog(String text) {
         _serverLog.append("\n " + text);
    }

    public void set_serverLog(int text) {
        _serverLog.append("\n" + text);
    }
    public void set_serverLog(boolean text) {
        _serverLog.append("\n" + text);
    }

    public void disconnectServer() {
        disconnectFlag = true;
    }

}

