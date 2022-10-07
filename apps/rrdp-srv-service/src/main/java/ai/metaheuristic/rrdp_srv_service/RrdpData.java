package ai.metaheuristic.rrdp_srv_service;

import org.springframework.lang.Nullable;

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

    public static class RrdpServerStatus {
        @Nullable
        public final RrdpData.TaskParams activeTaskParams;
        public final List<TaskParams> list;

        public RrdpServerStatus(@Nullable TaskParams activeTaskParams, List<TaskParams> list) {
            this.activeTaskParams = activeTaskParams;
            this.list = list;
        }
    }
}
