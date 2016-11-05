Classi predefinite
=====
Descrizione generica delle classi, suddivise per namespace.

unoxtutti
-----
- AutenticarsiController - Singleton che contiene un oggetto `Player` e una `WebClientConnection`.
- GiocareAUnoXTuttiController - Singleton che si occupa della gestione delle stanze.
Tiene una lista aggiornata di tutte le stanze.
- UnoXTutti - Controller principale dell'applicazione, nonché punto di avvio.

unoxtutti.connection
-----
- ClientConnectionException - Eccezione lanciata nel momento in cui il client
non riesce a stabilire una connessione col server.
- CommunicationException - Eccezione lanciata quando qualche aspetto della
comunicazione fra client e server non funziona
- MessageReceiver - Interfaccia usata per creare un pattern Observer-Observable
per l'arrivo dei messaggi.
- P2PConnection - Rappresenta una connessione P2P tra due gioactori, permette
di inviare e ricevere messaggi.
- P2PMessage - Rappresenta un messaggio, può essere allegato un oggetto.
- PartnerShutDownException
- ServerConnectionException
- ServerCreationException

