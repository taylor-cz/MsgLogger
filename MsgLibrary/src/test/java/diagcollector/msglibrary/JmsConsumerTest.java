package diagcollector.msglibrary;

import org.apache.activemq.ActiveMQSession;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class JmsConsumerTest {
    private JmsConsumer jmsConsumer;
    private MessageConsumer mockMessageConsumer;
    private ActiveMQSession mockSession;
    private Destination mockDestination;
    private MessageListener mockMessageListener;

    @Before
    public void setUp() {
        mockMessageConsumer = mock(MessageConsumer.class);
        mockSession = mock(ActiveMQSession.class);
        mockDestination = mock(Destination.class);
        mockMessageListener = mock(MessageListener.class);
        jmsConsumer = new JmsConsumer(mockMessageConsumer, mockSession, mockDestination);
    }

    // no need to test activemq - we never test frameworks that used, only self-created code.
    @Test
    public void receive() throws JMSException {
        jmsConsumer.receive(10);
        verify(mockMessageConsumer).receive(10);
    }

    @Test
    public void setMessageListener() throws JMSException {
        jmsConsumer.setMessageListener(mockMessageListener);
        verify(mockMessageConsumer).setMessageListener(mockMessageListener);
    }

    @Test
    public void close() throws JMSException {
        jmsConsumer.close();
        verify(mockMessageConsumer).close();
        verify(mockSession).close();
    }

    @Test
    public void closeException() throws JMSException {
        JMSException jmsException = new JMSException("Test Exception");

        doThrow(jmsException).when(mockMessageConsumer).close();
        try {
            jmsConsumer.close();
            fail("Expected JMSException.");
        } catch (JMSException e) {
            Assert.assertEquals(jmsException, e);
        }
        verify(mockMessageConsumer).close();
        verify(mockSession).close();
    }
}