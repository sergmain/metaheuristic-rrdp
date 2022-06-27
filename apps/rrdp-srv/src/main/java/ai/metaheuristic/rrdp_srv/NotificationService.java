package ai.metaheuristic.rrdp_srv;

import ai.metaheuristic.rrdp.RrdpEnums;
import ai.metaheuristic.rrdp_disk_storage.ChecksumPath;
import ai.metaheuristic.rrdp_disk_storage.FileChecksumProcessor;
import ai.metaheuristic.rrdp_disk_storage.NotificationUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
public class NotificationService {

    private final Globals globals;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    private final Map<String, String> notificationContents = new HashMap<>();

    public String getContent(String dataCode) {
        try {
            readLock.lock();
            return notificationContents.get(dataCode);
        } finally {
            readLock.unlock();
        }
    }

    @SneakyThrows
    public void refresh() {
        Files.walkFileTree(globals.metadata.path.path, EnumSet.noneOf(FileVisitOption.class), 1, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path p, BasicFileAttributes attrs) {
                if (!Files.isDirectory(p)) {
                    log.warn("Path "+globals.metadata.path.path+" must contain only dirs, "+p+" is file actually");
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
