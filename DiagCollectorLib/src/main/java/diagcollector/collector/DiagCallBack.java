package diagcollector.collector;

/**
 * This class allows an application that uses {@link DiagCallBack} library
 * to pass diagnostic data asynchronously to the library.
 */
public interface DiagCallBack {
    /**
     * @return app diagnostic data on request from the library
     */
    String getDiagData();
}
