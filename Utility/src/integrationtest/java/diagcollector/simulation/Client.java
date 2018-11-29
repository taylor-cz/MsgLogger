package diagcollector.simulation;

import com.google.gson.Gson;
import diagcollector.diagutil.api.CollectRequest;
import diagcollector.diagutil.api.CollectResponse;
import diagcollector.diagutil.api.DiscoveryRequest;
import diagcollector.diagutil.api.DiscoveryResponse;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

public class Client {
    private static final String DST_DISCOVERY = "msgdiag-discovery";
    private static final String DST_RETRIEVE = "msgdiag-retrieve";

    public static void main(String[] args) throws Exception {
        Gson gson = new Gson();
        BrokerWrapper wrapper = new BrokerWrapper("client");
        wrapper.clientMode();
        wrapper.start();

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(wrapper.embeddedURL());
        Connection connection = connectionFactory.createConnection();
        connection.setClientID("client-id");
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        MessageConsumer discoveryConsumer = session.createConsumer(session.createTopic(DST_DISCOVERY));
        discoveryConsumer.setMessageListener(message -> {
            try {
                String json = ((TextMessage) message).getText();
                DiscoveryRequest request = gson.fromJson(json, DiscoveryRequest.class);

                MessageProducer producer = session.createProducer(message.getJMSReplyTo());
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                ActiveMQTextMessage respMsg = new ActiveMQTextMessage();
                respMsg.setText(gson.toJson(new DiscoveryResponse("blabla")));
                producer.send(respMsg);
                producer.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });

        MessageConsumer collectConsumer = session.createConsumer(session.createTopic(DST_RETRIEVE) );
        collectConsumer.setMessageListener(message -> {
            try {
                String json = ((TextMessage) message).getText();
                CollectRequest request = gson.fromJson(json, CollectRequest.class);

                MessageProducer producer = session.createProducer(message.getJMSReplyTo());
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                ActiveMQTextMessage respMsg = new ActiveMQTextMessage();
                respMsg.setText(gson.toJson(new CollectResponse("nnnnnn")));
                producer.send(respMsg);
                producer.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });

        // wait for key press
        System.in.read();

        collectConsumer.close();
        discoveryConsumer.close();
        session.close();
        connection.close();

        wrapper.stop();
    }
}
