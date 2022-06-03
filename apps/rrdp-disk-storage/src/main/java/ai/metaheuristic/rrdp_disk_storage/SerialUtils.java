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
        return PersistenceUtils.getLatestContent(serialPath, SerialUtils::verifyAsInteger);
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
    public static String persistSerial(Path metadataPath, int serial, Supplier<LocalDate> localDateFunc ) {
        Path serialPath = PersistenceUtils.getSpecificMetadataPath(metadataPath, SERIAL_METADATA_PATH);
        return PersistenceUtils.persistContent(
                serialPath, ()->Integer.toString(serial), SerialUtils::verifyAsInteger, localDateFunc);
    }

    @SneakyThrows
    public static Path getSerialFile(Path metadataPath) {
        Path serialPath = PersistenceUtils.getSpecificMetadataPath(metadataPath, SERIAL_METADATA_PATH);
        return PersistenceUtils.getLatestContentFile(serialPath, null);
    }
}
