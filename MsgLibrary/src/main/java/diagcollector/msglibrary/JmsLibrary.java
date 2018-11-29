package diagcollector.msglibrary;

import javax.jms.*;

public class JmsLibrary implements AutoCloseable {

    private final ConnectionFactory connectionFactory;
    private Connection connection;

    public JmsLibrary(ConnectionFactory connectionFactory) throws JMSException {
        this.connectionFactory = connectionFactory;
        init();
    }

    private void init() throws JMSException {
        connection = connectionFactory.createConnection();
        connection.start();
    }

    // create temporary consumer, for use with replyTo message property
    //why it is not named getTemporaryJmsConsumer(..) ?
    public JmsConsumer getJmsConsumer(JmsDestType destType) throws JMSException {
        return getJmsConsumer(destType, null);
    }

    public JmsConsumer getJmsConsumer(JmsDestType destType, String destName) throws JMSException {
        MessageConsumer consumer;
        Session session = null;
        try {
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = null;
            switch (destType) {
                case QUEUE:
                    if (destName == null)
                        destination = session.createTemporaryQueue();
                    else
                        destination = session.createQueue(destName);
                    break;
                case TOPIC:
                    if (destName == null)
                        destination = session.createTemporaryTopic();
                    else
                        destination = session.createTopic(destName);
                    break;
            }
            consumer = session.createConsumer(destination);
            return new JmsConsumer(consumer, session, destination);
        } catch (JMSException e) {
            if (session != null) session.close();
            throw e;
        }
    }

    public JmsProducer getJmsProducer(JmsDestType destType, String destName) throws JMSException {
        MessageProducer producer = null;
        Session session = null;
        try {
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = null;
            switch (destType) {
                case QUEUE:
                    destination = session.createQueue(destName);
                    break;
                case TOPIC:
                    destination = session.createTopic(destName);
                    break;
            }
            producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            return new JmsProducer(producer, session);
        } catch (JMSException e) {
            if (producer != null) producer.close();
            if (session != null) session.close();
            throw e;
        }
    }

    public JmsProducer getJmsProducer(Destination destination) throws JMSException {
        MessageProducer producer = null;
        Session session = null;
        try {
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            return new JmsProducer(producer, session);
        } catch (JMSException e) {
            if (producer != null) producer.close();
            if (session != null) session.close();
            throw e;
        }
    }

    @Override
    public void close() throws JMSException {
        connection.close();
    }
}
