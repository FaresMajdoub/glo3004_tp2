/**
 * SubscriberThread.java
 * Implémente ISubscriber — correspond à SUB3 dans la spécification FSP.
 *
 * FSP :
 *   SUB3 = (connect_sub -> sub -> consume -> SUB3).
 *
 * Séquence de trace par itération :
 *   app.subscriber.id CONNECT_SUB  (affiché dans broker.connectSub)
 *   app.subscriber.id SUB          (affiché dans broker.sub)
 *   app.subscriber.id CLOSE_SUB
 *   app.subscriber.id CONSUME
 *
 * RESPONSABLE : Équipier 3
 */
public class SubscriberThread implements ISubscriber {

    // Broker partagé de cette instance d'app (i ou t)
    private final IBroker broker;

    // Préfixe complet du thread, ex : "i.subscriber.1"
    private final String label;

    /**
     * @param broker le broker partagé pour cette app
     * @param app    "i" ou "t"
     * @param id     numéro du subscriber, 1..NB_S
     */
    public SubscriberThread(IBroker broker, String app, int id) {
        this.broker = broker;
        this.label  = app + ".subscriber." + id;
    }

    /**
     * Boucle infinie — reproduit SUB3 :
     *   connect_sub -> sub -> consume -> SUB3
     *
     * S'arrête proprement si le thread est interrompu.
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // FSP : connect_sub
                broker.connectSub(label);

                // FSP : sub
                broker.sub(label);

                // Fin du cycle subscriber
                TraceLogger.log(label, "CLOSE_SUB");

                // FSP : consume
                TraceLogger.log(label, "CONSUME");

            } catch (InterruptedException e) {
                // Restaurer le flag d'interruption et quitter proprement
                Thread.currentThread().interrupt();
            }
        }
    }
}
