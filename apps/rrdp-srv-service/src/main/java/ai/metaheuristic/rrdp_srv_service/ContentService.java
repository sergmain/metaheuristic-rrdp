package ai.metaheuristic.rrdp_srv_service;

import ai.metaheuristic.rrdp_disk_storage.MetadataUtils;
import ai.metaheuristic.rrdp_disk_storage.NotificationUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static class NotificationCache {
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

        private final Map<String, String> notificationContents = new HashMap<>();
    }

    public static class CodesCache {
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

        private List<String> codes = new ArrayList<>();
    }

    public final NotificationCache notificationCache = new NotificationCache();
    public final CodesCache codesCache = new CodesCache();

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
            notificationCache.readLock.lock();
            return notificationCache.notificationContents.get(dataCode);
        } finally {
            notificationCache.readLock.unlock();
        }
    }

    @SneakyThrows
    public void refreshNotificationContents() {
        if (Files.notExists(globals.path.metadata.path)) {
            throw new RuntimeException("Path "+ globals.path.metadata.path +" doesn't exist");
        }
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
                    notificationCache.writeLock.lock();
                    notificationCache.notificationContents.put(dataCode, content);
                } finally {
                    notificationCache.writeLock.unlock();
                }

                return FileVisitResult.CONTINUE;
            }
        });
    }

    @SneakyThrows
    public List<String> getDataCodes() {
        try {
            codesCache.readLock.lock();
            return codesCache.codes;
        } finally {
            codesCache.readLock.unlock();
        }
    }

    @SneakyThrows
    public void refreshDataCodes() {
        try {
            codesCache.writeLock.lock();
            final List<String> collect;
            try (Stream<Path> stream = Files.list(globals.path.source.path)) {
                collect = stream
                        .filter(Files::isDirectory)
                        .map(o -> o.getFileName().toString())
                        .collect(Collectors.toList());
            }
            codesCache.codes = collect;
        } finally {
            codesCache.writeLock.unlock();
        }
    }
}
