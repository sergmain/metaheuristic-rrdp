package ai.metaheuristic.rrdp;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergio Lissner
 * Date: 6/28/2022
 * Time: 3:04 PM
 */
public class RrdpEntryXml {

    @AllArgsConstructor
    @NoArgsConstructor
    public static class Entry {
        public RrdpEnums.EntryState state;
        public String uri;
        public String hash;
        @Nullable
        public Integer length;

        private StringBuilder sb = null;

        @Nullable
        public String getContent() {
            return sb==null ? null : sb.toString();
        }

        public void add(String s) {
            if (sb==null) {
                sb = new StringBuilder(s);
            }
            else {
                sb.append(s);
            }
        }
    }

    public String sessionId;
    public int serial;
    public RrdpEnums.NotificationEntryType type;
    public final List<Entry> entries = new ArrayList<>();

}
