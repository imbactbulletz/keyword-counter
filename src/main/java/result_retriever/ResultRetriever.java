package result_retriever;

import job.FileJob;
import job.Job;
import job.ScanType;
import job.WebJob;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ResultRetriever {


    private Map<String, Future<Map>> fileJobsSummaryCache = new ConcurrentHashMap<>();
    private Map<String, Future<Map>> webJobsFutures = new ConcurrentHashMap<>();
    private Map<String, Future<Map>> webJobsSummaryCache = new ConcurrentHashMap<>();

    private ExecutorService executorService = Executors.newCachedThreadPool();


    public void addJob(Job job) {
//        System.out.println("Result Retriever has recieved a File Job with query: " + job.getQuery());

        String jobQuery = job.getQuery();


        if(job.getType() == ScanType.FILE) {
            fileJobsSummaryCache.put(jobQuery, job.getResult());
        } else {
            String pageURL = jobQuery;
            webJobsFutures.put(pageURL, job.getResult());
        }
    }


    public String getResult(String query) {
        String[] splitQuery = query.split("\\|");

        if(splitQuery.length < 2) {
            return "Type not specified.";
        }

        String scanType = splitQuery[0];
        String param = splitQuery[1];

        if(scanType.equals("file")) {
                return getFileResult(param);
        }

        if(scanType.equals("web")) {
                return getWebResult(param);
        }

        return "No such job type exists.";
    }

    public String queryResult(String query) {
        String[] splitQuery = query.split("\\|");

        if(splitQuery.length < 2) {
            return "Type not specified.";
        }

        String scanType = splitQuery[0];
        String name = splitQuery[1];

        if(scanType.equals("file")) {
            return queryFile(name);
        }

        if(scanType.equals("web")) {
            return queryWeb(name);
        }

        return "No such job type exists.";
    }

    private String getFileResult(String name) {

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

        return "No job under such query exists.";
    }

    private String queryFile(String name) {
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

        return "No job under such query exists.";
    }


    private String queryWeb(String param) {

        // if user called for summary
        if(param.equals("summary")) {
            return queryWebSummary();
        } else {
            return queryWebDomainResult(param);
        }
    }

    private String queryWebSummary() {
        List<Future> summaryFutures = new ArrayList<>(webJobsSummaryCache.values());

        for(Future summaryFuture : summaryFutures) {
            if(!summaryFuture.isDone()) {
                return "Summary has not finished yet.";
            }
        }

        return getWebSummaryCache();
    }

    private String queryWebDomainResult(String domainName) {
        // if user called for a specific domain
        Future domainFuture = webJobsSummaryCache.get(domainName);

        // try to get domain result, if it exists
        if(domainFuture != null) {
            if(domainFuture.isDone()) {
                try {
                    return domainFuture.get().toString();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        } else {
            domainFuture = executorService.submit(new DomainSummaryWorker(domainName, webJobsFutures));
            webJobsSummaryCache.put(domainName, domainFuture);
            return "Summary is not ready yet.";
        }

        return "No such job with that query exists.";
    }

    private String getWebResult(String domainName) {

        if(domainName.equals("summary")){
            return getWebSummary();
        }

        Future webFuture = webJobsFutures.get(domainName);

        if(webFuture != null) {

            try {
                return webFuture.get().toString();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        return "No job under such query exists";
    }

    private String getWebSummary() {
        List<Future<Map>> summaryFutures = new ArrayList<>(webJobsSummaryCache.values());

        for (Future summaryFuture : summaryFutures) {
            if (!summaryFuture.isDone()) {
                try {
                    summaryFuture.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
        return getWebSummaryCache();
    }

    public void clearFileSummary() {
        fileJobsSummaryCache = new ConcurrentHashMap<>();
        System.out.println("> Cleared File Summary.");
    }

    public void clearWebSummary() {
        webJobsSummaryCache = new ConcurrentHashMap<>();
        System.out.println("> Cleared Web Summary");
    }

    private String getWebSummaryCache() {

        Map<String, Map<String, Integer>> summaryMap = new HashMap<>();

        webJobsSummaryCache.forEach((k,v) -> {
            try {
                summaryMap.put(k, v.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

        return summaryMap.toString();
    }
}
