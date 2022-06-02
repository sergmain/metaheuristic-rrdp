package ai.metaheuristic.rrdp;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sergio Lissner
 * Date: 6/2/2022
 * Time: 8:55 AM
 */
public class RrdpTest {

    @Test
    public void test_snapshot_01() {
        String session = UUID.randomUUID().toString();
        int serial = 0;

        StringWriter snapshot = new StringWriter();
        StringWriter delta = new StringWriter();
        StringWriter notification = new StringWriter();

        RrdpEntry en1 = publishEntry("entry #1", "http://uri1");
        RrdpEntry en2 = publishEntry("entry #2", "http://uri2");
        RrdpEntry en3 = publishEntry("entry #3", "http://uri3");

        RrdpConfig cfg = new RrdpConfig()
                .withRfc8182(false)
                .withGetSession(()->session)
                .withCurrentNotification(()->null)
                .withRrdpEntryIteator(()-> List.of(en1, en2, en3).iterator())
                .withNextSerial((s)->serial+1)
                .withCurrSerial((s)->serial)
                .withPersistSnapshot(snapshot::write)
                .withPersistDelta(delta::write)
                .withPersistNotification(notification::write)
                .withProduceType(()-> RrdpEnums.ProduceType.SNAPHOST);

        Rrdp rrdp = new Rrdp(cfg);
        rrdp.produce();

        String snapshotXml = snapshot.toString();
        String deltaXml = delta.toString();
        String notificationXml = notification.toString();

        System.out.println(snapshotXml);

        assertNotEquals(0, snapshotXml.length());
        assertEquals(0, deltaXml.length());
    }

    @Test
    public void test_delta_01() {
        String session = UUID.randomUUID().toString();
        int serial = 1;

        StringWriter snapshot = new StringWriter();
        StringWriter delta = new StringWriter();
        StringWriter notification = new StringWriter();

        RrdpEntry en1 = publishEntry("entry #1", "http://uri1");
        RrdpEntry en2 = publishEntry("entry #2", "http://uri2");
        RrdpEntry en3 = publishEntry("entry #3", "http://uri3");
        RrdpEntry en4 = withdrawEntry("entry #4", "http://uri4");

        RrdpConfig cfg = new RrdpConfig()
                .withRfc8182(false)
                .withGetSession(()->session)
                .withCurrentNotification(()->null)
                .withRrdpEntryIteator(()-> List.of(en1, en2, en3, en4).iterator())
                .withNextSerial((s)->serial+1)
                .withCurrSerial((s)->serial)
                .withPersistSnapshot(snapshot::write)
                .withPersistDelta(delta::write)
                .withPersistNotification(notification::write)
                .withProduceType(()-> RrdpEnums.ProduceType.DELTA);

        Rrdp rrdp = new Rrdp(cfg);
        rrdp.produce();

        String snapshotXml = snapshot.toString();
        String deltaXml = delta.toString();
        String notificationXml = notification.toString();

        System.out.println(snapshotXml);

        assertEquals(0, snapshotXml.length());
        assertNotEquals(0, deltaXml.length());
    }

    private static RrdpEntry publishEntry(String content, String uri) {
        return createEntry(content, uri, RrdpEnums.EntryState.PUBLISHED);
    }

    private static RrdpEntry withdrawEntry(String content, String uri) {
        return createEntry(content, uri, RrdpEnums.EntryState.WITHDRAWAL);
    }

    private static RrdpEntry createEntry(String content, String uri, RrdpEnums.EntryState state) {
        RrdpEntry entry= new RrdpEntry()
                .withState(state)
                .withContent(()-> content)
                .withUri(()->uri)
                .withHash(()->DigestUtils.sha256Hex(content));

        return entry;
    }
}
