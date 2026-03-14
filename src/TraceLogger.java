/**
 * TraceLogger.java
 * Logger partagé et synchronisé — chaque thread appelle cette classe pour afficher ses actions.
 * Format de sortie : app.type.id ACTION  (ex : "i.publisher.2 SUPPLY")
 *
 * NE PAS MODIFIER — responsable : Fares
 */
public class TraceLogger {

    private TraceLogger() {}

    /**
     * Affiche une ligne de trace sur la sortie standard.
     * Synchronisé pour éviter les entrelacements entre les threads.
     *
     * @param label  préfixe complet du thread, ex : "i.publisher.2" ou "t.subscriber.1"
     * @param action nom de l'action en majuscules, ex : "SUPPLY", "CONNECT_PUB", "CONSUME"
     */
    public static synchronized void log(String label, String action) {
        System.out.println(label + " " + action);
    }
}
