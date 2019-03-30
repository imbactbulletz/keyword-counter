package job;

import java.util.Map;
import java.util.concurrent.Future;

public interface Job {

    ScanType getType();

    String getQuery();

    Map<String,Integer> getResult();
}
