package msglogger.msglibrary;

import javax.jms.*;

public class JmsConsumer implements AutoCloseable {
    private final MessageConsumer consumer;
    private final Session session;
    private final Destination destination;

    JmsConsumer(MessageConsumer consumer, Session session, Destination destination) {
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
    public void close() throws Exception {
        consumer.close();
        session.close();
    }
}
