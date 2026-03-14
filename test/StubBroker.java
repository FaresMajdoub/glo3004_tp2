/**
 * StubBroker.java — FICHIER DE TEST UNIQUEMENT, ne pas inclure dans le jar final
 *
 * Broker factice qui retourne immédiatement sans jamais bloquer.
 * Permet à l'équipier 3 (PublisherThread/SubscriberThread) et à l'équipier 4 (Main)
 * de tester leur code indépendamment avant que le vrai Broker soit prêt.
 */
public class StubBroker implements IBroker {

    @Override
    public void connectPub(String label) {
        TraceLogger.log(label, "CONNECT_PUB");
    }

    @Override
    public void pub(String label) {
        TraceLogger.log(label, "PUB");
    }

    @Override
    public void connectSub(String label) {
        TraceLogger.log(label, "CONNECT_SUB");
    }

    @Override
    public void sub(String label) {
        TraceLogger.log(label, "SUB");
    }
}
