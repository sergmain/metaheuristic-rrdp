package ai.metaheuristic.rrdp_srv_service;

import ai.metaheuristic.rrdp.paths.MetadataPath;
import ai.metaheuristic.rrdp.paths.SessionPath;
import ai.metaheuristic.rrdp_disk_storage.MetadataUtils;
import ai.metaheuristic.rrdp_disk_storage.PersistenceUtils;
import ai.metaheuristic.rrdp_disk_storage.SessionUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
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
        public final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        public final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        public final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

        public List<String> codes = new ArrayList<>();
    }

    public final NotificationCache notificationCache = new NotificationCache();
    public final CodesCache codesCache = new CodesCache();

    @PostConstruct
    public void init() {
        refreshDataCodes();
    }

    @SneakyThrows
    @Nullable
    public String getEntryContent(String dataCode, String entryFilename) {
        MetadataPath metadataPath = new MetadataPath(globals.path.metadata.path.resolve(dataCode));
        if (Files.notExists(metadataPath.path)) {
            return null;
        }
        SessionPath sessionPath = getSessionPath(metadataPath);

        Path entryPath = MetadataUtils.getEntryPath(sessionPath);
        Path entryFile = entryPath.resolve(entryFilename);
        if (Files.notExists(entryFile)) {
            return null;
        }
        final String entryStr = Files.readString(entryFile);
        return entryStr;
    }

    @SneakyThrows
    @NonNull
    private static SessionPath getSessionPath(MetadataPath metadataPath) {
        String session = SessionUtils.getSession(metadataPath);
        if (session==null) {
            session = SessionUtils.persistSession(metadataPath, UUID.randomUUID().toString(), LocalDate::now);
        }
        if (session==null) {
            throw new IllegalStateException("(session==null)");
        }
        SessionPath sessionPath = new SessionPath(metadataPath.path.resolve(session));
        if (Files.notExists(sessionPath.path)) {
            Files.createDirectory(sessionPath.path);
        }
        return sessionPath;
    }

    @Nullable
    @SneakyThrows
    public Path getDataContentPath(String dataUri) {
        String decodedPath = decodePath(dataUri);
        Path dataPath = globals.path.source.path.resolve(decodedPath);
        Path normalizedDataPath = dataPath.normalize();
        if (!normalizedDataPath.startsWith(dataPath)) {
            log.warn("Data uri "+ decodedPath+" doesn't point to source path");
            return null;
        }
        if (Files.notExists(dataPath)) {
            log.warn("path was found on disk: " + dataPath);
            return null;
        }
        return normalizedDataPath;
    }

    public static String decodePath(String dataUri) {
        final String path = UriUtils.decode(dataUri, StandardCharsets.UTF_8);
        return path;
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
        for (String code : getDataCodes()) {
            MetadataPath metadataPath = new MetadataPath(globals.path.metadata.path.resolve(code));

            SessionPath sessionPath = getSessionPath(metadataPath);

            String content = PersistenceUtils.getLatestContent(MetadataUtils.getNotificationPath(sessionPath), (o)->true);;
            try {
                notificationCache.writeLock.lock();
                notificationCache.notificationContents.put(code, content);
            } finally {
                notificationCache.writeLock.unlock();
            }
        }
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
