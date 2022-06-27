package ai.metaheuristic.rrdp_srv;

import ai.metaheuristic.rrdp_disk_storage.MetadataUtils;
import ai.metaheuristic.rrdp_disk_storage.NotificationUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Sergio Lissner
 * Date: 6/27/2022
 * Time: 12:14 AM
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContentService {

    private final Globals globals;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    private final Map<String, String> notificationContents = new HashMap<>();

    @SneakyThrows
    @Nullable
    public String getEntryContent(String dataCode, String entryFilename) {
        Path metadataPath = globals.path.metadata.path.resolve(dataCode);
        if (Files.notExists(metadataPath)) {
            return null;
        }
        Path entryPath = MetadataUtils.getEntryPath(metadataPath);
        Path entryFile = entryPath.resolve(entryFilename);
        if (Files.notExists(entryFile)) {
            return null;
        }
        final String entryStr = Files.readString(entryFile);
        return entryStr;
    }

    @Nullable
    @SneakyThrows
    public Path getDataContentPath(String dataUri) {
        Path dataPath = globals.path.source.path.resolve(dataUri);
        if (Files.notExists(dataPath)) {
            return null;
        }
        Path normalizedDataPath = dataPath.normalize();
        if (!normalizedDataPath.startsWith(dataPath)) {
            log.warn("Data uri "+ dataUri+" doesn't point to source path");
            return null;
        }
        return normalizedDataPath;
    }

    public String getNotificationContent(String dataCode) {
        try {
            readLock.lock();
            return notificationContents.get(dataCode);
        } finally {
            readLock.unlock();
        }
    }

    @SneakyThrows
    public void refreshNotificationContents() {
        Files.walkFileTree(globals.path.metadata.path, EnumSet.noneOf(FileVisitOption.class), 1, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path p, BasicFileAttributes attrs) {
                if (!Files.isDirectory(p)) {
                    log.warn("Path "+globals.path.metadata.path+" must contain only dirs, "+p+" is file actually");
                    return FileVisitResult.CONTINUE;
                }
                final String dataCode = p.getFileName().toString();
                String content = NotificationUtils.getNotification(p);
                try {
                    writeLock.lock();
                    notificationContents.put(dataCode, content);
                } finally {
                    writeLock.unlock();
                }

                return FileVisitResult.CONTINUE;
            }
        });
    }
}
