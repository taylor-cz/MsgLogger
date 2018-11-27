package diagcollector.msglibrary;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jms.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.*;

public class JmsLibraryTest {

    private ConnectionFactory mockConnectionFactory;
    private JmsLibrary jmsLib;
    private Connection mockConnection;
    private Session mockSession;
    private Destination mockDestination;
    private MessageConsumer mockConsumer;
    private MessageProducer mockProducer;

    @Before
    public void setUp() throws Exception {
        mockConnectionFactory = mock(ConnectionFactory.class);
        mockConnection = mock(Connection.class);
        mockSession = mock(Session.class);
        mockDestination = mock(Destination.class);
        mockConsumer = mock(MessageConsumer.class);
        mockProducer = mock(MessageProducer.class);
        when(mockConnectionFactory.createConnection()).thenReturn(mockConnection);
        when(mockConnection.createSession(anyBoolean(), anyInt())).thenReturn(mockSession);
        when(mockSession.createQueue(anyString())).thenReturn(mock(Queue.class));
        when(mockSession.createTemporaryQueue()).thenReturn(mock(TemporaryQueue.class));
        when(mockSession.createTopic(anyString())).thenReturn(mock(Topic.class));
        when(mockSession.createTemporaryTopic()).thenReturn(mock(TemporaryTopic.class));
        when(mockSession.createConsumer(any(Destination.class))).thenReturn(mockConsumer);
        when(mockSession.createProducer(any(Destination.class))).thenReturn(mockProducer);
        jmsLib = new JmsLibrary(mockConnectionFactory);
    }

    @After
    public void tearDown() throws Exception {
        jmsLib.close();
    }

    @Test
    public void lifecycle() throws Exception {
        verify(mockConnectionFactory).createConnection();
        verify(mockConnection).start();
        jmsLib.close();
        verify(mockConnection).close();
    }

    @Test
    public void getJmsConsumer() throws JMSException {
        jmsLib.getJmsConsumer(JmsDestType.QUEUE);
        verify(mockSession).createTemporaryQueue();
        verify(mockSession).createConsumer(any(Destination.class));

        jmsLib.getJmsConsumer(JmsDestType.TOPIC);
        verify(mockSession).createTemporaryTopic();
        verify(mockSession, times(2)).createConsumer(any(Destination.class));

        jmsLib.getJmsConsumer(JmsDestType.QUEUE, "TestQueue");
        verify(mockSession).createQueue("TestQueue");
        verify(mockSession, times(3)).createConsumer(any(Destination.class));

        jmsLib.getJmsConsumer(JmsDestType.TOPIC, "TestTopic");
        verify(mockSession).createTopic("TestTopic");
        verify(mockSession, times(4)).createConsumer(any(Destination.class));

        verifyNoMoreInteractions(mockSession);
    }

    @Test
    public void getJmsProducer() throws JMSException {
        jmsLib.getJmsProducer(JmsDestType.QUEUE, "TestQueue");
        verify(mockSession).createQueue("TestQueue");
        verify(mockSession).createProducer(any(Destination.class));

        jmsLib.getJmsProducer(JmsDestType.TOPIC, "TestTopic");
        verify(mockSession).createTopic("TestTopic");
        verify(mockSession, times(2)).createProducer(any(Destination.class));

        verifyNoMoreInteractions(mockSession);
    }

    @Test(expected = JMSException.class)
    public void getJmsConsumerException() throws JMSException {
        when(mockSession.createConsumer(any(Destination.class))).thenThrow(new JMSException(""));
        try {
            jmsLib.getJmsConsumer(JmsDestType.TOPIC);
        } finally {
            verify(mockConsumer, never()).close();
            verify(mockSession).close();
        }
    }

    @Test(expected = JMSException.class)
    public void getJmsProducerException() throws JMSException {
        when(mockSession.createProducer(any(Destination.class))).thenThrow(new JMSException(""));
        try {
            jmsLib.getJmsProducer(JmsDestType.TOPIC, "TestTopic");
        } finally {
            verify(mockProducer, never()).close();
            verify(mockSession).close();
        }
    }
}