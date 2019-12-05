import javax.print.DocFlavor;
import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Time;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientManager implements Runnable, Serializable {

    private static final String CONNECTION = "CONNECTION";
    private static final String DISCONNECT_FROM_CLIENT = "DISCONNECT_FROM_CLIENT";
    private static final String DISCONNECT_FROM_SERWER = "DISCONNECT_FROM_SERWER";
    private static final String CONNECTION_CONFIRM = "CONNECTION_CONFIRM";
    private static final String PING = "PING";
    private static final String PING_CONFIRM = "PING_CONFIRM";
    private static final String ELECTION = "ELECTION";
    private static final String COORDINATOR = "COORDINATOR";
    private static final String NEW_CONNECTION = "NEW_CONNECTION";
    private static final String ALTERNATIVE_CONNECTION = "ALTERNATIVE_CONNECTION";
    private static int _PRIORITY = 0;
    private boolean disconnectFlag = false;
    private int NUMBERINLIST;


    private PrintWriter outSerwer;
    private BufferedReader inSerwer;


    private ObjectInputStream inObjClientAdmin;

    private Thread thread;
    private Socket clientAdmin;
    private Socket clientSerwer;

    PrintWriter outAdmin;
    public String info;

    public boolean running = true;


    protected LinkedHashSet<Clients> _clientsList = new LinkedHashSet<>();
    protected LinkedHashSet<Serwers> _serwersList = new LinkedHashSet<>();

    private LinkedList<Clients> _clientsList1 = new LinkedList<>();
    private LinkedList<Serwers> _serwersList1 = new LinkedList<>();

    protected LinkedList<Serwers> lista = new LinkedList<>();


    private boolean isAdmin;
    private JTextArea _loggerArea;

    public ClientManager(String IP_ADMIN, int PORT_ADMIN, String IP_SERWER, int PORT_SERWER, int PRIORITY, JTextArea loggerArea) throws IOException {
        _loggerArea = loggerArea;
//        PORTPattern = Pattern.compile(AVAILABLE_PORT_NUMBERS);

        /**
         * DO SERWERA ADMINISTRUJACEGO
         */
        clientAdmin = new Socket(InetAddress.getByName(IP_ADMIN), PORT_ADMIN);
        outAdmin =
                new PrintWriter(clientAdmin.getOutputStream(), true);

        outAdmin.println(PRIORITY);
        outAdmin.println(PORT_SERWER);
        outAdmin.println(IP_SERWER);
        inObjClientAdmin = new ObjectInputStream(clientAdmin.getInputStream());


        _PRIORITY = PRIORITY;

        /**
         * DO SERWERA=CLIENTA
         */
        clientSerwer = new Socket(InetAddress.getByName(IP_SERWER), PORT_SERWER);
        inSerwer =
                new BufferedReader(
                        new InputStreamReader(clientSerwer.getInputStream()));
        outSerwer =
                new PrintWriter(clientSerwer.getOutputStream(), true);


    }

    @Override
    public void run() {

        if (isAdmin) {

            while (true) {

                LinkedList<Serwers> serwery = new LinkedList<Serwers>();
                LinkedList<Clients> klienci = new LinkedList<Clients>();
                try {
                    serwery = (LinkedList<Serwers>) inObjClientAdmin.readObject();
                    klienci = (LinkedList<Clients>) inObjClientAdmin.readObject();
                    NUMBERINLIST = (int) inObjClientAdmin.readObject();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }


                if (serwery.size() > 0 && klienci.size() > 0) {
                    _serwersList1.addAll(serwery);
                    _clientsList1.addAll(klienci);
                }

            }
        } else {
            outSerwer.println(CONNECTION);
            outSerwer.println(_PRIORITY);
            outSerwer.println(NEW_CONNECTION);
            while (running) {
                try {
                    info = inSerwer.readLine();


                    TimeUnit.SECONDS.sleep(4);
                    if (disconnectFlag)
                        info = DISCONNECT_FROM_CLIENT;

                    switch (info) {
                        case CONNECTION_CONFIRM:
                            logClient(CONNECTION_CONFIRM);
                            TimeUnit.SECONDS.sleep(4);
                            outSerwer.println(PING);
                            break;
                        case PING_CONFIRM:
                            logClient(PING_CONFIRM);
                            TimeUnit.SECONDS.sleep(4);

                            //SPRAWDZENIE CZY NIE ZOSTAL OTRZYMANY DODATKOWY KOMUNIKAT
                            String tmp = inSerwer.readLine();

                            //JEŻELI OTRZYMANY, TO ZACZYNAMY/KONTYNUUJEMY PROCES WYBORU NOWEGO KOORDYNATORA
                            if (!(tmp.equals("NIC"))) {
                                //logClient(tmp);

                                //SPRAWDZENIE POPRAWNOŚCI DŁUGOŚCI KOMUNIKATU(JEZELI MNIEJ NIZ 7 - ZLY)
                                if (tmp.length() > 7) {
                                    try {
                                        if (tmp.substring(0, 8).equals(ELECTION)) {

                                            /**
                                             * JEZELI KOMUNIKAT TO ELECTION, NUMERY PRIORYTETÓW KLIENTÓW KTÓRE OTRZYMUJEMY RAZEM Z KOMUNIAKTEM ZAPISUJEMY DO LOKALNEJ LISTY
                                             */
                                            LinkedList<Integer> listOfPriority = new LinkedList<>();
                                            for (int i = 9; i <= tmp.length(); i++) {
                                                listOfPriority.add(Integer.parseInt(tmp.substring((i - 1), i)));
                                            }

                                            /**
                                             * SPRAWDZENIE CZY KTÓRYŚ Z NUMERÓW WYSTĘPUJE W LIŚCIE TEGO KLIENTA, W CELU OKREŚLENIA CZY KLIENT JUZ WYSYŁAŁ KOMUNIKAT
                                             * ELECTION (W TYM PRZYPADKU NALEŻY ZAMIENIĆ KOMUNIKAT NA COORDINATOR), CZY JESZCZE NIE I WTEDY PRZESYŁAMY ELECTION DALEJ
                                             * DODAJĄC (NASZ) NUMER PRIORYTETU KLIENTA
                                             */
                                            if (checkIfYouAreInList(listOfPriority))
                                                //ZMIENIAMY KOMUNIAKAT NA COORDINATOR, PONIEWAŻ ELECTION JUZ OKRĄZYŁ CAŁY PIERŚCIEŃ
                                                //coordinatorMode(COORDINATOR + Collections.max(listOfPriority));
                                                messages((COORDINATOR + Collections.max(listOfPriority)),COORDINATOR);
                                            else {
                                                //KONTUNUUJEMY WYSYŁANIE KOMINIKATU ELECTION
                                                //election(tmp);
                                                messages(tmp,ELECTION);
                                            }

                                        } else if (tmp.substring(0, 11).equals(COORDINATOR)) {

                                            /**
                                             * JEZELI KOMUNIKAT TO COORDINATOR, NUMERY PRIORYTETÓW KLIENTÓW KTÓRE OTRZYMUJEMY RAZEM Z KOMUNIAKTEM ZAPISUJEMY DO LOKALNEJ LISTY,
                                             * OPRÓCZ 1 ELEMENTU, 1 ELEMENT TO INDEX(NAJWYŻSZY INDEX) NOWEGO KOORDYNATORA
                                             */
                                            LinkedList<Integer> listOfPriority = new LinkedList<>();
                                            for (int i = 13; i <= tmp.length(); i++) {
                                                listOfPriority.add(Integer.parseInt(tmp.substring((i - 1), i)));
                                            }

                                            /**
                                             * SPRAWDZENIE CZY KTÓRYŚ Z NUMERÓW WYSTĘPUJE W LIŚCIE TEGO KLIENTA, W CELU OKREŚLENIA CZY KLIENT JUZ WYSYŁAŁ KOMUNIKAT
                                             * COORDINATOR (W TYM PRZYPADKU NALEŻY ZAMIENIĆ KOMUNIKAT USUNĄĆ - KONIEC ELEKCJI I POWIADAMIANIA), CZY JESZCZE NIE I WTEDY
                                             * PRZESYŁAMY COORDINATOR DALEJ DODAJĄC (NASZ) NUMER PRIORYTETU KLIENTA
                                             */
                                            if (!checkIfYouAreInList(listOfPriority)) {
                                                messages(tmp,COORDINATOR);
                                                //coordinatorMode(tmp);

                                                //JEZELI NASZ PRIORYTET JEST RÓWNY WARTOŚCI INDEXU NOWEGO KOORDYNATORA(12 BIT) TO TEN KLIENT JEST NOWYM KOORDYNATOREM
                                                if (Integer.parseInt(tmp.substring(11, 12)) == _PRIORITY) {
                                                    logClient("JAM JEST KOORDYNATOREM");
                                                } else
                                                    logClient("KONIEC ELEKCJI! KOORDYNATOREM JEST: " + Integer.parseInt(tmp.substring(11, 12)));

                                            } else {
                                                //JEZELI NASZ PRIORYTET JEST RÓWNY WARTOŚCI INDEXU NOWEGO KOORDYNATORA(12 BIT) TO TEN KLIENT JEST NOWYM KOORDYNATOREM
                                                if (Collections.max(listOfPriority) == _PRIORITY) {
                                                    logClient("KONIEC ELEKCJI!" + "\nJAM JEST KOORDYNATOR!!");
                                                } else
                                                    logClient("KONIEC ELEKCJI! KOORDYNATOREM JEST: " + Collections.max(listOfPriority));
                                            }

                                        } else {
                                            logClient("WIADOMOŚĆ MIAŁA POWYŻĘJ 7 ZNAKÓW, ALE BYŁA TO ZLA WIADOMOSC!");
                                        }

                                    } catch (StringIndexOutOfBoundsException e) {
                                        logClient("PROBLEM Z ODCZYTANIEM SUBSTRINGÓW");
                                    }
                                } else {
                                    logClient("Wiadomosc zawiera mniej niz 7 znakow. Zła!");
                                }

                            }


                            /**
                             * KOMUNIKAT PING POTWIERDZAJACY KOMUNIKACJE Z SERWEREM
                             */
                            outSerwer.println(PING);
                            break;

                        case DISCONNECT_FROM_CLIENT:
                            logClient(DISCONNECT_FROM_CLIENT);
                            outSerwer.println(DISCONNECT_FROM_CLIENT);
                            inSerwer.close();
                            outSerwer.close();
                            clientSerwer.close();
                            running = false;
                            break;


                        case DISCONNECT_FROM_SERWER:
                            //logClient(DISCONNECT_FROM_SERWER);
                            throw new IOException();


                        default:
                            logClient(info);
                            logClient("ZLE");
                            break;
                    }


                } catch (IOException | InterruptedException e) {
                    running = false;
                    //logClient(clientSerwer.isClosed());
                    try {
                        logClient(DISCONNECT_FROM_SERWER);
                        inSerwer.close();
                        outSerwer.close();
                        clientSerwer.close();

                        //logClient(clientSerwer.isClosed());

                        createNewConnection();
                        try {
                            TimeUnit.SECONDS.sleep(12);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        //election(null);
                        messages(null,ELECTION);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }


                }

            }
            logClient("za whilem");


        }

        logClient("koniec");
        //metodaElction() -> w srodku nowy watek na kolejne polaczenie, wyslanie komunikatu!!
    }

    /**
     * METODA SPRAWDZAJĄCA CZY W LIŚCIE PRIORYTETÓW KTÓRE JUZ PRZYSŁY ZNAJDUJĘ SIE PRIORYTET TEGO KLIENTA,
     * JEŻELI TAK TO ZWRACA TRUE
     */
    private boolean checkIfYouAreInList(LinkedList<Integer> listOfPriority) {
        for (Integer o : listOfPriority) {
            if (_PRIORITY == o) {
                return true;
            }
        }

        return false;

    }

    public void startAdmin() {
        isAdmin = true;
        thread = new Thread(this);
        thread.start();
    }

    public void startSerwer() {
        isAdmin = false;
        thread = new Thread(this);
        thread.start();
    }



    public void messages(String message, String type) {
        Socket clientMessages;
        PrintWriter outClientMessages;
        boolean flagaPrzejscia = true;


        for (int i = 0; i < _serwersList1.size(); i++) {

            if(type.equals(ELECTION))
            {
                if (message != null)
                {
                    if (NUMBERINLIST != (_serwersList1.size() - 1))
                    {
                        if ((i <= NUMBERINLIST) && flagaPrzejscia)
                            continue;
                        else
                        {
                            if (i == (_serwersList1.size() - 1))
                            {
                                flagaPrzejscia = false;
                            }
                        }
                    }
                }
                //election inicjujacy
                else
                {
                    if (NUMBERINLIST + 1 < (_serwersList1.size() - 1)) {
                        if ((i <= NUMBERINLIST+1) && flagaPrzejscia)
                            continue;
                        else {
                            if (i == (_serwersList1.size() - 1))
                                flagaPrzejscia = true;
                        }
                    } else if (NUMBERINLIST + 1 > (_serwersList1.size() - 1)) {
                        if (i == 0)
                            continue;
                    }
//                    if(_serwersList1.size() > 3)
//                    {
//                        if (NUMBERINLIST != (_serwersList1.size() - 1))
//                        {
//                            if ((i <= NUMBERINLIST+1) && flagaPrzejscia)
//                            {
//                                continue;
//                            }
//                            else
//                            {
//                                if (i == (_serwersList1.size() - 1))
//                                flagaPrzejscia = false;
//                            }
//                        }
//                        else
//                        {
//                            if(i==0)
//                                continue;
//                        }
//
//                    }
//                    else if(_serwersList1.size() > 2){
//                        if (NUMBERINLIST+1 < (_serwersList1.size() - 1))
//                        {
//                            if ((i <= NUMBERINLIST+1) && flagaPrzejscia)
//                            {
//                                continue;
//                            }
//                            else
//                            {
//                                if (i == (_serwersList1.size() - 1))
//                                    flagaPrzejscia = false;
//                            }
//                        }
//                        else if(NUMBERINLIST+1 > (_serwersList1.size() - 1))
//                        {
//                            if(i==0)
//                                continue;
//                        }
//
//                    }


                }
            }
            else if (type.equals(COORDINATOR))
            {

                if(message.length()<13)
                {
                    if (NUMBERINLIST + 1 < (_serwersList1.size() - 1)) {
                        if ((i <= NUMBERINLIST+1) && flagaPrzejscia)
                            continue;
                        else {
                            if (i == (_serwersList1.size() - 1))
                                flagaPrzejscia = true;
                        }
                    } else if (NUMBERINLIST + 1 > (_serwersList1.size() - 1)) {
                        if (i == 0)
                            continue;
                    }
                }
                else
                    {

                        if (NUMBERINLIST != (_serwersList1.size() - 1))
                        {
                            if ((i <= NUMBERINLIST) && flagaPrzejscia)
                                continue;
                            else
                            {
                                if (i == (_serwersList1.size() - 1))
                                {
                                    flagaPrzejscia = false;
                                }
                            }
                        }


                }

            }






            //PRÓBA NAWIĄZANIA POŁACZENIA KLIENT-SERWER Z SERWEREM Z LISTY, ORAZ STRUMIENIA WYJŚCIOWEGO DO PRZESLANIA KOMUNIKATU
            try {
                clientMessages = new Socket(InetAddress.getByName(_serwersList1.get(i).get_IP()), _serwersList1.get(i).get_PORT());
                outClientMessages =
                        new PrintWriter(clientMessages.getOutputStream(), true);
            } catch (IOException e) {
                //W PRZYPADKU BRAKU TAKIEGO SERWERA, ZLEGO ADRESU IP, PORTU BIERZEMY KOLEJNY ELEMENT Z LISTY
                i = isEndOfLoop(i,(_serwersList1.size() - 1));
                continue;

            }




            if(type.equals(ELECTION)) {
                outClientMessages.println(ELECTION);
                //JEŻELI NIE JEST TO KLIENT WYSYŁAJĄCY !!INICJUJĄCY!! KOMUNIKAT, DO UZYSKANEGO KOMUNIKATU DOPISYWANY JEST NUMER PRIORYTETU TEGO KLIENTA
                if (message != null) {
                    outClientMessages.println(message + _PRIORITY);
                    logClient(message);
                } else {
                    //JEZELI JEST TO INICJUJACY KOMUNIKAT JEST ZDEFINIOWANA CALA TRESC KOMUNIKATU
                    outClientMessages.println(ELECTION + _PRIORITY);
                    logClient(ELECTION);
                }

            }
            else if(type.equals(COORDINATOR)){
                //ZDEFINIOWANIE I WYSŁANIE KOMUNIKATU
                outClientMessages.println(COORDINATOR);
                //JEŻELI NIE JEST TO KLIENT WYSYŁAJĄCY !!INICJUJĄCY!! KOMUNIKAT(A NIGDY NIE JEST W TYM MIEJSCU!) DO UZYSKANEGO KOMUNIKATU DOPISYWANY JEST NUMER PRIORYTETU TEGO KLIENTA
                outClientMessages.println(message + _PRIORITY);
                logClient(message);
            }



            try {
                outClientMessages.close();
                clientMessages.close();
            } catch (IOException e) {
                logClient("Niepoprawne zamkniecie!!!!!!!!!!!!!");
            }

            break;
        }

        //setEndOfLoop(false);
//        if(!(message.equals(null)))
////            logClient(message);
    }

//    /**
//     * METODA POWOŁUJĄCA/KONTUNUUJĄCA WYSYŁANIA KOMUNIKATU COORDINATOR DO KOLEJNYCH KLIENTÓW
//     */
//    public void coordinatorMode(String str) {
//
//        Socket clientSerwerCoordinator;
//        PrintWriter outSerwerCoordinator;
//        boolean flagaPrzejscia = false;
//
//        for (int i = 0; i < _serwersList1.size(); i++) {
//
//            if (isEndOfLoop()) {
//                i = 0;
//                setEndOfLoop(false);
//            }
//
//            if (NUMBERINLIST != (_serwersList1.size() - 1)) {
//                if ((i <= NUMBERINLIST) || flagaPrzejscia) {
//                    continue;
//                } else {
//                    if (i == (_serwersList1.size() - 1)) {
//                        setEndOfLoop(true);
//                        flagaPrzejscia = true;
//                    }
//                }
//            } else {
//                if (i == NUMBERINLIST) {
//                    setEndOfLoop(true);
//                }
//            }
//
//
//            //PRÓBA NAWIĄZANIA POŁACZENIA KLIENT-SERWER Z SERWEREM Z LISTY, ORAZ STRUMIENIA WYJŚCIOWEGO DO PRZESLANIA KOMUNIKATU
//            try {
//                clientSerwerCoordinator = new Socket(InetAddress.getByName(_serwersList1.get(i).get_IP()), _serwersList1.get(i).get_PORT());
//                outSerwerCoordinator =
//                        new PrintWriter(clientSerwerCoordinator.getOutputStream(), true);
//            } catch (IOException e) {
//                //W PRZYPADKU BRAKU TAKIEGO SERWERA, ZLEGO ADRESU IP, PORTU BIERZEMY KOLEJNY ELEMENT Z LISTY
//                continue;
//            }
//
//
//            //ZDEFINIOWANIE I WYSŁANIE KOMUNIKATU
//            outSerwerCoordinator.println(COORDINATOR);
//            //JEŻELI NIE JEST TO KLIENT WYSYŁAJĄCY !!INICJUJĄCY!! KOMUNIKAT(A NIGDY NIE JEST W TYM MIEJSCU!) DO UZYSKANEGO KOMUNIKATU DOPISYWANY JEST NUMER PRIORYTETU TEGO KLIENTA
//            outSerwerCoordinator.println(str + _PRIORITY);
//
//            //ZAMKNIECIE STRUMIENIA I GNIAZDA
//            try {
//                outSerwerCoordinator.close();
//                clientSerwerCoordinator.close();
//            } catch (IOException e) {
//                logClient("Niepoprawne zamkniecie!!!!!!!!!!!!!");
//            }
//
//            break;
//
//        }
//
//
//        logClient(str);
//    }



//
//    /**
//     * METODA POWOŁUJĄCA/KONTUNUUJĄCA WYSYŁANIA KOMUNIKATU ELECTION DO KOLEJNYCH KLIENTÓW
//     */
//    public void election(String str) {
//
//        Socket clientSerwerElection;
//        PrintWriter outSerwerElection;
//        boolean flagaPrzejscia = false;
//        boolean flagaPrzejscia1 = false;
//
//        //SPRAWDZANIE LISTY DOSTĘPNYCH SERWERÓW
//        for (int i = 0; i < _serwersList1.size(); i++) {
//
//            //if()
//
//            if (str != null) {
//                if (NUMBERINLIST != (_serwersList1.size() - 1)) {
//                    if ((i <= NUMBERINLIST) || flagaPrzejscia1) {
//                        continue;
//                    } else {
//                        if (i == (_serwersList1.size() - 1)) {
//                            i = 0;
//                            flagaPrzejscia1 = true;
//                        }
//                    }
//                } else {
//                    if (i == NUMBERINLIST) {
//                        i = 0;
//                    }
//                }
//
//            } else {
//                if (NUMBERINLIST != (_serwersList1.size() - 1)) {
//                    if ((i <= NUMBERINLIST) || flagaPrzejscia) {
//                        continue;
//                    } else {
//                        if (i == (_serwersList1.size() - 1)) {
//                            //i = 0;
//                            flagaPrzejscia = true;
//                        }
//                    }
//                } else {
//                    if (_serwersList1.size() > 2) {
//                        if (i == 0) {
//                            continue;
//                        } else if (i == NUMBERINLIST) {
//                            i = 1;
//                        }
//                    }
//
//
//                }
//            }
//
//            //PRÓBA NAWIĄZANIA POŁACZENIA KLIENT-SERWER Z SERWEREM Z LISTY, ORAZ STRUMIENIA WYJŚCIOWEGO DO PRZESLANIA KOMUNIKATU
//            try {
//                clientSerwerElection = new Socket(InetAddress.getByName(_serwersList1.get(i).get_IP()), _serwersList1.get(i).get_PORT());
//                outSerwerElection =
//                        new PrintWriter(clientSerwerElection.getOutputStream(), true);
//            } catch (IOException e) {
//                //W PRZYPADKU BRAKU TAKIEGO SERWERA, ZLEGO ADRESU IP, PORTU BIERZEMY KOLEJNY ELEMENT Z LISTY
//                continue;
//            }
//
//            //ZDEFINIOWANIE I WYSŁANIE KOMUNIKATU
//            outSerwerElection.println(ELECTION);
//
//            //JEŻELI NIE JEST TO KLIENT WYSYŁAJĄCY !!INICJUJĄCY!! KOMUNIKAT, DO UZYSKANEGO KOMUNIKATU DOPISYWANY JEST NUMER PRIORYTETU TEGO KLIENTA
//            if (str != null) {
//                outSerwerElection.println(str + _PRIORITY);
//            } else {
//                //JEZELI JEST TO INICJUJACY KOMUNIKAT JEST ZDEFINIOWANA CALA TRESC KOMUNIKATU
//                outSerwerElection.println(ELECTION + _PRIORITY);
//            }
//
//
//            //ZAMKNIECIE STRUMIENIA I GNIAZDA
//            try {
//                outSerwerElection.close();
//                clientSerwerElection.close();
//            } catch (IOException e) {
//                logClient("Niepoprawne zamkniecie!!!!!!!!!!!!!");
//            }
//
//            break;
//
//
//        }
//    }


    /**
     * METODA NAWIĄZUJĄCA POŁĄCZENIE Z NOWYM SERWEREM, PO UTRACIE POŁACZENIA Z PIERWOTNYM
     */
    public void createNewConnection() {
        //NASTEPNY SERWER NA LISCIE
        boolean flagaPrzejscia = true;

        for (int i = 0; i < _serwersList1.size(); i++)
        {

            if (NUMBERINLIST != (_serwersList1.size() - 1))
            {
                if ((i <= NUMBERINLIST) && flagaPrzejscia)
                {
                    continue;
                }
                else
                {
                    if (i == (_serwersList1.size() - 1))
                    {
                        flagaPrzejscia = false;
                    }
                }
            }



            //PRÓBA NAWIĄZANIA POŁACZENIA KLIENT-SERWER Z SERWEREM Z LISTY, ORAZ STRUMIENIA WYJŚCIOWEGO I WEJSCIOWEGO CELEM DALSZEJ STAŁEJ KOMUNIKACJI
            try {
                clientSerwer = new Socket(InetAddress.getByName(_serwersList1.get(i).get_IP()), _serwersList1.get(i).get_PORT());
                inSerwer =
                        new BufferedReader(
                                new InputStreamReader(clientSerwer.getInputStream()));
                outSerwer =
                        new PrintWriter(clientSerwer.getOutputStream(), true);
            } catch (IOException e) {
                i=isEndOfLoop(i,(_serwersList1.size() - 1));
                continue;
            }

            //WYSŁANIE KOMUNIKATÓW INICJUJĄCYCH POŁACZENIE
            outSerwer.println(CONNECTION);
            outSerwer.println(_PRIORITY);
            outSerwer.println(ALTERNATIVE_CONNECTION);

            //UMOŻLIWIENIE DALSZEJ STAŁEJ KOMUNIKACJI W SWITCHU
            running = true;
            break;

        }
    }


    public int isEndOfLoop(int number, int size){
        if(number==size){
            return 0;
        }
        else
            return number;
    }


    public LinkedHashSet<Clients> getListOfClients1() {
        return _clientsList;
    }

    public LinkedList<Clients> getListOfClients() {
        return _clientsList1;
    }

    public void logClient(String text) {
        _loggerArea.append("\n" + text);
    }

    public void logClient(int text) {
        _loggerArea.append("\n" + text);
    }

    public void logClient(boolean text) {
        _loggerArea.append("\n" + text);
    }

    public void disconnectClient() {
        disconnectFlag = true;
    }

//    private void command(String tmp) {
//        //tuutaj sprawdzac czy nie zmienic na coordinator czy election czy w ogole zakonczyc
//        matcher = ipPattern.matcher(tmp);
//        if (matcher.matches()) {
//            try {
//                election(tmp);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } else if (matcher2.matches()) {
//            //coordinator
//            System.out.println(COORDINATOR);
//        } else
//            System.out.println("ZLE");
//    }


}
