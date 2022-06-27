package ai.metaheuristic.rrdp_disk_storage;

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

    public static Path getSerialPath(Path metadataPath) {
        return PersistenceUtils.resolveSubPath(metadataPath, SERIAL_METADATA_PATH);
    }

    public static Path getSessionPath(Path metadataPath) {
        return PersistenceUtils.resolveSubPath(metadataPath, SESSION_METADATA_PATH);
    }

    public static Path getNotificationPath(Path metadataPath) {
        return PersistenceUtils.resolveSubPath(metadataPath, NOTIFICATION_METADATA_PATH);
    }

    public static Path getEntryPath(Path metadataPath) {
        return PersistenceUtils.resolveSubPath(metadataPath, ENTRY_METADATA_PATH);
    }

    public static Path getChecksumPath(Path metadataPath) {
        return PersistenceUtils.resolveSubPath(metadataPath, CHECKSUM_METADATA_PATH);
    }
}
