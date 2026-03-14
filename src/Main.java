import java.util.ArrayList;
import java.util.List;

/**
 * Main.java
 * Point d'entrée — correspond à ||SYSTEM11 dans la spécification FSP.
 *
 * Architecture :
 *   - Un Controller unique partagé entre toutes les apps (i et t)
 *     → correspond à APPS:CONTROLLER_SUB_SAME_ORIGIN_PUB (global)
 *   - Un Broker par app (i et t ont chacun leur propre buffer)
 *     → correspond à APPS.PREFIXES::BROKER4
 *
 * Commande de lancement :
 *   java -Dn=2 -Dp=2 -Ds=3 -Dt=100 -jar tp2.jar
 *
 * Paramètres :
 *   n  : taille max du buffer + max publishers simultanés global  (défaut : 2)
 *   p  : nombre de publishers par app                             (défaut : 2)
 *   s  : nombre de subscribers par app                            (défaut : 3)
 *   t  : durée d'exécution en millisecondes                       (défaut : 100)
 *
 * RESPONSABLE : Équipier 4
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {

        int N    = Integer.parseInt(System.getProperty("n", "2"));
        int NB_P = Integer.parseInt(System.getProperty("p", "2"));
        int NB_S = Integer.parseInt(System.getProperty("s", "3"));
        int T    = Integer.parseInt(System.getProperty("t", "100"));

        List<Thread> threads = new ArrayList<>();

        // Controller unique partagé entre toutes les apps
        // FSP : APPS:CONTROLLER_SUB_SAME_ORIGIN_PUB — global sur i et t
        Controller controller = new Controller(N);

        // Un broker par app, tous connectés au même controller global
        for (String app : new String[]{"i", "t"}) {

            IBroker broker = new Broker(N, controller);

            for (int p = 1; p <= NB_P; p++) {
                Thread t = new Thread(new PublisherThread(broker, app, p));
                t.setName(app + ".publisher." + p);
                threads.add(t);
            }

            for (int s = 1; s <= NB_S; s++) {
                Thread t = new Thread(new SubscriberThread(broker, app, s));
                t.setName(app + ".subscriber." + s);
                threads.add(t);
            }
        }

        threads.forEach(Thread::start);
        Thread.sleep(T);
        threads.forEach(Thread::interrupt);

        for (Thread t : threads) {
            t.join(500);
        }
    }
}