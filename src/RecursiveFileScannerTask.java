import job.FileJob;

import java.io.*;
import java.util.*;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

public class RecursiveFileScannerTask extends RecursiveTask<Map> {

    private List<File> files;

    public RecursiveFileScannerTask(List<File> files) {
        this.files = new ArrayList<>(files);
    }

    @Override
    protected Map compute() {
        Map<String, Integer> resultMap = new HashMap<>();

        if(size(files) <= ApplicationSettings.fileScanningSizeLimit) {
            return scanFilesForKeywords(files);
        } else {
            int listSize = files.size();
            int mid = listSize / 2;

            RecursiveFileScannerTask leftTask = new RecursiveFileScannerTask(files.subList(0, mid));
            leftTask.fork();

            RecursiveFileScannerTask rightTask = new RecursiveFileScannerTask(files.subList(mid, files.size()));

            Map<String, Integer> rightResult = rightTask.compute();
            Map<String, Integer> leftResult = leftTask.join();

            resultMap.putAll(leftResult);
            rightResult.forEach( (k, v) -> resultMap.merge(k, v, (v1, v2) -> v1 + v2));
        }

        return resultMap;
    }



    private Map<String, Integer> scanFilesForKeywords(List<File> files) {
        Map<String, Integer> resultMap = new HashMap<>();


        for(File file : files) {
            Map<String, Integer> map = scanFileForKeywords(file);

            map.forEach( (key, value) -> resultMap.merge(key, value ,(value1, value2) -> value1 + value2));
        }

        return resultMap;
    }

    private Map<String, Integer> scanFileForKeywords(File file) {
        Map<String, Integer> resultMap = new HashMap<>();
        List<String> keywordList = ApplicationSettings.keywordsList;

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();

                List<String> words = Arrays.stream(line.split(" "))
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
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return resultMap;
    }

    private long size(List<File> files) {
        long size = 0;

        for(File file : files) {
            size += file.length();
        }

        return size;
    }
}
