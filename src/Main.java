import job.Job;

import java.util.concurrent.*;

public class Main {

    public static CLI CLIThread;

    public static DirectoryCrawler directoryCrawlerThread;

    public static BlockingQueue<Job> jobQueue;

    public static JobDispatcher jobDispatcherThread;

    public static ForkJoinPool fileScannerPool;

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

        jobQueue = new LinkedBlockingQueue<>();

        jobDispatcherThread = new JobDispatcher();
        jobDispatcherThread.start();

        fileScannerPool = new ForkJoinPool();
    }

}