package job;

import java.util.Map;
import java.util.concurrent.Future;

public class FileJob implements Job {

    private ScanType type = ScanType.FILE;
    private String query;
    private Future<Map<String,Integer>> resultMap;

    public FileJob(String corpusName) {
        this.query = corpusName;
    }

    @Override
    public ScanType getType() {
        return type;
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

}
