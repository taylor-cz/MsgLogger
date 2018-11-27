package diagcollector.collector;

import diagcollector.msglibrary.JmsLibrary;
import diagcollector.msglibrary.JmsProducer;
import org.junit.Before;
import org.junit.Test;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.TextMessage;

import static org.mockito.Mockito.*;

public class DiscoveryMessageListenerTest {

    private static final String TEST_HOSTNAME = "test-hostname";
    private static final String DISCOVERY_REQ = "discovery";
    private JmsLibrary mockJmsLib;
    private JmsProducer mockJmsProducer;
    private DiscoveryMessageListener messageListener;

    @Before
    public void setUp() throws JMSException {
        mockJmsLib = mock(JmsLibrary.class);
        mockJmsProducer = mock(JmsProducer.class);
        when(mockJmsLib.getJmsProducer(any(Destination.class))).thenReturn(mockJmsProducer);
        messageListener = new DiscoveryMessageListener(mockJmsLib, TEST_HOSTNAME);
    }

    @Test
    public void testOnMessage() throws JMSException {
        TextMessage mockTextMessage = mock(TextMessage.class);
        Destination mockReplyDest = mock(Destination.class);
        when(mockTextMessage.getText()).thenReturn(DISCOVERY_REQ);
        when(mockTextMessage.getJMSReplyTo()).thenReturn(mockReplyDest);

        messageListener.onMessage(mockTextMessage);

        verify(mockJmsLib).getJmsProducer(mockReplyDest);
        verify(mockJmsProducer).sendTextMessage(TEST_HOSTNAME);
    }
}