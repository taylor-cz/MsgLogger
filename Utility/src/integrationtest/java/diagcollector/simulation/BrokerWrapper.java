package diagcollector.simulation;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.policy.DiscardingDeadLetterStrategy;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.network.NetworkConnector;
import org.apache.activemq.store.kahadb.KahaDBPersistenceAdapter;

import java.util.Collections;

public class BrokerWrapper {

    protected BrokerService brokerService;
    protected String name;
    protected int port = 61616;

    public BrokerWrapper(String name) throws Exception {
        this.name = name;
        initialize();
    }

    public void hubMode() throws Exception {
        brokerService.addConnector(String.format("tcp://localhost:%d", 61616));
    }

    public void clientMode() throws Exception {
        NetworkConnector networkConnector = brokerService.addNetworkConnector(String.format("static://(tcp://localhost:%d)", port));
        networkConnector.setDuplex(true);
        networkConnector.setNetworkTTL(3);
        networkConnector.setPrefetchSize(4);
    }

    public String embeddedURL() {
        return "vm://" + name + "?create=false";
    }

    protected void initialize() throws Exception {
        brokerService = new BrokerService();
        brokerService.setUseJmx(false);
        brokerService.setPersistent(true);
        brokerService.setBrokerName(name);
        brokerService.setDataDirectory("build/" + name);
        brokerService.setSplitSystemUsageForProducersConsumers(true);
        brokerService.setProducerSystemUsagePortion(50);
        brokerService.setConsumerSystemUsagePortion(50);
        PolicyEntry policyEntry = new PolicyEntry();
        policyEntry.setTopic(">");
        policyEntry.setDeadLetterStrategy(new DiscardingDeadLetterStrategy());
        PolicyMap policyMap = new PolicyMap();
        policyMap.setPolicyEntries(Collections.singletonList(policyEntry));
        brokerService.setDestinationPolicy(policyMap);

        KahaDBPersistenceAdapter adapter = (KahaDBPersistenceAdapter) brokerService.getPersistenceAdapter();
        adapter.setIgnoreMissingJournalfiles(true);
        adapter.setCheckForCorruptJournalFiles(true);
    }

    public void start() throws Exception {
        if (brokerService == null || brokerService.isStopping() || brokerService.isStopped()) {
            initialize();
        }
        brokerService.start();
    }

    public void stop() throws Exception {
        if (brokerService != null && !brokerService.isStopped() && !brokerService.isStopping()) {
            brokerService.stop();
        }
    }
}
