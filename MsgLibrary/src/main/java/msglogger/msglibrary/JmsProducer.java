package msglogger.msglibrary;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;

import javax.jms.*;

public class JmsProducer implements AutoCloseable {
    private final MessageProducer producer;
    private final Session session;
    private Destination replyDest;

    JmsProducer(MessageProducer producer, Session session) {
        this.producer = producer;
        this.session = session;
        this.replyDest = null;
    }

    /**
     * Sets requested reply destination for messages sent with this producer.
     * @param destType
     * @param destName
     */
    public void setReplyDest(JmsDestType destType, String destName) {
        switch (destType) {
            case QUEUE:
                this.replyDest = new ActiveMQQueue(destName);
                break;
            case TOPIC:
                this.replyDest = new ActiveMQTopic(destName);
                break;
        }
    }

    public void setReplyDest(Destination replyDest) {
        this.replyDest = replyDest;
    }

    public void sendTextMessage(String text) throws JMSException {
        TextMessage message = session.createTextMessage(text);
        if (replyDest != null)
            message.setJMSReplyTo(replyDest);
        producer.send(message);
    }

    @Override
    public void close() throws Exception {
        producer.close();
        session.close();
    }
}
