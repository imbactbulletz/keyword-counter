package job;

import java.util.Map;
import java.util.concurrent.Future;

public interface Job {

    ScanType getType();

    String getQuery();

    Future<Map<String,Integer>> getResult();

    void setResult(Future<Map<String,Integer>> resultMap);
}
