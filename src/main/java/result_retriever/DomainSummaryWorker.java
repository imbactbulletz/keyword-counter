package result_retriever;

import misc.ApplicationSettings;

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

        List<Future> domainFutures = new ArrayList<>();

        webJobFutures.forEach( (k, v) -> {
            if(k.equals(domainName)) {
                domainFutures.add(v);
            }
        });

        for(Future<Map> pageFuture : domainFutures) {
            try {
                Map<String, Integer> pageResult = pageFuture.get();

                pageResult.forEach( (key, value) -> domainSummarizedMap.merge(key, value ,(value1, value2) -> value1 + value2));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        return domainSummarizedMap;
    }


    private Map<String, Integer> scanForKeywords(String text) {
        Map<String, Integer> resultMap = new HashMap<>();
        List<String> keywordList = ApplicationSettings.keywordsList;


        List<String> words = Arrays.stream(text.split(" "))
                .map(word -> word.replaceAll("\\p{Punct}+$", "")).collect(Collectors.toList());

        for (String word : words) {
            if (keywordList.contains(word)) {
                if(resultMap.containsKey(word)) {
                    resultMap.put(word, resultMap.get(word) + 1);
                } else {
                    resultMap.put(word, 1);
                }
            }
        }

        return resultMap;
    }
}
