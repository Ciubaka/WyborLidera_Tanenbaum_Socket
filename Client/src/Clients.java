import java.io.Serializable;

public class Clients implements Serializable {

    private String _ip;
    private int _port;
    private int _priority;

    public Clients(String ip, int port, int priority) {
        _ip = ip;
        _port = port;
        _priority = priority;
    }

    public String get_ip() {
        return _ip;
    }

    public void set_ip(String _ip) {
        this._ip = _ip;
    }

    public int get_port() {
        return _port;
    }

    public void set_port(int _port) {
        this._port = _port;
    }

    public int get_priority() {
        return _priority;
    }

    public void set_priority(int _priority) {
        this._priority = _priority;
    }
}
