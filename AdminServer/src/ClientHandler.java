import java.io.*;
import java.net.Socket;

public class ClientHandler {
        ObjectOutputStream outObjClient = null;

    private final Socket _socket;

    public ClientHandler(Socket socket) throws IOException {
        _socket = socket;
        outObjClient = new ObjectOutputStream(socket.getOutputStream());
    }


}

