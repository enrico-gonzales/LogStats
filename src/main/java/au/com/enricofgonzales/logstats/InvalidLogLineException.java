package au.com.enricofgonzales.logstats;

public class InvalidLogLineException extends Exception {

    private final String value;

    public InvalidLogLineException(String message, Throwable cause, String value) {
        super(message, cause);
        this.value = value;
    }

    public InvalidLogLineException(String message, String value) {
        this(message, null, value);
    }

    public InvalidLogLineException(String message, Throwable cause) {
        this(message, cause, null);
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getMessage());
        if (getValue() != null) {
            sb.append(": ");
            sb.append(value);
        }
        if (getCause() != null) {
            sb.append(" (");
            sb.append(getCause().getClass().getName());
            sb.append(")");
        }
        return sb.toString();
    }
}
