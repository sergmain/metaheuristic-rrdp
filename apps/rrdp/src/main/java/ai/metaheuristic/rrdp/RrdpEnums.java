package ai.metaheuristic.rrdp;

/**
 * @author Serge
 * Date: 6/1/2022
 * Time: 5:04 AM
 */
public class RrdpEnums {

    // Blake3-256
    public enum ChecksumAlgo { SHA1, SHA256, MD5 }

    public enum ProduceType {SNAPSHOT, DELTA}

    public enum EntryState { PUBLISHED(true), UPDATED(false), WITHDRAWAL(true);

        public final boolean rfc8182;

        EntryState(boolean rfc8182) {
            this.rfc8182 = rfc8182;
        }
    }
}
