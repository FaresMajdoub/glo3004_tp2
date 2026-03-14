/**
 * IBroker.java
 * Contrat du broker — correspond à BROKER4 + CONTROLLER_SUB_SAME_ORIGIN_PUB dans la spécification FSP.
 *
 * Rappel FSP :
 *   BROKER4 = PUBSUB[0],
 *     PUBSUB[i:0..N] = (when (i < N) connect_pub -> pub -> queue -> PUBSUB[i+1]
 *                      | when (i > 0) connect_sub -> sub -> dequeue -> PUBSUB[i-1]).
 *
 * Les actions queue et dequeue ne sont pas implémentées explicitement.
 * Le count++ dans pub() joue le rôle de queue, et count-- dans sub() joue le rôle de dequeue.
 *
 * NE PAS MODIFIER — responsable : Fares
 */
public interface IBroker {

    /**
     * Correspond à : connect_pub dans FSP.
     * Bloque tant que N publishers sont déjà connectés (connected >= N).
     * Affiche CONNECT_PUB dans la trace.
     *
     * @param label ex : "i.publisher.2"
     */
    void connectPub(String label) throws InterruptedException;

    /**
     * Correspond à : pub -> queue dans FSP.
     * Ajoute un message dans le buffer et libère le slot publisher.
     * Affiche PUB dans la trace.
     *
     * @param label ex : "i.publisher.2"
     */
    void pub(String label) throws InterruptedException;

    /**
     * Correspond à : connect_sub dans FSP.
     * Bloque tant que le buffer est vide (count == 0).
     * Affiche CONNECT_SUB dans la trace.
     *
     * @param label ex : "i.subscriber.1"
     */
    void connectSub(String label) throws InterruptedException;

    /**
     * Correspond à : sub -> dequeue dans FSP.
     * Retire un message du buffer.
     * Affiche SUB dans la trace.
     *
     * @param label ex : "i.subscriber.1"
     */
    void sub(String label) throws InterruptedException;
}
