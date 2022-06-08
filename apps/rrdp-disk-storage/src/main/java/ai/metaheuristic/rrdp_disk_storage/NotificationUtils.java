package ai.metaheuristic.rrdp_disk_storage;

import lombok.SneakyThrows;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.function.Supplier;

/**
 * @author Sergio Lissner
 * Date: 6/8/2022
 * Time: 12:01 AM
 */
public class NotificationUtils {

    @SneakyThrows
    @Nullable
    public static String getNotification(Path metadataPath) {
        Path dataPath = MetadataUtils.getDataPath(metadataPath);
        return PersistenceUtils.getLatestContent(dataPath, (o)->true);
    }

    @SneakyThrows
    public static String persistNotification(Path metadataPath, String notificationXml, Supplier<LocalDate> localDateFunc ) {
        Path dataPath = MetadataUtils.getDataPath(metadataPath);
        return PersistenceUtils.persistContent(dataPath, ()->notificationXml, (o)->true, localDateFunc);
    }

    @SneakyThrows
    @Nullable
    public static Path getNotificationFile(Path metadataPath) {
        Path dataPath = MetadataUtils.getDataPath(metadataPath);
        return PersistenceUtils.getLatestContentFile(dataPath, null);
    }
}
