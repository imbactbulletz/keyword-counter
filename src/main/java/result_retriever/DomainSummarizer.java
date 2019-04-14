package result_retriever;

import misc.ApplicationSettings;
import misc.URIUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class DomainSummarizer implements Callable<Map<String, Integer>> {

    String domainName;
    private Map<String, Future<Map<String, Integer>>> webJobFutures;

    public DomainSummarizer(String domainName, Map<String, Future<Map<String, Integer>>> webJobFutures) {
        this.domainName = domainName;
        this.webJobFutures = new HashMap<>(webJobFutures);
    }

    @Override
    public Map<String, Integer> call() {
        Map<String, Integer> domainSummarizedMap = new HashMap<>();

        List<Future<Map<String,Integer>>> domainPageFutures = new ArrayList<>();

        webJobFutures.forEach( (pageURL, noOfOccurencies) -> {
            if(URIUtil.convertPageURLtoDomain(pageURL).equals(domainName)) {
                domainPageFutures.add(noOfOccurencies);
            }
        });

        for(Future<Map<String,Integer>> pageFuture : domainPageFutures) {
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
}
