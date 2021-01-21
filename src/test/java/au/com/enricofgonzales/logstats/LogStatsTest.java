package au.com.enricofgonzales.logstats;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogStatsTest {

    private LogStats logStats;

    @BeforeEach
    public void init() {
        logStats = new LogStats();
    }

    @Test
    public void behavesWhenEmpty() {
        assertEquals(0, logStats.getNumberOfUniqueIps());
        assertTrue(logStats.getTopIps(3).isEmpty());
        assertTrue(logStats.getTopURLs(3).isEmpty());
    }

    @Test
    public void returnsDataEvenIfLessThanRequested() {
        logStats.addLine(new LogLine("ip1", "url1"));

        LinkedHashMap<String, Integer> topIps = logStats.getTopIps(3);
        assertEquals(1, topIps.size());
        assertEquals(1, topIps.get("ip1"));

        LinkedHashMap<String, Integer> topURLs = logStats.getTopURLs(3);
        assertEquals(1, topURLs.size());
        assertEquals(1, topURLs.get("url1"));
    }

    @Test
    public void countsUniqueIps() {
        logStats.addLine(new LogLine("ip1", "url1"));
        logStats.addLine(new LogLine("ip2", "url1"));
        logStats.addLine(new LogLine("ip2", "url1"));
        logStats.addLine(new LogLine("ip2", "url3"));
        logStats.addLine(new LogLine("ip3", "url3"));
        logStats.addLine(new LogLine("ip3", "url1"));

        assertEquals(3, logStats.getNumberOfUniqueIps());
    }

    @Test
    public void returnsOnlyRequestedTopIps() {
        IntStream.rangeClosed(1, 2).forEach(i -> logStats.addLine(new LogLine("ip4", "url" + i)));
        IntStream.rangeClosed(1, 3).forEach(i -> logStats.addLine(new LogLine("ip3", "url" + i)));
        IntStream.rangeClosed(1, 4).forEach(i -> logStats.addLine(new LogLine("ip2", "url" + i)));
        IntStream.rangeClosed(1, 5).forEach(i -> logStats.addLine(new LogLine("ip1", "url" + i)));

        LinkedHashMap<String, Integer> topIps = logStats.getTopIps(3);
        assertEquals(3, topIps.size());

        @SuppressWarnings("unchecked") Map.Entry<String, Integer>[] orderedEntries = new Map.Entry[topIps.size()];
        orderedEntries = topIps.entrySet().toArray(orderedEntries);

        assertEquals("ip1", orderedEntries[0].getKey());
        assertEquals(5, orderedEntries[0].getValue());

        assertEquals("ip2", orderedEntries[1].getKey());
        assertEquals(4, orderedEntries[1].getValue());

        assertEquals("ip3", orderedEntries[2].getKey());
        assertEquals(3, orderedEntries[2].getValue());
    }

    @Test
    public void returnsOnlyRequestedTopURLs() {
        IntStream.rangeClosed(1, 2).forEach(i -> logStats.addLine(new LogLine("ip" + i, "url4")));
        IntStream.rangeClosed(1, 3).forEach(i -> logStats.addLine(new LogLine("ip" + i, "url3")));
        IntStream.rangeClosed(1, 4).forEach(i -> logStats.addLine(new LogLine("ip" + i, "url2")));
        IntStream.rangeClosed(1, 5).forEach(i -> logStats.addLine(new LogLine("ip" + i, "url1")));

        LinkedHashMap<String, Integer> topURLs = logStats.getTopURLs(3);
        assertEquals(3, topURLs.size());

        @SuppressWarnings("unchecked") Map.Entry<String, Integer>[] orderedEntries = new Map.Entry[topURLs.size()];
        orderedEntries = topURLs.entrySet().toArray(orderedEntries);

        assertEquals("url1", orderedEntries[0].getKey());
        assertEquals(5, orderedEntries[0].getValue());

        assertEquals("url2", orderedEntries[1].getKey());
        assertEquals(4, orderedEntries[1].getValue());

        assertEquals("url3", orderedEntries[2].getKey());
        assertEquals(3, orderedEntries[2].getValue());
    }

    @Test
    public void doesNotBreakTiedTopIpsGroupEvenIfInExcessOfHowManyRequested() {
        IntStream.rangeClosed(1, 3).forEach(i -> logStats.addLine(new LogLine("ip1", "url1")));
        IntStream.rangeClosed(1, 3).forEach(i -> logStats.addLine(new LogLine("ip2", "url1")));
        IntStream.rangeClosed(1, 2).forEach(i -> logStats.addLine(new LogLine("ip3", "url1")));
        IntStream.rangeClosed(1, 2).forEach(i -> logStats.addLine(new LogLine("ip4", "url1")));
        IntStream.rangeClosed(1, 2).forEach(i -> logStats.addLine(new LogLine("ip5", "url1")));
        logStats.addLine(new LogLine("ip6", "url1"));
        logStats.addLine(new LogLine("ip7", "url1"));
        logStats.addLine(new LogLine("ip8", "url1"));

        LinkedHashMap<String, Integer> topIps = logStats.getTopIps(3);
        assertEquals(5, topIps.size());

        List<String> numberOneIps = topIps.entrySet().stream().filter(e -> e.getValue() == 3).map(Map.Entry::getKey).collect(Collectors.toList());
        assertEquals(2, numberOneIps.size());
        assertTrue(numberOneIps.containsAll(Arrays.asList("ip1", "ip2")));

        List<String> numberTwoIps = topIps.entrySet().stream().filter(e -> e.getValue() == 2).map(Map.Entry::getKey).collect(Collectors.toList());
        assertEquals(3, numberTwoIps.size());
        assertTrue(numberTwoIps.containsAll(Arrays.asList("ip3", "ip4", "ip5")));
    }

    @Test
    public void doesNotBreakTiedTopURLsEvenIfInExcessOfHowManyRequested() {
        IntStream.rangeClosed(1, 3).forEach(i -> logStats.addLine(new LogLine("ip1", "url1")));
        IntStream.rangeClosed(1, 3).forEach(i -> logStats.addLine(new LogLine("ip1", "url2")));
        IntStream.rangeClosed(1, 2).forEach(i -> logStats.addLine(new LogLine("ip1", "url3")));
        IntStream.rangeClosed(1, 2).forEach(i -> logStats.addLine(new LogLine("ip1", "url4")));
        IntStream.rangeClosed(1, 2).forEach(i -> logStats.addLine(new LogLine("ip1", "url5")));
        logStats.addLine(new LogLine("ip1", "url6"));
        logStats.addLine(new LogLine("ip1", "url7"));
        logStats.addLine(new LogLine("ip1", "url8"));

        LinkedHashMap<String, Integer> topURLs = logStats.getTopURLs(3);
        assertEquals(5, topURLs.size());

        List<String> numberOneURLs = topURLs.entrySet().stream().filter(e -> e.getValue() == 3).map(Map.Entry::getKey).collect(Collectors.toList());
        assertEquals(2, numberOneURLs.size());
        assertTrue(numberOneURLs.containsAll(Arrays.asList("url1", "url2")));

        List<String> numberTwoURLs = topURLs.entrySet().stream().filter(e -> e.getValue() == 2).map(Map.Entry::getKey).collect(Collectors.toList());
        assertEquals(3, numberTwoURLs.size());
        assertTrue(numberTwoURLs.containsAll(Arrays.asList("url3", "url4", "url5")));
    }

}