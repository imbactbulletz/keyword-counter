public class Main {

    public static CLI CLIThread;

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
    }
}
