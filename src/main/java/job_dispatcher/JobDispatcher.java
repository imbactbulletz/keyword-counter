package job_dispatcher;

import app.Main;
import file_scanner.RecursiveFileScannerTask;
import job.FileJob;
import job.Job;
import job.ScanType;
import misc.Messages;

import java.io.File;
import java.util.List;
import java.util.Map;
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
                    System.out.println("> File Scanner shutting down.");
                    Main.fileScannerPool.shutdown();

                    return;
                }

                if(job.getType() == ScanType.FILE) {
                    String corpus = job.getQuery().substring("file|".length());

                    Map<File, Long> cachedCorpusFiles = Main.directoryCrawlerThread.getCache().get(corpus);

                    List<File> corpusFiles = cachedCorpusFiles.keySet().stream().collect(Collectors.toList());

//                    System.out.println("Job Dispatcher took a File Job (" + job.getType() + ", " + job.getQuery() + ")");

                    Future<Map> jobResult = Main.fileScannerPool.submit(new RecursiveFileScannerTask(corpusFiles));
                    job.setResult(jobResult);
                    Main.resultRetriever.addFileJob((FileJob)job);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
