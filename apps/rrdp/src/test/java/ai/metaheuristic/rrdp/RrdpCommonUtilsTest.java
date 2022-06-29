package ai.metaheuristic.rrdp;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Sergio Lissner
 * Date: 6/28/2022
 * Time: 10:02 PM
 */
public class RrdpCommonUtilsTest {

    @Test
    public void test() throws IOException {
        String xml = IOUtils.resourceToString("/notification-02.xml", StandardCharsets.UTF_8);
        RrdpNotificationXml n = RrdpNotificationXmlUtils.parseNotificationXml(xml);

        List<RrdpNotificationXml.Entry> sorted = RrdpCommonUtils.sortNotificationXmlEntries(n);
        RrdpNotificationXml.Entry entry;
        entry = sorted.get(0);
        assertEquals(RrdpEnums.NotificationEntryType.SNAPSHOT, entry.type);
        assertNull(entry.serial);

        entry = sorted.get(1);
        assertEquals(RrdpEnums.NotificationEntryType.DELTA, entry.type);
        assertEquals(2, entry.serial);

        entry = sorted.get(2);
        assertEquals(3, entry.serial);
    }

    @Test
    public void test_01() throws IOException {
        String xml = IOUtils.resourceToString("/notification-03.xml", StandardCharsets.UTF_8);
        RrdpNotificationXml n = RrdpNotificationXmlUtils.parseNotificationXml(xml);

        List<RrdpNotificationXml.Entry> sorted = RrdpCommonUtils.sortNotificationXmlEntries(n);
        RrdpNotificationXml.Entry entry;
        entry = sorted.get(0);
        assertEquals(RrdpEnums.NotificationEntryType.SNAPSHOT, entry.type);
        assertNull(entry.serial);

        entry = sorted.get(1);
        assertEquals(RrdpEnums.NotificationEntryType.DELTA, entry.type);
        assertEquals(2, entry.serial);
    }
}
