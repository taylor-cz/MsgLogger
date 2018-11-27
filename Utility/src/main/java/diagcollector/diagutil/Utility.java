package diagcollector.diagutil;

import diagcollector.msglibrary.JmsConsumer;
import diagcollector.msglibrary.JmsDestType;
import diagcollector.msglibrary.JmsLibrary;
import diagcollector.msglibrary.JmsProducer;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.List;

public class Utility {
    private static final Logger LOG = LoggerFactory.getLogger(Utility.class);
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String DST_DISCOVERY = "msgdiag-discovery";
    private static final String DST_RETRIEVE = "msgdiag-retrieve";
    private static final int RCV_TOUT = 2000;

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Search pattern expected as an argument");
            System.exit(1);
        }
        String pattern = args[0];
        try (JmsLibrary jmsLib = new JmsLibrary(new ActiveMQConnectionFactory(BROKER_URL))) {
            for (String nodeName : doDiscovery(jmsLib)) {
                if (nodeName.contains(pattern)) {
                    collectData(jmsLib, nodeName);
                }
            }
        } catch (JMSException jmsLibExc) {
            LOG.error("Can't initialize JMS library", jmsLibExc);
            throw jmsLibExc;
        }
    }

    private static List<String> doDiscovery(JmsLibrary jmsLib) throws Exception {
        try (JmsProducer discoveryProducer = jmsLib.getJmsProducer(JmsDestType.TOPIC, DST_DISCOVERY);
             JmsConsumer discoveryConsumer = jmsLib.getJmsConsumer(JmsDestType.QUEUE)  // temporary queue
        ) {
            discoveryProducer.setReplyDest(discoveryConsumer.getDestination());
            discoveryProducer.sendTextMessage("*discovery-request*");
            List<String> discoveredNodes = new ArrayList<>();
            Message message;
            while ((message = discoveryConsumer.receive(RCV_TOUT)) != null) {
                String msgText = ((TextMessage) message).getText();
                LOG.debug("Discovery reply arrived {}", msgText);
                String nodeName = msgText;  //TODO: make return data structured (json)
                discoveredNodes.add(nodeName);
            }
            return discoveredNodes;
        } catch (JMSException discExc) {
            LOG.error("Error during discovery phase", discExc);
            throw new Exception("Failed discovering node list");
        }
    }

    private static void collectData(JmsLibrary jmsLib, String destNodeName) throws Exception {
        try (JmsProducer retrieveProducer = jmsLib.getJmsProducer(JmsDestType.TOPIC, DST_RETRIEVE);
             JmsConsumer retrieveConsumer = jmsLib.getJmsConsumer(JmsDestType.QUEUE)  // temporary queue
        ) {
            retrieveProducer.setReplyDest(retrieveConsumer.getDestination());
            retrieveProducer.sendTextMessage(destNodeName);
            Message message;
            if ((message = retrieveConsumer.receive(RCV_TOUT)) != null) {
                String msgText = ((TextMessage) message).getText();
                String sysDateTime = msgText;  //TODO: make return data structured (json)
                LOG.info("Diagnostic data collected from {}", destNodeName);
                System.out.printf("%s : %s\n", destNodeName, sysDateTime);
            }
        } catch (JMSException retrExc) {
            LOG.error("Error during retrieve phase", retrExc);
            throw new Exception("Failed to retrieve data from node " + destNodeName);
        }
    }
}
