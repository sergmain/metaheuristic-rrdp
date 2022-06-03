package ai.metaheuristic.rrdp_disk_storage;

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

    private static final String SERIAL_METADATA_PATH = "serial";

    @Nullable
    @SneakyThrows
    public static String getSerial(Path metadataPath) {
        Path serialPath = PersistenceUtils.getSpecificMetadataPath(metadataPath, SERIAL_METADATA_PATH);
        return PersistenceUtils.getLatestContent(serialPath, SerialUtils::verifyInteger);
    }

    private static Boolean verifyInteger(@Nullable String s) {
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
    public static String persistSerial(Path metadataPath, int serial, Supplier<LocalDate> localDateFunc ) {
        Path serialPath = PersistenceUtils.getSpecificMetadataPath(metadataPath, SERIAL_METADATA_PATH);
        return PersistenceUtils.persistContent(
                serialPath, ()->Integer.toString(serial), SerialUtils::verifyInteger, localDateFunc);
    }

    @SneakyThrows
    public static Path getSerialFile(Path metadataPath) {
        return getSerialFile(metadataPath, LocalDate::now);
    }

    @SuppressWarnings("WeakerAccess")
    @SneakyThrows
    public static Path getSerialFile(Path metadataPath, Supplier<LocalDate> localDateFunc) {
        Path serialPath = PersistenceUtils.getSpecificMetadataPath(metadataPath, SERIAL_METADATA_PATH);
        return PersistenceUtils.getLatestContentFile(serialPath, localDateFunc);
    }
}
