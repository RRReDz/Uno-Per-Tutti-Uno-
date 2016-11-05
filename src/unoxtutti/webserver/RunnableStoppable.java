/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
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
