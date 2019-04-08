package result_retriever;

import misc.ApplicationSettings;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class DomainSummaryWorker implements Callable<Map<String, Integer>> {

    String domainName;
    private Map<String, Future<Map>> webJobFutures;

    public DomainSummaryWorker(String domainName, Map<String, Future<Map>> webJobFutures) {
        this.domainName = domainName;
        this.webJobFutures = webJobFutures;
    }

    @Override
    public Map<String, Integer> call() {
        Map<String, Integer> domainSummarizedMap = new HashMap<>();

        List<Future> domainPageFutures = new ArrayList<>();

        webJobFutures.forEach( (pageURL, noOfOccurencies) -> {
            if(convertPageURLtoDomain(pageURL).equals(domainName)) {
                domainPageFutures.add(noOfOccurencies);
            }
        });

        for(Future<Map> pageFuture : domainPageFutures) {
            try {
                Map<String, Integer> pageResult = pageFuture.get();

                if(pageResult == null) {
                    continue;
                }

                pageResult.forEach( (key, value) -> domainSummarizedMap.merge(key, value ,(value1, value2) -> value1 + value2));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();

            }
        }

        return domainSummarizedMap;
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
}
