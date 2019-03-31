import job.FileJob;
import job.Job;
import job.ScanType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class JobDispatcher extends Thread {


    @Override
    public void run() {
        System.out.println("> Job Dispatcher started.");
        while(true) {
            try {
                Job job = Main.jobQueue.take();

                // got poisoned
                if(job.getQuery().equals(Messages.POSION_MESSAGE)) {
                    System.out.println("> Job Dispatcher shutting down.");
                    Main.fileScannerPool.shutdown();

                    // todo poison file scanner and web scanner
                    return;
                }

                if(job.getType() == ScanType.FILE) {
                    String corpus = job.getQuery().substring("file|".length());

                    Map<File, Long> cachedCorpusFiles = Main.directoryCrawlerThread.getCache().get(corpus);

                    List<File> corpusFiles = cachedCorpusFiles.keySet().stream().collect(Collectors.toList());

                    System.out.println("Recieved a Job. (" + job.getType() + "," + job.getQuery() + ")");

                    Future<Map> jobResult = Main.fileScannerPool.submit(new RecursiveFileScannerTask(corpusFiles));
                    job.setResult(jobResult);

                    try {
                        Map j = jobResult.get();

                        System.out.println(j);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
