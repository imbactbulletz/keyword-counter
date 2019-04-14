package result_retriever;

import app.Main;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class FileCorpusSummarizer  implements Callable<String> {

    @Override
    public String call() throws Exception {
        Map<String, Map<String, Integer>> summaryMap = new ConcurrentHashMap<>();

        Main.resultRetriever.getFileJobs().forEach((k, v) -> {
            try {
                summaryMap.put(k, v.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

        return summaryMap.toString();
    }
}
