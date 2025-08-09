package ai.metaheuristic.rrdp;

import ai.metaheuristic.rrdp.core.paths.MetadataPath;
import ai.metaheuristic.rrdp.disk_storage.SessionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sergio Lissner
 * Date: 6/2/2022
 * Time: 9:52 PM
 */
public class SessionUtilsTest {

    @Test
    public void test(@TempDir Path tempPath) {

        MetadataPath metadataPath = new MetadataPath(tempPath);
        assertNull(SessionUtils.getSessionFile(metadataPath));

        String session = SessionUtils.getSession(metadataPath);
        assertNull(session);

        String session1 = SessionUtils.persistSession(metadataPath, UUID.randomUUID().toString(), LocalDate::now);
        assertNotNull(session1);
        assertDoesNotThrow(()-> UUID.fromString(session1));

        Path sessionPath1 = SessionUtils.getSessionFile(metadataPath);
        assertNotNull(sessionPath1);


        String session2 = SessionUtils.getSession(metadataPath);
        assertNotNull(session2);
        assertDoesNotThrow(()-> UUID.fromString(session2));

        assertEquals(session1, session2);

        Path sessionPath2 = SessionUtils.getSessionFile(metadataPath);
        assertNotNull(sessionPath2);


        assertEquals(sessionPath1, sessionPath2);
    }

    @Test
    public void testWithDates(@TempDir Path tempPath) {

        MetadataPath metadataPath = new MetadataPath(tempPath);
        String session = SessionUtils.getSession(metadataPath);
        assertNull(session);

        String session1 = SessionUtils.persistSession(metadataPath, UUID.randomUUID().toString(), ()->LocalDate.now().minusDays(6));
        assertNotNull(session1);
        assertDoesNotThrow(()-> UUID.fromString(session1));

        Path sessionPath1 = SessionUtils.getSessionFile(metadataPath);
        assertNotNull(sessionPath1);

        String session2 = SessionUtils.getSession(metadataPath);
        assertNotNull(session2);
        assertDoesNotThrow(()-> UUID.fromString(session2));

        assertEquals(session1, session2);

        Path sessionPath2 = SessionUtils.getSessionFile(metadataPath);
        assertNotNull(sessionPath2);


        assertEquals(sessionPath1, sessionPath2);
    }
}
