import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.sql.Time;
import java.util.concurrent.TimeUnit;

public class ClientHandlerServer implements Runnable {
    private PrintWriter outP;
    private BufferedReader in;
    private final Socket _socket;
    private JTextArea _serverLog;
    private static final String CONNECTION = "CONNECTION";
    private static final String DISCONNECT = "DISCONNECT";
    private static final String CONNECTION_CONFIRM = "CONNECTION_CONFIRM";
    private static final String PING = "PING";
    private static final String PING_CONFIRM = "PING_CONFIRM";
    public String info;
    public boolean running = true;


    public ClientHandlerServer(Socket socket, JTextArea serverLog) throws IOException {
        _socket = socket;
        _serverLog = serverLog;
        in =
                new BufferedReader(
                        new InputStreamReader(_socket.getInputStream()));
        outP =
                new PrintWriter(_socket.getOutputStream(), true);
    }


    @Override
    public void run() {
        while (running) {
            try {
                info = in.readLine();
                TimeUnit.SECONDS.sleep(4);
                switch (info) {
                    case CONNECTION:
                        set_serverLog(CONNECTION);
                        TimeUnit.SECONDS.sleep(4);
                        outP.println(CONNECTION_CONFIRM);
                        break;
                    case PING:
                        set_serverLog(PING);
                        TimeUnit.SECONDS.sleep(4);
                        outP.println(PING_CONFIRM);
                        break;
                    case DISCONNECT:
                        set_serverLog(DISCONNECT);
                        TimeUnit.SECONDS.sleep(4);
                        outP.println(DISCONNECT);
                        throw new IOException();
                        //create method with tanenbaum algoritm
                    default:
                        set_serverLog("ZLE");
                        break;
                }
            } catch (IOException | InterruptedException e) {
                //e.printStackTrace();
                set_serverLog("DZIALA KURWY");
                running = false;

//                if (in != null) {
//                    try {
//                        in.close();
//                    } catch (IOException ex) {
//                        ex.printStackTrace();
//                    }
//                }
//                if (outP != null)
//                    outP.close();
//                try {
//                    _socket.close();
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }

            }

        }
        /**
         * ZA WHILEM
         */


    }

    public void set_serverLog(String text) {
        _serverLog.append("\n" + text);
    }

    public void set_serverLog(int text) {
        _serverLog.append("\n" + text);
    }

    public void disconnectServer() {
        info = DISCONNECT;

    }
}

