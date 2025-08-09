package ai.metaheuristic.rrdp.core;

/**
 * @author Serge
 * Date: 6/1/2022
 * Time: 5:04 AM
 */
public class RrdpEnums {

    // Blake3-256
    public enum ChecksumAlgo { SHA1, SHA256, MD5, BLAKE }

    public enum NotificationEntryType {SNAPSHOT, DELTA}

    public enum EntryState { PUBLISH(true), UPDATE(false), WITHDRAWAL(true);

        public final boolean rfc8182;

        EntryState(boolean rfc8182) {
            this.rfc8182 = rfc8182;
        }

        public static EntryState to(String s) {
            switch(s) {
                case "publish":
                    return PUBLISH;
                case "withdraw":
                    return WITHDRAWAL;
                case "update":
                    return UPDATE;
                default:
                    throw new IllegalStateException("Unknown state: " + s);
            }
        }
    }
}
