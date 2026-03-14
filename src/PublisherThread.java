/**
 * PublisherThread.java
 * Implémente IPublisher — correspond à PUB3 dans la spécification FSP.
 *
 * FSP :
 *   PUB3 = (supply -> connect_pub -> pub -> PUB3).
 *
 * Séquence de trace par itération :
 *   app.publisher.id SUPPLY
 *   app.publisher.id CONNECT_PUB   (affiché dans broker.connectPub)
 *   app.publisher.id PUB           (affiché dans broker.pub)
 *   app.publisher.id CLOSE_PUB
 *
 * RESPONSABLE : Équipier 3
 */
public class PublisherThread implements IPublisher {

    // Broker partagé de cette instance d'app (i ou t)
    private final IBroker broker;

    // Préfixe complet du thread, ex : "i.publisher.2"
    private final String label;

    /**
     * @param broker le broker partagé pour cette app
     * @param app    "i" ou "t"
     * @param id     numéro du publisher, 1..NB_P
     */
    public PublisherThread(IBroker broker, String app, int id) {
        this.broker = broker;
        this.label  = app + ".publisher." + id;
    }

    /**
     * Boucle infinie — reproduit PUB3 :
     *   supply -> connect_pub -> pub -> PUB3
     *
     * S'arrête proprement si le thread est interrompu.
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // FSP : supply
                TraceLogger.log(label, "SUPPLY");

                // FSP : connect_pub
                broker.connectPub(label);

                // FSP : pub
                broker.pub(label);

                // Fin du cycle publisher
                TraceLogger.log(label, "CLOSE_PUB");

                // Pause pour éviter la famine des subscribers :
                // sans ce sleep, les publishers bouclent trop vite et
                // monopolisent le broker avant que les subscribers puissent se connecter.
                Thread.sleep(1);

            } catch (InterruptedException e) {
                // Restaurer le flag d'interruption et quitter proprement
                Thread.currentThread().interrupt();
            }
        }
    }
}