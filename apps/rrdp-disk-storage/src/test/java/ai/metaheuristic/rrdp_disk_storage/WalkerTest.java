package ai.metaheuristic.rrdp_disk_storage;

import ai.metaheuristic.rrdp.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

/**
 * @author Sergio Lissner
 * Date: 6/3/2022
 * Time: 6:13 PM
 */
public class WalkerTest {

    @Test
    public void process() throws IOException {
        final String startPoint = "D:\\test-files-edition\\edition\\stat2";
//        final String startPoint = "D:\\test-files-edition";
        final Path metadataPath = PersistenceUtils.resolveSubPath(Path.of("result"), "metadata");
        final Path actualDataPath = Path.of(startPoint);


        List<Path> metadataDataPaths = PersistenceUtils.getPaths(metadataPath);
        for (Path metadataDataPath : metadataDataPaths) {
            processDataPath(metadataDataPath, actualDataPath);
        }
//        FileChecksumProcessor.process(metadataPath, Path.of(startPoint), (x) -> {
//            x.values().forEach(System.out::println);
//        });
    }

    private void processDataPath(Path path, Path actualDataPath) {

        String session = SessionUtils.getSession(path);
        if (session==null) {
            session = UUID.randomUUID().toString();
            SessionUtils.persistSession(path, session, LocalDate::now);
        }

        Integer serial = SerialUtils.getSerial(path);
        if (serial==null) {
            serial = 1;
            SerialUtils.persistSerial(path, serial, LocalDate::now);
        }

        String notificationXml = NotificationUtils.getNotification(path);
        final Notification n = notificationXml == null ? new Notification() : RrdpUtils.parseNotificationXml(notificationXml);

        FileChecksumProcessor.process(path, actualDataPath, (x) -> {
            x.values().forEach(System.out::println);
        });

        Map<String, ChecksumPath> map = ChecksumManager.load(path);
        Iterator<ChecksumPath> iter = map.values().iterator();

        Iterator<RrdpEntry> it = new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }
            @Override
            public RrdpEntry next() {
                return toRrdpEntry(iter.next());
            }
        };

        StringWriter snapshot = new StringWriter();
        StringWriter delta = new StringWriter();

        final String finalSession = session;
        final int finalSerial = serial;
        RrdpConfig cfg = new RrdpConfig()
                .withRfc8182(false)
                .withFileContent(false)
                .withGetSession(()-> finalSession)
                .withCurrentNotification(()->n)
                .withRrdpEntryIterator(()-> it)
                .withNextSerial((s)-> finalSerial +1)
                .withCurrSerial((s)->finalSerial)
                .withPersistSnapshot(snapshot::write)
                .withPersistDelta(delta::write)
                .withProduceType(()-> finalSerial==1 ? RrdpEnums.ProduceType.SNAPSHOT : RrdpEnums.ProduceType.DELTA);

        Rrdp rrdp = new Rrdp(cfg);
        rrdp.produce();

        StringWriter notification = new StringWriter();
        rrdp.produceNotification(new UriAndHash("http://notification-snapshot", DigestUtils.sha256Hex(snapshot.toString())), notification::write);

        NotificationUtils.persistNotification(path, notification.toString(), LocalDate::now);
    }

    public static RrdpEntry toRrdpEntry(ChecksumPath checksumPath) {
        RrdpEntry entry= new RrdpEntry()
                .withState(checksumPath.state)
                .withUri(()->checksumPath.path)
                .withHash(()->checksumPath.sha1);

        return entry;
    }

}
