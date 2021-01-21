package au.com.enricofgonzales.logstats;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LogParserTest {

    private LogParser parser;

    @BeforeEach
    public void init() {
        parser = new LogParser();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "test",
            // timestamp not withing square brackets
            "168.41.191.40 - - \"09/Jul/2018:10:12:03 +0200\" \"GET /docs/manage-websites/ HTTP/1.1\" 200 3574 \"-\" \"Mozilla/5.0 (X11; Linux i686; rv:6.0) Gecko/20100101 Firefox/6.0\"",
            // not enough tokens
            "168.41.191.40 - - [09/Jul/2018:10:12:03 +0200]",
            // request details not within double quotes
            "168.41.191.40 - - [09/Jul/2018:10:12:03 +0200] [GET /docs/manage-websites/ HTTP/1.1]",
            // missing HTTP method
            "168.41.191.40 - - [09/Jul/2018:10:12:03 +0200] \"/docs/manage-websites/ HTTP/1.1\"",
            // missing protocol
            "168.41.191.40 - - [09/Jul/2018:10:12:03 +0200] \"GET /docs/manage-websites/",
            "168.41.191.40 - - [09/Jul/2018:10:11:30 +0200] \"invalid\" 200 3574 \"-\" \"Mozilla/5.0 (Linux; U; Android 2.3.5; en-us; HTC Vision Build/GRI40) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1\""
    })
    public void throwsExceptionIfLineDoesNotMatchExpectedFormat(String line) {
        InvalidLogLineException thrown = assertThrows(InvalidLogLineException.class, () -> parser.parseLogLine(line));
        assertEquals(line, thrown.getValue());
    }

    @Test
    public void throwsIOExceptionIfFileCannotBeFound() throws URISyntaxException {
        final String filename = getResourceAbsolutePath("programming-task-example-data.log") + "-doesNotExist";
        assertThrows(IOException.class, () -> parser.parseFile(filename, line -> {}, exception -> {}));
    }

    @ParameterizedTest
    @MethodSource("provideValidLines")
    public void parsesLinesIfInCorrectFormat(String line, String expectedIp, String expectedURL) throws InvalidLogLineException {
        LogLine logLine = parser.parseLogLine(line);
        assertEquals(logLine.getIpAddress(), expectedIp);
        assertEquals(logLine.getUrl(), expectedURL);
    }

    private static Stream<Arguments> provideValidLines() {
        return Stream.of(
                Arguments.of("168.41.191.40 - - [09/Jul/2018:10:12:03 +0200] \"GET /docs/manage-websites/ HTTP/1.1\"", "168.41.191.40", "/docs/manage-websites/"),
                Arguments.of("168.41.191.40 test test [09/Jul/2018:10:12:03 +0200] \"POST /docs/manage-websites/ HTTP/1.1\"", "168.41.191.40", "/docs/manage-websites/"),
                Arguments.of("168.41.191.40 test - [09/Jul/2018:10:12:03 +0200] \"DELETE /docs/manage-websites/ HTTP/1.0\"", "168.41.191.40", "/docs/manage-websites/"),
                Arguments.of("168.41.191.40 - test [09/Jul/2018:10:12:03 +0200] \"PUT /docs/manage-websites/ blah\"", "168.41.191.40", "/docs/manage-websites/"),
                Arguments.of("168.41.191.40 - - [09/Jul/2018:10:12:03 +0200] \"meh /docs/manage-websites/ whatever\" whatever follows additional junk", "168.41.191.40", "/docs/manage-websites/")
        );
    }

    @Test
    public void parsesAllLinesInProvidedTestDataFile() throws URISyntaxException, IOException {
        final String filename = getResourceAbsolutePath("programming-task-example-data.log");

        @SuppressWarnings("unchecked")
        Consumer<LogLine> mockedLineConsumer = Mockito.mock(Consumer.class);
        @SuppressWarnings("unchecked")
        Consumer<InvalidLogLineException> mockedExceptionConsumer = Mockito.mock(Consumer.class);

        parser.parseFile(filename, mockedLineConsumer, mockedExceptionConsumer);

        verify(mockedExceptionConsumer, never()).accept(any());
        verify(mockedLineConsumer, times(23)).accept(any());
    }

    @Test
    public void parsesTestFileAsExpected() throws URISyntaxException, IOException {
        final String filename = getResourceAbsolutePath("test.log");

        @SuppressWarnings("unchecked")
        Consumer<LogLine> mockedLineConsumer = Mockito.mock(Consumer.class);
        @SuppressWarnings("unchecked")
        Consumer<InvalidLogLineException> mockedExceptionConsumer = Mockito.mock(Consumer.class);

        parser.parseFile(filename, mockedLineConsumer, mockedExceptionConsumer);

        String invalidLine = "168.41.191.40 - - [09/Jul/2018:10:11:30 +0200] \"invalid\" 200 3574 \"-\" \"Mozilla/5.0 (Linux; U; Android 2.3.5; en-us; HTC Vision Build/GRI40) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1\"";
        ArgumentCaptor<InvalidLogLineException> captor = ArgumentCaptor.forClass(InvalidLogLineException.class);
        verify(mockedExceptionConsumer).accept(captor.capture());
        assertEquals(invalidLine, captor.getValue().getValue());

        verify(mockedLineConsumer).accept(new LogLine("177.71.128.21", "/intranet-analytics/"));
        verify(mockedLineConsumer).accept(new LogLine("168.41.191.41", "https://this/page/does/not/exist/"));
    }

    private String getResourceAbsolutePath(String filename) throws URISyntaxException {
        URL res = getClass().getClassLoader().getResource(filename);
        @SuppressWarnings({"ConstantConditions"})
        File file = Paths.get(res.toURI()).toFile();
        return file.getAbsolutePath();
    }

}