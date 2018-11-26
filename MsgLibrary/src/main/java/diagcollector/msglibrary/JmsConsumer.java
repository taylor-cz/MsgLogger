package diagcollector.msglibrary;

import javax.jms.*;

public class JmsConsumer implements AutoCloseable {
    private final MessageConsumer consumer;
    private final Session session;
    private final Destination destination;

    /* package */ JmsConsumer(MessageConsumer consumer, Session session, Destination destination) {
        this.consumer = consumer;
        this.session = session;
        this.destination = destination;
    }

    public Destination getDestination() {
        return destination;
    }

    public Message receive(int timeout) throws JMSException {
        return consumer.receive(timeout);
    }

    public void setMessageListener(MessageListener listener) throws JMSException {
        consumer.setMessageListener(listener);
    }

    @Override
    public void close() throws JMSException {
        JMSException exc = null;
        try {
            consumer.close();
        } catch (JMSException e) {
            exc = e;
        }
        try {
            session.close();
        } catch (JMSException e) {
            if (exc == null) exc = e;
        }
        if (exc != null) throw exc;
    }
}
