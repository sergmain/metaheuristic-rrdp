package ai.metaheuristic.rrdp_disk_storage;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Sergio Lissner
 * Date: 6/3/2022
 * Time: 7:27 PM
 */
public class MetadataUtils {
    private static final String SERIAL_METADATA_PATH = "serial";
    private static final String SESSION_METADATA_PATH = "session";
    private static final String DATA_METADATA_PATH = "data";

    static Path getSerialPath(Path metadataPath) throws IOException {
        return PersistenceUtils.resolveSubPath(metadataPath, SERIAL_METADATA_PATH);
    }

    static Path getSessionPath(Path metadataPath) throws IOException {
        return PersistenceUtils.resolveSubPath(metadataPath, SESSION_METADATA_PATH);
    }

    static Path getDataPath(Path metadataPath) throws IOException {
        return PersistenceUtils.resolveSubPath(metadataPath, DATA_METADATA_PATH);
    }
}
