package ai.metaheuristic.rrdp_disk_storage;

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
    public static String getSession(Path metadataPath) {
        Path sessionPath = MetadataUtils.getSessionPath(metadataPath);
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
    public static String persistSession(Path metadataPath, String session, Supplier<LocalDate> localDateFunc ) {
        Path serialPath = MetadataUtils.getSessionPath(metadataPath);
        return PersistenceUtils.persistContent(
                serialPath, ()->session, SessionUtils::verifyAsUUID, localDateFunc);
    }

    @Nullable
    @SneakyThrows
    public static Path getSessionFile(Path metadataPath) {
        Path sessionPath = MetadataUtils.getSessionPath(metadataPath);
        return PersistenceUtils.getLatestContentFile(sessionPath, null);
    }
}
