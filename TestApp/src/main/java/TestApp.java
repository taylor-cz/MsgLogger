import diagcollector.collector.DiagCollector;

import javax.jms.JMSException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
            DiagCollector diagCollector = null;
            try {
                diagCollector = new DiagCollector(BROKER_URL, hostname,
                        () -> LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss:SSS")));
                while (!Thread.currentThread().isInterrupted())
                    Thread.sleep(5000);
            } catch (JMSException e) {
                System.err.println(hostname + ": failed to set up diagnostic library");
                e.printStackTrace();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                try {
                    if (diagCollector != null)
                        diagCollector.terminate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
