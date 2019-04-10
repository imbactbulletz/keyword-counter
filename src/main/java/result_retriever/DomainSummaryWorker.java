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
        String domain = null;
        try {
            // get rid of spaces in parameters (i ran into one)
            URI uri = new URI(pageURL.split(" ")[0]);

            domain = new URI(uri.getScheme(),
                    uri.getAuthority(),
                    null,
                    null, // Ignore the query part of the input url
                    uri.getFragment()).toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if(domain.startsWith("http://")) {
            domain = domain.substring("http://".length());
        }

        if(domain.startsWith("https://")) {
            domain = domain.substring("https://".length());
        }

        if(domain.startsWith("www.")) {
            domain = domain.substring("www.".length());
        }

        return domain;
    }
}
