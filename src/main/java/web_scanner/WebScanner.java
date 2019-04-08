package web_scanner;

import app.Main;
import job.WebJob;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class WebScanner {
    private List<WebJob> scannedJobs;
    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutorService;
    public WebScanner() {
        this.scannedJobs = new CopyOnWriteArrayList<>();
        this.executorService = Executors.newCachedThreadPool();
        scheduledExecutorService = Executors.newScheduledThreadPool(2);
    }

    public void addJob(WebJob job) {


        if(isJobAlreadyScanned(job)) {
            return;
        } else {
            scannedJobs.add(job);
        }

        Future jobFutureResult = executorService.submit(new PageScannerWorker(job));
        job.setResult(jobFutureResult);
        Main.resultRetriever.addJob(job);
    }

    private boolean isJobAlreadyScanned(WebJob job) {

        for (WebJob scannedJob : scannedJobs) {
            if (scannedJob.getQuery().equals(job.getQuery())) {
//                System.out.println("Found duplicate. (" + job.getQuery() + ")");
                return true;
            }
        }

        return false;
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    public List<WebJob> getScannedJobs() {
        return scannedJobs;
    }
}
