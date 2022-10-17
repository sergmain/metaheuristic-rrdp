package ai.metaheuristic.rrdp;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sergio Lissner
 * Date: 6/28/2022
 * Time: 3:34 PM
 */
public class RrdpEntryXmlUtilsTest {

    @Test
    public void test_snapshot() {
        String xml = RrdpCommonUtils.resourceAsString("/snapshot.xml");
        RrdpEntryXml e = RrdpEntryXmlUtils.parseRrdpEntryXml(IOUtils.toInputStream(xml, StandardCharsets.UTF_8));

        assertEquals("9df4b597-af9e-4dca-bdda-719cce2c4e28", e.sessionId);
        assertEquals(2, e.serial);
        assertEquals(3, e.entries.size());

        RrdpEntryXml.Entry en;
        en = e.entries.get(0);
        assertEquals(RrdpEnums.EntryState.PUBLISH, en.state);
        assertEquals("rsync://rpki.ripe.net/Alice/Bob.cer", en.uri);
        assertEquals("123", en.hash);
        assertEquals(2, en.length);
        assertNotNull(en.getContent());
        assertEquals("ZXhhbXBsZTE=1", en.getContent().strip());

        en = e.entries.get(1);
        assertEquals(RrdpEnums.EntryState.PUBLISH, en.state);
        assertEquals("rsync://rpki.ripe.net/Alice/Alice.mft", en.uri);
        assertEquals("456", en.hash);
        assertEquals(3, en.length);
        assertNotNull(en.getContent());
        assertEquals("ZXhhbXBsZTI=2", en.getContent().strip());

        en = e.entries.get(2);
        assertEquals(RrdpEnums.EntryState.PUBLISH, en.state);
        assertEquals("rsync://rpki.ripe.net/Alice/Alice.crl", en.uri);
        assertEquals("789", en.hash);
        assertEquals(4, en.length);
        assertNotNull(en.getContent());
        assertEquals("ZXhhbXBsZTM=3", en.getContent().strip());

    }

    @Test
    public void test_delta() {
        String xml = RrdpCommonUtils.resourceAsString("/delta-01.xml");
        RrdpEntryXml e = RrdpEntryXmlUtils.parseRrdpEntryXml(IOUtils.toInputStream(xml, StandardCharsets.UTF_8));

        assertEquals("9df4b597-af9e-4dca-bdda-719cce2c4e28-delta", e.sessionId);
        assertEquals(3, e.serial);
        assertEquals(3, e.entries.size());

        RrdpEntryXml.Entry en;
        en = e.entries.get(0);
        assertEquals(RrdpEnums.EntryState.PUBLISH, en.state);
        assertEquals("rsync://rpki.ripe.net/repo/Alice/Alice.mft", en.uri);
        assertEquals("50d8...545c", en.hash);
        assertNull(en.length);
        assertNotNull(en.getContent());
        assertEquals("ZXhhbXBsZTQ=", en.getContent().strip());

        en = e.entries.get(1);
        assertEquals(RrdpEnums.EntryState.PUBLISH, en.state);
        assertEquals("rsync://rpki.ripe.net/repo/Alice/Alice.crl", en.uri);
        assertEquals("5fb1...6a56", en.hash);
        assertNull(en.length);
        assertNotNull(en.getContent());
        assertEquals("ZXhhbXBsZTU=", en.getContent().strip());

        en = e.entries.get(2);
        assertEquals(RrdpEnums.EntryState.WITHDRAWAL, en.state);
        assertEquals("rsync://rpki.ripe.net/repo/Alice/Bob.cer", en.uri);
        assertEquals("caeb...15c1", en.hash);
        assertNull(en.length);
        assertNull(en.getContent());

    }
}
