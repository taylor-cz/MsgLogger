package diagcollector.collector;

import diagcollector.msglibrary.JmsLibrary;
import diagcollector.msglibrary.JmsProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

class RetrieveMessageListener implements MessageListener {
    private static final Logger LOG = LoggerFactory.getLogger(RetrieveMessageListener.class);
    private JmsLibrary jmsLib;
    private String myHostName;
    private DiagCallBack diagCallback;

    RetrieveMessageListener(JmsLibrary jmsLib, String myHostName, DiagCallBack diagCallback) {
        this.jmsLib = jmsLib;
        this.myHostName = myHostName;
        this.diagCallback = diagCallback;
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
            LOG.debug("Received retrieve request " + msgText);
            String destName = msgText;  //TODO: make data structured (json)
            if (myHostName.equals(destName)) {
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
