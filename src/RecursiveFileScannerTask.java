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

        List<File> takenFiles = takeFiles();

        if (files.size() > 0) {
            RecursiveFileScannerTask leftTask = new RecursiveFileScannerTask(files);
            leftTask.fork();

            RecursiveFileScannerTask rightTask = new RecursiveFileScannerTask(takenFiles);
            rightTask.compute();
        }

        return scanFilesForKeywords(takenFiles);
    }

    private List<File> takeFiles() {
        List<File> takenFiles = new ArrayList<>();

        int takenFilesSize = 0;

        for (File file : files) {
            takenFilesSize += files.size();

            if (takenFilesSize > ApplicationSettings.fileScanningSizeLimit) {
                break;
            }

            takenFiles.add(file);
        }

        for (File takenFile : takenFiles) {
            files.remove(takenFile);
        }

        return takenFiles;
    }

    private Map<String, Integer> scanFilesForKeywords(List<File> files) {
        Map<String, Integer> resultMap = new HashMap<>();

        for (File file : files) {
            resultMap.forEach((fileName, keywordCount) ->
                    scanFileForKeywords(file)
                            .merge(fileName, keywordCount, (keyWordCount1, keywordCount2) -> keyWordCount1 + keywordCount2));
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
                        .map(word -> word.replaceAll("\\P{Alnum}", "")).collect(Collectors.toList());


                for (String word : words) {
                    if (keywordList.contains(word)) {
                        int val = resultMap.get(word);

                        if (val == 0) {
                            resultMap.put(word, 1);
                        } else {
                            resultMap.put(word, resultMap.get(word) + 1);
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
}
