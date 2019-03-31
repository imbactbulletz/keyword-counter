package job;

import java.util.Map;
import java.util.concurrent.Future;

public interface Job {

    ScanType getType();

    String getQuery();

    Future<Map> getResult();

    void setResult(Future<Map> resultMap);
}
