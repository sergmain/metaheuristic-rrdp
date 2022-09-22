package ai.metaheuristic.rrdp_client;

import ai.metaheuristic.rrdp.RrdpEntryXml;
import ai.metaheuristic.rrdp.RrdpEntryXmlUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sergio Lissner
 * Date: 9/21/2022
 * Time: 9:29 PM
 */
public class ContentServiceTest {

    @Test
    public void test_55() throws IOException {
        String snapshot = IOUtils.resourceToString("/entry/000001.xml", StandardCharsets.UTF_8);


        RrdpEntryXml entryXml = RrdpEntryXmlUtils.parseRrdpEntryXml(snapshot);


        assertEquals(1, entryXml.entries.size());
        assertDoesNotThrow(()-> ContentService.getBuilder(entryXml.entries.get(0).uri));
    }
}
