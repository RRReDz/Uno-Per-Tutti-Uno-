/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoxtutti.webserver;

/**
 *
 * @author picardi
 */
public abstract class RunnableStoppable implements Runnable {
		private boolean stopSuggested = false;
		private boolean stopped = true;
		
		public synchronized void setStopSuggested(boolean b) {
			stopSuggested = b;
		}
		
		public synchronized boolean isStopSuggested() {
			return stopSuggested;
		}
		
		public synchronized boolean isStopped() {
			return stopped;
		}
		
		synchronized protected void setStopped(boolean s) {
			stopped = s;
		}	
}
