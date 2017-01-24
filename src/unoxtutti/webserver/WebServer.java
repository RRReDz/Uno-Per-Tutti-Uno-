/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.webserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import unoxtutti.utils.GUIUtils;

/**
 *
 * @author picardi
 */
public class WebServer implements Runnable {

    /**
     * Indirizzo e porta del WebServer
     */
    public final static String WEBSERVER_IP = "localhost";
    public final static int WEBSERVER_PORT = 9000;
    
    private class WebServerHelper implements Runnable {

        private final Socket clientSocket;
        private ObjectInputStream sockIn;
        private ObjectOutputStream sockOut;
        private boolean isStopping;

        private WebServerHelper(Socket s) {
            clientSocket = s;
            isStopping = false;
        }

        synchronized private void stop() {
            boolean wasStopping = isStopping;
            isStopping = true;
            if (!wasStopping) {
                close();
            }
        }

        synchronized private void close() {
            try {
                sockOut.close();
                sockIn.close();
                clientSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void run() {
            try {
                System.out.println("Running helper thread");
                sockIn = new ObjectInputStream(clientSocket.getInputStream());
                sockOut = new ObjectOutputStream(clientSocket.getOutputStream());
                WebRequest req;
                req = (WebRequest) sockIn.readObject();
                if (req.isDummyRequest()) {
                    sockOut.writeObject("ok");
                } else {
                    /* Log del nome della richiesta */
                    WebServer.log("Ricevuta richiesta di tipo: " + req.getName());
                    
                    WebRequestHandler theHandler = null;
                    boolean conflict = false;
                    for (WebRequestHandler wrh : requestHandlers) {
                        if (wrh.canHandle(req)) {
                            if (theHandler == null) {
                                theHandler = wrh;
                            } else {
                                conflict = true;
                            }
                        }
                    }

                    if (theHandler == null) {
                        throw new WebRequestException("No handler for Request " + req.toString());
                    } else if (conflict) {
                        throw new WebRequestException("Multiple handlers for Request " + req.toString());
                    } else {
                        theHandler.handle(req, sockOut);
                    }
                }
                stop();
                System.out.println("Helper thread terminated");

            } catch (IOException | ClassNotFoundException ex) {
                if (isStopping) {
                    System.out.println("legitimate close");
                } else {
                    Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private final ArrayList<WebRequestHandler> requestHandlers;
    private ServerSocket ssocket;
    private static DBController dbController;
    private final ArrayList<WebServerHelper> helpers;
    
    private static WebServerStopDialog stopDialog = null;

    public static DBController getDBController() {
        return dbController;
    }

    public void registerRequestHandler(WebRequestHandler webRequestHandler) {
        this.requestHandlers.add(webRequestHandler);
    }

    public WebServer(DBController dbc) {
        requestHandlers = new ArrayList<>();
        helpers = new ArrayList<>();
        dbController = dbc;
    }

    public synchronized InetAddress getInetAddress() {
        try {
            return InetAddress.getByName(WEBSERVER_IP);
        } catch (UnknownHostException ex) {
            Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    synchronized public void setStopSuggested(boolean b) {
        stopSuggested = b;
        if (isStopped()) {
            return;
        }
        if (b) {
            try {
                Socket socket = new Socket(InetAddress.getByName(WEBSERVER_IP), WEBSERVER_PORT);
                ObjectOutputStream sockOut = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream sockIn = new ObjectInputStream(socket.getInputStream());
                sockOut.writeObject(WebRequest.getDummyRequest());
                sockIn.readObject();
                sockIn.close();
                sockOut.close();
                socket.close();
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void run() {
        try {
            ssocket = new ServerSocket(WEBSERVER_PORT);
            /**
             * HA SENSO? -> con "false" non esegue mai il suo metodo, in più
             * "stopped" è inizializzato a "true"
             */
            setStopSuggested(false);
            setStopped(false);
            while (!isStopSuggested()) {
                Socket clientSocket = ssocket.accept();
                System.out.println("Connection accepted.");
                WebServerHelper h = new WebServerHelper(clientSocket);
                Thread t = new Thread(h);
                helpers.add(h);
                t.start();
            }
            System.out.println("Server out of main cycle");
            helpers.forEach((t) -> {
                t.stop();
            });
            System.out.println("All helpers stopped");
            ssocket.close();
            setStopped(true);
        } catch (IOException ex) {
            Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        try {
            GUIUtils.installLookAndFeel();
            
            DBController dbc = new DBController();
            dbc.connect();
            
            WebServer app = new WebServer(dbc);

            /* Aggiungere qui altri request handler */
            app.registerRequestHandler(new AuthRequestHandler());  //Handler dell'autenticazione(verifica user e aggiungi user)
            
            /* Avvio del Web Server */
            (new Thread(app)).start();
            
            /* Tenta l'inizializzazione di WebLaF */
            GUIUtils.installLookAndFeel();
            
            /* Avvio dell'interfaccia grafica del Web Server */
            stopDialog = new WebServerStopDialog(null, app);
            log("Web Server avviato...");
            stopDialog.setVisible(true);
            dbc.disconnect();
        } catch(SQLNonTransientConnectionException ex) {
            Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
            GUIUtils.showErrorMessage(
                    null,
                    "Impossibile connettersi al database: controllare di aver avviato il servizio correttamente.",
                    "Errore database"
            );
        } catch (SQLException ex) {
            Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean stopSuggested = false;
    private boolean stopped = true;

    public synchronized boolean isStopSuggested() {
        return stopSuggested;
    }

    public synchronized boolean isStopped() {
        return stopped;
    }

    synchronized protected void setStopped(boolean s) {
        stopped = s;
    }
    
    /**
     * Manda un messaggio di log all'interfaccia grafica del Web Server,
     * questo messaggio sarà visualizzato nella console dell'host.
     * @param message Messaggio da tracciare
     */
    synchronized public static void log(String message) {
        if(stopDialog != null) {
            stopDialog.appendMessageToConsole(message);
        }
    }
}
