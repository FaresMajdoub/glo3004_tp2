/**
 * BrokerTest.java — FICHIER DE TEST UNIQUEMENT, ne pas inclure dans le jar final
 *
 * Tests manuels pour valider Broker.java sans avoir besoin des autres classes.
 * L'équipier 2 lance ces tests directement pour vérifier son implémentation.
 *
 * Compilation (depuis tp2/) :
 *   javac -d out src/TraceLogger.java src/IBroker.java src/Broker.java test/BrokerTest.java
 *   java -cp out BrokerTest
 */
public class BrokerTest {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== TEST 1 : flux normal publisher -> subscriber ===");
        testerFluxNormal();

        System.out.println("\n=== TEST 2 : subscriber bloque si buffer vide ===");
        testerSubscriberBloqueBufferVide();

        System.out.println("\n=== TEST 3 : jamais plus de N publishers simultanés ===");
        testerMaxPublishersSimultanes();
    }

    // -------------------------------------------------------------------------
    // Test 1 : un publisher publie, un subscriber consomme
    // Attendu : CONNECT_PUB → PUB → CONNECT_SUB → SUB (dans cet ordre logique)
    // -------------------------------------------------------------------------
    static void testerFluxNormal() throws InterruptedException {
        IBroker broker = new Broker(2);

        // Thread publisher : se connecte et publie un message
        Thread pub = new Thread(() -> {
            try {
                broker.connectPub("test.publisher.1");
                broker.pub("test.publisher.1");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Thread subscriber : attend un message et le consomme
        Thread sub = new Thread(() -> {
            try {
                broker.connectSub("test.subscriber.1");
                broker.sub("test.subscriber.1");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Le publisher publie d'abord, puis le subscriber consomme
        pub.start(); pub.join();
        sub.start(); sub.join();
        System.out.println("TEST 1 PASSÉ");
    }

    // -------------------------------------------------------------------------
    // Test 2 : le subscriber doit bloquer si aucun message n'est disponible
    // Attendu : le thread sub attend indéfiniment, on le force à s'arrêter après 200ms
    // -------------------------------------------------------------------------
    static void testerSubscriberBloqueBufferVide() throws InterruptedException {
        IBroker broker = new Broker(2);

        Thread sub = new Thread(() -> {
            try {
                // Doit bloquer car le buffer est vide
                broker.connectSub("test.subscriber.1");
                System.out.println("ERREUR : le subscriber ne devrait pas avancer sans message !");
            } catch (InterruptedException e) {
                System.out.println("TEST 2 PASSÉ — subscriber bloqué correctement, interrompu proprement");
            }
        });

        sub.start();
        Thread.sleep(200); // laisser le temps au subscriber de bloquer
        sub.interrupt();   // forcer l'arrêt
        sub.join();
    }

    // -------------------------------------------------------------------------
    // Test 3 : avec N=2, jamais plus de 2 publishers connectés simultanément
    // On lance 4 publishers, chacun connectPub + sleep(50ms) + pub
    // Attendu : les 4 passent, mais jamais plus de 2 en même temps
    // -------------------------------------------------------------------------
    static void testerMaxPublishersSimultanes() throws InterruptedException {
        final int N = 2;
        IBroker broker = new Broker(N);

        // Compteurs partagés pour mesurer la concurrence
        int[] connectes = {0};   // publishers actuellement connectés
        int[] maxObserve = {0};  // maximum observé en même temps

        Thread[] publishers = new Thread[4];
        for (int i = 0; i < 4; i++) {
            final int id = i + 1;
            publishers[i] = new Thread(() -> {
                try {
                    broker.connectPub("test.publisher." + id);

                    // Mesurer combien de publishers sont connectés simultanément
                    synchronized (connectes) {
                        connectes[0]++;
                        if (connectes[0] > maxObserve[0]) maxObserve[0] = connectes[0];
                        if (connectes[0] > N) {
                            System.out.println("ERREUR : " + connectes[0] +
                                " publishers connectés simultanément (max autorisé = " + N + ")");
                        }
                    }

                    Thread.sleep(50); // simuler un temps de traitement

                    synchronized (connectes) { connectes[0]--; }
                    broker.pub("test.publisher." + id);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        for (Thread t : publishers) t.start();
        for (Thread t : publishers) t.join();

        System.out.println("Maximum de publishers simultanés observé : "
            + maxObserve[0] + " (limite N=" + N + ")");
        System.out.println(maxObserve[0] <= N ? "TEST 3 PASSÉ" : "TEST 3 ÉCHOUÉ");
    }
}
