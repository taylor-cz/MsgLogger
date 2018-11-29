package diagcollector.simulation;


public class Hub {
    public static void main(String[] args) throws Exception {
        BrokerWrapper brokerWrapper = new BrokerWrapper("hub");
        brokerWrapper.hubMode();
        brokerWrapper.start();

        System.in.read();
        brokerWrapper.stop();
    }
}
