package ai.metaheuristic.rrdp_disk_storage;

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

        assertNull(SessionUtils.getSessionFile(tempPath));
        String session = SessionUtils.getSession(tempPath);
        assertNotNull(session);
        assertDoesNotThrow(()-> UUID.fromString(session));

        String session1 = SessionUtils.getSession(tempPath);
        assertNotNull(session1);
        assertDoesNotThrow(()-> UUID.fromString(session1));

        assertEquals(session, session1);

        Path sessionPath1 = SessionUtils.getSessionFile(tempPath);
        assertNotNull(sessionPath1);


        String session2 = SessionUtils.getSession(tempPath);
        assertNotNull(session2);
        assertDoesNotThrow(()-> UUID.fromString(session2));

        assertEquals(session1, session2);

        Path sessionPath2 = SessionUtils.getSessionFile(tempPath);
        assertNotNull(sessionPath2);


        assertEquals(sessionPath1, sessionPath2);
    }

    @Test
    public void testWithDates(@TempDir Path tempPath) {

        String session = SessionUtils.getSession(tempPath, ()->LocalDate.now().minusDays(6));
        assertNotNull(session);
        assertDoesNotThrow(()-> UUID.fromString(session));

        String session1 = SessionUtils.getSession(tempPath);
        assertNotNull(session1);
        assertDoesNotThrow(()-> UUID.fromString(session1));

        assertEquals(session, session1);

        Path sessionPath1 = SessionUtils.getSessionFile(tempPath);
        assertNotNull(sessionPath1);


        String session2 = SessionUtils.getSession(tempPath);
        assertNotNull(session2);
        assertDoesNotThrow(()-> UUID.fromString(session2));

        assertEquals(session1, session2);

        Path sessionPath2 = SessionUtils.getSessionFile(tempPath);
        assertNotNull(sessionPath2);


        assertEquals(sessionPath1, sessionPath2);
    }
}
