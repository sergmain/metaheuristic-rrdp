package ai.metaheuristic.rrdp.disk_storage;

import ai.metaheuristic.rrdp.core.paths.MetadataPath;
import lombok.SneakyThrows;

import javax.annotation.Nullable;
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

    @SneakyThrows
    @Nullable
    public static String getSession(MetadataPath metadataPath) {
        Path sessionPath = MetadataUtils.getPathForSession(metadataPath);
        return PersistenceUtils.getLatestContent(sessionPath, SessionUtils::verifyAsUUID);
    }

    private static boolean verifyAsUUID(String s) {
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

    @Nullable
    @SneakyThrows
    public static String persistSession(MetadataPath metadataPath, String session, Supplier<LocalDate> localDateFunc ) {
        Path pathForSession = MetadataUtils.getPathForSession(metadataPath);
        return PersistenceUtils.persistContent(
                pathForSession, ()->session, SessionUtils::verifyAsUUID, localDateFunc);
    }

    @Nullable
    @SneakyThrows
    public static Path getSessionFile(MetadataPath metadataPath) {
        Path sessionPath = MetadataUtils.getPathForSession(metadataPath);
        return PersistenceUtils.getLatestContentFile(sessionPath, null);
    }
}
