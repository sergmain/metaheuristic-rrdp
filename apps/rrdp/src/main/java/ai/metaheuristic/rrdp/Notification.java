package ai.metaheuristic.rrdp;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergio Lissner
 * Date: 6/2/2022
 * Time: 10:15 AM
 */
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

    @AllArgsConstructor
    @NoArgsConstructor
    public static class Entry {
        public RrdpEnums.ProduceType type;
        public String uri;
        public String hash;
        public Integer serial;
    }

    public String sessionId;
    public int serial;
    public final List<Entry> entries = new ArrayList<>();

}
