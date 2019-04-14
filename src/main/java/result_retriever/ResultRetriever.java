package result_retriever;

import job.Job;
import job.ScanType;
import misc.URIUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ResultRetriever {

    private Future<String> fileSummaryJobs;
    private Map<String, Future<Map<String, Integer>>> fileJobs = new ConcurrentHashMap<>();

    private Map<String, Future<Map<String, Integer>>> webJobsFutures = new ConcurrentHashMap<>();
    private Map<String, Future<Map<String, Integer>>> domainSummaries = new ConcurrentHashMap<>();
    private Future<Map<String, Map<String, Integer>>> webJobsSummaryCache;

    private ExecutorService executorService = Executors.newCachedThreadPool();


    public void addJob(Job job) {
        String jobQuery = job.getQuery();


        if (job.getType() == ScanType.FILE) {
            fileJobs.put(jobQuery, job.getResult());
        } else {
            String pageURL = jobQuery;

            String domainName = URIUtil.convertPageURLtoDomain(pageURL);

            domainSummaries.remove(domainName);

            webJobsFutures.put(pageURL, job.getResult());
        }
    }


    public String getResult(String query) {
        String[] splitQuery = query.split("\\|");

        if (splitQuery.length < 2) {
            return "Type not specified.";
        }

        String scanType = splitQuery[0];
        String param = splitQuery[1];

        if (scanType.equals("file")) {
            return getFileResult(param);
        }

        if (scanType.equals("web")) {
            return getWebResult(param);
        }

        return "No such job type exists.";
    }


    public String queryResult(String query) {

        String[] splitQuery = query.split("\\|");

        if (splitQuery.length < 2) {
            return "Type not specified.";
        }

        String scanType = splitQuery[0];
        String name = splitQuery[1];

        if (scanType.equals("file")) {
            return queryFile(name);
        }

        if (scanType.equals("web")) {
            return queryWeb(name);
        }

        return "No such job type exists.";
    }


    private String getFileResult(String name) {

        if (name.equals("summary")) {
            if (fileSummaryJobs != null && fileSummaryJobs.isDone()) {
                try {
                    return fileSummaryJobs.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            } else {
                fileSummaryJobs = executorService.submit(new FileCorpusSummarizer());
                try {
                    return fileSummaryJobs.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }

        Future<Map<String, Integer>> jobFuture = fileJobs.get(name);

        if (jobFuture != null) {
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

        if (name.equals("summary")) {
            if (fileSummaryJobs != null && fileSummaryJobs.isDone()) {
                try {
                    return fileSummaryJobs.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            } else {
                fileSummaryJobs = executorService.submit(new FileCorpusSummarizer());

                return "Summary is not ready yet.";
            }
        }

        Future<Map<String, Integer>> jobFuture = fileJobs.get(name);

        if (jobFuture != null) {

            if (jobFuture.isDone()) {
                try {
                    return jobFuture.get().toString();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            } else {
                return "Job isn't finished yet.";
            }
        }

        return "No job under such query exists.";
    }


    private String queryWeb(String param) {

        // if user called for summary
        if (param.equals("summary")) {
            return queryWebSummary();
        } else {
            return queryWebDomainResult(param);
        }
    }


    private String queryWebDomainResult(String domainName) {

        Future<Map<String, Integer>> domainResultFuture = domainSummaries.get(domainName);

        if (domainResultFuture != null) {
            if (domainResultFuture.isDone()) {
                try {
                    return domainResultFuture.get().toString();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            } else {
                return "Domain results are not ready yet.";
            }
        } else {
            domainResultFuture = executorService
                    .submit(new DomainSummarizer(domainName, webJobsFutures));

            domainSummaries.put(domainName, domainResultFuture);

            return "Summary is not ready yet.";
        }

        return "No such job with that query exists.";
    }


    private String getWebResult(String domainName) {
        if (domainName.equals("summary")) {
            return getWebSummary();
        } else {
            return getWebDomainResult(domainName);
        }
    }

    private String queryWebSummary() {


        if (webJobsSummaryCache != null) {
            if (webJobsSummaryCache.isDone()) {
                try {
                    return webJobsSummaryCache.get().toString();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            } else {
                return "Summary is not ready yet.";
            }
        }
        webJobsSummaryCache = executorService.submit(new WebCorpusSummarizer(webJobsFutures));
        return "Summary is not ready yet.";
    }

    private String getWebDomainResult(String domainName) {
        Future domainResult = domainSummaries.get(domainName);

        if (domainResult != null) {
            try {
                return domainResult.get().toString();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            Future<Map<String, Integer>> domainResultFuture = executorService.submit(new DomainSummarizer(domainName, webJobsFutures));
            domainSummaries.put(domainName, domainResultFuture);

            try {
                return domainResultFuture.get().toString();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }


        return "No job under such query exists.";
    }


    private String getWebSummary() {

        if (webJobsSummaryCache != null) {
            try {
                return webJobsSummaryCache.get().toString();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        webJobsSummaryCache = executorService.submit(new WebCorpusSummarizer(webJobsFutures));

        try {
            return webJobsSummaryCache.get().toString();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return "Oops, an error happened while processing web summary!";
        }

    }


    public void clearFileSummary() {
        fileSummaryJobs = null;
        System.out.println("> Cleared File Summary.");
    }


    public void clearWebSummary() {
        webJobsSummaryCache = null;
        System.out.println("> Cleared Web Summary");
    }


    public ExecutorService getExecutorService() {
        return executorService;
    }


    public Map<String, Future<Map<String, Integer>>> getFileJobs() {
        return fileJobs;
    }
}
