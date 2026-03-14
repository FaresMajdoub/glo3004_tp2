import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Controller.java
 * Implémente CONTROLLER_SUB_SAME_ORIGIN_PUB — instance unique partagée entre toutes les apps.
 *
 * FSP :
 *   CONTROLLER_SUB_SAME_ORIGIN_PUB = C[0],
 *     C[nb:0..N] = (when (nb < N) PREFIXES_PUB.connect_pub -> C[nb+1]
 *                  | when (nb > 0) PREFIXES_SUB.connect_sub -> C[nb-1]).
 *
 * Compteur global sur toutes les apps (i et t ensemble).
 * pending++ dans onConnectPub, pending-- dans onConnectSub.
 * Un connect_pub est interdit tant que pending >= N.
 */
public class Controller {

    private final int N;

    private int pending = 0;
    private int queued  = 0;

    private final ReentrantLock lock      = new ReentrantLock();
    private final Condition     pubSlot   = lock.newCondition();
    private final Condition     queueDone = lock.newCondition();

    public Controller(int n) {
        this.N = n;
    }

    /**
     * FSP : when (nb < N) connect_pub -> C[nb+1]
     * Bloque tant que pending >= N, puis pending++.
     * Le log est fait à l'intérieur du verrou pour garantir l'ordre strict.
     */
    public void onConnectPub(String label) throws InterruptedException {
        lock.lock();
        try {
            while (pending >= N) {
                pubSlot.await();
            }
            pending++;
            TraceLogger.log(label, "CONNECT_PUB");
        } finally {
            lock.unlock();
        }
    }

    /**
     * Barrière pub -> queue.
     * Attend que onConnectSub() signale que queue est consommée.
     */
    public void onPub() throws InterruptedException {
        lock.lock();
        try {
            queued++;
            while (queued > 0) {
                queueDone.await();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * FSP : when (nb > 0) connect_sub -> C[nb-1]
     * Décrémente pending et libère un slot publisher + débloque la barrière queue.
     * Le log est fait à l'intérieur du verrou.
     */
    public void onConnectSub(String label) {
        lock.lock();
        try {
            if (queued > 0) {
                pending--;
                queued--;
                pubSlot.signal();
                queueDone.signal();
            }
            TraceLogger.log(label, "CONNECT_SUB");
        } finally {
            lock.unlock();
        }
    }
}