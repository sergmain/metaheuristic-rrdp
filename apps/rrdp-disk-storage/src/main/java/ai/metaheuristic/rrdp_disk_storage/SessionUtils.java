package ai.metaheuristic.rrdp_disk_storage;

import lombok.SneakyThrows;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * @author Sergio Lissner
 * Date: 6/2/2022
 * Time: 8:56 PM
 */
public class SessionUtils {

    private static final String SESSION_METADATA_PATH = "session";

    @SneakyThrows
    public static String getSession(Path metadataPath) {
        Path sessionPath = PersistenceUtils.getSpecificMetadataPath(metadataPath, SESSION_METADATA_PATH);
        return PersistenceUtils.getLatestContent(sessionPath, SessionUtils::verifyUUID);
    }

    private static boolean verifyUUID(String s) {
        try {
            //noinspection unused
            UUID sessionUUD = UUID.fromString(s);
            return true;
        }
        catch (IllegalArgumentException e) {
            //
        }
        return false;
    }

    @SneakyThrows
    public static String persistSession(Path metadataPath, String session, Supplier<LocalDate> localDateFunc ) {
        Path serialPath = PersistenceUtils.getSpecificMetadataPath(metadataPath, SESSION_METADATA_PATH);
        return PersistenceUtils.persistContent(
                serialPath, ()->session, SessionUtils::verifyUUID, localDateFunc);
    }

    @SuppressWarnings("WeakerAccess")
    @SneakyThrows
    public static Path getSessionFile(Path metadataPath) {
        Path sessionPath = PersistenceUtils.getSpecificMetadataPath(metadataPath, SESSION_METADATA_PATH);
        return PersistenceUtils.getLatestContentFile(sessionPath, null);
    }
}
