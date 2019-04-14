package job;

import java.util.Map;
import java.util.concurrent.Future;

public class WebJob implements Job {

    private ScanType scanType = ScanType.WEB;
    private String query;
    private Future<Map<String,Integer>> resultMap;
    private long hops;

    public WebJob(String pageURL, long hops) {
        if(pageURL.startsWith("web|")) {
            pageURL = pageURL.substring("web|".length());
        }

        this.query = pageURL;
        this.hops = hops;
    }

    @Override
    public ScanType getType() {
        return scanType;
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public Future<Map<String,Integer>> getResult() {
        return resultMap;
    }

    @Override
    public void setResult(Future<Map<String,Integer>> resultMap) {
        this.resultMap = resultMap;
    }

    public long getHops() {
        return hops;
    }

}
