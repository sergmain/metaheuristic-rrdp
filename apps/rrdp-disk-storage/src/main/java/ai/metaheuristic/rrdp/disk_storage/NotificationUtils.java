package ai.metaheuristic.rrdp.disk_storage;

import ai.metaheuristic.rrdp.core.paths.SessionPath;
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
    public static String getNotification(SessionPath sessionPath) {
        Path dataPath = MetadataUtils.getNotificationPath(sessionPath);
        return PersistenceUtils.getLatestContent(dataPath, (o)->true);
    }

    @SneakyThrows
    @Nullable
    public static String persistNotification(SessionPath sessionPath, String notificationXml, Supplier<LocalDate> localDateFunc ) {
        Path dataPath = MetadataUtils.getNotificationPath(sessionPath);
        return PersistenceUtils.persistContent(dataPath, ()->notificationXml, (o)->true, localDateFunc);
    }

    @SneakyThrows
    @Nullable
    public static Path getNotificationFile(SessionPath sessionPath) {
        Path dataPath = MetadataUtils.getNotificationPath(sessionPath);
        return PersistenceUtils.getLatestContentFile(dataPath, null);
    }
}
