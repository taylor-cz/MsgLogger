package msglogger.msglibrary;

public enum JmsDestType {
    QUEUE,
    TOPIC;

    public static JmsDestType fromString(String str) {
        switch (str.toUpperCase()) {
            case "QUEUE":
                return QUEUE;
            case "TOPIC":
                return TOPIC;
            default:
                return null;
        }
    }
}
