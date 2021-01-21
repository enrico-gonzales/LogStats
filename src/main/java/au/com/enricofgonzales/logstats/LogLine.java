package au.com.enricofgonzales.logstats;

import java.util.Objects;

public class LogLine {
    private final String ipAddress;
    private final String url;

    public LogLine(String ipAddress, String url) {
        this.ipAddress = ipAddress;
        this.url = url;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogLine logLine = (LogLine) o;
        return ipAddress.equals(logLine.ipAddress) && url.equals(logLine.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ipAddress, url);
    }
}
