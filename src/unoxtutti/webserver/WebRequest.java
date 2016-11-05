/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.webserver;

import java.io.Serializable;

/**
 *
 * @author picardi
 */
public class WebRequest implements Serializable {

	private static final String DUMMY = "___dummy___";
	
	private String name;
	private Object[] parameters;
	
	public WebRequest(String name) {
		this.name = name;
	}
	
	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}
	
	public String getName() {
		return name;
	}
	
	public Object[] getParameters(){
		return parameters;
	}
	
	public boolean isDummyRequest() {
		return name.equals(DUMMY);
	}
	
	public static WebRequest getDummyRequest() {
		return new WebRequest(DUMMY);
	}
}
