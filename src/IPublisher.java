/**
 * IPublisher.java
 * Contrat d'un thread publisher — correspond à PUB3 dans la spécification FSP.
 *
 * FSP :
 *   PUB3 = (supply -> connect_pub -> pub -> PUB3).
 *
 * NE PAS MODIFIER — responsable : Fares
 */
public interface IPublisher extends Runnable {

    /**
     * Boucle infinie qui reproduit le comportement de PUB3 :
     *   1. Afficher SUPPLY
     *   2. broker.connectPub(label)   → affiche CONNECT_PUB
     *   3. broker.pub(label)          → affiche PUB
     *   4. Afficher CLOSE_PUB
     *   5. Recommencer
     *
     * Doit s'arrêter proprement quand Thread.currentThread().isInterrupted() est vrai.
     */
    @Override
    void run();
}
