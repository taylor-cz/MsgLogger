package diagcollector.collector;

import diagcollector.msglibrary.JmsLibrary;
import diagcollector.msglibrary.JmsProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

class DiscoveryMessageListener implements MessageListener {
    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryMessageListener.class);
    private JmsLibrary jmsLib;
    private String myHostName;

    /* package */ DiscoveryMessageListener(JmsLibrary jmsLib, String myHostName) {
        this.jmsLib = jmsLib;
        this.myHostName = myHostName;
    }

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
            // content of the message is non-significant
            try (JmsProducer discoveryProducer = jmsLib.getJmsProducer(message.getJMSReplyTo())) {
                discoveryProducer.sendTextMessage(myHostName);
            } catch (JMSException e) {
                LOG.error("Error sending reply on discovery request", e);
            }
        }
    }
}
