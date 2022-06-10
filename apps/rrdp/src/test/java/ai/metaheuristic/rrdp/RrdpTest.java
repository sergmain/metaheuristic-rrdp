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
        int serial = 1;

        StringWriter snapshot = new StringWriter();
        StringWriter delta = new StringWriter();

        RrdpEntry en1 = publishEntry("entry #1", "http://uri1");
        RrdpEntry en2 = publishEntry("entry #2", "http://uri2");
        RrdpEntry en3 = publishEntry("entry #3", "http://uri3");
        RrdpEntry en4 = publishEntry("entry #4", "http://uri4");


        //noinspection ReturnOfNull
        RrdpConfig cfg = new RrdpConfig()
                .withRfc8182(false)
                .withGetSession(()->session)
                .withCurrentNotification(()->null)
                .withRrdpEntryIterator(()-> List.of(en1, en2, en3).iterator())
                .withCurrSerial((s)->serial)
                .withPersistNotificationEntry(snapshot::write)
                .withPersistNotificationEntry(delta::write)
                .withProduceType(()-> RrdpEnums.NotificationEntryType.SNAPSHOT);

        Rrdp rrdp = new Rrdp(cfg);
        rrdp.produce();

        StringWriter notification = new StringWriter();
        rrdp.produceNotification(new UriAndHash("http://notification-snapshot", DigestUtils.sha256Hex(snapshot.toString())), notification::write);

        String snapshotXml = snapshot.toString();
        String deltaXml = delta.toString();
        String notificationXml = notification.toString();

        System.out.println(snapshotXml);
        System.out.println(notificationXml);

        assertNotEquals(0, snapshotXml.length());
        assertEquals(0, deltaXml.length());
        assertNotEquals(0, notificationXml.length());

        verifyNotification(notificationXml, session, en4);
    }

    private static void verifyNotification(String notificationXml, String session, RrdpEntry en4) {
        Notification n = RrdpUtils.parseNotificationXml(notificationXml);

        assertEquals(session, n.sessionId);
        assertEquals(1, n.serial);
        assertEquals(1, n.entries.size());

        Notification.Entry e1 = n.entries.stream().filter(o->o.serial==null).findFirst().orElseThrow();

        assertNull(e1.serial);
        assertEquals(RrdpEnums.NotificationEntryType.SNAPSHOT, e1.type);
        assertEquals(en4.hash.get(), e1.hash);
        assertEquals(en4.uri.get(), e1.uri);
    }

    @Test
    public void test_delta_01() {
        String session = UUID.randomUUID().toString();
        int serial = 2;

        StringWriter notificationEntry = new StringWriter();
        StringWriter notification = new StringWriter();

        RrdpEntry en1 = publishEntry("entry #1", "http://uri1");
        RrdpEntry en2 = publishEntry("entry #2", "http://uri2");
        RrdpEntry en3 = publishEntry("entry #3", "http://uri3");
        RrdpEntry en4 = withdrawEntry("entry #4", "http://uri4");
        RrdpEntry en5 = withdrawEntry("entry #5", "http://uri5");

        //noinspection ReturnOfNull
        RrdpConfig cfg = new RrdpConfig()
                .withRfc8182(false)
                .withGetSession(()->session)
                .withCurrentNotification(()->null)
                .withRrdpEntryIterator(()-> List.of(en1, en2, en3, en4).iterator())
                .withCurrSerial((s)->serial)
                .withPersistNotificationEntry(notificationEntry::write)
                .withProduceType(()-> RrdpEnums.NotificationEntryType.DELTA);

        Rrdp rrdp = new Rrdp(cfg);
        rrdp.produce();

        String notificationEntryXml = notificationEntry.toString();
        String notificationXml = notification.toString();

        System.out.println(notificationEntryXml);
        System.out.println(notificationXml);

        assertNotEquals(0, notificationEntryXml.length());
        assertNotEquals(0, notificationXml.length());
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
