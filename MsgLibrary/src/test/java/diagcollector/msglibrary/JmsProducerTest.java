package diagcollector.msglibrary;

import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.command.ActiveMQDestination;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class JmsProducerTest {
    private JmsProducer jmsProducer;
    private MessageProducer mockMessageProducer;
    private ActiveMQSession mockSession;
    private Destination mockDestination;
    private TextMessage mockMessage;

    @Before
    public void setUp() throws Exception {
        mockMessageProducer = mock(MessageProducer.class);
        mockSession = mock(ActiveMQSession.class);
        mockDestination = mock(Destination.class);
        mockMessage = mock(TextMessage.class);
        jmsProducer = new JmsProducer(mockMessageProducer, mockSession);
        when(mockSession.createTextMessage(anyString())).thenReturn(mockMessage);
    }

    @Test
    public void setReplyDest1() throws JMSException {
        jmsProducer.setReplyDest(JmsDestType.QUEUE, "TestQueue");
        jmsProducer.sendTextMessage("Message Test");
        ArgumentCaptor<Destination> destCaptor = ArgumentCaptor.forClass(Destination.class);
        verify(mockMessage).setJMSReplyTo(destCaptor.capture());
        ActiveMQDestination dest = (ActiveMQDestination) destCaptor.getValue();
        Assert.assertEquals(ActiveMQDestination.QUEUE_TYPE, dest.getDestinationType());
        Assert.assertEquals("TestQueue", dest.getPhysicalName());
    }

    @Test
    public void setReplyDest2() throws JMSException {
        jmsProducer.setReplyDest(mockDestination);
        jmsProducer.sendTextMessage("Message Text");
        verify(mockMessage).setJMSReplyTo(mockDestination);
    }

    @Test
    public void sendTextMessage() throws JMSException {
        jmsProducer.sendTextMessage("Test Message");
        ArgumentCaptor<TextMessage> textMessageCaptor = ArgumentCaptor.forClass(TextMessage.class);
        verify(mockSession).createTextMessage("Test Message");
        verify(mockMessageProducer).send(mockMessage);
    }

    @Test
    public void close() throws JMSException {
        jmsProducer.close();
        verify(mockMessageProducer).close();
        verify(mockSession).close();
    }

    @Test
    public void closeException() throws JMSException {
        JMSException jmsException = new JMSException("Test Exception");

        doThrow(jmsException).when(mockMessageProducer).close();
        try {
            jmsProducer.close();
            fail("Expected JMSException.");
        } catch (JMSException e) {
            Assert.assertEquals(jmsException, e);
        }
        verify(mockMessageProducer).close();
        verify(mockSession).close();
    }
}