package ai.metaheuristic.rrdp_disk_storage;

import ai.metaheuristic.rrdp.*;
import ai.metaheuristic.rrdp.paths.MetadataPath;
import ai.metaheuristic.rrdp.paths.SessionPath;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.file.StandardOpenOption.READ;

/**
 * @author Sergio Lissner
 * Date: 6/3/2022
 * Time: 5:10 PM
 */
public class FileChecksumProcessor {

    public enum DifferenceType {new_one, different, the_same}

    @RequiredArgsConstructor
    public static class ProcessorParams {
        @Nullable
        public final String entryUriPrefix;
        @Nullable
        public final String notificationEntryUriPrefix;
    }

    @SneakyThrows
    public static void processPath(Path metadataPath, Path actualDataPath, String code, List<String> paths, ProcessorParams processorParams) {
        Path dataPath = actualDataPath.resolve(code);
        if (Files.notExists(dataPath)) {
            System.out.println("Path "+ dataPath+" doesn't exists");
            return;
        }
        MetadataPath actualMetadataPath = new MetadataPath(PersistenceUtils.resolveSubPath(metadataPath, dataPath.getFileName().toString()));
        processDataPath(actualMetadataPath, dataPath, processorParams, paths);
    }

    @SneakyThrows
    public static Map<String, ChecksumPath> process(SessionPath sessionPath, Path dataPath, List<String> paths) {
        if (Files.notExists(dataPath)) {
            return Map.of();
        }

        Map<String, ChecksumPath> diff = processDiff(sessionPath, dataPath, paths);
        return diff;
    }

    private static void processDataPath(MetadataPath metadataPath, Path actualDataPath, final ProcessorParams params, List<String> paths) throws IOException {

        String prefixPath = metadataPath.path.getFileName().toString();

        String session = SessionUtils.getSession(metadataPath);
        if (session==null) {
            session = UUID.randomUUID().toString();
            SessionUtils.persistSession(metadataPath, session, LocalDate::now);
        }
        SessionPath sessionPath = new SessionPath(metadataPath.path.resolve(session));
        Integer serial = SerialUtils.getSerial(sessionPath);
        if (serial==null) {
            serial = 1;
            SerialUtils.persistSerial(sessionPath, serial, LocalDate::now);
        }

        String notificationXml = NotificationUtils.getNotification(sessionPath);
        final RrdpNotificationXml n = notificationXml == null
                ? new RrdpNotificationXml()
                : RrdpNotificationXmlUtils.parseNotificationXml(notificationXml);

        Map<String, ChecksumPath> diff = FileChecksumProcessor.process(sessionPath, actualDataPath, paths);
        ChecksumManager.persist(sessionPath, diff);

        if (diff.isEmpty()) {
            System.out.println("No difference since last check");
            return;
        }
        else {
            System.out.println("Found " + diff.size() +" new or changed entries");
        }

        Iterator<ChecksumPath> iter = diff.values().iterator();

        Iterator<RrdpEntryProvider> it = new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }
            @Override
            public RrdpEntryProvider next() {
                final ChecksumPath next = iter.next();
                return toRrdpEntry(next, prefixPath, params);
            }
        };

        final StringWriter notificationEntry = new StringWriter();
        final String finalSession = session;
        final int finalSerial = serial;
        RrdpConfig cfg = new RrdpConfig()
                .withRfc8182(false)
                .withFileContent(false)
                .withLengthOfContent(true)
                .withGetSession(() -> finalSession)
                .withCurrentNotification(() -> n)
                .withRrdpEntryIterator(() -> it)
                .withCurrSerial((s) -> finalSerial)
                .withPersistNotificationEntry(notificationEntry::write)
                .withProduceType(() -> finalSerial == 1 ? RrdpEnums.NotificationEntryType.SNAPSHOT : RrdpEnums.NotificationEntryType.DELTA);

        Rrdp rrdp = new Rrdp(cfg);
        rrdp.produce();

        SerialUtils.persistSerial(sessionPath, serial + 1, LocalDate::now);

        Path entryPath = MetadataUtils.getEntryPath(sessionPath);
        String entryFilename = PersistenceUtils.formatFilename(serial, ".xml");
        Path entryFile = entryPath.resolve(entryFilename);
        final String notificationEntryStr = notificationEntry.toString();
        Files.writeString(entryFile, notificationEntryStr);

        StringWriter notification = new StringWriter();
        final String uri = PersistenceUtils.asUri(params.notificationEntryUriPrefix, prefixPath + '/' + entryFilename);
        rrdp.produceNotification(new UriHashLength(uri, DigestUtils.sha256Hex(notificationEntryStr), notificationEntryStr.length()), notification::write);

        NotificationUtils.persistNotification(sessionPath, notification.toString(), LocalDate::now);
    }

    public static RrdpEntryProvider toRrdpEntry(ChecksumPath checksumPath, String prefixPath, final ProcessorParams params) {
        RrdpEntryProvider entry= new RrdpEntryProvider()
                .withState(checksumPath.state)
                .withUri(()-> PersistenceUtils.asUri(params.entryUriPrefix, prefixPath + '/' + checksumPath.path))
                .withHash(()->checksumPath.sha1)
                .withLength(()->(int)checksumPath.size);

        return entry;
    }

    @SneakyThrows
    private static Map<String, ChecksumPath> processDiff(SessionPath sessionPath, Path dataPath, List<String> paths) {
        final Map<String, ChecksumPath> calculatedMap = ChecksumManager.load(sessionPath);
        final Map<String, ChecksumPath> newMap = loadChecksumPath(dataPath, calculatedMap, paths);
        return newMap;
    }

    // calc checksums for data source
    public static Map<String, ChecksumPath> loadChecksumPath(Path dataPath, Map<String, ChecksumPath> calculatedMap, List<String> paths) throws IOException {
        final AtomicInteger count = new AtomicInteger();
        final Map<String, ChecksumPath> map = new HashMap<>(10000);
        final Set<String> processed = new HashSet<>(10000);
        Files.walkFileTree(dataPath, new SimpleFileVisitor<>() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public FileVisitResult visitFile(Path p, BasicFileAttributes attrs) throws IOException {
                Path relativePath = dataPath.relativize(p);
                String relativeName = relativePath.toString();
                if (!paths.isEmpty()) {
                    String fullPath = p.toString();
                    if (paths.stream().noneMatch(o->o.equals(fullPath))) {
                        return FileVisitResult.CONTINUE;
                    }
                }
                ChecksumPath cs = new ChecksumPath();
                cs.path = relativeName;
                cs.size = Files.size(p);
                String nameMd5 = DigestUtils.md5Hex(relativeName);
                cs.md5First2Chars = nameMd5.substring(0, 2);
                cs.sha1 = calcSha1(p);

                final DifferenceType differenceType = isDifferent(calculatedMap, cs);
                if (differenceType!=DifferenceType.the_same) {
                    cs.state = differenceType==DifferenceType.new_one ? RrdpEnums.EntryState.PUBLISH : RrdpEnums.EntryState.UPDATE;
                    map.put(relativeName, cs);
                }
                processed.add(relativeName);

                count.incrementAndGet();
                return FileVisitResult.CONTINUE;
            }
        });

        for (Map.Entry<String, ChecksumPath> en : calculatedMap.entrySet()) {
            if (en.getValue().state!= RrdpEnums.EntryState.WITHDRAWAL && !processed.contains(en.getKey())) {
                map.put(en.getKey(), new ChecksumPath(en.getValue(), RrdpEnums.EntryState.WITHDRAWAL));
            }
        }

        System.out.println(""+dataPath+", total: " + count + ", different: " + map.size());
        return map;
    }

    private static DifferenceType isDifferent(Map<String, ChecksumPath> map, ChecksumPath cs) {
        ChecksumPath check = map.get(cs.path);
        if (check==null) {
            return DifferenceType.new_one;
        }
        if (check.size!=cs.size) {
            return DifferenceType.different;
        }
        if (!check.sha1.equals(cs.sha1)) {
            return DifferenceType.different;
        }
        return DifferenceType.the_same;
    }

    @Nullable
    public static String calcSha1(Path p) throws IOException {
        if (Files.size(p)==0) {
            return null;
        }
        try (InputStream is = Files.newInputStream(p, READ)) {
            return DigestUtils.sha1Hex(is);
        }
    }
}
