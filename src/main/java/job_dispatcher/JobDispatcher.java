package job_dispatcher;

import app.Main;
import file_scanner.RecursiveFileScannerTask;
import job.FileJob;
import job.Job;
import job.ScanType;
import job.WebJob;
import misc.Messages;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

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
                    System.out.println("> Web Scanner shutting down.");
                    Main.webScannerPool.getScheduledExecutorService().shutdown();
                    System.out.println("> Result Retriever shutting down.");
                    Main.resultRetriever.getExecutorService().shutdown();
                    return;
                }

                if(job.getType() == ScanType.FILE) {
                    String corpus = job.getQuery();

                    Map<File, Long> cachedCorpusFiles = Main.directoryCrawlerThread.getCache().get(corpus);

                    List<File> corpusFiles = new ArrayList<>(cachedCorpusFiles.keySet());

//                    System.out.println("Job Dispatcher took a File Job (" + job.getType() + ", " + job.getQuery() + ")");

                    Future<Map<String,Integer>> jobResult = Main.fileScannerPool.submit(new RecursiveFileScannerTask(corpusFiles));
                    job.setResult(jobResult);
                    Main.resultRetriever.addJob(job);
                }


                if(job.getType() == ScanType.WEB) {
//                    System.out.println("Job Dispatcher: Scheduling job with query " + job.getQuery() +" to Web Scanner");
                    Main.webScannerPool.addJob((WebJob)job);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
