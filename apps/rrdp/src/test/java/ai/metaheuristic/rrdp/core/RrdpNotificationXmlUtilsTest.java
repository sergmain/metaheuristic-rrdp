package ai.metaheuristic.rrdp.core;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Sergio Lissner
 * Date: 6/2/2022
 * Time: 10:41 AM
 */
public class RrdpNotificationXmlUtilsTest {

    @Test
    public void testParse() throws IOException {
        String xml = IOUtils.resourceToString("/notification-01.xml", StandardCharsets.UTF_8);
        RrdpNotificationXml n = RrdpNotificationXmlUtils.parseNotificationXml(xml);

        assertEquals("9df4b597-af9e-4dca-bdda-719cce2c4e28", n.sessionId);
        assertEquals(3, n.serial);
        assertEquals(3, n.entries.size());

        RrdpNotificationXml.Entry e1 = n.entries.stream().filter(o->o.serial==null).findFirst().orElseThrow();
        RrdpNotificationXml.Entry e2 = n.entries.stream().filter(o->Integer.valueOf(2).equals(o.serial)).findFirst().orElseThrow();
        RrdpNotificationXml.Entry e3 = n.entries.stream().filter(o->Integer.valueOf(3).equals(o.serial)).findFirst().orElseThrow();

        assertNull(e1.serial);
        assertEquals(RrdpEnums.NotificationEntryType.SNAPSHOT, e1.type);
        assertEquals("AB", e1.hash);
        assertEquals("https://host/9d-8/1/snapshot.xml", e1.uri);

        assertEquals(2, e2.serial);
        assertEquals(RrdpEnums.NotificationEntryType.DELTA, e2.type);
        assertEquals("EF", e2.hash);
        assertEquals("https://host/9d-8/2/delta.xml", e2.uri);

        assertEquals(3, e3.serial);
        assertEquals(RrdpEnums.NotificationEntryType.DELTA, e3.type);
        assertEquals("CD", e3.hash);
        assertEquals("https://host/9d-8/3/delta.xml", e3.uri);
    }
}
