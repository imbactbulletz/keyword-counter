package cli;

import app.Main;
import job.WebJob;
import misc.ApplicationSettings;
import misc.Messages;

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
                    if(parameter == null ) {
                        System.out.println("A parameter was expected but wasn't passed.");
                        break;
                    }

                    Main.directoryCrawlerThread.addDirectory(parameter);
                    break;
                case "aw":
                    if(parameter == null ) {
                        System.out.println("A parameter was expected but wasn't passed.");
                        break;
                    }

                    Main.jobQueue.add(new WebJob(parameter, ApplicationSettings.hopCount));
                    break;
                case "get":
                    if(parameter == null ) {
                        System.out.println("A parameter was expected but wasn't passed.");
                        break;
                    }

                    String getResult = Main.resultRetriever.getResult(parameter);
                    System.out.println(getResult);
                    break;
                case "query":
                    if(parameter == null ) {
                        System.out.println("A parameter was expected but wasn't passed.");
                        break;
                    }

                    String queryResult = Main.resultRetriever.queryResult(parameter);
                    System.out.println(queryResult);
                    break;
                case "cws":
                    Main.resultRetriever.clearWebSummary();
                    break;
                case "cfs":
                    Main.resultRetriever.clearFileSummary();
                    break;
                case "stop":
                    System.out.println("Stopping components..");
                    Main.directoryCrawlerThread.addDirectory(Messages.POSION_MESSAGE);
                    return;
                default:
                    System.err.println("You have issued an inexistent command.");
            }

        }

    }
}
