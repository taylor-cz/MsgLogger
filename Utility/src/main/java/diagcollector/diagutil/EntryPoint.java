package diagcollector.diagutil;

import diagcollector.diagutil.errors.FailedRetrieveDiscoveryDataException;
import diagcollector.msglibrary.JmsLibrary;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;

@Slf4j
public class EntryPoint {
    private static final String BROKER_URL = "tcp://localhost:61616";
    //impossible to use in tests - logic hidden in the main()
    // impossible to use without defining command-line parameters , probably some defaults ould be useful

    //throws Exception in the main definition - not user-friendly
    public static void main(String[] args)  {
//        if (args.length == 0) {
//            System.err.println("Search pattern expected as an argument");
//            System.exit(1);
//        }
        String pattern = "a";
        log.info("Pattern to use : {}", pattern);
        DiagnosticCollector diagnosticCollector = new DiagnosticCollector();
        try (JmsLibrary jmsLib = new JmsLibrary(new ActiveMQConnectionFactory(BROKER_URL))) {
            diagnosticCollector.collectDiagnostic(pattern, jmsLib);
        } catch (FailedRetrieveDiscoveryDataException e) {
            log.error(e.getLocalizedMessage(), e);
            System.err.println(e.getLocalizedMessage() + " , see logs for details");
        } catch (Exception jmsLibExc) {
            //no point to capture error , log it and rethrow the same error.
            // Best practice here: if you capturing exception, it can be :
            //   1. logged, and that is all
            //   2. Rethrown as a different exception

            // also question of consistency: error, that "Search pattern expected as an argument" is in the System.err, this exception - in log .
            // Probably it should be the same.
            log.error("Can't initialize JMS library {}" , jmsLibExc.getLocalizedMessage(), jmsLibExc);
            System.err.println("Can't initialize JMS library , see logs for details");
        }
    }
}
