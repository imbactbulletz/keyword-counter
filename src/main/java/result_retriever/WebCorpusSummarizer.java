package result_retriever;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import misc.URIUtil;

import java.util.*;
import java.util.concurrent.*;

public class WebCorpusSummarizer implements Callable<Map<String, Map<String, Integer>>> {

    private ExecutorService executorService;

    private Map<String, Future<Map<String, Integer>>> webJobsFutures;

    private Map<String, Future<Map<String, Integer>>> domainSummary;

    private Map<String, Map<String, Integer>> resultMap;

    public WebCorpusSummarizer(Map<String, Future<Map<String, Integer>>> webJobsFutures) {
        this.executorService = Executors.newCachedThreadPool();

        this.webJobsFutures = webJobsFutures;

        this.domainSummary = new HashMap<>();

        this.resultMap = new HashMap<>();
    }

    @Override
    public Map<String, Map<String, Integer>> call() throws Exception {
        List<String> domainNames = getDomainNames(webJobsFutures);

        for(String domainName : domainNames) {
            Future<Map<String, Integer>> domainResultFuture = executorService
                    .submit(new DomainSummarizer(domainName, webJobsFutures));

            domainSummary.put(domainName, domainResultFuture);
        }


        for(String domainName : domainNames) {
            Future<Map<String, Integer>> domainResultFuture =  domainSummary.get(domainName);
            Map<String, Integer> domainResult = domainResultFuture.get();

            if(domainResult == null) {
                continue;
            }

            domainResult.forEach((key, value) -> {
                Map<String, Integer> oldDomainResult = resultMap.get(domainName);

                if(oldDomainResult == null) {
                    resultMap.put(domainName, domainResult);
                    return;
                } else {
                    oldDomainResult.merge(key, value, (v1, v2) -> v1 + v2);
                }
            });
        }

        return resultMap;
    }

    private List<String> getDomainNames(Map<String, Future<Map<String, Integer>>> webJobs) {
        Set<String> domainNamesSet = new HashSet<>();

        List<String> pageURLs = new ArrayList<>(webJobs.keySet());

        for(String pageURL: pageURLs) {
            String domainName = URIUtil.convertPageURLtoDomain(pageURL);

            if(domainName == null){
                continue;
            }

            domainNamesSet.add(domainName);
        }

        return new ArrayList<>(domainNamesSet);
    }

}
