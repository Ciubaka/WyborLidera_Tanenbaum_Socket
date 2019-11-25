import javax.swing.*;
import java.io.*;
import java.net.Socket;
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
    public String info;
    private int PRIORITY;
    public boolean running = true;
    private boolean disconnectFlag = false;
    private ArrayList<Integer> arrayList = new ArrayList<>();

    private SharedCommand _sharedCommand;


    public ClientHandlerServer(Socket socket, JTextArea serverLog, SharedCommand sharedCommand) throws IOException {
        _socket = socket;
        _serverLog = serverLog;
        in =
                new BufferedReader(
                        new InputStreamReader(_socket.getInputStream()));
        outP =
                new PrintWriter(_socket.getOutputStream(), true);

        _sharedCommand = sharedCommand;
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
                        arrayList.add(PRIORITY);
                        set_serverLog(CONNECTION + PRIORITY);
                        TimeUnit.SECONDS.sleep(4);
                        outP.println(CONNECTION_CONFIRM);
                        break;
                    case PING:
                        set_serverLog(PING);
                        TimeUnit.SECONDS.sleep(4);
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

                        //create method with tanenbaum algoritm
                    case DISCONNECT_FROM_CLIENT:
                        set_serverLog(DISCONNECT_FROM_CLIENT);
                        throw new IOException();

                    case ELECTION:
                        int number= in.read();
                        set_serverLog(ELECTION + number);
                        _sharedCommand.set(ELECTION + number);
                        in.close();
                        outP.close();
                        _socket.close();
                        running = false;


                    default:
                        set_serverLog("Zle:" + info);
                        break;
                }
            } catch (IOException | InterruptedException e) {
                try {
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
        _serverLog.append("\n" + text);
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

