package ai.metaheuristic.rrdp_disk_storage;

import lombok.SneakyThrows;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.function.Supplier;

/**
 * @author Sergio Lissner
 * Date: 6/3/2022
 * Time: 8:35 PM
 */
public class ChechsumUtils {

    @Nullable
    @SneakyThrows
    public static String getChecksum(Path checksumPath) {
        return PersistenceUtils.getLatestContent(checksumPath, (s)->true);
    }

    private static Boolean verifyAsInteger(@Nullable String s) {
        return true;
    }

    @Nullable
    @SneakyThrows
    public static String persistChecksum(Path checksumPath, String json, Supplier<LocalDate> localDateFunc ) {
        return PersistenceUtils.persistContent(
                checksumPath, ()->json, (s)->true, localDateFunc);
    }

    @Nullable
    @SneakyThrows
    public static Path getChecksumFile(Path checksumPath) {
        return PersistenceUtils.getLatestContentFile(checksumPath, null);
    }

}
