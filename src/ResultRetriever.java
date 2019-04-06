import job.FileJob;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ResultRetriever extends Thread {


    private Map<String, Future<Map>> fileJobsSummaryCache = new ConcurrentHashMap<>();


    @Override
    public void run() {
        System.out.println("> Result Retriever is up and running.");
    }

    public void addFileJob(FileJob job) {
        System.out.println("Result Retriever has recieved a File Job with query: " + job.getQuery());

        String jobQuery = job.getQuery();
        String corpusName = jobQuery.substring("file|".length());

        fileJobsSummaryCache.put(corpusName, job.getResult());
    }

    public String getResult(String query) {
        String[] splitQuery = query.split("\\|");

        String scanType = splitQuery[0];
        String name = splitQuery[1];

        if(scanType.equals("file")) {
            Future<Map> jobFuture = fileJobsSummaryCache.get(name);

            if(jobFuture != null) {
                try {
                    return jobFuture.get().toString();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            } else {
                return "No job under such query exists.";
            }
        }

        return "No job under such query exists.";
    }

    public String queryResult(String query) {
        String[] splitQuery = query.split("\\|");

        String scanType = splitQuery[0];
        String name = splitQuery[1];

        if(scanType.equals("file")) {
            Future<Map> jobFuture = fileJobsSummaryCache.get(name);

            if(jobFuture != null) {

                if(jobFuture.isDone()) {
                    try {
                        return jobFuture.get().toString();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                } else {
                    return "Job isn't finished yet.";
                }
            } else {
                return "No job under such query exists.";
            }
        }

        return "No job under such query exists.";
    }

    public void clearFileSummary() {
        fileJobsSummaryCache = new ConcurrentHashMap<>();
        System.out.println("> Cleared File Summary.");
    }
}
