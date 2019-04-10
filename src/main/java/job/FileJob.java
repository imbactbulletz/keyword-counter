package job;

import java.util.Map;
import java.util.concurrent.Future;

public class FileJob implements Job {

    private ScanType type = ScanType.FILE;
    private String query;
    private Future<Map> resultMap;

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
    public Future<Map> getResult() {
        return resultMap;
    }

    @Override
    public void setResult(Future<Map> resultMap) {
        this.resultMap = resultMap;
    }

}
