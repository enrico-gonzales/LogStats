package au.com.enricofgonzales.logstats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogParser {

    private static final String LOG_LINE_PARSING_REGEX = "^(\\S+?) \\S+? \\S+? \\[.+?] \"\\S+? (\\S+?) \\S+?\".*$";

    private final Pattern regexPattern;

    public LogParser() {
        regexPattern = Pattern.compile(LOG_LINE_PARSING_REGEX);
    }

    public void parseFile(String filename, Consumer<LogLine> lineConsumer,
                          Consumer<InvalidLogLineException> lineExceptionConsumer) throws IOException {
        Files.lines(Paths.get(filename)).forEach(line -> {
            // ignore empty lines (if any)
            if (line.trim().length() == 0) return;
            try {
                lineConsumer.accept(parseLogLine(line));
            } catch (InvalidLogLineException e) {
                lineExceptionConsumer.accept(e);
            } catch (Exception e) {
                lineExceptionConsumer.accept(new InvalidLogLineException(line, e));
            }
        });
    }

    LogLine parseLogLine(String line) throws InvalidLogLineException {
        Matcher m = regexPattern.matcher(line);
        if ((!m.find()) || m.groupCount() != 2) {
            throw new InvalidLogLineException("Could not parse line from file", line);
        }
        return new LogLine(m.group(1), m.group(2));
    }
}
