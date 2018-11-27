package diagcollector.collector;

import diagcollector.msglibrary.JmsLibrary;
import diagcollector.msglibrary.JmsProducer;
import org.junit.Before;
import org.junit.Test;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.TextMessage;

import static org.mockito.Mockito.*;

public class RetrieveMessageListenerTest {

    private static final String TEST_HOSTNAME = "test-hostname";
    private static final String TEST_REQ1 = "test-hostname";
    private static final String TEST_REQ2 = "hostname2";
    private static final String TEST_DATA = "Some bogus diagnostic data";
    private JmsLibrary mockJmsLib;
    private JmsProducer mockJmsProducer;
    private RetrieveMessageListener messageListener;
    private TextMessage mockTextMessage;
    private Destination mockReplyDest;

    @Before
    public void setUp() throws JMSException {
        mockJmsLib = mock(JmsLibrary.class);
        mockJmsProducer = mock(JmsProducer.class);
        when(mockJmsLib.getJmsProducer(any(Destination.class))).thenReturn(mockJmsProducer);
        mockTextMessage = mock(TextMessage.class);
        mockReplyDest = mock(Destination.class);
        when(mockTextMessage.getJMSReplyTo()).thenReturn(mockReplyDest);

        messageListener = new RetrieveMessageListener(mockJmsLib, TEST_HOSTNAME, () -> TEST_DATA);
    }

    @Test
    public void testOnMessageReplyExpected() throws JMSException {
        when(mockTextMessage.getText()).thenReturn(TEST_REQ1);

        messageListener.onMessage(mockTextMessage);

        verify(mockJmsLib).getJmsProducer(mockReplyDest);
        verify(mockJmsProducer).sendTextMessage(TEST_DATA);
    }

    @Test
    public void testOnMessageReplyNotExpected() throws JMSException {
        when(mockTextMessage.getText()).thenReturn(TEST_REQ2);

        messageListener.onMessage(mockTextMessage);

        verify(mockJmsProducer, never()).sendTextMessage(anyString());
    }
}