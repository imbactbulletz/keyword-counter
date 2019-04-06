import job.FileJob;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

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

            if(name.equals("summary")) {
                List<Future<Map>> jobFutures = new ArrayList<>(fileJobsSummaryCache.values());

                for(Future jobFuture : jobFutures) {
                    if(!jobFuture.isDone()) {
                        try {
                            jobFuture.get();
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                }

                Map<String, Map<String, Integer>> summaryMap = new HashMap<>();

                fileJobsSummaryCache.forEach((k,v) -> {
                    try {
                        summaryMap.put(k, v.get());
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                });

                return summaryMap.toString();
            }

            Future<Map> jobFuture = fileJobsSummaryCache.get(name);

            if(jobFuture != null) {
                try {
                    return jobFuture.get().toString();
                } catch (InterruptedException | ExecutionException e) {
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
            if(name.equals("summary")) {
                List<Future<Map>> jobFutures = new ArrayList<>(fileJobsSummaryCache.values());

                for(Future jobFuture : jobFutures) {
                    if(!jobFuture.isDone()) {
                       return "File Summary is not ready yet.";
                    }
                }

                Map<String, Map<String, Integer>> summaryMap = new HashMap<>();

                fileJobsSummaryCache.forEach((k,v) -> {
                    try {
                        summaryMap.put(k, v.get());
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                });

                return summaryMap.toString();
            }

            Future<Map> jobFuture = fileJobsSummaryCache.get(name);

            if(jobFuture != null) {

                if(jobFuture.isDone()) {
                    try {
                        return jobFuture.get().toString();
                    } catch (InterruptedException | ExecutionException e) {
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
