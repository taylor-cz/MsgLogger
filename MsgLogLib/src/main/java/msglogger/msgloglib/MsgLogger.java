package msglogger.msgloglib;

import msglogger.msglibrary.JmsConsumer;
import msglogger.msglibrary.JmsDestType;
import msglogger.msglibrary.JmsLibrary;
import msglogger.msglibrary.JmsProducer;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class MsgLogger {
    private static final Logger LOG = LoggerFactory.getLogger(MsgLogger.class);
    private static final String DST_DISCOVERY = "msgdiag-discovery";
    private static final String DST_RETRIEVE = "msgdiag-retrieve";
    private final String myHostName;
    private final String brokerURL;
    private final DiagCallBack diagCallback;
    private JmsLibrary jmsLib;
    private JmsConsumer discoveryConsumer;
    private JmsConsumer retrieveConsumer;
    private JmsProducer retrieveProducer;

    public MsgLogger(String brokerURL, String myHostName, DiagCallBack diagCallback) {
        this.brokerURL = brokerURL;
        this.myHostName = myHostName;
        this.diagCallback = diagCallback;
    }

    public void init() throws JMSException {
        try {
            jmsLib = new JmsLibrary(new ActiveMQConnectionFactory(brokerURL));
            discoveryConsumer = jmsLib.getJmsConsumer(JmsDestType.TOPIC, DST_DISCOVERY);
            discoveryConsumer.setMessageListener(new DiscoveryMessageListener());
            retrieveConsumer = jmsLib.getJmsConsumer(JmsDestType.TOPIC, DST_RETRIEVE);
            retrieveConsumer.setMessageListener(new RetrieveMessageListener());
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
        discoveryConsumer.close();
        retrieveConsumer.close();
        jmsLib.close();
    }

    private class DiscoveryMessageListener implements MessageListener {
        @Override
        public void onMessage(Message message) {
            if (TextMessage.class.isAssignableFrom(message.getClass())) {
                String msgText = null;
                try {
                    msgText = ((TextMessage) message).getText();
                } catch (JMSException e) {
                    LOG.error("Error extracting message content", e);
                }
                LOG.debug("Received discovery request " + msgText);
                System.out.println("Received discovery request: " + msgText);   // just bcos logger is not working now
                // content of the message is non-significant
                try (JmsProducer discoveryProducer = jmsLib.getJmsProducer(message.getJMSReplyTo())) {
                    discoveryProducer.sendTextMessage(myHostName);
                } catch (JMSException e) {
                    LOG.error("Error sending reply on discovery request", e);
                }
            }
        }
    }

    private class RetrieveMessageListener implements MessageListener {
        @Override
        public void onMessage(Message message) {
            if (TextMessage.class.isAssignableFrom(message.getClass())) {
                String msgText = null;
                try {
                    msgText = ((TextMessage) message).getText();
                } catch (JMSException e) {
                    LOG.error("Error extracting message content", e);
                }
                LOG.debug("Received retrieve request " + msgText);
                String destName = msgText;  //TODO: make data structured (json)
                if (myHostName.equals(destName)) {
                    System.out.println("Received retrieve request: " + msgText);   // just bcos logger is not working now
                    try (JmsProducer retrieveProducer = jmsLib.getJmsProducer(message.getJMSReplyTo())) {
                        String diagData = diagCallback.getDiagData();
                        //TODO: make diag data structured
                        retrieveProducer.sendTextMessage(diagData);
                    } catch (JMSException e) {
                        LOG.error("Error sending reply on discovery request", e);
                    }
                }
            }
        }
    }
}
