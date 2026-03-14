import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Broker.java
 * Implémente IBroker — correspond à BROKER4 dans la spécification FSP.
 *
 * FSP :
 *   BROKER4 = PUBSUB[0],
 *     PUBSUB[i:0..N] = (when (i < N) connect_pub -> pub -> queue -> PUBSUB[i+1]
 *                      | when (i > 0) connect_sub -> sub -> dequeue -> PUBSUB[i-1]).
 *
 * Le Broker gère le buffer de messages (count = i dans FSP).
 * La synchronisation CONTROLLER est déléguée au Controller global.
 *
 * RESPONSABLE : Équipier 2
 */
public class Broker implements IBroker {

    private final int N;
    private int count = 0;

    private final Controller controller;

    private final ReentrantLock lock     = new ReentrantLock();
    private final Condition     notEmpty = lock.newCondition();

    public Broker(int n, Controller controller) {
        this.N          = n;
        this.controller = controller;
    }

    /**
     * FSP : when (i < N) connect_pub
     * Délègue au Controller (bloque si pending >= N, puis pending++, puis log).
     */
    @Override
    public void connectPub(String label) throws InterruptedException {
        controller.onConnectPub(label);
    }

    /**
     * FSP : pub -> queue -> PUBSUB[i+1]
     * Dépose le message, affiche PUB, puis attend la barrière queue.
     */
    @Override
    public void pub(String label) throws InterruptedException {
        lock.lock();
        try {
            count++;
            notEmpty.signal();
            TraceLogger.log(label, "PUB");
        } finally {
            lock.unlock();
        }
        controller.onPub(); // barrière pub -> queue
    }

    /**
     * FSP : when (i > 0) connect_sub
     * Bloque tant que count == 0, puis notifie le Controller (pending--, log).
     */
    @Override
    public void connectSub(String label) throws InterruptedException {
        lock.lock();
        try {
            while (count == 0) {
                notEmpty.await();
            }
        } finally {
            lock.unlock();
        }
        controller.onConnectSub(label); // pending--, libère pubSlot et queueDone, log
    }

    /**
     * FSP : sub -> dequeue -> PUBSUB[i-1]
     */
    @Override
    public void sub(String label) throws InterruptedException {
        lock.lock();
        try {
            count--;
            TraceLogger.log(label, "SUB");
        } finally {
            lock.unlock();
        }
    }
}