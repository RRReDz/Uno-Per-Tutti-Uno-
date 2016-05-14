package unoxtutti.webserver;


import java.io.IOException;
import java.io.ObjectOutputStream;

/*
 * Classe base per un gestore di richieste "web" dell'applicazione UXT.
 * Ogni sottoclasse definisce quali richieste può soddisfare sovrascrivendo
 * il metodo canHandle, e soddisfa effettivamente tali richieste sovrascrivendo
 * il metodo handleRequest
package spikesocket;

/**
 *
 * @author picardi
 */
public abstract class WebRequestHandler {
	/* Restituisce true se l'handler gestisce la richiesta specificata
	 * In assenza di OVERRIDE, restituisce sempre false.
	*/ 
	public boolean canHandle(WebRequest request) {
		return false;
	}
	
	/* Gestisce la richiesta (se di sua competenza) trasformandola in una chiamata
	 * ad un metodo. Scrive il risultato sull'output stream e lo restituisce.
	*/
	public abstract Object handle(WebRequest request, ObjectOutputStream out) throws IOException; 
}
