import java.io.Serializable;

public class Serwers implements Serializable {

    private String _IP;
    private int _PORT;

    public Serwers(String IP, int PORT) {
        _IP = IP;
        _PORT = PORT;


    }

    public String get_IP() {
        return _IP;
    }

    public void set_IP(String _IP) {
        this._IP = _IP;
    }

    public int get_PORT() {
        return _PORT;
    }

    public void set_PORT(int _PORT) {
        this._PORT = _PORT;
    }
}
