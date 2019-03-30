import job.FileJob;
import job.Job;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Traverses through directories given by the CLI and searches for corpuses (subdirectories with a certain prefix name).
 * When a corpus has been found, its file attribute (last modified attribute) is saved in cache.
 * Directory Crawler sleeps and repeats this task periodically.
 * If last modified attribute of some file in a corpus has been changed, DirectoryCrawler creates a new Job for JobQueue.
 */
public class DirectoryCrawler extends Thread {
    private Queue<String> directoryPathsForScanning;
    private List<String> scannedDirectoryPaths;
    private Map <String, Map<String, Long>> cache;

    @Override
    public void run() {
        System.out.println("> Directory Crawler started.");

        directoryPathsForScanning = new ConcurrentLinkedQueue<>();
        scannedDirectoryPaths = new ArrayList<>();
        cache = new ConcurrentHashMap<>();

        while (true) {
            // not blocking because a copy of queue's iterator is made
            for (String directoryPath : directoryPathsForScanning) {
                // got poisoned
                if (directoryPath.equals(Messages.POSION_MESSAGE)) {
                    // poisons job dispatcher
                    Main.jobQueue.add(new FileJob(Messages.POSION_MESSAGE));

                    System.out.println("> Directory Crawler shutting down.");
                    return;
                }


                //finds corpuses
                List<File> foundCorpuses = findCorpuses(directoryPath);
                directoryPathsForScanning.remove(directoryPath);
                scannedDirectoryPaths.add(directoryPath);

                // gets last modified dates for each corpus' files
                for (File foundCorpus : foundCorpuses) {
                    Map map = getLastModifiedFiles(foundCorpus);


                    Map oldMap = cache.get(foundCorpus.getPath());

                    // last modified attribute has changed for a corpus
                    if(!sameLastModifiedValues(map, oldMap)) {
                        cache.put(foundCorpus.getPath(), map);

                        Main.jobQueue.add(new FileJob(foundCorpus.getName()));
                    }

                }
            }

            // finished scanning, goes to sleep
            if (directoryPathsForScanning.size() == 0) {
                try {
                    System.out.println("> Directory Crawler is going to sleep.");

                    Thread.sleep(ApplicationSettings.directoryCrawlerSleepTime);
                    // adds all of the previously scanned corpuses to be rescanned
                    directoryPathsForScanning.addAll(scannedDirectoryPaths);
                    scannedDirectoryPaths.clear();

                    System.out.println("> Directory Crawler came back from sleep.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addDirectory(String directory) {
        directoryPathsForScanning.add(directory);
    }

    /**
     * Recursively looks for corpuses, starting from a certain folder path.
     * @param folderPath Path of the folder from which the search begins.
     * @return
     */
    private List<File> findCorpuses(String folderPath) {
        try {
            List<File> corpusesList = Files.walk(Paths.get(new File(folderPath).getPath()))
                    .filter(Files::isDirectory)
                    .filter(path -> path.getFileName()
                            .toString()
                            .startsWith(ApplicationSettings.fileCorpusPrefix))
                    .map(Path::toFile)
                    .collect(Collectors.toList());

            return corpusesList;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Map<String, Long> getLastModifiedFiles(File corpus) {
        Map<String, Long> lastModifiedFilesMap;
        try {
            lastModifiedFilesMap = Files.walk(corpus.toPath())
                    .filter(Files::isRegularFile)
                    .map(path -> path.toFile())
                    .collect(Collectors.toMap(File::getName, File::lastModified));

            return lastModifiedFilesMap;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Compares whether two maps are the same
     * @param map
     * @param otherMap
     * @return
     */
    private boolean sameLastModifiedValues(Map<String, Long> map, Map<String, Long> otherMap) {
        if(map == null || otherMap == null) {
            return false;
        }

        if(map.keySet().size() != otherMap.keySet().size()) {
            return false;
        }

        for(String key: map.keySet()) {
            long value = map.get(key);
            long otherValue = otherMap.get(key);

            if(value != otherValue) {
                return false;
            }
        }

        return true;
    }

    public Map<String, Map<String, Long>> getCache() {
        return cache;
    }
}