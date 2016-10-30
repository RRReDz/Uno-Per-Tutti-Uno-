/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
