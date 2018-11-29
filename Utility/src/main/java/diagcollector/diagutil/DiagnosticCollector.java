package diagcollector.diagutil;

import com.google.gson.Gson;
import diagcollector.diagutil.api.CollectRequest;
import diagcollector.diagutil.api.CollectResponse;
import diagcollector.diagutil.api.DiscoveryRequest;
import diagcollector.diagutil.api.DiscoveryResponse;
import diagcollector.diagutil.errors.FailedRetrieveCollectDataException;
import diagcollector.diagutil.errors.FailedRetrieveDiscoveryDataException;
import diagcollector.msglibrary.JmsConsumer;
import diagcollector.msglibrary.JmsDestType;
import diagcollector.msglibrary.JmsLibrary;
import diagcollector.msglibrary.JmsProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.List;

//DiagnosticCollector , Commons, Helper - generic names , that can be applied on any context and in most cases means that class does not have dedicated purpose.
//They tend collect lots of trash(some kinda helper methods that needed in 1-2 places but developers are not sure where to put it) through the time and will interconnect code heavily.
//I am advocating to avoid this type of names in general in the code, because quite often we moving our habits of developing small POC's into code style of production application without much thinking.

//log here is defined , but no supporting logback.xml on classpath for this module - how will it work?
@Slf4j
public class DiagnosticCollector {

    private static final String DST_DISCOVERY = "msgdiag-discovery";
    private static final String DST_RETRIEVE = "msgdiag-retrieve";
    private static final int RCV_TOUT = 2000;

    public void collectDiagnostic(String pattern, JmsLibrary jmsLib) throws FailedRetrieveDiscoveryDataException {
        for (String nodeName : doDiscovery(jmsLib)) {
            if (nodeName.contains(pattern)) {
                //keep possibility to have partial data
                try {
                    collectData(jmsLib, nodeName);
                } catch (FailedRetrieveCollectDataException e) {
                    log.error(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    private List<String> doDiscovery(JmsLibrary jmsLib) throws FailedRetrieveDiscoveryDataException {
        Gson gson = new Gson();
        try (JmsProducer discoveryProducer = jmsLib.getJmsProducer(JmsDestType.TOPIC, DST_DISCOVERY);
             JmsConsumer discoveryConsumer = jmsLib.getJmsConsumer(JmsDestType.QUEUE)) {
            discoveryProducer.setReplyDest(discoveryConsumer.getDestination());
            discoveryProducer.sendTextMessage(gson.toJson(new DiscoveryRequest("*discovery-request*")));
            List<String> discoveredNodes = new ArrayList<>();
            Message message;
            while ((message = discoveryConsumer.receive(RCV_TOUT)) != null) {
                String msgText = ((TextMessage) message).getText();
                DiscoveryResponse response = gson.fromJson(msgText, DiscoveryResponse.class);
                log.debug("Discovery reply arrived {}", response.getText());
                String nodeName = response.getText();  //TODO: make return data structured (json)
                discoveredNodes.add(nodeName);
            }
            return discoveredNodes;
        } catch (JMSException discExc) {
//            log.error("Error during discovery phase", discExc);
//            throw new Exception("Failed discovering node list");
            throw new FailedRetrieveDiscoveryDataException("Failed discovering node list", discExc);
        }
    }

    private void collectData(JmsLibrary jmsLib, String destNodeName) throws FailedRetrieveCollectDataException {
        Gson gson = new Gson();
        try (JmsProducer retrieveProducer = jmsLib.getJmsProducer(JmsDestType.TOPIC, DST_RETRIEVE);
             JmsConsumer retrieveConsumer = jmsLib.getJmsConsumer(JmsDestType.QUEUE)) {
            retrieveProducer.setReplyDest(retrieveConsumer.getDestination());
            retrieveProducer.sendTextMessage(gson.toJson(new CollectRequest(destNodeName)));
            Message message;
            if ((message = retrieveConsumer.receive(RCV_TOUT)) != null) {
                String msgText = ((TextMessage) message).getText();
                CollectResponse response = gson.fromJson(msgText, CollectResponse.class);
                String sysDateTime = response.getText();  //TODO: make return data structured (json)
                log.info("Diagnostic data collected from {} {}", destNodeName, sysDateTime);
//                System.out.printf("%s : %s\n", destNodeName, sysDateTime);
            }
        } catch (JMSException retrExc) {
            log.error("Error during retrieve phase", retrExc);
//            throw new Exception("Failed to retrieve data from node " + destNodeName);
            throw new FailedRetrieveCollectDataException("Failed to retrieve data from node " + destNodeName, retrExc);
        }
    }
}
