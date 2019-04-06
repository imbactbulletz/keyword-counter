import java.util.Map;
import java.util.Scanner;

/**
 * Thread which scans user input from the command line and then calls other components accordingly.
 */
public class CLI extends Thread {

    private Scanner scanner;

    @Override
    public void run() {
        System.out.println("> CLI initiated");

        scanner = new Scanner(System.in);
        String input;
        while(scanner.hasNext()) {

            input = scanner.nextLine();

            String[] splitInput = input.split(" ");
            String command = splitInput[0];
            String parameter = null;

            if(splitInput.length > 1) {
                parameter = splitInput[1];
            }

            switch (command) {
                case "ad":
                    Main.directoryCrawlerThread.addDirectory(parameter);
                    break;
                case "aw":
                    System.out.println("You've issued command to add a web domain.");
                    System.out.println("Parameter passed: " + parameter);
                    System.err.println("Not yet implemented.");
                    break;
                case "get":
                    String getResult = Main.resultRetriever.getResult(parameter);
                    System.out.println(getResult);
                    break;
                case "query":
                    String queryResult = Main.resultRetriever.queryResult(parameter);
                    System.out.println(queryResult);
                    break;
                case "cws":
                    System.out.println("You've issued command to clear web summary.");
                    System.err.println("Not yet implemented.");
                    break;
                case "cfs":
                    Main.resultRetriever.clearFileSummary();
                    break;
                case "stop":
                    System.out.println("Stopping components..");
                    Main.directoryCrawlerThread.addDirectory(Messages.POSION_MESSAGE);
                    return;
            }

        }

    }
}
