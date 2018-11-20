import msglogger.msgloglib.DiagCallBack;
import msglogger.msgloglib.MsgLogger;

import javax.jms.JMSException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TestApp {
    private static final String BROKER_URL = "tcp://localhost:61616";

    public static void main(String[] args) throws InterruptedException {
        // simulate multiple running apps
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Thread thr = new Thread(new TestAppThread(String.format("testapp-%04d", i)));
            thr.start();
            threads.add(thr);
            System.out.printf("Thread %d started\n", i);
        }
        for (Thread t : threads)
            t.join();
    }

    private static class TestAppThread implements Runnable {
        private String hostname;

        private TestAppThread(String hostname) {
            this.hostname = hostname;
        }

        @Override
        public void run() {
            MsgLogger msgLogger = null;
            try {
                msgLogger = new MsgLogger(BROKER_URL, hostname, new DiagCallBack() {
                    @Override
                    public String getDiagData() {
                        return new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss:SS").format(Calendar.getInstance().getTime());
                    }
                });
                msgLogger.init();
                while (!Thread.currentThread().isInterrupted())
                    Thread.sleep(5000);
            } catch (JMSException e) {
                System.err.println(hostname + ": failed to set up diagnostic library");
                e.printStackTrace();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                try {
                    if (msgLogger != null)
                        msgLogger.terminate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
