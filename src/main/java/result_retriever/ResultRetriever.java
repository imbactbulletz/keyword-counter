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

public class ResultRetriever extends Thread {


    private Map<String, Future<Map>> fileJobsSummaryCache = new ConcurrentHashMap<>();
    private Map<String, Future<Map>> webJobsFutures = new ConcurrentHashMap<>();
    private Map<String, Future<Map>> webJobsSummaryCache = new ConcurrentHashMap<>();

    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void run() {
        System.out.println("> Result Retriever is up and running.");
    }

    public void addJob(Job job) {
//        System.out.println("Result Retriever has recieved a File Job with query: " + job.getQuery());

        String jobQuery = job.getQuery();


        if(job.getType() == ScanType.FILE) {
            String corpusName = jobQuery.substring("file|".length());
            fileJobsSummaryCache.put(corpusName, job.getResult());
        } else {
            String pageURL = jobQuery.substring("web|".length());
            webJobsFutures.put(pageURL, job.getResult());
        }
    }


    public String getResult(String query) {
        String[] splitQuery = query.split("\\|");

        String scanType = splitQuery[0];
        String name = splitQuery[1];

        if(scanType.equals("file")) {
                return getFileResult(name);
        }

        if(scanType.equals("web")) {
                return getWebResult(name);
        }

        return "No such job type exists.";
    }

    public String queryResult(String query) {
        String[] splitQuery = query.split("\\|");

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


    private String queryWeb(String pageURL) {

        if(pageURL.equals("summary")) {
            List<Future> summaryFutures = new ArrayList<>(webJobsSummaryCache.values());

            for(Future summaryFuture : summaryFutures) {
                if(!summaryFuture.isDone()) {
                    return "Summary has not finished yet.";
                }
            }

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

        Future<Map> webFuture = webJobsFutures.get(pageURL);

        if(webFuture != null) {
            if(webFuture.isDone()) {
                try {
                    String result = webFuture.get().toString();
                    startDomainSummary(pageURL);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            } else {
                return "Job isn't finished yet.";
            }
        }

        return "No job under such query exists";
    }

    private String getWebResult(String name) {
        Future<Map> webFuture = webJobsFutures.get(name);

        if(webFuture != null) {

            try {
                return webFuture.get().toString();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        return "No job under such query exists";
    }


    private void startDomainSummary(String pageURL) {
        String domainName = convertPageURLtoDomain(pageURL);
        Future domainResult = executorService.submit(new DomainSummaryWorker(domainName, webJobsFutures));

        webJobsSummaryCache.put(domainName, domainResult);
    }

    public void clearFileSummary() {
        fileJobsSummaryCache = new ConcurrentHashMap<>();
        System.out.println("> Cleared File Summary.");
    }



    public String convertPageURLtoDomain(String pageURL) {
        if (pageURL.startsWith("http:/")) {
            if (!pageURL.contains("http://")) {
                pageURL = pageURL.replaceAll("http:/", "http://");
            }
        } else {
            if (pageURL.startsWith("https:/")) {
                if (!pageURL.contains("https://")) {
                    pageURL = pageURL.replaceAll("https:/", "https://");
                }
            } else {
                pageURL = "http://" + pageURL;
            }
        }

        URI uri = null;
        try {
            uri = new URI(pageURL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    public Map<String, Future<Map>> getWebJobsSummaryCache() {
        return webJobsSummaryCache;
    }

    public Map<String, Future<Map>> getFileJobsSummaryCache() {
        return fileJobsSummaryCache;
    }
}
