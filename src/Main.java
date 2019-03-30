import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class Main {

    public static CLI CLIThread;

    public static DirectoryCrawler directoryCrawlerThread;

    public static void main(String[] args) {
        initialize();
    }

    private static void initialize() {
        ApplicationSettings.loadSettings();

        initializeComponents();
    }

    private static void initializeComponents() {
        CLIThread = new CLI();
        CLIThread.start();

        directoryCrawlerThread = new DirectoryCrawler();
        directoryCrawlerThread.start();
    }

    public static void stop() {
        CLIThread.interrupt();
        System.exit(0);
    }
}
