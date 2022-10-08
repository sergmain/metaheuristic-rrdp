package ai.metaheuristic.rrdp_disk_storage;

import ai.metaheuristic.rrdp.paths.SessionPath;
import lombok.SneakyThrows;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.function.Supplier;

/**
 * @author Sergio Lissner
 * Date: 6/3/2022
 * Time: 12:42 AM
 */
public class SerialUtils {

    @Nullable
    @SneakyThrows
    public static Integer getSerial(SessionPath sessionPath) {
        Path serialPath = MetadataUtils.getSerialPath(sessionPath);
        final String content = PersistenceUtils.getLatestContent(serialPath, SerialUtils::verifyAsInteger);
        return content!=null ? Integer.valueOf(content) : null;
    }

    private static Boolean verifyAsInteger(@Nullable String s) {
        if (s ==null) {
            return false;
        }
        try {
            //noinspection unused
            int serial = Integer.parseInt(s);
            return true;
        }
        catch (IllegalArgumentException e) {
            //
        }
        return false;
    }

    @SneakyThrows
    @Nullable
    public static String persistSerial(SessionPath sessionPath, int serial, Supplier<LocalDate> localDateFunc ) {
        Path serialPath = MetadataUtils.getSerialPath(sessionPath);
        return PersistenceUtils.persistContent(
                serialPath, () -> Integer.toString(serial), SerialUtils::verifyAsInteger, localDateFunc);
    }

    @SneakyThrows
    @Nullable
    public static Path getSerialFile(SessionPath sessionPath) {
        Path serialPath = MetadataUtils.getSerialPath(sessionPath);
        return PersistenceUtils.getLatestContentFile(serialPath, null);
    }
}
