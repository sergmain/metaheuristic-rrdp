package ai.metaheuristic.rrdp_disk_storage;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Sergio Lissner
 * Date: 10/4/2022
 * Time: 10:19 PM
 */
public class RrdpData {
    public static class TaskParams {
        public final String code;
        public final List<String> paths;

        public TaskParams(String code, List<String> paths) {
            this.code = code;
            this.paths = paths;
        }
    }
}
