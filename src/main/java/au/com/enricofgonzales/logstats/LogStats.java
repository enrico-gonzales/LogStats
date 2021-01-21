package au.com.enricofgonzales.logstats;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class LogStats {
    public static final int TOP_RESULTS_LIMIT = 3;
    private final Map<String, Integer> requestsByIp = new HashMap<>();
    private final Map<String, Integer> requestsByUrl = new HashMap<>();

    public LogStats() {
    }

    public void addLine(LogLine logLine) {
        requestsByIp.merge(logLine.getIpAddress(), 1, (oldValue, newValue) -> ++oldValue);
        requestsByUrl.merge(logLine.getUrl(), 1, (oldValue, newValue) -> ++oldValue);
    }

    public int getNumberOfUniqueIps() {
        return requestsByIp.entrySet().size();
    }

    public LinkedHashMap<String, Integer> getTopURLs(int top) {
        return getTopInMapByValue(requestsByUrl, top);
    }

    public LinkedHashMap<String, Integer> getTopIps(int top) {
        return getTopInMapByValue(requestsByIp, top);
    }

    private LinkedHashMap<String, Integer> getTopInMapByValue(Map<String, Integer> map, int top) {
        final AtomicInteger counter = new AtomicInteger(0);
        final AtomicInteger previousValue = new AtomicInteger(0);

        return map.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .takeWhile(entry -> {
                    // this is to make sure that ties are all reported if needed (for example, 6 urls tied at 1st place
                    // should all be returned even if top < 6)
                    boolean keepGoing = counter.getAndIncrement() < top || entry.getValue() == previousValue.get();
                    previousValue.set(entry.getValue());
                    return keepGoing;
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
    }

    public static void main(String... args) {
        if (args.length != 1 || args[0].trim().length() == 0) {
            System.err.println("Please pass the log file name as the first and only argument");
            return;
        }
        String filename = args[0];
        try {
            LogStats logStats = new LogStats();
            LogParser parser = new LogParser();

            parser.parseFile(filename, logStats::addLine, System.err::println);

            System.out.printf("Number of unique IP addresses: %d%n", logStats.getNumberOfUniqueIps());

            System.out.printf("%nTop %d URLs:%n", TOP_RESULTS_LIMIT);
            logStats.getTopURLs(TOP_RESULTS_LIMIT).forEach((key, value) -> System.out.printf("\t%s %d%n", key, value));

            System.out.printf("%nTop %d active IPs:%n", TOP_RESULTS_LIMIT);
            logStats.getTopIps(TOP_RESULTS_LIMIT).forEach((key, value) -> System.out.printf("\t%s %d%n", key, value));
        } catch (IOException e) {
            System.err.println("Could not read file " + filename);
        }
    }
}
