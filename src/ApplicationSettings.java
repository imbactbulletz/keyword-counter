import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Holds data of the settings that were specified in the configuration file.
 */
public class ApplicationSettings {

    /**
     * Path of the configuration file from which the properties will be read.
     */
    private static final String configurationFilePath = "./res/app.properties";

    /**
     * List of keywords that were specified in the configuration file.
     */
    public static List<String> keywordsList;

    /**
     * Represents a name prefix that all directories which are treated as
     * corpuses begin with.
     */
    public static String fileCorpusPrefix;


    /**
     * Interval (in milliseconds) for which Directory Crawler should sleep when idle.
     */
    public static long directoryCrawlerSleepTime;

    /**
     * Upper limit (in bytes) of a Job that Directory Crawler can perform at once.
     */
    public static long fileScanningSizeLimit;

    /**
     * Maximum number of hops that Web Scanner will make while scanning.
     */
    public static long hopCount;

    /**
     * Time (in milliseconds) after which the visited URLs are removed.
     */
    public static long URLRefreshTime;

    /**
     * Loads Application Settings by parsing the properties from its configuration file.
     */
    public static void loadSettings() {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(new File(configurationFilePath)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            while (Objects.requireNonNull(bufferedReader).ready()) {
                String line = bufferedReader.readLine();

                // ran into a line that contains property
                if (line.contains("=")) {
                    parseProperty(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void loadKeywords (String line){
        keywordsList = new ArrayList<>();

        String[] keywords = line.split(",");

        for(String keyword: keywords) {
            keywordsList.add( keyword.trim());
        }

    }

    /**
     * Parses data from the line which contains a property.
     * @param line Line that contains property and its data.
     */
    private static void parseProperty(String line) {
        String[] splitString = line.split("=");
        String prefix = splitString[0].trim();
        String param = splitString[1].trim();

        switch (prefix) {
            case "keywords":
                loadKeywords(param);
                break;

            case "file_corpus_prefix":
                fileCorpusPrefix = param;
                break;

            case "dir_crawler_sleep_time":
                directoryCrawlerSleepTime = Long.valueOf(param);
                break;

            case "file_scanning_size_limit":
                fileScanningSizeLimit = Long.valueOf(param);
                break;
            case "hop_count":
                hopCount = Long.valueOf(param);
                break;
            case "url_refresh_time":
                URLRefreshTime = Long.valueOf(param);
                break;
        }
    }
}
