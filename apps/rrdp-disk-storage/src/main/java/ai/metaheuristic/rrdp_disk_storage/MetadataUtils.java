package ai.metaheuristic.rrdp_disk_storage;

import ai.metaheuristic.rrdp.paths.MetadataPath;
import ai.metaheuristic.rrdp.paths.SessionPath;

import java.nio.file.Path;

/**
 * @author Sergio Lissner
 * Date: 6/3/2022
 * Time: 7:27 PM
 */
public class MetadataUtils {
    private static final String SERIAL_METADATA_PATH = "serial";
    private static final String SESSION_METADATA_PATH = "session";
    private static final String NOTIFICATION_METADATA_PATH = "notification";
    public static final String ENTRY_METADATA_PATH = "entry";
    private static final String CHECKSUM_METADATA_PATH = "checksum";

    public static Path getPathForSession(MetadataPath metadataPath) {
        return PersistenceUtils.resolveSubPath(metadataPath.path, SESSION_METADATA_PATH);
    }

    public static Path getSerialPath(SessionPath sessionPath) {
        return PersistenceUtils.resolveSubPath(sessionPath.path, SERIAL_METADATA_PATH);
    }

    public static Path getNotificationPath(SessionPath sessionPath) {
        return PersistenceUtils.resolveSubPath(sessionPath.path, NOTIFICATION_METADATA_PATH);
    }

    public static Path getEntryPath(SessionPath sessionPath) {
        return PersistenceUtils.resolveSubPath(sessionPath.path, ENTRY_METADATA_PATH);
    }

    public static Path getChecksumPath(SessionPath sessionPath) {
        return PersistenceUtils.resolveSubPath(sessionPath.path, CHECKSUM_METADATA_PATH);
    }
}
