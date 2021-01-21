## Assumptions

1. HTTP methods have not to be considered when counting visits (i.e. they can be ignored)
2. Log files' format is as per provided sample data file
3. Log lines have to be parsed for the sake of producing the desired stats only
   (i.e. info which is not required is simply ignored)
4. For both top active IPs and top visited URLs, the algorithm will aim to return / print 3 items.
   In case it runs into multiple items tied at the same position, however:
    * it will count items, not positions (e.g. if 3 IPs are tie at #1, only those 3 will be printed)
    * it won't break a group of multiple tied items (e.g. once 3 URLs have been printed,
      if there are more URLs which scored the same number of visits they will all be printed
      as well - even if this means printing more than 3 URLs overall)
5. Individual invalid lines (lines in the given log file which cannot be parsed), if any,
   do not have to halt the execution: they are reported as invalid (to standard err) and skipped

### Requisites

* JDK 11+
* Maven 3.1+ (to build and run unit tests)

### How to build

From the project folder, run:

`mvn package`

### How to run

Once built, from the `target` folder under the project root run:

`java -jar logstats-1.0.jar filename`

where `filename` is the full or relative path to the log file to be processes and analyzed.

E.g.: 
* On *nix systems: `java -jar logstats-1.0.jar ../src/test/resources/manual-test.log`
* On Win systems: `java -jar logstats-1.0.jar ..\src\test\resources\manual-test.log`

