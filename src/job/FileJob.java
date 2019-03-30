package job;

import java.util.Map;

public class FileJob implements Job {

    private ScanType type = ScanType.FILE;
    private String query;

    private Map<String, Integer> resultMap;

    public FileJob(String query) {
        this.query = query;
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
    public Map<String, Integer> getResult() {
        return resultMap;
    }

}
