package diagcollector.collector;

import diagcollector.msglibrary.JmsConsumer;
import diagcollector.msglibrary.JmsDestType;
import diagcollector.msglibrary.JmsLibrary;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;

/**
 * Main class that is supposed to be included in user application.
 */
public class DiagCollector {
    private static final Logger LOG = LoggerFactory.getLogger(DiagCollector.class);
    private static final String DST_DISCOVERY = "msgdiag-discovery";
    private static final String DST_RETRIEVE = "msgdiag-retrieve";
    private final String myHostName;
    private final DiagCallBack diagCallback;
    private JmsLibrary jmsLib;
    private JmsConsumer discoveryConsumer;
    private JmsConsumer retrieveConsumer;

    public DiagCollector(String brokerURL, String myHostName, DiagCallBack diagCallback) throws JMSException {
        this.jmsLib = new JmsLibrary(new ActiveMQConnectionFactory(brokerURL));
        this.myHostName = myHostName;
        this.diagCallback = diagCallback;
        init();
    }

    private void init() throws JMSException {
        try {
            discoveryConsumer = jmsLib.getJmsConsumer(JmsDestType.TOPIC, DST_DISCOVERY);
            discoveryConsumer.setMessageListener(new DiscoveryMessageListener(jmsLib, myHostName));
            retrieveConsumer = jmsLib.getJmsConsumer(JmsDestType.TOPIC, DST_RETRIEVE);
            retrieveConsumer.setMessageListener(new RetrieveMessageListener(jmsLib, myHostName, diagCallback));
        } catch (JMSException initException) {
            try {
                if (discoveryConsumer != null)
                    discoveryConsumer.close();
                if (jmsLib != null)
                    jmsLib.close();
            } catch (Exception e) {
                ///
            }
            throw initException;
        }
    }

    public void terminate() throws Exception {
        Exception exc = null;
        try {
            discoveryConsumer.close();
        } catch (JMSException e) {
            exc = e;
        }
        try {
            retrieveConsumer.close();
        } catch (JMSException e) {
            if (exc == null) exc = e;
        }
        try {
            jmsLib.close();
        } catch (Exception e) {
            if (exc == null) exc = e;
        }
        if (exc != null) throw exc;
    }
}
