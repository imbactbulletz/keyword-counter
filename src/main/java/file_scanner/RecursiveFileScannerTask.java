package file_scanner;

import misc.ApplicationSettings;

import java.io.*;
import java.util.*;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

public class RecursiveFileScannerTask extends RecursiveTask<Map<String, Integer>> {

    private List<File> files;

    public RecursiveFileScannerTask(List<File> files) {
        this.files = new ArrayList<>(files);
    }

    @Override
    protected Map<String, Integer> compute() {
        List<File> takenFiles = takeFiles(files);
        Map<String,Integer> resultMap = new HashMap<>();

        if(files.size() > 0) {
            RecursiveFileScannerTask leftTask = new RecursiveFileScannerTask(files);
            leftTask.fork();

            RecursiveFileScannerTask rightTask = new RecursiveFileScannerTask(takenFiles);

            Map<String, Integer> rightTaskResult = rightTask.compute();
            Map<String, Integer> leftTaskResult = leftTask.join();

            resultMap.putAll(rightTaskResult);

            leftTaskResult.forEach((k,v) -> resultMap.merge(k,v, (v1,v2) -> v1 + v2));

            return resultMap;

        } else {
            return scanFilesForKeywords(takenFiles);
        }
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

        } catch (Exception e) {
            System.out.println("Could not read file with name: " + file.getName());
            return new HashMap<>();
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

    private List<File> takeFiles(List<File> fileList) {
        List<File> takenFiles = new ArrayList<>();

        int takenFilesSizeSum = 0;

        // take as many files until it goes past the limit
        for(File file: fileList) {
            takenFilesSizeSum += file.length();

            takenFiles.add(file);

            if(takenFilesSizeSum > ApplicationSettings.fileScanningSizeLimit) {
                break;
            }
        }

        // remove taken files from the original list
        for(File takenFile: takenFiles) {
            fileList.remove(takenFile);
        }

        return takenFiles;
    }

}
