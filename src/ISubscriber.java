/**
 * ISubscriber.java
 * Contrat d'un thread subscriber — correspond à SUB3 dans la spécification FSP.
 *
 * FSP :
 *   SUB3 = (connect_sub -> sub -> consume -> SUB3).
 *
 * NE PAS MODIFIER — responsable : Fares
 */
public interface ISubscriber extends Runnable {

    /**
     * Boucle infinie qui reproduit le comportement de SUB3 :
     *   1. broker.connectSub(label)   → affiche CONNECT_SUB
     *   2. broker.sub(label)          → affiche SUB
     *   3. Afficher CLOSE_SUB
     *   4. Afficher CONSUME
     *   5. Recommencer
     *
     * Doit s'arrêter proprement quand Thread.currentThread().isInterrupted() est vrai.
     */
    @Override
    void run();
}
